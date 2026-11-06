
package com.example.eventplanningapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Query("""
        SELECT * FROM events
        ORDER BY
            CAST(substr(date, 7, 4) AS INTEGER) ASC,
            CAST(substr(date, 1, 2) AS INTEGER) ASC,
            CAST(substr(date, 4, 2) AS INTEGER) ASC,
            CASE
                WHEN upper(time) LIKE '%AM'
                    AND CAST(substr(time, 1, instr(time, ':') - 1) AS INTEGER) = 12
                    THEN 0
                WHEN upper(time) LIKE '%AM'
                    THEN CAST(substr(time, 1, instr(time, ':') - 1) AS INTEGER)
                WHEN upper(time) LIKE '%PM'
                    AND CAST(substr(time, 1, instr(time, ':') - 1) AS INTEGER) = 12
                    THEN 12
                ELSE CAST(substr(time, 1, instr(time, ':') - 1) AS INTEGER) + 12
            END ASC,
            CAST(substr(time, instr(time, ':') + 1, 2) AS INTEGER) ASC
    """)
    fun getAllEvents(): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)
}
