package com.example.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import java.util.Calendar

object FitBusterNotificationHelper {
    private const val LEGACY_CHANNEL_REMINDERS = "fit_buster_reminders"
    private const val LEGACY_CHANNEL_TIMERS = "fit_buster_timers"
    private const val LEGACY_CHANNEL_ACHIEVEMENTS = "fit_buster_achievements"

    const val CHANNEL_REMINDERS = "fit_buster_reminders_v2"
    const val CHANNEL_TIMERS = "fit_buster_timers_v2"
    const val CHANNEL_ACHIEVEMENTS = "fit_buster_achievements_v2"

    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_BODY = "extra_body"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

    private const val REQ_WORKOUT_ALARM = 1001
    private const val REQ_WATER_ALARM = 1002
    private const val REQ_MISSION_ALARM = 1003
    private const val REQ_STREAK_ALARM = 1004

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // Remove silent legacy channels so upgraded channels apply audible sound immediately
            runCatching {
                manager.deleteNotificationChannel(LEGACY_CHANNEL_REMINDERS)
                manager.deleteNotificationChannel(LEGACY_CHANNEL_TIMERS)
                manager.deleteNotificationChannel(LEGACY_CHANNEL_ACHIEVEMENTS)
            }

            val reminderSound = SoundAlertHelper.defaultSoundUri(isAlarmOrTimer = false)
            val timerSound = SoundAlertHelper.defaultSoundUri(isAlarmOrTimer = true)

            val reminderAudioAttrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val alarmAudioAttrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channels = listOf(
                NotificationChannel(
                    CHANNEL_REMINDERS,
                    "Fitness & Hydration Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Audible daily workout, water intake, mission, and streak reminders."
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0L, 250L, 120L, 250L)
                    setSound(reminderSound, reminderAudioAttrs)
                },
                NotificationChannel(
                    CHANNEL_TIMERS,
                    "Rest & Sprint Timers",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Loud alerts when rest timers finish or personal bests are set."
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0L, 300L, 140L, 300L)
                    setSound(timerSound, alarmAudioAttrs)
                },
                NotificationChannel(
                    CHANNEL_ACHIEVEMENTS,
                    "Medals & Certificates",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Celebration alerts with sound when medals or certificates unlock."
                    enableVibration(true)
                    setSound(reminderSound, reminderAudioAttrs)
                }
            )
            manager.createNotificationChannels(channels)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showNotification(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        body: String
    ) {
        ensureChannels(context)
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val isTimer = channelId == CHANNEL_TIMERS
        val soundUri = SoundAlertHelper.defaultSoundUri(isAlarmOrTimer = isTimer)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(if (isTimer) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_REMINDER)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0L, 250L, 120L, 250L))
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
        }
    }

    fun scheduleReminders(
        context: Context,
        masterEnabled: Boolean,
        workoutEnabled: Boolean,
        workoutHour: Int,
        workoutMinute: Int,
        waterEnabled: Boolean,
        missionEnabled: Boolean,
        streakEnabled: Boolean
    ) {
        ensureChannels(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (!masterEnabled) {
            cancelAlarm(context, alarmManager, REQ_WORKOUT_ALARM)
            cancelAlarm(context, alarmManager, REQ_WATER_ALARM)
            cancelAlarm(context, alarmManager, REQ_MISSION_ALARM)
            cancelAlarm(context, alarmManager, REQ_STREAK_ALARM)
            return
        }

        if (workoutEnabled) {
            scheduleDailyAlarm(
                context, alarmManager, REQ_WORKOUT_ALARM,
                workoutHour.coerceIn(0, 23), workoutMinute.coerceIn(0, 59),
                "FIT BUSTER Workout Time",
                "Time to hit your daily training session and build your streak!"
            )
        } else {
            cancelAlarm(context, alarmManager, REQ_WORKOUT_ALARM)
        }

        if (waterEnabled) {
            scheduleRepeatingInterval(
                context, alarmManager, REQ_WATER_ALARM,
                AlarmManager.INTERVAL_HOUR * 2,
                "Hydration Check",
                "Log 250 ml of water in FIT BUSTER to stay on track for your daily goal."
            )
        } else {
            cancelAlarm(context, alarmManager, REQ_WATER_ALARM)
        }

        if (missionEnabled) {
            scheduleDailyAlarm(
                context, alarmManager, REQ_MISSION_ALARM,
                18, 0,
                "Daily Fitness Missions",
                "Check your active FIT BUSTER missions and claim automatic reward points!"
            )
        } else {
            cancelAlarm(context, alarmManager, REQ_MISSION_ALARM)
        }

        if (streakEnabled) {
            scheduleDailyAlarm(
                context, alarmManager, REQ_STREAK_ALARM,
                20, 30,
                "Protect Your Streak",
                "Keep your FIT BUSTER streak alive—log a workout or enable a Planned Recovery Day."
            )
        } else {
            cancelAlarm(context, alarmManager, REQ_STREAK_ALARM)
        }
    }

    private fun scheduleDailyAlarm(
        context: Context,
        alarmManager: AlarmManager,
        requestCode: Int,
        hour: Int,
        minute: Int,
        title: String,
        body: String
    ) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        val pi = buildPendingIntent(context, requestCode, title, body)
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pi
        )
    }

    private fun scheduleRepeatingInterval(
        context: Context,
        alarmManager: AlarmManager,
        requestCode: Int,
        intervalMillis: Long,
        title: String,
        body: String
    ) {
        val pi = buildPendingIntent(context, requestCode, title, body)
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + intervalMillis,
            intervalMillis,
            pi
        )
    }

    private fun cancelAlarm(context: Context, alarmManager: AlarmManager, requestCode: Int) {
        val pi = buildPendingIntent(context, requestCode, "", "")
        alarmManager.cancel(pi)
    }

    private fun buildPendingIntent(
        context: Context,
        requestCode: Int,
        title: String,
        body: String
    ): PendingIntent {
        val intent = Intent(context, FitBusterAlarmReceiver::class.java).apply {
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_BODY, body)
            putExtra(EXTRA_NOTIFICATION_ID, requestCode)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
