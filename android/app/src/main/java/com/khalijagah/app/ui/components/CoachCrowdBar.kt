package com.khalijagah.app.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.khalijagah.app.data.model.Coach
import com.khalijagah.app.ui.theme.*

@Composable
fun CoachCrowdBar(
    coach: Coach,
    isBestCoach: Boolean = false,
    modifier: Modifier = Modifier
) {
    val barColor = when (coach.level.uppercase()) {
        "LOW" -> CrowdLow
        "MODERATE" -> CrowdModerate
        "HIGH" -> CrowdHigh
        "VERY_HIGH" -> CrowdVeryHigh
        else -> Slate400
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Coach label
        Text(
            text = coach.id,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isBestCoach) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isBestCoach) CrowdLow else TextPrimary,
            modifier = Modifier.width(38.dp)
        )

        // Progress Bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(18.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Slate100)
                .border(0.5.dp, BorderSubtle, RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (coach.score.coerceIn(0, 100)) / 100f)
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor)
                    .animateContentSize()
            )
        }

        Spacer(Modifier.width(10.dp))

        // Score
        Text(
            text = "${coach.score}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            textAlign = TextAlign.End,
            modifier = Modifier.width(28.dp)
        )

        Spacer(Modifier.width(8.dp))

        // Status badge
        CrowdLevelBadge(
            level = coach.level,
            modifier = Modifier.width(86.dp)
        )
    }
}
