package com.example.data.repository

import com.example.data.model.Course
import com.example.data.model.TimeSlot
import java.util.Locale

data class ParsedCourseSlot(
    val courseName: String,
    val courseCode: String = "",
    val professor: String = "",
    val room: String = "",
    val dayOfWeek: Int, // 1..7
    val startTime: String,
    val endTime: String,
    val colorHex: String
)

object TimetableParser {

    val GROOVY_COLORS = listOf(
        "#FF2E93", // Neon Fuchsia
        "#00F5D4", // Electric Turquoise
        "#9D4EDD", // Psychedelic Purple
        "#FF7A00", // Groovy Orange
        "#FFD166", // Cyber Yellow
        "#4CC9F0", // Electric Blue
        "#F72585", // Hot Pink
        "#7209B7"  // Deep Violet
    )

    fun parseTimetableText(text: String): List<ParsedCourseSlot> {
        val results = mutableListOf<ParsedCourseSlot>()
        val lines = text.lines()
        var colorIndex = 0

        val dayKeywords = mapOf(
            "mon" to 1, "monday" to 1,
            "tue" to 2, "tues" to 2, "tuesday" to 2,
            "wed" to 3, "wednesday" to 3,
            "thu" to 4, "thur" to 4, "thurs" to 4, "thursday" to 4,
            "fri" to 5, "friday" to 5,
            "sat" to 6, "saturday" to 6,
            "sun" to 7, "sunday" to 7
        )

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) continue

            // Try CSV split first: Day, StartTime, EndTime, CourseName, [Code], [Room], [Prof]
            val commaParts = line.split(",").map { it.trim() }
            if (commaParts.size >= 4) {
                val dayStr = commaParts[0].lowercase(Locale.ROOT)
                val day = dayKeywords[dayStr] ?: extractDayOfWeek(dayStr)
                val start = formatTime(commaParts[1])
                val end = formatTime(commaParts[2])
                val name = commaParts[3]
                val code = if (commaParts.size > 4) commaParts[4] else ""
                val room = if (commaParts.size > 5) commaParts[5] else ""
                val prof = if (commaParts.size > 6) commaParts[6] else ""
                val color = GROOVY_COLORS[colorIndex % GROOVY_COLORS.size]
                colorIndex++

                results.add(
                    ParsedCourseSlot(
                        courseName = name,
                        courseCode = code,
                        professor = prof,
                        room = room,
                        dayOfWeek = day,
                        startTime = start,
                        endTime = end,
                        colorHex = color
                    )
                )
                continue
            }

            // Fallback: heuristic pattern matching like "Mon 09:00 - 10:30 Data Structures"
            val tokens = line.split(Regex("\\s+"))
            if (tokens.size >= 3) {
                val firstToken = tokens[0].lowercase(Locale.ROOT).replace(Regex("[^a-z]"), "")
                val day = dayKeywords[firstToken] ?: 1

                // Look for time pattern
                val timeRegex = Regex("(\\d{1,2}[:.]?\\d{0,2})\\s*[-to–]+\\s*(\\d{1,2}[:.]?\\d{0,2})")
                val match = timeRegex.find(line)
                if (match != null) {
                    val rawStart = match.groupValues[1]
                    val rawEnd = match.groupValues[2]
                    val start = formatTime(rawStart)
                    val end = formatTime(rawEnd)

                    val remaining = line.replace(match.value, "").replace(tokens[0], "").trim()
                    val courseName = if (remaining.isNotBlank()) remaining else "Course Slot"
                    val color = GROOVY_COLORS[colorIndex % GROOVY_COLORS.size]
                    colorIndex++

                    results.add(
                        ParsedCourseSlot(
                            courseName = courseName,
                            dayOfWeek = day,
                            startTime = start,
                            endTime = end,
                            colorHex = color
                        )
                    )
                }
            }
        }
        return results
    }

    private fun extractDayOfWeek(input: String): Int {
        val lower = input.lowercase(Locale.ROOT)
        return when {
            lower.contains("mon") -> 1
            lower.contains("tue") -> 2
            lower.contains("wed") -> 3
            lower.contains("thu") -> 4
            lower.contains("fri") -> 5
            lower.contains("sat") -> 6
            lower.contains("sun") -> 7
            else -> 1
        }
    }

    private fun formatTime(timeStr: String): String {
        val clean = timeStr.trim().replace(".", ":")
        if (clean.contains(":")) {
            val parts = clean.split(":")
            val h = parts[0].padStart(2, '0')
            val m = if (parts.size > 1 && parts[1].length >= 2) parts[1].substring(0, 2) else "00"
            return "$h:$m"
        }
        val num = clean.filter { it.isDigit() }
        return when (num.length) {
            1, 2 -> "${num.padStart(2, '0')}:00"
            3 -> "0${num[0]}:${num.substring(1)}"
            4 -> "${num.substring(0, 2)}:${num.substring(2)}"
            else -> "09:00"
        }
    }

    fun getSamplePresets(): Map<String, List<ParsedCourseSlot>> {
        return mapOf(
            "Computer Science & AI" to listOf(
                ParsedCourseSlot("Algorithms & Data Structures", "CS301", "Dr. Vance", "Hall 101", 1, "09:00", "10:30", "#FF2E93"),
                ParsedCourseSlot("Artificial Intelligence", "CS402", "Prof. Hendrix", "Lab B", 1, "11:00", "12:30", "#00F5D4"),
                ParsedCourseSlot("Database Systems", "CS305", "Dr. Clarke", "Hall 204", 2, "10:00", "11:30", "#9D4EDD"),
                ParsedCourseSlot("Web Architecture & Mobile", "CS310", "Prof. Groovy", "Tech Lab", 2, "13:30", "15:00", "#FF7A00"),
                ParsedCourseSlot("Algorithms & Data Structures", "CS301", "Dr. Vance", "Hall 101", 3, "09:00", "10:30", "#FF2E93"),
                ParsedCourseSlot("Computer Networks", "CS308", "Dr. Turing", "Hall 105", 3, "11:00", "12:30", "#FFD166"),
                ParsedCourseSlot("Database Systems", "CS305", "Dr. Clarke", "Hall 204", 4, "10:00", "11:30", "#9D4EDD"),
                ParsedCourseSlot("Cloud Computing", "CS415", "Prof. Nova", "Lab C", 4, "14:00", "15:30", "#4CC9F0"),
                ParsedCourseSlot("Artificial Intelligence", "CS402", "Prof. Hendrix", "Lab B", 5, "09:30", "11:00", "#00F5D4"),
                ParsedCourseSlot("Project Seminar & Groove", "CS490", "Panel", "Auditorium", 5, "11:30", "13:00", "#F72585")
            ),
            "Business & Design Groove" to listOf(
                ParsedCourseSlot("Brand Strategy", "MKT201", "Dr. Madison", "Studio A", 1, "09:30", "11:00", "#FF2E93"),
                ParsedCourseSlot("Financial Modeling", "FIN302", "Prof. Sterling", "Hall 3B", 1, "11:30", "13:00", "#00F5D4"),
                ParsedCourseSlot("UI/UX & Retro Design", "DES105", "Prof. Ziggy", "Design Hub", 2, "10:00", "12:00", "#9D4EDD"),
                ParsedCourseSlot("Corporate Law", "LAW210", "Dr. Cooper", "Hall 1A", 3, "09:00", "10:30", "#FF7A00"),
                ParsedCourseSlot("Brand Strategy", "MKT201", "Dr. Madison", "Studio A", 4, "11:00", "12:30", "#FF2E93"),
                ParsedCourseSlot("Digital Media Production", "MED304", "Prof. Apollo", "Sound Lab", 5, "13:00", "15:00", "#4CC9F0")
            ),
            "Science & Engineering" to listOf(
                ParsedCourseSlot("Quantum Physics", "PHY301", "Dr. Feynman", "Lecture Hall 4", 1, "08:30", "10:00", "#FF2E93"),
                ParsedCourseSlot("Thermodynamics", "ENG204", "Prof. Watt", "Hall 2", 1, "10:30", "12:00", "#00F5D4"),
                ParsedCourseSlot("Organic Chemistry", "CHM305", "Dr. Curie", "Chemistry Lab", 2, "09:00", "11:00", "#FFD166"),
                ParsedCourseSlot("Linear Algebra", "MTH202", "Prof. Euler", "Hall 3", 3, "11:00", "12:30", "#9D4EDD"),
                ParsedCourseSlot("Quantum Physics", "PHY301", "Dr. Feynman", "Lecture Hall 4", 4, "08:30", "10:00", "#FF2E93"),
                ParsedCourseSlot("Engineering Workshop", "ENG108", "Prof. Otto", "Maker Space", 5, "14:00", "16:30", "#FF7A00")
            )
        )
    }
}
