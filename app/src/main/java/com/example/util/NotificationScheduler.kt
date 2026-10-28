package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.Course
import com.example.data.model.TimeSlot
import com.example.receiver.ClassReminderReceiver
import java.util.Calendar

object NotificationScheduler {

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                ClassReminderReceiver.CHANNEL_ID,
                "Class Check-in Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminds you to check in for scheduled classes"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun triggerTestNotification(context: Context) {
        ensureChannel(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ClassReminderReceiver.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Tally: Check-in Reminder")
            .setContentText("Class reminder active! You'll be notified at class start times.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(9999, notification)
    }

    fun scheduleRemindersForSlots(
        context: Context,
        slots: List<TimeSlot>,
        courses: List<Course>
    ) {
        ensureChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val courseMap = courses.associateBy { it.id }

        slots.forEach { slot ->
            val course = courseMap[slot.courseId] ?: return@forEach
            val (hour, minute) = parseHourMinute(slot.startTime)

            val intent = Intent(context, ClassReminderReceiver::class.java).apply {
                putExtra(ClassReminderReceiver.EXTRA_COURSE_NAME, course.name)
                putExtra(ClassReminderReceiver.EXTRA_COURSE_CODE, course.code)
                putExtra(ClassReminderReceiver.EXTRA_NOTIFICATION_ID, slot.id.toInt())
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                slot.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Convert dayOfWeek (1=Mon..7=Sun) to Calendar DayOfWeek (1=Sun, 2=Mon... 7=Sat)
            val calDay = when (slot.dayOfWeek) {
                1 -> Calendar.MONDAY
                2 -> Calendar.TUESDAY
                3 -> Calendar.WEDNESDAY
                4 -> Calendar.THURSDAY
                5 -> Calendar.FRIDAY
                6 -> Calendar.SATURDAY
                else -> Calendar.SUNDAY
            }

            val cal = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, calDay)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(Calendar.getInstance())) {
                    add(Calendar.WEEK_OF_YEAR, 1)
                }
            }

            try {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    cal.timeInMillis,
                    AlarmManager.INTERVAL_DAY * 7,
                    pendingIntent
                )
            } catch (_: SecurityException) {
                // In case exact alarm permission is restricted
            }
        }
    }

    private fun parseHourMinute(timeStr: String): Pair<Int, Int> {
        val parts = timeStr.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return Pair(h, m)
    }
}
