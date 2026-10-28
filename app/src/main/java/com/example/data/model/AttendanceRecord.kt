package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = Course::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["courseId"]),
        Index(value = ["date", "courseId", "timeSlotId"], unique = true)
    ]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val courseId: Long,
    val timeSlotId: Long = 0,
    val date: String, // YYYY-MM-DD format
    val status: AttendanceStatus,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
