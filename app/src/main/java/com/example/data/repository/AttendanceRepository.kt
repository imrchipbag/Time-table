package com.example.data.repository

import com.example.data.db.AttendanceRecordDao
import com.example.data.db.CourseDao
import com.example.data.db.TimeSlotDao
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.Course
import com.example.data.model.CourseAttendanceStats
import com.example.data.model.OverallAttendanceStats
import com.example.data.model.ScheduleItem
import com.example.data.model.TimeSlot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.ceil
import kotlin.math.floor

class AttendanceRepository(
    private val courseDao: CourseDao,
    private val timeSlotDao: TimeSlotDao,
    private val attendanceDao: AttendanceRecordDao
) {

    val allCourses: Flow<List<Course>> = courseDao.getAllCourses()
    val allTimeSlots: Flow<List<TimeSlot>> = timeSlotDao.getAllTimeSlots()
    val allRecords: Flow<List<AttendanceRecord>> = attendanceDao.getAllRecords()

    /**
     * Returns scheduled classes for a specific date, merging time slots and any logged attendance.
     */
    fun getScheduleForDate(dateStr: String): Flow<List<ScheduleItem>> {
        val date = try {
            LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (_: Exception) {
            LocalDate.now()
        }
        val dayOfWeek = date.dayOfWeek.value // 1 = Monday .. 7 = Sunday

        return combine(
            courseDao.getAllCourses(),
            timeSlotDao.getTimeSlotsForDay(dayOfWeek),
            attendanceDao.getRecordsForDate(dateStr)
        ) { courses, slots, records ->
            val courseMap = courses.associateBy { it.id }
            val recordMap = records.associateBy { it.timeSlotId }

            slots.mapNotNull { slot ->
                val course = courseMap[slot.courseId] ?: return@mapNotNull null
                val record = recordMap[slot.id]
                ScheduleItem(
                    slot = slot,
                    course = course,
                    record = record
                )
            }.sortedBy { it.slot.startTime }
        }
    }

    /**
     * Computes real-time comprehensive statistics per course and overall.
     */
    val overallStats: Flow<OverallAttendanceStats> = combine(
        courseDao.getAllCourses(),
        attendanceDao.getAllRecords()
    ) { courses, records ->
        if (courses.isEmpty()) {
            return@combine OverallAttendanceStats()
        }

        val recordsByCourse = records.groupBy { it.courseId }

        var totalAttended = 0
        var totalMissed = 0
        var totalCancelled = 0

        val courseStatsList = courses.map { course ->
            val courseRecords = recordsByCourse[course.id] ?: emptyList()
            val attended = courseRecords.count { it.status == AttendanceStatus.ATTENDED }
            val missed = courseRecords.count { it.status == AttendanceStatus.MISSED }
            val cancelled = courseRecords.count { it.status == AttendanceStatus.CANCELLED }
            val total = attended + missed

            totalAttended += attended
            totalMissed += missed
            totalCancelled += cancelled

            val percentage = if (total > 0) {
                (attended.toFloat() / total) * 100f
            } else {
                100f // Default optimistic
            }

            val target = course.targetPercent.coerceIn(1, 99).toFloat() / 100f
            val isSafe = percentage >= (target * 100f)

            val margin = if (total == 0) {
                0
            } else if (isSafe) {
                // Classes you can bunk: floor((attended - target*total) / target)
                val canBunk = floor((attended - target * total) / target).toInt()
                canBunk.coerceAtLeast(0)
            } else {
                // Classes needed: ceil((target*total - attended) / (1 - target))
                val needed = ceil((target * total - attended) / (1f - target)).toInt()
                needed.coerceAtLeast(1)
            }

            CourseAttendanceStats(
                course = course,
                attendedCount = attended,
                missedCount = missed,
                cancelledCount = cancelled,
                totalCount = total,
                percentage = percentage,
                bunkMargin = margin,
                isSafe = isSafe
            )
        }

        val totalAll = totalAttended + totalMissed
        val overallPct = if (totalAll > 0) {
            (totalAttended.toFloat() / totalAll) * 100f
        } else {
            100f
        }

        // Calculate attendance streak by dates
        val streak = calculateStreak(records)

        OverallAttendanceStats(
            totalAttended = totalAttended,
            totalMissed = totalMissed,
            totalCancelled = totalCancelled,
            totalClasses = totalAll,
            overallPercentage = overallPct,
            currentStreak = streak,
            coursesStats = courseStatsList
        )
    }

    private fun calculateStreak(records: List<AttendanceRecord>): Int {
        if (records.isEmpty()) return 0
        val byDate = records.groupBy { it.date }
        var streak = 0
        var checkDate = LocalDate.now()

        // Check if today has records or start from yesterday
        val todayStr = checkDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val todayRecords = byDate[todayStr]

        if (todayRecords != null) {
            val attendedAny = todayRecords.any { it.status == AttendanceStatus.ATTENDED }
            val missedAny = todayRecords.any { it.status == AttendanceStatus.MISSED }
            if (attendedAny && !missedAny) {
                streak++
            }
        }

        // Walk backwards
        for (i in 1..60) {
            val pastDate = checkDate.minusDays(i.toLong())
            val pastStr = pastDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val dayRecords = byDate[pastStr] ?: continue
            val attended = dayRecords.any { it.status == AttendanceStatus.ATTENDED }
            val missed = dayRecords.any { it.status == AttendanceStatus.MISSED }
            if (attended && !missed) {
                streak++
            } else if (missed) {
                break
            }
        }
        return streak
    }

    suspend fun setAttendance(
        courseId: Long,
        timeSlotId: Long,
        date: String,
        status: AttendanceStatus,
        note: String = ""
    ) {
        val existing = attendanceDao.getRecordForSlot(date, timeSlotId)
        val record = existing?.copy(
            status = status,
            note = note,
            timestamp = System.currentTimeMillis()
        ) ?: AttendanceRecord(
            courseId = courseId,
            timeSlotId = timeSlotId,
            date = date,
            status = status,
            note = note,
            timestamp = System.currentTimeMillis()
        )
        attendanceDao.insertOrUpdate(record)
    }

    suspend fun removeAttendance(date: String, timeSlotId: Long) {
        attendanceDao.deleteRecordForSlot(date, timeSlotId)
    }

    suspend fun addCourse(course: Course): Long {
        return courseDao.insertCourse(course)
    }

    suspend fun updateCourse(course: Course) {
        courseDao.updateCourse(course)
    }

    suspend fun deleteCourse(courseId: Long) {
        timeSlotDao.deleteSlotsForCourse(courseId)
        courseDao.deleteCourseById(courseId)
    }

    suspend fun addTimeSlot(slot: TimeSlot): Long {
        return timeSlotDao.insertTimeSlot(slot)
    }

    suspend fun deleteTimeSlot(slotId: Long) {
        timeSlotDao.deleteTimeSlotById(slotId)
    }

    suspend fun importParsedSlots(slots: List<ParsedCourseSlot>, replaceExisting: Boolean) {
        if (replaceExisting) {
            attendanceDao.clearAllAttendance()
            timeSlotDao.clearAllTimeSlots()
            courseDao.clearAllCourses()
        }

        // Group by course name
        val groupedByName = slots.groupBy { it.courseName.trim() }
        for ((courseName, courseSlots) in groupedByName) {
            val first = courseSlots.first()
            val course = Course(
                name = courseName,
                code = first.courseCode,
                professor = first.professor,
                room = first.room,
                colorHex = first.colorHex,
                targetPercent = 75
            )
            val courseId = courseDao.insertCourse(course)

            val timeSlots = courseSlots.map { slotInfo ->
                TimeSlot(
                    courseId = courseId,
                    dayOfWeek = slotInfo.dayOfWeek,
                    startTime = slotInfo.startTime,
                    endTime = slotInfo.endTime,
                    roomOverride = slotInfo.room
                )
            }
            timeSlotDao.insertTimeSlots(timeSlots)
        }
    }

    suspend fun seedInitialDataIfEmpty() {
        // Only seed if no courses exist yet
        val existing = courseDao.getCourseById(1L)
        if (existing == null) {
            val sample = TimetableParser.getSamplePresets()["Computer Science & AI"] ?: return
            importParsedSlots(sample, replaceExisting = false)
        }
    }
}
