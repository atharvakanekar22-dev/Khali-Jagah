package com.khalijagah.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Station(
    val id: String,
    val name: String,
    val shortName: String,
    val line: String,
    val order: Int
)

val DEFAULT_STATIONS: List<Station> = listOf(
    Station(id = "CSMT", name = "Chhatrapati Shivaji Maharaj Terminus", shortName = "CSMT", line = "central", order = 1),
    Station(id = "MAS", name = "Masjid Bunder", shortName = "Masjid", line = "central", order = 2),
    Station(id = "BY", name = "Byculla", shortName = "Byculla", line = "central", order = 3),
    Station(id = "DR", name = "Dadar", shortName = "Dadar", line = "central", order = 4),
    Station(id = "KR", name = "Kurla", shortName = "Kurla", line = "central", order = 5),
    Station(id = "GC", name = "Ghatkopar", shortName = "Ghatkopar", line = "central", order = 6),
    Station(id = "VK", name = "Vikhroli", shortName = "Vikhroli", line = "central", order = 7),
    Station(id = "BN", name = "Bhandup", shortName = "Bhandup", line = "central", order = 8),
    Station(id = "MN", name = "Mulund", shortName = "Mulund", line = "central", order = 9),
    Station(id = "TN", name = "Thane", shortName = "Thane", line = "central", order = 10),
    Station(id = "DI", name = "Dombivli", shortName = "Dombivli", line = "central", order = 11),
    Station(id = "KY", name = "Kalyan", shortName = "Kalyan", line = "central", order = 12),
    Station(id = "CCG", name = "Churchgate", shortName = "Churchgate", line = "western", order = 1),
    Station(id = "AN", name = "Andheri", shortName = "Andheri", line = "western", order = 5),
    Station(id = "BV", name = "Borivali", shortName = "Borivali", line = "western", order = 8)
)

@Serializable
data class StationsResponse(
    val success: Boolean,
    val count: Int,
    val data: List<Station>
)

@Serializable
data class TrainResult(
    val id: String,
    val name: String,
    val type: String,
    val departure: String,
    val arrival: String,
    val journeyMinutes: Int,
    val stops: List<String>,
    val crowdScore: Int? = null,
    val crowdLevel: String = "UNKNOWN",
    val period: String? = null
)

@Serializable
data class TrainsResponse(
    val success: Boolean,
    val source: String,
    val destination: String,
    val count: Int,
    val dataDisclaimer: String = "",
    val data: List<TrainResult>
)

@Serializable
data class Coach(
    val id: String,
    val score: Int,
    val level: String
)

@Serializable
data class CoachSummary(
    val id: String? = null,
    val coach: String? = null,
    val score: Int,
    val level: String
)

@Serializable
data class CrowdResponse(
    val success: Boolean,
    val trainId: String,
    val overallScore: Int,
    val overallLevel: String,
    val period: String,
    val historicalAvg: Int,
    val trend: String,
    val coachCount: Int,
    val coaches: List<Coach>,
    val bestCoach: CoachSummary,
    val worstCoach: CoachSummary,
    val dataDisclaimer: String = ""
)

@Serializable
data class AlternativeTrain(
    val trainId: String = "",
    val trainName: String = "",
    val departure: String = "",
    val crowdScore: Int = 0,
    val crowdLevel: String = "",
    val difference: Int = 0,
    val reason: String = ""
)

@Serializable
data class BestCoachRecommendation(
    val coach: String = "",
    val id: String? = null,
    val score: Int = 0,
    val level: String = ""
) {
    val coachName: String
        get() = if (coach.isNotBlank()) coach else (id ?: "")
}

@Serializable
data class AnalysisResult(
    val crowdLevel: String = "",
    val crowdScore: Int = 0,
    val confidence: Double = 0.0,
    val summary: String = "",
    val reasoning: List<String> = emptyList(),
    val recommendation: String = "",
    val bestCoach: BestCoachRecommendation? = null,
    val alternative: AlternativeTrain? = null,
    val source: String = "",
    val model: String? = null,
    val disclaimer: String = ""
)

@Serializable
data class AnalyzeResponse(
    val success: Boolean,
    val trainId: String,
    val trainName: String,
    val departure: String,
    val arrival: String,
    val analysis: AnalysisResult,
    val dataDisclaimer: String = ""
)

@Serializable
data class AnalyzeRequest(
    val trainId: String
)

@Serializable
data class HealthResponse(
    val status: String = "",
    val app: String = "",
    val version: String = "",
    val gemmaModel: String = "",
    val apiKeyConfigured: Boolean = false,
    val timestamp: String = ""
)
