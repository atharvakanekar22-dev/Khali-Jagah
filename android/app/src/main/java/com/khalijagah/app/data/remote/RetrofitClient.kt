package com.khalijagah.app.data.remote

import android.os.Build
import android.util.Log
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val TAG = "KhaliJagahNet"

    private const val DEFAULT_EMULATOR_HOST = "10.0.2.2"
    private const val DEFAULT_PORT = 3000
    private const val INITIAL_BASE_URL = "http://$DEFAULT_EMULATOR_HOST:$DEFAULT_PORT"

    // Candidate host resolution for emulator vs physical device
    private val candidateHosts: List<String> by lazy {
        if (isEmulator()) {
            listOf("10.0.2.2", "127.0.0.1", "192.168.0.102")
        } else {
            // Physical device: try USB reverse (127.0.0.1) first, then LAN Wi-Fi (192.168.0.102), then fallback
            listOf("127.0.0.1", "192.168.0.102", "10.0.2.2")
        }
    }

    @Volatile
    private var verifiedHost: String? = null

    private fun isEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator")
    }

    private val hostSelectionInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url

        val currentWorking = verifiedHost
        if (currentWorking != null) {
            val redirectedUrl = originalUrl.newBuilder().host(currentWorking).build()
            val redirectedRequest = originalRequest.newBuilder().url(redirectedUrl).build()
            try {
                return@Interceptor chain.proceed(redirectedRequest)
            } catch (e: IOException) {
                Log.w(TAG, "Cached host $currentWorking failed (${e.message}), attempting fallback candidates...")
                verifiedHost = null
            }
        }

        var lastError: IOException? = null
        for (host in candidateHosts) {
            val candidateUrl = originalUrl.newBuilder().host(host).build()
            val candidateRequest = originalRequest.newBuilder().url(candidateUrl).build()
            try {
                Log.d(TAG, "Trying backend candidate: http://$host:$DEFAULT_PORT${originalUrl.encodedPath}")
                val response = chain.proceed(candidateRequest)
                if (response.isSuccessful || response.code < 500) {
                    verifiedHost = host
                    Log.d(TAG, "Successfully connected to backend via $host:$DEFAULT_PORT")
                    return@Interceptor response
                }
                response.close()
            } catch (e: IOException) {
                lastError = e
                Log.d(TAG, "Candidate $host failed (${e.message})")
            }
        }

        throw lastError ?: IOException("Could not connect to backend on any candidate host ($candidateHosts)")
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS) // AI analysis may take time
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(hostSelectionInterceptor)
        .addInterceptor(HttpLoggingInterceptor { message ->
            Log.d("KhaliJagahHttp", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(INITIAL_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
