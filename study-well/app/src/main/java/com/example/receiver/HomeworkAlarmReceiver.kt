package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.util.Log
import com.example.data.db.AppDatabase
import com.example.utils.HomeworkNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class HomeworkAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d("HomeworkAlarmReceiver", "Received action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val activeReminders = db.homeworkReminderDao().getActiveRemindersList()
                    val now = System.currentTimeMillis()
                    for (reminder in activeReminders) {
                        if (reminder.dueDateTimestamp > now) {
                            HomeworkNotificationHelper.scheduleHomeworkAlarms(
                                context = context,
                                reminderId = reminder.id,
                                subject = reminder.subjectName,
                                topic = reminder.topicName,
                                dueTimestamp = reminder.dueDateTimestamp
                            )
                        }
                    }
                    Log.d("HomeworkAlarmReceiver", "Rescheduled ${activeReminders.size} reminders after boot")
                } catch (e: Exception) {
                    Log.e("HomeworkAlarmReceiver", "Error restoring reminders on boot: ${e.message}")
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }

        if (action == HomeworkNotificationHelper.ACTION_HOMEWORK_ALARM) {
            val reminderId = intent.getLongExtra(HomeworkNotificationHelper.EXTRA_REMINDER_ID, 0L)
            val subject = intent.getStringExtra(HomeworkNotificationHelper.EXTRA_SUBJECT) ?: "Homework"
            val topic = intent.getStringExtra(HomeworkNotificationHelper.EXTRA_TOPIC) ?: ""
            val alarmType = intent.getStringExtra(HomeworkNotificationHelper.EXTRA_ALARM_TYPE) ?: HomeworkNotificationHelper.ALARM_TYPE_DUE
            val dueTimestamp = intent.getLongExtra(HomeworkNotificationHelper.EXTRA_DUE_TIMESTAMP, 0L)

            val topicSuffix = if (topic.isNotBlank()) " ($topic)" else ""
            val dueTimeStr = if (dueTimestamp > 0L) {
                DateFormat.format("hh:mm a", Date(dueTimestamp)).toString()
            } else ""

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    if (reminderId > 0L) {
                        val reminder = db.homeworkReminderDao().getReminderById(reminderId)
                        if (reminder == null || reminder.isFinished) {
                            Log.d("HomeworkAlarmReceiver", "Reminder $reminderId finished or removed, skipping alert.")
                            return@launch
                        }
                    }

                    val (title, message, notificationId) = when (alarmType) {
                        HomeworkNotificationHelper.ALARM_TYPE_24H -> {
                            val timePart = if (dueTimeStr.isNotBlank()) "tomorrow at $dueTimeStr" else "tomorrow"
                            Triple(
                                "📅 24h Reminder: $subject$topicSuffix",
                                "Your homework assignment for $subject is due in 24 hours ($timePart). Get started now to finish on time!",
                                (reminderId * 10 + 1).toInt()
                            )
                        }
                        HomeworkNotificationHelper.ALARM_TYPE_2H -> {
                            val timePart = if (dueTimeStr.isNotBlank()) "today at $dueTimeStr" else "soon"
                            Triple(
                                "⏳ Urgent 2h Alert: $subject$topicSuffix",
                                "Only 2 hours left before your $subject homework is due ($timePart)! Complete and submit it now.",
                                (reminderId * 10 + 2).toInt()
                            )
                        }
                        else -> {
                            val timePart = if (dueTimeStr.isNotBlank()) " ($dueTimeStr)" else ""
                            Triple(
                                "🚨 Homework Due Now: $subject$topicSuffix",
                                "The deadline for your $subject homework$timePart has arrived. Don't forget to submit your work!",
                                (reminderId * 10 + 3).toInt()
                            )
                        }
                    }

                    HomeworkNotificationHelper.sendHomeworkNotification(
                        context = context,
                        title = title,
                        message = message,
                        notificationId = notificationId
                    )

                    // Sync flag with database so foreground checker won't re-trigger
                    if (reminderId > 0L) {
                        if (alarmType == HomeworkNotificationHelper.ALARM_TYPE_24H) {
                            db.homeworkReminderDao().markNotified24h(reminderId)
                        } else if (alarmType == HomeworkNotificationHelper.ALARM_TYPE_2H) {
                            db.homeworkReminderDao().markNotified2h(reminderId)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("HomeworkAlarmReceiver", "Error processing alarm: ${e.message}")
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
