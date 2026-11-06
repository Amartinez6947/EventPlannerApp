
package com.example.eventplanningapp

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.text.SimpleDateFormat
import java.util.Locale

object EventReminderScheduler {

    const val CHANNEL_ID = "event_reminders"
    const val EXTRA_EVENT_ID = "event_id"
    const val EXTRA_EVENT_NAME = "event_name"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Event reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for your planned events"
            }

            val manager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            manager.createNotificationChannel(channel)
        }
    }

    fun schedule(context: Context, event: EventEntity) {
        cancel(context, event.id)

        if (event.id == 0 || event.reminderMinutes <= 0) return

        val formatter = SimpleDateFormat(
            "MM/dd/yyyy h:mm a",
            Locale.US
        ).apply {
            isLenient = false
        }

        val eventDate = try {
            formatter.parse("${event.date} ${event.time}")
        } catch (_: Exception) {
            null
        } ?: return

        val reminderTime =
            eventDate.time - event.reminderMinutes * 60_000L

        if (reminderTime <= System.currentTimeMillis()) return

        val intent = Intent(context, EventReminderReceiver::class.java).apply {
            putExtra(EXTRA_EVENT_ID, event.id)
            putExtra(EXTRA_EVENT_NAME, event.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminderTime,
            pendingIntent
        )
    }

    fun cancel(context: Context, eventId: Int) {
        if (eventId == 0) return

        val intent = Intent(context, EventReminderReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}

class EventReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getIntExtra(
            EventReminderScheduler.EXTRA_EVENT_ID, 0
        )
        val eventName = intent.getStringExtra(
            EventReminderScheduler.EXTRA_EVENT_NAME
        ) ?: "Your upcoming event"

        if (eventId == 0) return

        EventReminderScheduler.createNotificationChannel(context)

        val openAppIntent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)

        val contentIntent = openAppIntent?.let {
            PendingIntent.getActivity(
                context,
                eventId,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(
            context,
            EventReminderScheduler.CHANNEL_ID
        )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Upcoming event")
            .setContentText(eventName)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$eventName is coming up!")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .apply {
                if (contentIntent != null) {
                    setContentIntent(contentIntent)
                }
            }
            .build()

        if (Build.VERSION.SDK_INT < 33 ||
            context.checkSelfPermission(
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context)
                .notify(eventId, notification)
        }
    }
}
