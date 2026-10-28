package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Course
import com.example.data.model.TimeSlot
import com.example.ui.components.GroovyCard
import com.example.ui.theme.GroovyBg
import com.example.ui.theme.GroovyBorder
import com.example.ui.theme.GroovyCyan
import com.example.ui.theme.GroovyMagenta
import com.example.ui.theme.GroovyMissed
import com.example.ui.theme.GroovyOrange
import com.example.ui.theme.GroovySurfaceCard
import com.example.ui.theme.GroovySurfaceElevated
import com.example.ui.theme.GroovyTextMuted
import com.example.ui.theme.GroovyTextPrimary
import com.example.ui.theme.GroovyTextSecondary
import com.example.ui.theme.GroovyYellow

@Composable
fun TimetableScreen(
    courses: List<Course>,
    timeSlots: List<TimeSlot>,
    onOpenUploadDialog: () -> Unit,
    onAddCourseWithSlots: (String, String, String, String, String, Int, List<Int>, String, String) -> Unit,
    onDeleteSlot: (Long) -> Unit,
    onDeleteCourse: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDayOfWeek by remember { mutableIntStateOf(1) } // 1 = Monday .. 7 = Sunday
    var showAddClassDialog by remember { mutableStateOf(false) }

    val days = listOf(
        1 to "Mon",
        2 to "Tue",
        3 to "Wed",
        4 to "Thu",
        5 to "Fri",
        6 to "Sat",
        7 to "Sun"
    )

    val courseMap = remember(courses) { courses.associateBy { it.id } }
    val slotsForDay = remember(timeSlots, selectedDayOfWeek) {
        timeSlots.filter { it.dayOfWeek == selectedDayOfWeek }.sortedBy { it.startTime }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GroovyBg)
            .testTag("timetable_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "WEEKLY TIMETABLE",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            color = GroovyMagenta
                        )
                    )
                    Text(
                        text = "${courses.size} Subjects • ${timeSlots.size} Weekly Classes",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = GroovyCyan
                        )
                    )
                }

                Button(
                    onClick = onOpenUploadDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = GroovyMagenta),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("upload_timetable_screen_button")
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null, tint = GroovyBg, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Upload", color = GroovyBg, fontWeight = FontWeight.Black)
                }
            }
        }

        // Quick Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showAddClassDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GroovySurfaceElevated),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_custom_class_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = GroovyCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Class Manually", color = GroovyCyan, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Day of Week Selector Pills
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(days) { (dayNum, dayLabel) ->
                    val isSelected = selectedDayOfWeek == dayNum
                    val countForDay = timeSlots.count { it.dayOfWeek == dayNum }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) GroovyMagenta else GroovySurfaceCard)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) GroovyMagenta else GroovyBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedDayOfWeek = dayNum }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("day_tab_$dayNum"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) GroovyBg else GroovyTextPrimary
                                )
                            )
                            Text(
                                text = "$countForDay classes",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = if (isSelected) GroovyBg.copy(alpha = 0.8f) else GroovyTextMuted
                                )
                            )
                        }
                    }
                }
            }
        }

        // Selected Day Classes
        item {
            val dayName = days.firstOrNull { it.first == selectedDayOfWeek }?.second ?: "Day"
            Text(
                text = "$dayName's Classes (${slotsForDay.size})",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = GroovyYellow
                )
            )
        }

        if (slotsForDay.isEmpty()) {
            item {
                GroovyCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🎸", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No classes on this day",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = GroovyTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add a class slot or upload your full timetable above.",
                            style = MaterialTheme.typography.bodySmall.copy(color = GroovyTextSecondary)
                        )
                    }
                }
            }
        } else {
            items(slotsForDay, key = { it.id }) { slot ->
                val course = courseMap[slot.courseId]
                if (course != null) {
                    TimetableSlotCard(
                        slot = slot,
                        course = course,
                        onDeleteSlot = { onDeleteSlot(slot.id) }
                    )
                }
            }
        }
    }

    if (showAddClassDialog) {
        AddClassDialog(
            initialDay = selectedDayOfWeek,
            onDismiss = { showAddClassDialog = false },
            onConfirm = { name, code, prof, room, color, target, daysSelected, start, end ->
                onAddCourseWithSlots(name, code, prof, room, color, target, daysSelected, start, end)
                showAddClassDialog = false
            }
        )
    }
}

@Composable
fun TimetableSlotCard(
    slot: TimeSlot,
    course: Course,
    onDeleteSlot: () -> Unit
) {
    val courseColor = remember(course.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(course.colorHex))
        } catch (_: Exception) {
            GroovyMagenta
        }
    }

    GroovyCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(courseColor)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = course.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = GroovyTextPrimary
                        )
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (course.code.isNotBlank()) {
                            Text(
                                text = course.code,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = courseColor
                                )
                            )
                        }
                        Text(
                            text = "${slot.startTime} - ${slot.endTime}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = GroovyCyan
                            )
                        )
                        val loc = slot.roomOverride.ifEmpty { course.room }
                        if (loc.isNotBlank()) {
                            Text(
                                text = "• $loc",
                                style = MaterialTheme.typography.labelSmall.copy(color = GroovyTextMuted)
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = onDeleteSlot,
                modifier = Modifier.testTag("delete_slot_${slot.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Class Slot",
                    tint = GroovyMissed
                )
            }
        }
    }
}

@Composable
fun AddClassDialog(
    initialDay: Int,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, Int, List<Int>, String, String) -> Unit
) {
    var courseName by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf("") }
    var professor by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:30") }
    var selectedDays by remember { mutableStateOf(setOf(initialDay)) }
    var selectedColor by remember { mutableStateOf("#FF2E93") }

    val daysList = listOf(1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu", 5 to "Fri", 6 to "Sat", 7 to "Sun")
    val colorOptions = listOf("#FF2E93", "#00F5D4", "#9D4EDD", "#FF7A00", "#FFD166", "#4CC9F0")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = GroovySurfaceCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, GroovyBorder),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .testTag("add_class_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "ADD NEW CLASS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = GroovyMagenta
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = courseName,
                    onValueChange = { courseName = it },
                    label = { Text("Course Name *") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GroovyMagenta,
                        unfocusedBorderColor = GroovyBorder,
                        focusedTextColor = GroovyTextPrimary,
                        unfocusedTextColor = GroovyTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_course_name")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = courseCode,
                        onValueChange = { courseCode = it },
                        label = { Text("Code (e.g. CS101)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GroovyMagenta,
                            unfocusedBorderColor = GroovyBorder,
                            focusedTextColor = GroovyTextPrimary,
                            unfocusedTextColor = GroovyTextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = room,
                        onValueChange = { room = it },
                        label = { Text("Room / Hall") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GroovyMagenta,
                            unfocusedBorderColor = GroovyBorder,
                            focusedTextColor = GroovyTextPrimary,
                            unfocusedTextColor = GroovyTextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GroovyMagenta,
                            unfocusedBorderColor = GroovyBorder,
                            focusedTextColor = GroovyTextPrimary,
                            unfocusedTextColor = GroovyTextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GroovyMagenta,
                            unfocusedBorderColor = GroovyBorder,
                            focusedTextColor = GroovyTextPrimary,
                            unfocusedTextColor = GroovyTextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Weekly Days:",
                    style = MaterialTheme.typography.labelSmall.copy(color = GroovyCyan, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    daysList.forEach { (d, label) ->
                        val isChecked = selectedDays.contains(d)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isChecked) GroovyMagenta else GroovySurfaceElevated)
                                .clickable {
                                    selectedDays = if (isChecked) {
                                        if (selectedDays.size > 1) selectedDays - d else selectedDays
                                    } else {
                                        selectedDays + d
                                    }
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChecked) GroovyBg else GroovyTextPrimary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = GroovySurfaceElevated)
                    ) {
                        Text("Cancel", color = GroovyTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (courseName.isNotBlank()) {
                                onConfirm(
                                    courseName,
                                    courseCode,
                                    professor,
                                    room,
                                    selectedColor,
                                    75,
                                    selectedDays.toList(),
                                    startTime,
                                    endTime
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GroovyMagenta),
                        modifier = Modifier.testTag("save_new_class_button")
                    ) {
                        Text("Add Class", color = GroovyBg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
