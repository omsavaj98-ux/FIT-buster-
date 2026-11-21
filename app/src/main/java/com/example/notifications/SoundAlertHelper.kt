package com.example.notifications

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Handler
import android.os.Looper

object SoundAlertHelper {

    fun defaultSoundUri(isAlarmOrTimer: Boolean): Uri? {
        return if (isAlarmOrTimer) {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }
    }

    fun playReminderSound(context: Context, isAlarmOrTimer: Boolean = false) {
        val playedRingtone = runCatching {
            val uri = defaultSoundUri(isAlarmOrTimer) ?: return@runCatching false
            val ringtone: Ringtone = RingtoneManager.getRingtone(context.applicationContext, uri)
                ?: return@runCatching false
            ringtone.audioAttributes = AudioAttributes.Builder()
                .setUsage(
                    if (isAlarmOrTimer) AudioAttributes.USAGE_ALARM
                    else AudioAttributes.USAGE_NOTIFICATION_EVENT
                )
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            ringtone.play()
            if (isAlarmOrTimer) {
                Handler(Looper.getMainLooper()).postDelayed({
                    runCatching {
                        if (ringtone.isPlaying) ringtone.stop()
                    }
                }, 2200L)
            }
            true
        }.getOrDefault(false)

        if (!playedRingtone) {
            playFallbackTone(isAlarmOrTimer)
        }
    }

    private fun playFallbackTone(isAlarmOrTimer: Boolean) {
        runCatching {
            val stream = if (isAlarmOrTimer) AudioManager.STREAM_ALARM else AudioManager.STREAM_NOTIFICATION
            val toneType = if (isAlarmOrTimer) ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD else ToneGenerator.TONE_PROP_BEEP2
            val tg = ToneGenerator(stream, 90)
            tg.startTone(toneType, 450)
            Handler(Looper.getMainLooper()).postDelayed({
                runCatching {
                    tg.stopTone()
                    tg.release()
                }
            }, 550L)
        }
    }
}
