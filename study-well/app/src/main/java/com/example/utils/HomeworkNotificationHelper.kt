package com.example.utils

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.receiver.HomeworkAlarmReceiver

object HomeworkNotificationHelper {
    const val CHANNEL_ID = "homework_reminders_channel"
    const val ACTION_HOMEWORK_ALARM = "com.example.ACTION_HOMEWORK_ALARM"

    const val EXTRA_REMINDER_ID = "extra_reminder_id"
    const val EXTRA_SUBJECT = "extra_subject"
    const val EXTRA_TOPIC = "extra_topic"
    const val EXTRA_ALARM_TYPE = "extra_alarm_type"
    const val EXTRA_DUE_TIMESTAMP = "extra_due_timestamp"

    const val ALARM_TYPE_24H = "24h"
    const val ALARM_TYPE_2H = "2h"
    const val ALARM_TYPE_DUE = "due"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Homework Reminders"
            val descriptionText = "Notifications for upcoming homework due in 24 hours, 2 hours, and due time"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                enableLights(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleHomeworkAlarms(
        context: Context,
        reminderId: Long,
        subject: String,
        topic: String,
        dueTimestamp: Long
    ) {
        createChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val now = System.currentTimeMillis()

        // 1. 24 Hours Before Alert (only if due timestamp is more than 24 hours in future)
        val time24h = dueTimestamp - (24 * 60 * 60 * 1000L)
        if (time24h > now) {
            scheduleSingleAlarm(
                context = context,
                alarmManager = alarmManager,
                triggerAtMillis = time24h,
                reminderId = reminderId,
                subject = subject,
                topic = topic,
                dueTimestamp = dueTimestamp,
                alarmType = ALARM_TYPE_24H,
                requestCode = (reminderId * 10 + 1).toInt()
            )
        }

        // 2. 2 Hours Before Alert (only if due timestamp is more than 2 hours in future)
        val time2h = dueTimestamp - (2 * 60 * 60 * 1000L)
        if (time2h > now) {
            scheduleSingleAlarm(
                context = context,
                alarmManager = alarmManager,
                triggerAtMillis = time2h,
                reminderId = reminderId,
                subject = subject,
                topic = topic,
                dueTimestamp = dueTimestamp,
                alarmType = ALARM_TYPE_2H,
                requestCode = (reminderId * 10 + 2).toInt()
            )
        }

        // 3. Exact Due Time Alert
        if (dueTimestamp > now) {
            scheduleSingleAlarm(
                context = context,
                alarmManager = alarmManager,
                triggerAtMillis = dueTimestamp,
                reminderId = reminderId,
                subject = subject,
                topic = topic,
                dueTimestamp = dueTimestamp,
                alarmType = ALARM_TYPE_DUE,
                requestCode = (reminderId * 10 + 3).toInt()
            )
        }
    }

    private fun scheduleSingleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        reminderId: Long,
        subject: String,
        topic: String,
        dueTimestamp: Long,
        alarmType: String,
        requestCode: Int
    ) {
        val intent = Intent(context, HomeworkAlarmReceiver::class.java).apply {
            action = ACTION_HOMEWORK_ALARM
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_SUBJECT, subject)
            putExtra(EXTRA_TOPIC, topic)
            putExtra(EXTRA_DUE_TIMESTAMP, dueTimestamp)
            putExtra(EXTRA_ALARM_TYPE, alarmType)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            Log.d("HomeworkNotificationHelper", "Scheduled $alarmType alarm for $subject at $triggerAtMillis")
        } catch (e: Exception) {
            Log.e("HomeworkNotificationHelper", "Failed to set exact alarm: ${e.message}", e)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            } catch (_: Exception) {}
        }
    }

    fun cancelHomeworkAlarms(context: Context, reminderId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        listOf(1, 2, 3).forEach { offset ->
            val requestCode = (reminderId * 10 + offset).toInt()
            val intent = Intent(context, HomeworkAlarmReceiver::class.java).apply {
                action = ACTION_HOMEWORK_ALARM
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    fun sendHomeworkNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int
    ) {
        createChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val appIconBitmap = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_wear_app_icon)
                ?: BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
                ?: BitmapFactory.decodeResource(context.resources, R.drawable.app_logo)
        } catch (e: Exception) {
            null
        }

        val wearOpenIntent = PendingIntent.getActivity(
            context,
            notificationId + 5000,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val wearAction = NotificationCompat.Action.Builder(
            R.mipmap.ic_launcher,
            "Open Study Well",
            wearOpenIntent
        ).build()

        val wearableExtender = NotificationCompat.WearableExtender()
            .addAction(wearAction)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setSubText("Study Well")
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)

        if (appIconBitmap != null) {
            builder.setLargeIcon(appIconBitmap)
        }

        builder.extend(wearableExtender)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: Exception) {
            Log.e("HomeworkNotificationHelper", "Error displaying notification: ${e.message}")
        }
    }
}

