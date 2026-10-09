package com.khalijagah.app.data.repository

import android.util.Log
import com.khalijagah.app.data.model.*
import com.khalijagah.app.data.remote.RetrofitClient

class KhaliJagahRepository {
    private val api = RetrofitClient.apiService

    suspend fun getHealth(): Result<HealthResponse> = runCatching {
        api.getHealth()
    }

    suspend fun getStations(): Result<List<Station>> = runCatching {
        val remote = api.getStations().data
        if (remote.isNotEmpty()) remote else DEFAULT_STATIONS
    }.recover { error ->
        Log.w("KhaliJagah", "Remote getStations failed (${error.message}). Using DEFAULT_STATIONS fallback.")
        DEFAULT_STATIONS
    }

    suspend fun getTrains(source: String, destination: String): Result<List<TrainResult>> = runCatching {
        api.getTrains(source, destination).data
    }

    suspend fun getCrowdData(trainId: String): Result<CrowdResponse> = runCatching {
        api.getCrowdData(trainId)
    }

    suspend fun analyzeCrowd(trainId: String): Result<AnalyzeResponse> = runCatching {
        api.analyzeCrowd(AnalyzeRequest(trainId))
    }
}
