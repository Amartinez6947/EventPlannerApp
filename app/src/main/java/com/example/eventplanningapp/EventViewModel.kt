
package com.example.eventplanningapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EventViewModel(application: Application) :
    AndroidViewModel(application) {

    private val appContext = application.applicationContext

    private val database =
        EventDatabase.getDatabase(application)

    private val repository =
        EventRepository(database.eventDao())

    val events = repository.allEvents.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccess() {
        _successMessage.value = null
    }

    fun addEvent(
        name: String,
        date: String,
        time: String,
        location: String,
        description: String,
        reminderMinutes: Int = 0
    ) {
        viewModelScope.launch {
            try {
                val event = EventEntity(
                    name = name.trim(),
                    date = date.trim(),
                    time = time.trim(),
                    location = location.trim(),
                    description = description.trim(),
                    reminderMinutes = reminderMinutes
                )

                val newId = repository.insertEvent(event)
                val savedEvent = event.copy(id = newId.toInt())

                EventReminderScheduler.createNotificationChannel(appContext)
                EventReminderScheduler.schedule(appContext, savedEvent)

                _errorMessage.value = null
                _successMessage.value = "Event saved successfully."
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _errorMessage.value =
                    "Unable to save the event. Please try again."
                _successMessage.value = null
            }
        }
    }

    fun updateEvent(event: EventEntity) {
        viewModelScope.launch {
            try {
                repository.updateEvent(event)

                EventReminderScheduler.createNotificationChannel(appContext)
                EventReminderScheduler.schedule(appContext, event)

                _errorMessage.value = null
                _successMessage.value = "Event updated successfully."
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _errorMessage.value =
                    "Unable to update the event. Please try again."
                _successMessage.value = null
            }
        }
    }

    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch {
            try {
                EventReminderScheduler.cancel(appContext, event.id)
                repository.deleteEvent(event)

                _errorMessage.value = null
                _successMessage.value = "Event deleted successfully."
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _errorMessage.value =
                    "Unable to delete the event. Please try again."
                _successMessage.value = null
            }
        }
    }
}
