package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class FitBusterAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(FitBusterNotificationHelper.EXTRA_TITLE)
            ?: "FIT BUSTER Reminder"
        val body = intent.getStringExtra(FitBusterNotificationHelper.EXTRA_BODY)
            ?: "Stay consistent with your FIT BUSTER training."
        val id = intent.getIntExtra(FitBusterNotificationHelper.EXTRA_NOTIFICATION_ID, 1001)

        FitBusterNotificationHelper.showNotification(
            context = context,
            channelId = FitBusterNotificationHelper.CHANNEL_REMINDERS,
            notificationId = id,
            title = title,
            body = body
        )

        if (!FitBusterNotificationHelper.hasNotificationPermission(context)) {
            SoundAlertHelper.playReminderSound(context, isAlarmOrTimer = true)
        }
    }
}
