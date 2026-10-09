package com.khalijagah.app.data.model

import com.khalijagah.app.data.remote.RetrofitClient
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class ModelsSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun testHealthResponseDeserialization() {
        val payload = """
            {
              "status": "ok",
              "app": "Khali Jagah",
              "version": "1.0.0",
              "gemmaModel": "gemma-4-31b-it",
              "apiKeyConfigured": true,
              "timestamp": "2026-10-09T15:59:36.650Z"
            }
        """.trimIndent()

        val response = json.decodeFromString<HealthResponse>(payload)
        assertEquals("ok", response.status)
        assertEquals("Khali Jagah", response.app)
        assertEquals("gemma-4-31b-it", response.gemmaModel)
        assertTrue(response.apiKeyConfigured)
    }

    @Test
    fun testStationsResponseDeserialization() {
        val payload = """
            {
              "success": true,
              "count": 2,
              "data": [
                {
                  "id": "CSMT",
                  "name": "Chhatrapati Shivaji Maharaj Terminus",
                  "shortName": "CSMT",
                  "line": "central",
                  "order": 1
                },
                {
                  "id": "DR",
                  "name": "Dadar",
                  "shortName": "Dadar",
                  "line": "central",
                  "order": 4
                }
              ]
            }
        """.trimIndent()

        val response = json.decodeFromString<StationsResponse>(payload)
        assertTrue(response.success)
        assertEquals(2, response.count)
        assertEquals("CSMT", response.data[0].id)
        assertEquals("Dadar", response.data[1].shortName)
    }

    @Test
    fun testTrainsResponseDeserialization() {
        val payload = """
            {
              "success": true,
              "source": "DR",
              "destination": "TN",
              "count": 1,
              "dataDisclaimer": "Prototype schedule and crowd estimate data. Not official railway data.",
              "data": [
                {
                  "id": "MUM-0712",
                  "name": "Kalyan Fast",
                  "type": "fast",
                  "departure": "07:12",
                  "arrival": "08:28",
                  "journeyMinutes": 76,
                  "stops": ["CSMT", "BY", "DR", "KR", "GC", "TN", "DI", "KY"],
                  "crowdScore": 88,
                  "crowdLevel": "VERY_HIGH",
                  "period": "peak"
                }
              ]
            }
        """.trimIndent()

        val response = json.decodeFromString<TrainsResponse>(payload)
        assertTrue(response.success)
        assertEquals(1, response.count)
        assertEquals("MUM-0712", response.data[0].id)
        assertEquals("VERY_HIGH", response.data[0].crowdLevel)
    }

    @Test
    fun testCrowdResponseDeserialization() {
        val payload = """
            {
              "success": true,
              "trainId": "MUM-0842",
              "overallScore": 82,
              "overallLevel": "VERY_HIGH",
              "period": "peak",
              "historicalAvg": 80,
              "trend": "increasing",
              "coachCount": 12,
              "coaches": [
                { "id": "C1", "score": 78, "level": "VERY_HIGH" },
                { "id": "C7", "score": 65, "level": "HIGH" }
              ],
              "bestCoach": { "id": "C7", "score": 65, "level": "HIGH" },
              "worstCoach": { "id": "C2", "score": 91, "level": "VERY_HIGH" },
              "dataDisclaimer": "AI-generated estimate based on prototype dataset."
            }
        """.trimIndent()

        val response = json.decodeFromString<CrowdResponse>(payload)
        assertTrue(response.success)
        assertEquals("MUM-0842", response.trainId)
        assertEquals(82, response.overallScore)
        assertEquals("C7", response.bestCoach.id)
        assertEquals(65, response.bestCoach.score)
    }

    @Test
    fun testLiveGemmaAnalyzeResponseDeserialization() {
        val payload = """
            {
              "success": true,
              "trainId": "MUM-0842",
              "trainName": "Kalyan Fast",
              "departure": "08:42",
              "arrival": "09:55",
              "analysis": {
                "crowdLevel": "VERY_HIGH",
                "crowdScore": 82,
                "confidence": 0.9,
                "summary": "Based on prototype data, the Kalyan Fast (MUM-0842) is expected to be extremely crowded during the peak period.",
                "reasoning": [
                  "Overall crowd score of 82/100 indicates very high density",
                  "Peak period travel is reflected in the high historical average of 80/100"
                ],
                "recommendation": "Consider shifting to the 09:25 Kalyan Slow train if your schedule permits.",
                "bestCoach": {
                  "coach": "C7",
                  "score": 65,
                  "level": "HIGH"
                },
                "alternative": {
                  "trainId": "MUM-0925",
                  "trainName": "Kalyan Slow",
                  "departure": "09:25",
                  "crowdScore": 35,
                  "crowdLevel": "MODERATE",
                  "difference": 47,
                  "reason": "Significantly lower estimated crowd score based on prototype data"
                },
                "source": "gemma-4",
                "model": "gemma-4-31b-it",
                "disclaimer": "AI-generated estimate based on prototype dataset. Not official railway data."
              },
              "dataDisclaimer": "AI-generated estimate based on prototype dataset. Not official railway data."
            }
        """.trimIndent()

        val response = json.decodeFromString<AnalyzeResponse>(payload)
        assertTrue(response.success)
        assertEquals("gemma-4", response.analysis.source)
        assertEquals("gemma-4-31b-it", response.analysis.model)
        assertNotNull(response.analysis.bestCoach)
        assertEquals("C7", response.analysis.bestCoach?.coachName)
        assertEquals(65, response.analysis.bestCoach?.score)
        assertNotNull(response.analysis.alternative)
        assertEquals("MUM-0925", response.analysis.alternative?.trainId)
        assertEquals(47, response.analysis.alternative?.difference)
    }

    @Test
    fun testFallbackAnalyzeResponseDeserialization() {
        val payload = """
            {
              "success": true,
              "trainId": "MUM-0842",
              "trainName": "Kalyan Fast",
              "departure": "08:42",
              "arrival": "09:55",
              "analysis": {
                "crowdLevel": "VERY_HIGH",
                "crowdScore": 82,
                "confidence": 0.7,
                "summary": "Crowding is estimated to be very high for this peak train.",
                "reasoning": [
                  "Peak-hour travel period typically sees higher passenger volume"
                ],
                "recommendation": "Consider waiting for a later train if your schedule allows.",
                "bestCoach": {
                  "coach": "C7",
                  "score": 65,
                  "level": "HIGH"
                },
                "alternative": {
                  "trainId": "MUM-0925",
                  "trainName": "Kalyan Slow",
                  "departure": "09:25",
                  "crowdScore": 35,
                  "crowdLevel": "MODERATE",
                  "difference": 47,
                  "reason": "47% lower estimated crowd score"
                },
                "source": "rule-based-fallback",
                "disclaimer": "AI service temporarily unavailable. Showing prototype rule-based analysis."
              },
              "dataDisclaimer": "AI-generated estimate based on prototype dataset. Not official railway data."
            }
        """.trimIndent()

        val response = json.decodeFromString<AnalyzeResponse>(payload)
        assertTrue(response.success)
        assertEquals("rule-based-fallback", response.analysis.source)
        assertNull(response.analysis.model)
        assertEquals("C7", response.analysis.bestCoach?.coachName)
        assertEquals("MUM-0925", response.analysis.alternative?.trainId)
    }

    @Test
    fun testAnalyzeResponseWithNullOrEmptyBestCoachAndAlternative() {
        val payload = """
            {
              "success": true,
              "trainId": "MUM-0842",
              "trainName": "Kalyan Fast",
              "departure": "08:42",
              "arrival": "09:55",
              "analysis": {
                "crowdLevel": "LOW",
                "crowdScore": 25,
                "confidence": 0.9,
                "summary": "Comfortable travel conditions expected.",
                "reasoning": [],
                "recommendation": "Normal travel recommended.",
                "bestCoach": null,
                "alternative": null,
                "source": "gemma-4",
                "model": "gemma-4-31b-it",
                "disclaimer": "Sample disclaimer"
              }
            }
        """.trimIndent()

        val response = json.decodeFromString<AnalyzeResponse>(payload)
        assertTrue(response.success)
        assertNull(response.analysis.bestCoach)
        assertNull(response.analysis.alternative)
    }

    @Test
    fun testBestCoachRecommendationWithIdFallback() {
        val payloadWithId = """
            {
              "id": "C5",
              "score": 50,
              "level": "MODERATE"
            }
        """.trimIndent()

        val rec = json.decodeFromString<BestCoachRecommendation>(payloadWithId)
        assertEquals("C5", rec.coachName)
        assertEquals(50, rec.score)
    }

    @Test
    fun testProductionBaseUrlConfiguration() {
        assertEquals("https://khali-jagah-backend.onrender.com/", RetrofitClient.PRODUCTION_BASE_URL)
        assertEquals("http://10.0.2.2:3000/", RetrofitClient.EMULATOR_LOCAL_BASE_URL)
    }
}
