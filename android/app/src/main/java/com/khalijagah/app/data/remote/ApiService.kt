package com.khalijagah.app.data.remote

import com.khalijagah.app.data.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("/api/stations")
    suspend fun getStations(): StationsResponse

    @GET("/api/trains")
    suspend fun getTrains(
        @Query("source") source: String,
        @Query("destination") destination: String
    ): TrainsResponse

    @GET("/api/crowd/{trainId}")
    suspend fun getCrowdData(
        @Path("trainId") trainId: String
    ): CrowdResponse

    @POST("/api/analyze-crowd")
    suspend fun analyzeCrowd(
        @Body request: AnalyzeRequest
    ): AnalyzeResponse
}
