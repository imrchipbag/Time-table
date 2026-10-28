package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TimeSlot
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeSlotDao {
    @Query("SELECT * FROM time_slots ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllTimeSlots(): Flow<List<TimeSlot>>

    @Query("SELECT * FROM time_slots WHERE dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    fun getTimeSlotsForDay(dayOfWeek: Int): Flow<List<TimeSlot>>

    @Query("SELECT * FROM time_slots WHERE courseId = :courseId ORDER BY dayOfWeek ASC, startTime ASC")
    fun getTimeSlotsForCourse(courseId: Long): Flow<List<TimeSlot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeSlot(slot: TimeSlot): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeSlots(slots: List<TimeSlot>): List<Long>

    @Update
    suspend fun updateTimeSlot(slot: TimeSlot)

    @Delete
    suspend fun deleteTimeSlot(slot: TimeSlot)

    @Query("DELETE FROM time_slots WHERE id = :id")
    suspend fun deleteTimeSlotById(id: Long)

    @Query("DELETE FROM time_slots WHERE courseId = :courseId")
    suspend fun deleteSlotsForCourse(courseId: Long)

    @Query("DELETE FROM time_slots")
    suspend fun clearAllTimeSlots()
}
