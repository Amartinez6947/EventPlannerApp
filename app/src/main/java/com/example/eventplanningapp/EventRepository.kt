
package com.example.eventplanningapp

import kotlinx.coroutines.flow.Flow

class EventRepository(
    private val eventDao: EventDao
) {
    val allEvents: Flow<List<EventEntity>> = eventDao.getAllEvents()

    suspend fun insertEvent(event: EventEntity): Long {
        return eventDao.insertEvent(event)
    }

    suspend fun updateEvent(event: EventEntity) {
        eventDao.updateEvent(event)
    }

    suspend fun deleteEvent(event: EventEntity) {
        eventDao.deleteEvent(event)
    }
}
