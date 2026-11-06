
package com.example.eventplanningapp

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventDaoTest {

    private lateinit var database: EventDatabase
    private lateinit var dao: EventDao

    @Before
    fun setUp() {
        val context =
            ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            EventDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = database.eventDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveEvents() = runBlocking {
        val event = EventEntity(
            id = 1,
            name = "Birthday Party",
            date = "11/15/2026",
            time = "2:00 PM",
            location = "Community Center",
            description = "Birthday celebration",
            reminderMinutes = 0
        )

        dao.insertEvent(event)

        val events = dao.getAllEvents().first()

        assertEquals(1, events.size)
        assertEquals("Birthday Party", events[0].name)
        assertEquals("Community Center", events[0].location)
    }

    @Test
    fun updateEventChangesSavedInformation() = runBlocking {
        val originalEvent = EventEntity(
            id = 1,
            name = "Birthday Party",
            date = "11/15/2026",
            time = "2:00 PM",
            location = "Community Center",
            description = "Birthday celebration",
            reminderMinutes = 0
        )

        dao.insertEvent(originalEvent)

        val updatedEvent = originalEvent.copy(
            name = "Family Dinner",
            location = "Home"
        )

        dao.updateEvent(updatedEvent)

        val events = dao.getAllEvents().first()

        assertEquals(1, events.size)
        assertEquals("Family Dinner", events[0].name)
        assertEquals("Home", events[0].location)
    }

    @Test
    fun deleteEventRemovesSavedInformation() = runBlocking {
        val event = EventEntity(
            id = 1,
            name = "Birthday Party",
            date = "11/15/2026",
            time = "2:00 PM",
            location = "Community Center",
            description = "Birthday celebration",
            reminderMinutes = 0
        )

        dao.insertEvent(event)
        dao.deleteEvent(event)

        val events = dao.getAllEvents().first()

        assertEquals(0, events.size)
    }

    @Test
    fun eventsAreRetrievedInDateAndTimeOrder() = runBlocking {
        val laterEvent = EventEntity(
            id = 1,
            name = "Later Event",
            date = "12/20/2026",
            time = "6:00 PM",
            location = "Park",
            description = "Later event",
            reminderMinutes = 0
        )

        val earlierEvent = EventEntity(
            id = 2,
            name = "Earlier Event",
            date = "11/15/2026",
            time = "2:00 PM",
            location = "Hall",
            description = "Earlier event",
            reminderMinutes = 0
        )

        dao.insertEvent(laterEvent)
        dao.insertEvent(earlierEvent)

        val events = dao.getAllEvents().first()

        assertEquals(2, events.size)
        assertEquals("Earlier Event", events[0].name)
        assertEquals("Later Event", events[1].name)
    }

    @Test
    fun eventsAreSortedCorrectlyAcrossDifferentYears() = runBlocking {
        val januaryEvent = EventEntity(
            id = 1,
            name = "January Event",
            date = "01/10/2027",
            time = "9:00 AM",
            location = "Library",
            description = "January event",
            reminderMinutes = 0
        )

        val novemberEvent = EventEntity(
            id = 2,
            name = "November Event",
            date = "11/15/2026",
            time = "9:00 AM",
            location = "Park",
            description = "November event",
            reminderMinutes = 0
        )

        dao.insertEvent(januaryEvent)
        dao.insertEvent(novemberEvent)

        val events = dao.getAllEvents().first()

        assertEquals(2, events.size)
        assertEquals("November Event", events[0].name)
        assertEquals("January Event", events[1].name)
    }

    @Test
    fun eventsOnSameDayAreSortedByAMAndPMTime() = runBlocking {
        val afternoonEvent = EventEntity(
            id = 1,
            name = "Afternoon Event",
            date = "11/15/2026",
            time = "2:00 PM",
            location = "Park",
            description = "Afternoon event",
            reminderMinutes = 0
        )

        val morningEvent = EventEntity(
            id = 2,
            name = "Morning Event",
            date = "11/15/2026",
            time = "9:00 AM",
            location = "Hall",
            description = "Morning event",
            reminderMinutes = 0
        )

        dao.insertEvent(afternoonEvent)
        dao.insertEvent(morningEvent)

        val events = dao.getAllEvents().first()

        assertEquals(2, events.size)
        assertEquals("Morning Event", events[0].name)
        assertEquals("Afternoon Event", events[1].name)
    }

    @Test
    fun eventsAreSortedCorrectlyAroundNoonAndMidnight() = runBlocking {
        val midnightEvent = EventEntity(
            id = 1,
            name = "Midnight Event",
            date = "11/15/2026",
            time = "12:00 AM",
            location = "Home",
            description = "Midnight event",
            reminderMinutes = 0
        )

        val noonEvent = EventEntity(
            id = 2,
            name = "Noon Event",
            date = "11/15/2026",
            time = "12:00 PM",
            location = "Cafe",
            description = "Noon event",
            reminderMinutes = 0
        )

        val afternoonEvent = EventEntity(
            id = 3,
            name = "Afternoon Event",
            date = "11/15/2026",
            time = "1:00 PM",
            location = "Park",
            description = "Afternoon event",
            reminderMinutes = 0
        )

        dao.insertEvent(afternoonEvent)
        dao.insertEvent(noonEvent)
        dao.insertEvent(midnightEvent)

        val events = dao.getAllEvents().first()

        assertEquals(3, events.size)
        assertEquals("Midnight Event", events[0].name)
        assertEquals("Noon Event", events[1].name)
        assertEquals("Afternoon Event", events[2].name)
    }
}
