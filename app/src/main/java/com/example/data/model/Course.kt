package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val code: String = "",
    val professor: String = "",
    val room: String = "",
    val colorHex: String = "#FFFFFF",
    val targetPercent: Int = 75,
    val sessionType: String = "CLASS" // CLASS, POSTING, LAB
)
