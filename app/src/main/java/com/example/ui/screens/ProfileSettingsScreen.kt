package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.model.WorkoutType
import com.example.ui.FitBusterDashboardData
import com.example.ui.components.FitBusterCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SuccessGreen
import java.util.Locale

@Composable
fun ProfileSettingsScreen(
    data: FitBusterDashboardData,
    signatureBitmap: Bitmap?,
    profilePhotoBitmap: Bitmap?,
    onSaveProfile: (String, Long, String, String, String, Boolean) -> Unit,
    onUpdateNotifications: (Boolean, Boolean, Int, Int, Boolean, Boolean, Boolean) -> Unit,
    onUploadSignature: (Uri) -> Unit,
    onRemoveSignature: () -> Unit,
    onUploadProfilePhoto: (Uri) -> Unit,
    onExportBackup: (Uri) -> Unit,
    onImportBackup: (Uri) -> Unit,
    onResetProgress: () -> Unit,
    onTestReminderSound: () -> Unit
) {
    val context = LocalContext.current

    var displayName by remember(data.profile.displayName) { mutableStateOf(data.profile.displayName) }
    var ageText by remember(data.profile.age) { mutableStateOf(data.profile.age.toString()) }
    var fitnessGoal by remember(data.profile.fitnessGoal) { mutableStateOf(data.profile.fitnessGoal) }
    var preferredWorkout by remember(data.profile.preferredWorkout) { mutableStateOf(data.profile.preferredWorkout) }
    var themeAccent by remember(data.profile.themeAccent) { mutableStateOf(data.profile.themeAccent) }
    var profilePrivate by remember(data.profile.profilePrivate) { mutableStateOf(data.profile.profilePrivate) }

    var masterNotif by remember(data.profile.notificationsEnabled) { mutableStateOf(data.profile.notificationsEnabled) }
    var workoutNotif by remember(data.profile.workoutReminderEnabled) { mutableStateOf(data.profile.workoutReminderEnabled) }
    var alarmHour by remember(data.profile.workoutReminderHour) { mutableIntStateOf(data.profile.workoutReminderHour.toInt()) }
    var alarmMinute by remember(data.profile.workoutReminderMinute) { mutableIntStateOf(data.profile.workoutReminderMinute.toInt()) }
    var waterNotif by remember(data.profile.waterReminderEnabled) { mutableStateOf(data.profile.waterReminderEnabled) }
    var missionNotif by remember(data.profile.missionReminderEnabled) { mutableStateOf(data.profile.missionReminderEnabled) }
    var streakNotif by remember(data.profile.streakReminderEnabled) { mutableStateOf(data.profile.streakReminderEnabled) }

    var showResetConfirm by remember { mutableStateOf(false) }

    val signaturePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let(onUploadSignature) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let(onUploadProfilePhoto) }

    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let(onExportBackup) }

    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(onImportBackup) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onUpdateNotifications(masterNotif, workoutNotif, alarmHour, alarmMinute, waterNotif, missionNotif, streakNotif)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Account & Profile Photo Header
        item {
            FitBusterCard(highlighted = true) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profilePhotoBitmap != null) {
                            Image(
                                bitmap = profilePhotoBitmap.asImageBitmap(),
                                contentDescription = "Profile Photo",
                                contentScale = ContentScale.Crop,
                                filterQuality = FilterQuality.High,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(Icons.Filled.Person, contentDescription = "Default Avatar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(data.profile.displayName, style = MaterialTheme.typography.titleLarge)
                        Text(data.profile.fitnessGoal.ifBlank { "FIT BUSTER Athlete" }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.Storage, null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                            Text("On-Device Storage Active", style = MaterialTheme.typography.labelSmall, color = SuccessGreen)
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.testTag("change_profile_photo_btn")
                    ) {
                        Text("Photo")
                    }
                }
            }
        }

        // 2. Profile & Preferences
        item {
            SectionHeader(title = "Profile & Preferences", subtitle = "Account name appears on earned certificates")
            Spacer(modifier = Modifier.height(8.dp))
            FitBusterCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Account Name (Certificate Recipient)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("profile_name_input")
                    )
                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { ageText = it },
                        label = { Text("Age") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = fitnessGoal,
                        onValueChange = { fitnessGoal = it },
                        label = { Text("Fitness Goals") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Workout Preference", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(WorkoutType.OUTDOOR_RUN, WorkoutType.FUNCTIONAL_STRENGTH, WorkoutType.SPRINT_TRAINING).forEach { wt ->
                            FilterChip(
                                selected = preferredWorkout == wt.name,
                                onClick = { preferredWorkout = wt.name },
                                label = { Text(wt.displayName.substringBefore(" ")) }
                            )
                        }
                    }

                    Text("Theme Accent", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("NEON_LIME" to "Neon Lime", "ELECTRIC_VOLT" to "Volt", "CYBER_EMERALD" to "Emerald").forEach { (key, label) ->
                            FilterChip(
                                selected = themeAccent == key,
                                onClick = { themeAccent = key },
                                label = { Text(label) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Private Profile Mode", style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = profilePrivate, onCheckedChange = { profilePrivate = it })
                    }

                    Button(
                        onClick = {
                            onSaveProfile(
                                displayName,
                                ageText.toLongOrNull() ?: data.profile.age,
                                fitnessGoal,
                                preferredWorkout,
                                themeAccent,
                                profilePrivate
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = ObsidianBlack),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_profile_btn")
                    ) {
                        Text("Save Profile & Theme", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. Signature Settings (Private & Secure, Untouched Original Image)
        item {
            SectionHeader(title = "Signature Settings", subtitle = "Stored privately & rendered untouched on certificates")
            Spacer(modifier = Modifier.height(8.dp))
            FitBusterCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (signatureBitmap != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(92.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ObsidianBlack)
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = signatureBitmap.asImageBitmap(),
                                contentDescription = "Uploaded Original Signature",
                                contentScale = ContentScale.Fit,
                                filterQuality = FilterQuality.High,
                                modifier = Modifier.fillMaxSize().padding(8.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "No signature image uploaded yet. Select your original signature image from your device gallery.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                signaturePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = ObsidianBlack),
                            modifier = Modifier.weight(1f).height(48.dp).testTag("upload_signature_btn")
                        ) {
                            Icon(Icons.Filled.Draw, null, Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (signatureBitmap != null) "Replace Signature" else "Upload Signature", fontWeight = FontWeight.Bold)
                        }
                        if (signatureBitmap != null) {
                            OutlinedButton(onClick = onRemoveSignature, modifier = Modifier.height(48.dp)) {
                                Text("Remove")
                            }
                        }
                    }
                }
            }
        }

        // 4. Notifications & Alarms (Section 15)
        item {
            SectionHeader(title = "Notifications & Alarms", subtitle = "Daily workout, water, mission & streak sound alerts")
            Spacer(modifier = Modifier.height(8.dp))
            FitBusterCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    NotifToggleRow("All Notifications", masterNotif) { masterNotif = it }
                    NotifToggleRow("Daily Workout Reminder", workoutNotif) { workoutNotif = it }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format(Locale.US, "Custom Alarm Time: %02d:%02d", alarmHour, alarmMinute),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(onClick = { alarmHour = (alarmHour + 1) % 24 }) { Text("+1h") }
                            OutlinedButton(onClick = { alarmMinute = (alarmMinute + 15) % 60 }) { Text("+15m") }
                        }
                    }
                    NotifToggleRow("Water Reminders (2h)", waterNotif) { waterNotif = it }
                    NotifToggleRow("Mission Reminders", missionNotif) { missionNotif = it }
                    NotifToggleRow("Streak Protection Reminders", streakNotif) { streakNotif = it }

                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                            ) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                onUpdateNotifications(masterNotif, workoutNotif, alarmHour, alarmMinute, waterNotif, missionNotif, streakNotif)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = ObsidianBlack),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_notifications_btn")
                    ) {
                        Icon(Icons.Filled.Notifications, null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Alarms & Notifications", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onTestReminderSound,
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("test_reminder_sound_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Reminder Sound")
                    }
                }
            }
        }

        // 5. Backup, Restore on New Phone & Reset
        item {
            SectionHeader(title = "Backup, Restore & Reset", subtitle = "Local database + portable JSON export/import")
            Spacer(modifier = Modifier.height(8.dp))
            FitBusterCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { exportBackupLauncher.launch("fit_buster_backup.json") },
                            modifier = Modifier.weight(1f).height(48.dp).testTag("export_backup_btn")
                        ) {
                            Icon(Icons.Filled.CloudUpload, null, Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Backup JSON")
                        }
                        OutlinedButton(
                            onClick = { importBackupLauncher.launch(arrayOf("application/json", "*/*")) },
                            modifier = Modifier.weight(1f).height(48.dp).testTag("import_backup_btn")
                        ) {
                            Icon(Icons.Filled.CloudDownload, null, Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restore Data")
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Security, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Your workouts, medals, certificates, and signature stay stored privately on this device. Export a JSON backup anytime to move to a new phone.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = { showResetConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertCrimson),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("reset_progress_btn")
                    ) {
                        Icon(Icons.Filled.DeleteForever, null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset Progress")
                    }
                }
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset All Progress?") },
            text = { Text("This permanently deletes all workouts, sprints, meals, water logs, missions, medals, certificates, and streaks from your FIT BUSTER profile.") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirm = false
                        onResetProgress()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertCrimson)
                ) { Text("Confirm Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun NotifToggleRow(label: String, checked: Boolean, onChanged: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onChanged)
    }
}
