package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "time_slots",
    foreignKeys = [
        ForeignKey(
            entity = Course::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["courseId"])]
)
data class TimeSlot(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val courseId: Long,
    val dayOfWeek: Int, // 1 = Monday, 2 = Tuesday, ... 7 = Sunday
    val startTime: String, // HH:mm format, e.g. "09:00"
    val endTime: String,   // HH:mm format, e.g. "10:30"
    val roomOverride: String = "",
    val sessionType: String = "CLASS" // CLASS, POSTING, LAB
)
