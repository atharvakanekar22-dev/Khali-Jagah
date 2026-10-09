package com.khalijagah.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khalijagah.app.data.model.AnalyzeResponse
import com.khalijagah.app.data.model.CrowdResponse
import com.khalijagah.app.data.model.TrainResult
import com.khalijagah.app.ui.components.AIInsightCard
import com.khalijagah.app.ui.components.CoachCrowdBar
import com.khalijagah.app.ui.components.CrowdLevelBadge
import com.khalijagah.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrowdDetailScreen(
    train: TrainResult,
    sourceName: String,
    destName: String,
    crowdData: CrowdResponse?,
    crowdLoading: Boolean,
    crowdError: String?,
    analysisResult: AnalyzeResponse?,
    analysisLoading: Boolean,
    analysisError: String?,
    onAnalyze: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // Top App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SurfaceCard,
            tonalElevation = 0.dp
        ) {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "$sourceName → $destName",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${train.name} • ${train.departure}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Slate800
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SurfaceCard
                    )
                )

                if (crowdData != null) {
                    // Train Crowd Score Banner
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp),
                        color = Slate50,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "OVERALL CROWD",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "${crowdData.overallScore}",
                                        style = MaterialTheme.typography.displayLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "/100",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                CrowdLevelBadge(level = crowdData.overallLevel)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "${crowdData.period.replace("_", " ")} • trend: ${crowdData.trend}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = BorderSubtle, thickness = 1.dp)

        // Scrollable content
        when {
            crowdLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = Slate800,
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 3.dp
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "Loading crowd breakdown...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            crowdError != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = CrowdVeryHigh,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            crowdError,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            crowdData != null -> {
                val bestCoachId = crowdData.bestCoach.id ?: crowdData.bestCoach.coach ?: ""

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Coach-wise breakdown card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceCard,
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Coach Occupancy",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${crowdData.coachCount} Coaches",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }

                            if (bestCoachId.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Recommended: $bestCoachId (Score: ${crowdData.bestCoach.score})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CrowdLow,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            crowdData.coaches.forEach { coach ->
                                CoachCrowdBar(
                                    coach = coach,
                                    isBestCoach = coach.id == bestCoachId
                                )
                            }
                        }
                    }

                    // Get AI Insight button
                    if (analysisResult == null && !analysisLoading) {
                        Button(
                            onClick = onAnalyze,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Slate900,
                                contentColor = TextOnDark
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Analyze Route with Gemma 4",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Analysis loading state
                    if (analysisLoading) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = SurfaceCard,
                            border = BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    color = Slate800,
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 3.dp
                                )
                                Spacer(Modifier.height(14.dp))
                                Text(
                                    "Analyzing crowd with Gemma 4...",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Generating actionable travel recommendation",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // Analysis error state
                    if (analysisError != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = CrowdVeryHighBg,
                            border = BorderStroke(1.dp, CrowdVeryHighBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    "Analysis unavailable",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CrowdVeryHigh
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    analysisError,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Spacer(Modifier.height(8.dp))
                                TextButton(
                                    onClick = onAnalyze,
                                    colors = ButtonDefaults.textButtonColors(contentColor = Slate900)
                                ) {
                                    Text("Retry Analysis", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // AI Insight result
                    analysisResult?.let { result ->
                        AIInsightCard(analysis = result.analysis)
                    }

                    // Bottom disclaimer
                    Text(
                        text = "Prototype demonstration data • Not official railway measurements",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
