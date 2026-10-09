package com.khalijagah.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khalijagah.app.data.model.AnalysisResult
import com.khalijagah.app.ui.theme.*

@Composable
fun AIInsightCard(
    analysis: AnalysisResult,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = SurfaceCard,
        border = BorderStroke(1.dp, BorderSubtle),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title + Model tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI Travel Insight",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                val isGemma = analysis.source.contains("gemma", ignoreCase = true) || 
                    analysis.model?.contains("gemma", ignoreCase = true) == true
                val isFallback = analysis.source.contains("fallback", ignoreCase = true) ||
                    analysis.source.contains("rule", ignoreCase = true)

                val modelLabel = when {
                    isGemma -> "Gemma 4"
                    isFallback -> "Rule-Based Fallback"
                    else -> "Analysis"
                }
                val badgeBg = if (isFallback) CrowdModerateBg else Slate100
                val badgeText = if (isFallback) CrowdModerate else Slate700
                val badgeBorder = if (isFallback) CrowdModerateBorder else BorderSubtle

                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, badgeBorder)
                ) {
                    Text(
                        text = modelLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeText,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Score & status bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                CrowdLevelBadge(level = analysis.crowdLevel)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "${analysis.crowdScore}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "/100",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(Modifier.weight(1f))
                if (analysis.confidence > 0) {
                    Text(
                        text = "Confidence: ${(analysis.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Summary text
            Text(
                text = analysis.summary,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                lineHeight = 22.sp
            )

            // Why section (Reasoning)
            if (analysis.reasoning.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = BorderSubtle)
                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Why this crowd level?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(8.dp))
                analysis.reasoning.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            color = Slate600,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Recommendation
            if (analysis.recommendation.isNotBlank()) {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = BorderSubtle)
                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Recommendation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = analysis.recommendation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )
            }

            // Best coach highlight
            analysis.bestCoach?.let { best ->
                val coachName = best.coachName
                if (coachName.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = CrowdLowBg,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, CrowdLowBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recommended Coach: $coachName",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CrowdLow
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = "Score: ${best.score}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = CrowdLow
                            )
                        }
                    }
                }
            }

            // Alternative train suggestion
            analysis.alternative?.let { alt ->
                if (alt.trainId.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Slate50,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Less Crowded Alternative",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "${alt.trainName} • ${alt.departure}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                                CrowdLevelBadge(level = alt.crowdLevel)
                            }
                            if (alt.reason.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = alt.reason,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            // Disclaimer
            if (analysis.disclaimer.isNotBlank()) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = analysis.disclaimer,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
