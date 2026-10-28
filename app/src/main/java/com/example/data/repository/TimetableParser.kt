package com.example.data.repository

import java.util.Locale

data class ParsedCourseSlot(
    val courseName: String,
    val courseCode: String = "",
    val professor: String = "",
    val room: String = "",
    val dayOfWeek: Int, // 1..7 (Monday=1, Sunday=7)
    val startTime: String,
    val endTime: String,
    val colorHex: String = "#FFFFFF",
    val sessionType: String = "CLASS" // CLASS, POSTING, LAB
)

object TimetableParser {

    val MONO_SHADES = listOf(
        "#FFFFFF",
        "#E0E0E0",
        "#BDBDBD",
        "#9E9E9E",
        "#757575",
        "#EEEEEE"
    )

    private val dayKeywords = mapOf(
        "mon" to 1, "monday" to 1,
        "tue" to 2, "tues" to 2, "tuesday" to 2,
        "wed" to 3, "wednesday" to 3,
        "thu" to 4, "thur" to 4, "thurs" to 4, "thursday" to 4,
        "fri" to 5, "friday" to 5,
        "sat" to 6, "saturday" to 6,
        "sun" to 7, "sunday" to 7
    )

    fun parseTimetableText(text: String): List<ParsedCourseSlot> {
        val results = mutableListOf<ParsedCourseSlot>()
        val lines = text.lines()
        var shadeIndex = 0

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) continue

            val shade = MONO_SHADES[shadeIndex % MONO_SHADES.size]
            shadeIndex++

            // Detect session type (Class, Posting, Lab)
            val sessionType = detectSessionType(line)

            // Format A: CSV split (Day, Start, End, Course, [Code], [Type])
            val parts = line.split(Regex("[,;]")).map { it.trim() }

            if (parts.size >= 4) {
                val dayCandidate1 = dayKeywords[parts[0].lowercase(Locale.ROOT)]
                val dayCandidate2 = dayKeywords[parts[1].lowercase(Locale.ROOT)]

                if (dayCandidate1 != null) {
                    val day = dayCandidate1
                    val start = formatTime(parts[1])
                    val end = formatTime(parts[2])
                    val name = parts[3]
                    val code = if (parts.size > 4) parts[4] else ""
                    val explicitType = if (parts.size > 5) parts[5].uppercase() else sessionType
                    val finalType = if (explicitType in listOf("CLASS", "POSTING", "LAB")) explicitType else sessionType
                    results.add(ParsedCourseSlot(name, code, "", "", day, start, end, shade, finalType))
                    continue
                } else if (dayCandidate2 != null) {
                    val name = parts[0]
                    val day = dayCandidate2
                    val start = formatTime(parts[2])
                    val end = formatTime(parts[3])
                    val explicitType = if (parts.size > 4) parts[4].uppercase() else sessionType
                    val finalType = if (explicitType in listOf("CLASS", "POSTING", "LAB")) explicitType else sessionType
                    results.add(ParsedCourseSlot(name, "", "", "", day, start, end, shade, finalType))
                    continue
                }
            }

            // Format B: Heuristic string search, e.g. "Monday 09:00 - 10:30 Surgery Posting"
            val timeRegex = Regex("(\\d{1,2}(?::\\d{2})?\\s*(?:am|pm)?)\\s*(?:-|to|–)\\s*(\\d{1,2}(?::\\d{2})?\\s*(?:am|pm)?)", RegexOption.IGNORE_CASE)
            val timeMatch = timeRegex.find(line)

            if (timeMatch != null) {
                val rawStart = timeMatch.groupValues[1]
                val rawEnd = timeMatch.groupValues[2]
                val start = formatTime(rawStart)
                val end = formatTime(rawEnd)

                val daysFound = mutableListOf<Int>()
                val lineLower = line.lowercase(Locale.ROOT)
                dayKeywords.forEach { (keyword, dayNum) ->
                    val wordRegex = Regex("\\b$keyword\\b", RegexOption.IGNORE_CASE)
                    if (wordRegex.containsMatchIn(lineLower) && !daysFound.contains(dayNum)) {
                        daysFound.add(dayNum)
                    }
                }
                if (daysFound.isEmpty()) {
                    daysFound.add(1)
                }

                var remaining = line.replace(timeMatch.value, "")
                dayKeywords.keys.forEach { kw ->
                    remaining = remaining.replace(Regex("\\b$kw\\b", RegexOption.IGNORE_CASE), "")
                }
                remaining = remaining.replace(Regex("[,:;-]"), " ").trim()
                val courseName = if (remaining.isNotBlank()) remaining.split(Regex("\\s+")).joinToString(" ") else "Class"

                daysFound.forEach { day ->
                    results.add(ParsedCourseSlot(courseName, "", "", "", day, start, end, shade, sessionType))
                }
                continue
            }

            // Format C: Plain course on one line with simple tokens
            if (parts.size >= 2) {
                val day = dayKeywords[parts[0].lowercase(Locale.ROOT)] ?: 1
                val name = parts.last()
                results.add(ParsedCourseSlot(name, "", "", "", day, "09:00", "10:30", shade, sessionType))
            }
        }
        return results
    }

    private fun detectSessionType(text: String): String {
        val lower = text.lowercase(Locale.ROOT)
        return when {
            lower.contains("posting") || lower.contains("ward") || lower.contains("clinical") || lower.contains("clinic") || lower.contains("internship") -> "POSTING"
            lower.contains("lab") || lower.contains("practical") || lower.contains("dissection") || lower.contains("workshop") -> "LAB"
            else -> "CLASS"
        }
    }

    private fun formatTime(raw: String): String {
        var str = raw.trim().lowercase(Locale.ROOT)
        val isPm = str.contains("pm")
        val isAm = str.contains("am")
        str = str.replace("am", "").replace("pm", "").trim()
        str = str.replace(".", ":")

        var hour = 9
        var min = 0

        if (str.contains(":")) {
            val parts = str.split(":")
            hour = parts[0].filter { it.isDigit() }.toIntOrNull() ?: 9
            min = parts.getOrNull(1)?.filter { it.isDigit() }?.toIntOrNull() ?: 0
        } else {
            val num = str.filter { it.isDigit() }.toIntOrNull() ?: 9
            hour = if (num > 24) num / 100 else num
            min = if (num > 24) num % 100 else 0
        }

        if (isPm && hour < 12) hour += 12
        if (isAm && hour == 12) hour = 0

        val hStr = hour.toString().padStart(2, '0')
        val mStr = min.toString().padStart(2, '0')
        return "$hStr:$mStr"
    }
}

