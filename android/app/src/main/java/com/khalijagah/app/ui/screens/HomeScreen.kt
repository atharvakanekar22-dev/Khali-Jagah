package com.khalijagah.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khalijagah.app.data.model.Station
import com.khalijagah.app.ui.components.StationSelectorSheet
import com.khalijagah.app.ui.theme.*
import com.khalijagah.app.viewmodel.AppUiState

@Composable
fun HomeScreen(
    uiState: AppUiState,
    onSourceSelect: (Station) -> Unit,
    onDestSelect: (Station) -> Unit,
    onSwap: () -> Unit,
    onCheckCrowd: () -> Unit,
    onAbout: () -> Unit
) {
    var showSourceSheet by remember { mutableStateOf(false) }
    var showDestSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // Top App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SurfaceCard,
            border = BorderStroke(0.dp, Color.Transparent),
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Train,
                            contentDescription = null,
                            tint = Slate900,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Khali Jagah",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "Know the crowd before you board",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = onAbout,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "About",
                        tint = Slate600
                    )
                }
            }
        }

        HorizontalDivider(color = BorderSubtle, thickness = 1.dp)

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Station selection card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = SurfaceCard,
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Plan Route",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(14.dp))

                    // Source station
                    StationField(
                        label = "ORIGIN STATION",
                        station = uiState.sourceStation,
                        placeholder = "Tap to select origin",
                        onClick = { showSourceSheet = true }
                    )

                    // Swap button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            onClick = onSwap,
                            enabled = uiState.sourceStation != null || uiState.destStation != null,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.SwapVert,
                                contentDescription = "Swap stations",
                                tint = Slate700,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Destination station
                    StationField(
                        label = "DESTINATION STATION",
                        station = uiState.destStation,
                        placeholder = "Tap to select destination",
                        onClick = { showDestSheet = true }
                    )

                    Spacer(Modifier.height(18.dp))

                    // Check Crowd button
                    val isButtonEnabled = uiState.sourceStation != null &&
                            uiState.destStation != null &&
                            uiState.sourceStation.id != uiState.destStation?.id

                    Button(
                        onClick = onCheckCrowd,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = isButtonEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Slate900,
                            contentColor = TextOnDark,
                            disabledContainerColor = Slate200,
                            disabledContentColor = Slate400
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Check Crowd & Trains",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Informational tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceCard,
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Gemma 4 Intelligence",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Contextual risk & coach reasoning",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceCard,
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "12-Coach Breakdown",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Coach-level crowd distribution",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Disclaimer
            Text(
                text = "Prototype demonstration \u2022 Crowd data is simulated for demo purposes",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Bottom sheets
    if (showSourceSheet) {
        StationSelectorSheet(
            stations = uiState.stations,
            title = "Select Origin Station",
            onStationSelected = { station ->
                android.util.Log.d("KhaliJagah", "Origin station selected in HomeScreen: ${station.shortName} (${station.id})")
                onSourceSelect(station)
                showSourceSheet = false
            },
            onDismiss = { showSourceSheet = false }
        )
    }
    if (showDestSheet) {
        StationSelectorSheet(
            stations = uiState.stations,
            title = "Select Destination Station",
            onStationSelected = { station ->
                android.util.Log.d("KhaliJagah", "Destination station selected in HomeScreen: ${station.shortName} (${station.id})")
                onDestSelect(station)
                showDestSheet = false
            },
            onDismiss = { showDestSheet = false }
        )
    }
}

@Composable
private fun StationField(
    label: String,
    station: Station?,
    placeholder: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        color = Slate50,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Slate500,
                letterSpacing = 0.5.sp
            )
            Spacer(Modifier.height(4.dp))
            if (station != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = station.shortName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = station.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    val lineBadgeColor = when (station.line.lowercase()) {
                        "central" -> CentralLineColor
                        "western" -> WesternLineColor
                        "harbour" -> HarbourLineColor
                        else -> Slate700
                    }
                    Surface(
                        color = lineBadgeColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = station.line.uppercase(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = lineBadgeColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextMuted
                )
            }
        }
    }
}
