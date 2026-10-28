package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.Course
import com.example.data.model.TimeSlot

@Database(
    entities = [Course::class, TimeSlot::class, AttendanceRecord::class],
    version = 3,
    exportSchema = false
)
abstract class GrooveTrackDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun timeSlotDao(): TimeSlotDao
    abstract fun attendanceRecordDao(): AttendanceRecordDao

    companion object {
        @Volatile
        private var INSTANCE: GrooveTrackDatabase? = null

        fun getDatabase(context: Context): GrooveTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GrooveTrackDatabase::class.java,
                    "groove_track_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
