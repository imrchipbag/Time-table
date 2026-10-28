package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttendanceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceRecordDao {
    @Query("SELECT * FROM attendance_records ORDER BY date DESC, timestamp DESC")
    fun getAllRecords(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE date = :date")
    fun getRecordsForDate(date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE date = :date AND timeSlotId = :timeSlotId LIMIT 1")
    suspend fun getRecordForSlot(date: String, timeSlotId: Long): AttendanceRecord?

    @Query("SELECT * FROM attendance_records WHERE courseId = :courseId")
    fun getRecordsForCourse(courseId: Long): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: AttendanceRecord): Long

    @Update
    suspend fun updateRecord(record: AttendanceRecord)

    @Delete
    suspend fun deleteRecord(record: AttendanceRecord)

    @Query("DELETE FROM attendance_records WHERE date = :date AND timeSlotId = :timeSlotId")
    suspend fun deleteRecordForSlot(date: String, timeSlotId: Long)

    @Query("DELETE FROM attendance_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("DELETE FROM attendance_records")
    suspend fun clearAllAttendance()
}
