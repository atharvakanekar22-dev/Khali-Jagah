package com.khalijagah.app.data.remote

import android.os.Build
import android.util.Log
import com.khalijagah.app.BuildConfig
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

    const val PRODUCTION_BASE_URL = "https://khali-jagah-backend.onrender.com/"
    const val EMULATOR_LOCAL_BASE_URL = "http://10.0.2.2:3000/"

    /**
     * Set to true to force local emulator backend (10.0.2.2:3000) when running on an emulator.
     * By default, it follows BuildConfig.USE_LOCAL_BACKEND (which is false for production/judging builds).
     */
    @Volatile
    var useLocalEmulatorBackend: Boolean = BuildConfig.USE_LOCAL_BACKEND

    fun isEmulator(): Boolean {
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

    /**
     * Interceptor ensuring the hosted production HTTPS backend is used on physical devices,
     * while preserving optional local fallback only within an Android emulator environment.
     */
    private val hostSelectionInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url

        // If local backend is explicitly requested AND we are in an emulator, route to 10.0.2.2:3000
        if (useLocalEmulatorBackend && isEmulator()) {
            val localUrl = originalUrl.newBuilder()
                .scheme("http")
                .host("10.0.2.2")
                .port(3000)
                .build()
            val localRequest = originalRequest.newBuilder().url(localUrl).build()
            Log.d(TAG, "Routing to local emulator backend: $localUrl")
            return@Interceptor chain.proceed(localRequest)
        }

        // On physical devices: Always connect directly to the hosted HTTPS production backend.
        // No local IPs, no candidate loops, no 10.0.2.2.
        if (!isEmulator()) {
            return@Interceptor chain.proceed(originalRequest)
        }

        // On emulator when local backend is not forced: attempt the production URL first.
        try {
            return@Interceptor chain.proceed(originalRequest)
        } catch (e: IOException) {
            // Only on an emulator, if production is unreachable (e.g. offline dev), fallback to 10.0.2.2
            Log.w(TAG, "Production backend unreachable on emulator (${e.message}). Falling back to local emulator backend (10.0.2.2:3000)...")
            val fallbackUrl = originalUrl.newBuilder()
                .scheme("http")
                .host("10.0.2.2")
                .port(3000)
                .build()
            val fallbackRequest = originalRequest.newBuilder().url(fallbackUrl).build()
            return@Interceptor chain.proceed(fallbackRequest)
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS) // Generous connect timeout for mobile networks & Render cold-start
        .readTimeout(60, TimeUnit.SECONDS)    // AI inference may take time
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(hostSelectionInterceptor)
        .addInterceptor(HttpLoggingInterceptor { message ->
            Log.d("KhaliJagahHttp", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(if (BuildConfig.BASE_URL.isNotBlank()) BuildConfig.BASE_URL else PRODUCTION_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
