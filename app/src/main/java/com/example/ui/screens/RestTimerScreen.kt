package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.FitBusterDashboardData
import com.example.ui.RestTimerState
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FitBusterCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.HydrationCyan
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SuccessGreen
import com.example.util.FitBusterDateUtil

@Composable
fun RestTimerScreen(
    data: FitBusterDashboardData,
    restTimer: RestTimerState,
    onSetDuration: (Int) -> Unit,
    onStartOrResume: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onExtend: (Int) -> Unit,
    onSkip: () -> Unit
) {
    var customSecText by remember { mutableStateOf("") }
    val primaryColor = MaterialTheme.colorScheme.primary

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            FitBusterCard(highlighted = restTimer.isRunning || restTimer.completedBannerVisible) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("REST TIMER", style = MaterialTheme.typography.titleLarge)

                    if (restTimer.completedBannerVisible) {
                        Surface(
                            color = SuccessGreen.copy(alpha = 0.18f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.CheckCircle, null, tint = SuccessGreen)
                                Text(
                                    text = "Rest Complete! Ready for next set.",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = SuccessGreen
                                )
                            }
                        }
                    }

                    // Circular Countdown Ring
                    Box(
                        modifier = Modifier.size(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val progress = if (restTimer.plannedSeconds > 0) {
                            restTimer.remainingSeconds.toFloat() / restTimer.plannedSeconds.toFloat()
                        } else 0f
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = Color(0xFF1F2630),
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 16f, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = primaryColor,
                                startAngle = -90f,
                                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                                useCenter = false,
                                style = Stroke(width = 16f, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = FitBusterDateUtil.formatDurationHms(restTimer.remainingSeconds.toLong()),
                                style = MaterialTheme.typography.displayLarge,
                                color = primaryColor
                            )
                            Text(
                                text = "of ${restTimer.plannedSeconds}s",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(30, 60, 90, 120).forEach { sec ->
                            FilterChip(
                                selected = restTimer.plannedSeconds == sec,
                                onClick = { onSetDuration(sec) },
                                label = { Text("${sec}s") }
                            )
                        }
                    }

                    // Primary Controls: Start/Resume, Pause, Reset
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (restTimer.isRunning) {
                            OutlinedButton(
                                onClick = onPause,
                                modifier = Modifier.weight(1f).height(48.dp).testTag("rest_pause_btn")
                            ) {
                                Icon(Icons.Filled.Pause, null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pause")
                            }
                        } else {
                            Button(
                                onClick = onStartOrResume,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = ObsidianBlack
                                ),
                                modifier = Modifier.weight(1f).height(48.dp).testTag("rest_start_btn")
                            ) {
                                Icon(Icons.Filled.PlayArrow, null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (restTimer.isPaused) "Resume" else "Start", fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = onReset,
                            modifier = Modifier.weight(1f).height(48.dp).testTag("rest_reset_btn")
                        ) {
                            Icon(Icons.Filled.Refresh, null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset")
                        }
                    }

                    // Extend (+15s / +30s) & Skip Rest
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onExtend(15) },
                            modifier = Modifier.weight(1f).height(48.dp).testTag("rest_extend_15_btn")
                        ) {
                            Icon(Icons.Filled.Add, null, Modifier.size(16.dp))
                            Text("15s")
                        }
                        OutlinedButton(
                            onClick = { onExtend(30) },
                            modifier = Modifier.weight(1f).height(48.dp).testTag("rest_extend_30_btn")
                        ) {
                            Icon(Icons.Filled.Add, null, Modifier.size(16.dp))
                            Text("30s")
                        }
                        OutlinedButton(
                            onClick = onSkip,
                            modifier = Modifier.weight(1f).height(48.dp).testTag("rest_skip_btn")
                        ) {
                            Icon(Icons.Filled.SkipNext, null, Modifier.size(16.dp))
                            Text("Skip")
                        }
                    }

                    // Custom Rest Duration Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = customSecText,
                            onValueChange = { customSecText = it },
                            label = { Text("Custom Seconds") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                val sec = customSecText.toIntOrNull() ?: return@Button
                                customSecText = ""
                                onSetDuration(sec)
                            },
                            modifier = Modifier.height(52.dp)
                        ) {
                            Text("Set")
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(title = "Rest History", subtitle = "${data.restLogs.size} recorded intervals")
        }

        if (data.restLogs.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Filled.Timer,
                    title = "No rest history yet",
                    description = "Complete or skip a rest countdown to track your recovery discipline."
                )
            }
        } else {
            items(data.restLogs.take(15), key = { it.id.ifBlank { it.hashCode().toString() } }) { log ->
                FitBusterCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Planned ${log.plannedSeconds}s • Actual ${log.actualSeconds}s", style = MaterialTheme.typography.titleMedium)
                            Text(FitBusterDateUtil.formatDisplayDate(log.dateKey), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = log.status,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (log.status == "COMPLETED") SuccessGreen else HydrationCyan
                        )
                    }
                }
            }
        }
    }
}
