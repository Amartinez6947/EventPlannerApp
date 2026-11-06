
package com.example.eventplanningapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EventReminderBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (
            intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }

        val pendingResult = goAsync()
        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = EventDatabase.getDatabase(appContext)
                val events = database.eventDao().getAllEvents().first()

                EventReminderScheduler.createNotificationChannel(appContext)

                events.forEach { event ->
                    EventReminderScheduler.schedule(appContext, event)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
