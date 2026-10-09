package com.khalijagah.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khalijagah.app.ui.theme.*

@Composable
fun CrowdLevelBadge(
    level: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, borderColor, textColor, label) = when (level.uppercase()) {
        "LOW" -> Quadruple(CrowdLowBg, CrowdLowBorder, CrowdLow, "LOW")
        "MODERATE" -> Quadruple(CrowdModerateBg, CrowdModerateBorder, CrowdModerate, "MODERATE")
        "HIGH" -> Quadruple(CrowdHighBg, CrowdHighBorder, CrowdHigh, "HIGH")
        "VERY_HIGH" -> Quadruple(CrowdVeryHighBg, CrowdVeryHighBorder, CrowdVeryHigh, "VERY HIGH")
        else -> Quadruple(Slate100, BorderSubtle, Slate600, level)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
