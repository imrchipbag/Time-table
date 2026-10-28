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
    assertEquals("GrooveTrack", appName)
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
  fun `sample presets contain slots`() {
    val presets = TimetableParser.getSamplePresets()
    assertTrue(presets.isNotEmpty())
    assertTrue(presets.containsKey("Computer Science & AI"))
  }
}
