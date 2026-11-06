
package com.example.eventplanningapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val date: String,
    val time: String,
    val location: String,
    val description: String,
    val reminderMinutes: Int = 0
)
