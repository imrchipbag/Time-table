package com.example.data.model

data class ScheduleItem(
    val slot: TimeSlot,
    val course: Course,
    val record: AttendanceRecord? = null
)

data class CourseAttendanceStats(
    val course: Course,
    val attendedCount: Int,
    val missedCount: Int,
    val cancelledCount: Int,
    val totalCount: Int, // attended + missed (cancelled doesn't count against student)
    val percentage: Float, // (attended / totalCount) * 100 or 100% if total == 0
    val bunkMargin: Int, // How many classes student can afford to miss or needs to attend
    val isSafe: Boolean // true if >= targetPercent
)

data class OverallAttendanceStats(
    val totalAttended: Int = 0,
    val totalMissed: Int = 0,
    val totalCancelled: Int = 0,
    val totalClasses: Int = 0,
    val overallPercentage: Float = 100f,
    val currentStreak: Int = 0,
    val coursesStats: List<CourseAttendanceStats> = emptyList()
)
