package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.FitBusterDashboardData
import com.example.ui.components.BarChartPoint
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FitBusterCard
import com.example.ui.components.NeonProgressBar
import com.example.ui.components.SectionHeader
import com.example.ui.components.WeeklyBarChart
import com.example.ui.theme.HydrationCyan
import com.example.ui.theme.ObsidianBlack
import com.example.util.FitBusterDateUtil

@Composable
fun WaterTrackerScreen(
    data: FitBusterDashboardData,
    onAddWater: (Long) -> Unit,
    onEditGoal: (Long) -> Unit,
    onToggleWaterReminder: (Boolean) -> Unit,
    onTestReminderSound: () -> Unit
) {
    val today = remember { FitBusterDateUtil.todayKey() }
    val todayLogs = remember(data.waterLogs, today) { data.waterLogs.filter { it.dateKey == today } }
    val todayMl = remember(todayLogs) { todayLogs.sumOf { it.amountMl } }
    val goalMl = data.profile.dailyWaterGoalMl.coerceAtLeast(500L)

    val last7Days = remember { FitBusterDateUtil.lastNDaysKeys(7) }
    val weeklyWaterPoints = remember(data.waterLogs, last7Days) {
        last7Days.map { key ->
            val sum = data.waterLogs.filter { it.dateKey == key }.sumOf { it.amountMl }.toFloat()
            BarChartPoint(FitBusterDateUtil.shortDayLabel(key), sum)
        }
    }

    var customMlText by remember { mutableStateOf("") }
    var showGoalDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            FitBusterCard(highlighted = true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("TODAY'S HYDRATION", style = MaterialTheme.typography.labelMedium, color = HydrationCyan)
                        Text(
                            text = "$todayMl / $goalMl ml",
                            style = MaterialTheme.typography.displayMedium
                        )
                    }
                    IconButton(onClick = { showGoalDialog = true }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Water Goal", tint = HydrationCyan)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                NeonProgressBar(
                    progress = todayMl.toFloat() / goalMl.toFloat(),
                    color = HydrationCyan
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(150L, 250L, 500L).forEach { ml ->
                        Button(
                            onClick = { onAddWater(ml) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HydrationCyan,
                                contentColor = ObsidianBlack
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("add_water_${ml}_btn")
                        ) {
                            Text("+${ml}ml", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = customMlText,
                        onValueChange = { customMlText = it },
                        label = { Text("Custom ML (e.g. 350)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("custom_water_input")
                    )
                    OutlinedButton(
                        onClick = {
                            val ml = customMlText.toLongOrNull() ?: return@OutlinedButton
                            customMlText = ""
                            onAddWater(ml)
                        },
                        modifier = Modifier.height(52.dp).testTag("add_custom_water_btn")
                    ) {
                        Text("Add ML")
                    }
                }
            }
        }

        // Water Reminders Switch
        item {
            FitBusterCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Filled.NotificationsActive, contentDescription = "Water Reminders", tint = HydrationCyan)
                            Column {
                                Text("Water Reminders", style = MaterialTheme.typography.titleMedium)
                                Text("Every 2 hours with alert sound", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = data.profile.waterReminderEnabled,
                            onCheckedChange = onToggleWaterReminder
                        )
                    }
                    OutlinedButton(
                        onClick = onTestReminderSound,
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("test_water_sound_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, null, tint = HydrationCyan)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Water Reminder Sound")
                    }
                }
            }
        }

        // Weekly Water Report Graph
        item {
            SectionHeader(title = "Weekly Water Report", subtitle = "Daily intake in milliliters (last 7 days)")
            Spacer(modifier = Modifier.height(8.dp))
            FitBusterCard {
                WeeklyBarChart(
                    points = weeklyWaterPoints,
                    barColor = HydrationCyan,
                    unitSuffix = ""
                )
            }
        }

        // Daily Water History
        item {
            SectionHeader(title = "Daily Water History", subtitle = "${todayLogs.size} entries today")
        }
        if (data.waterLogs.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Filled.WaterDrop,
                    title = "No water logged yet",
                    description = "Tap +250ml above to start tracking your hydration."
                )
            }
        } else {
            items(data.waterLogs.take(20), key = { it.id.ifBlank { it.hashCode().toString() } }) { log ->
                FitBusterCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.WaterDrop, null, tint = HydrationCyan)
                            Text("+${log.amountMl} ml", style = MaterialTheme.typography.titleMedium)
                        }
                        Text(FitBusterDateUtil.formatDisplayDate(log.dateKey), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    if (showGoalDialog) {
        var goalInput by remember { mutableStateOf(goalMl.toString()) }
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text("Edit Daily Water Goal") },
            text = {
                OutlinedTextField(
                    value = goalInput,
                    onValueChange = { goalInput = it },
                    label = { Text("Goal in ML (500–10000)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val valMl = goalInput.toLongOrNull() ?: goalMl
                        showGoalDialog = false
                        onEditGoal(valMl)
                    }
                ) { Text("Update") }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) { Text("Cancel") }
            }
        )
    }
}
