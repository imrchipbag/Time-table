package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.TimetableParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Tally", appName)
  }

  @Test
  fun `parse timetable text successfully`() {
    val csv = "Monday, 09:00, 10:30, Computer Networks, CS308, Hall 105"
    val slots = TimetableParser.parseTimetableText(csv)
    assertEquals(1, slots.size)
    assertEquals("Computer Networks", slots[0].courseName)
    assertEquals("09:00", slots[0].startTime)
    assertEquals("10:30", slots[0].endTime)
    assertEquals(1, slots[0].dayOfWeek)
  }

  @Test
  fun `schedule parser supports classes postings and labs`() {
    val input = """
      Monday, 08:30, 11:30, General Surgery, SURG301, POSTING
      Tuesday, 14:00, 16:00, Microbiology Practical, MIC204, LAB
      Wednesday, 12:00, 13:00, Pathology Theory, PATH202, CLASS
    """.trimIndent()
    val slots = TimetableParser.parseTimetableText(input)
    assertEquals(3, slots.size)
    assertTrue(slots.any { it.sessionType == "POSTING" })
    assertTrue(slots.any { it.sessionType == "LAB" })
    assertTrue(slots.any { it.sessionType == "CLASS" })
  }

  @Test
  fun `parse posting and lab keywords`() {
    val text = """
      Monday, 08:30, 11:30, Internal Medicine Posting, MED301
      Tuesday, 14:00, 16:30, Biochemistry Lab, BIO201L
      Wednesday, 09:00, 10:00, Pathology Lecture, MED202
    """.trimIndent()
    val slots = TimetableParser.parseTimetableText(text)
    assertEquals(3, slots.size)
    assertEquals("POSTING", slots[0].sessionType)
    assertEquals("LAB", slots[1].sessionType)
    assertEquals("CLASS", slots[2].sessionType)
  }

  @Test
  fun `extracted timetable result constructs successfully`() {
    val slot = com.example.data.repository.ParsedCourseSlot(
      courseName = "General Surgery",
      courseCode = "SURG301",
      dayOfWeek = 1,
      startTime = "08:30",
      endTime = "11:30",
      sessionType = "POSTING"
    )
    val result = com.example.data.ai.ExtractedTimetableResult(
      slots = listOf(slot),
      startDate = "2026-08-01",
      endDate = "2026-12-15",
      isSuccess = true,
      statusMessage = "Extraction successful"
    )
    assertTrue(result.isSuccess)
    assertEquals(1, result.slots.size)
    assertEquals("POSTING", result.slots[0].sessionType)
  }
}

