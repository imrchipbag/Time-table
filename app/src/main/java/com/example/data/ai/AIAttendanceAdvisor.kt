package com.example.data.ai

import com.example.data.model.CourseAttendanceStats
import com.example.data.model.OverallAttendanceStats
import com.example.data.model.ScheduleItem
import com.example.data.model.TimeSlot
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

data class AIMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

object AIAttendanceAdvisor {

    fun generateAdvice(
        userQuery: String,
        todaySchedule: List<ScheduleItem>,
        allSlots: List<TimeSlot>,
        stats: OverallAttendanceStats
    ): String {
        val query = userQuery.lowercase(Locale.ROOT)
        val today = LocalDate.now()
        val todayName = today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())

        val coursesStats = stats.coursesStats
        val safeCourses = coursesStats.filter { it.isSafe && it.bunkMargin > 0 }
        val dangerCourses = coursesStats.filter { !it.isSafe || it.bunkMargin <= 0 }

        // Analysis 1: "When can I skip classes" / "can I skip classes"
        if (query.contains("when") || query.contains("skip") || query.contains("bunk") || query.contains("miss")) {
            val sb = StringBuilder()

            if (coursesStats.isEmpty()) {
                return "You haven't set up your timetable yet! Upload or add your classes in the Timetable tab, and I'll calculate your exact safe skip windows."
            }

            if (safeCourses.isNotEmpty()) {
                sb.append("Here is when you can safely skip classes:\n\n")
                safeCourses.forEach { c ->
                    val courseSlots = allSlots.filter { it.courseId == c.course.id }
                    val days = courseSlots.map { slotDayName(it.dayOfWeek) }.distinct().joinToString(", ")
                    sb.append("• ${c.course.name} (${c.percentage.toInt()}% attendance):\n")
                    sb.append("  You have a buffer of ${c.bunkMargin} class${if (c.bunkMargin > 1) "es" else ""} you can skip. Scheduled on: $days.\n\n")
                }
            } else {
                sb.append("⚠️ Currently, you have NO safe skip buffer in any subject!\n\n")
            }

            if (dangerCourses.isNotEmpty()) {
                sb.append("🚫 DO NOT SKIP these courses (Borderline or Danger):\n")
                dangerCourses.forEach { c ->
                    if (c.isSafe) {
                        sb.append("• ${c.course.name} (${c.percentage.toInt()}%): Exactly on the ${c.course.targetPercent}% border! Skipping even 1 class will put you below the target.\n")
                    } else {
                        sb.append("• ${c.course.name} (${c.percentage.toInt()}%): You must attend the next ${c.bunkMargin} class${if (c.bunkMargin > 1) "es" else ""} to reach ${c.course.targetPercent}%.\n")
                    }
                }
            }

            // Today's classes check
            if (todaySchedule.isNotEmpty()) {
                sb.append("\n📅 Today ($todayName) Check:\n")
                todaySchedule.forEach { item ->
                    val cStat = coursesStats.firstOrNull { it.course.id == item.course.id }
                    val canSkip = cStat != null && cStat.isSafe && cStat.bunkMargin > 0
                    if (canSkip) {
                        sb.append("• ${item.course.name} at ${item.slot.startTime}: ✅ Can safely skip (Buffer: ${cStat?.bunkMargin} classes)\n")
                    } else {
                        sb.append("• ${item.course.name} at ${item.slot.startTime}: ❌ MUST ATTEND (Buffer: ${cStat?.bunkMargin ?: 0})\n")
                    }
                }
            } else {
                sb.append("\n🏖️ You have no classes scheduled for today ($todayName). Perfect rest day!")
            }

            return sb.toString()
        }

        // Analysis 2: "Today"
        if (query.contains("today")) {
            if (todaySchedule.isEmpty()) {
                return "You have no classes today ($todayName)! You're totally free to relax without affecting your attendance."
            }
            val sb = StringBuilder("Today ($todayName), you have ${todaySchedule.size} class(es):\n\n")
            todaySchedule.forEach { item ->
                val cStat = coursesStats.firstOrNull { it.course.id == item.course.id }
                val canSkip = cStat != null && cStat.isSafe && cStat.bunkMargin > 0
                sb.append("• ${item.course.name} (${item.slot.startTime} - ${item.slot.endTime})\n")
                if (canSkip) {
                    sb.append("  Status: Safe to skip if needed (Current: ${cStat?.percentage?.toInt()}%, Margin: ${cStat?.bunkMargin})\n")
                } else {
                    sb.append("  Status: HIGH PRIORITY - Please attend to stay above ${item.course.targetPercent}%\n")
                }
            }
            return sb.toString()
        }

        // Analysis 3: Default overall smart summary
        val sb = StringBuilder()
        sb.append("📊 Current Attendance Summary:\n")
        sb.append("• Overall: ${stats.overallPercentage.toInt()}% (${stats.totalAttended} attended, ${stats.totalMissed} missed)\n")
        sb.append("• Safe subjects with skip buffer: ${safeCourses.size} of ${coursesStats.size}\n\n")

        if (safeCourses.isNotEmpty()) {
            sb.append("Top courses you can skip:\n")
            safeCourses.sortedByDescending { it.bunkMargin }.take(3).forEach {
                sb.append("• ${it.course.name}: ${it.bunkMargin} skip(s) available\n")
            }
        } else {
            sb.append("Keep attending your upcoming classes to build your skip safety buffer!")
        }

        return sb.toString()
    }

    private fun slotDayName(day: Int): String {
        return when (day) {
            1 -> "Monday"
            2 -> "Tuesday"
            3 -> "Wednesday"
            4 -> "Thursday"
            5 -> "Friday"
            6 -> "Saturday"
            else -> "Sunday"
        }
    }
}
