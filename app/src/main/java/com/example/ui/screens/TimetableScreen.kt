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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteOutline
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Course
import com.example.data.model.OverallAttendanceStats
import com.example.data.model.TimeSlot
import com.example.ui.components.GroovyCard
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoBorder
import com.example.ui.theme.MonoBorderBright
import com.example.ui.theme.MonoDangerRed
import com.example.ui.theme.MonoDangerRedBg
import com.example.ui.theme.MonoDangerRedBorder
import com.example.ui.theme.MonoGrey
import com.example.ui.theme.MonoLightGrey
import com.example.ui.theme.MonoSurfaceCard
import com.example.ui.theme.MonoSurfaceElevated
import com.example.ui.theme.MonoWhite

@Composable
fun TimetableScreen(
    courses: List<Course>,
    timeSlots: List<TimeSlot>,
    stats: OverallAttendanceStats,
    timetableValidity: Pair<String, String> = Pair("", ""),
    onOpenUploadDialog: () -> Unit,
    onAddCourseWithSlots: (String, String, Int, String, List<Int>, String, String) -> Unit,
    onDeleteSlot: (Long) -> Unit,
    onDeleteCourse: (Long) -> Unit,
    onClearAll: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedDayOfWeek by remember { mutableIntStateOf(1) } // 1 = Monday .. 7 = Sunday
    var showAllDays by remember { mutableStateOf(false) }
    var selectedTypeFilter by remember { mutableStateOf<String?>(null) } // null = All, "CLASS", "POSTING", "LAB"
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
    val courseStatsMap = remember(stats.coursesStats) {
        stats.coursesStats.associateBy { it.course.id }
    }

    val displayedSlots = remember(timeSlots, selectedDayOfWeek, showAllDays, selectedTypeFilter) {
        val base = if (showAllDays) {
            timeSlots.sortedWith(compareBy({ it.dayOfWeek }, { it.startTime }))
        } else {
            timeSlots.filter { it.dayOfWeek == selectedDayOfWeek }.sortedBy { it.startTime }
        }
        if (selectedTypeFilter == null) base else base.filter { it.sessionType.equals(selectedTypeFilter, ignoreCase = true) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MonoBlack)
            .testTag("timetable_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header with bold typography matching TALLY
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TIMETABLE",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = MonoWhite
                        )
                    )
                    Text(
                        text = "${courses.size} Subjects • ${timeSlots.size} Sessions Weekly",
                        style = MaterialTheme.typography.bodySmall.copy(color = MonoGrey)
                    )
                    if (timetableValidity.first.isNotBlank() && timetableValidity.second.isNotBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MonoLightGrey,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Term: ${timetableValidity.first} to ${timetableValidity.second}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MonoLightGrey,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onOpenUploadDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = MonoWhite),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("upload_timetable_screen_button")
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MonoBlack, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Set Schedule", color = MonoBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Action Buttons Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showAddClassDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MonoSurfaceElevated),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("add_custom_class_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = MonoWhite, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Session", color = MonoWhite)
                }

                Button(
                    onClick = { showAllDays = !showAllDays },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showAllDays) MonoWhite else MonoSurfaceElevated
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("toggle_all_days_button")
                ) {
                    Text(
                        if (showAllDays) "By Day View" else "View All Days",
                        color = if (showAllDays) MonoBlack else MonoWhite
                    )
                }
            }
        }

        // Category Filter Chips: All, Classes, Postings, Labs
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val types = listOf(
                    null to "ALL",
                    "CLASS" to "CLASSES",
                    "POSTING" to "POSTINGS",
                    "LAB" to "LABS"
                )
                items(types) { (typeKey, label) ->
                    val isSelected = selectedTypeFilter == typeKey
                    val count = if (typeKey == null) timeSlots.size else timeSlots.count { it.sessionType.equals(typeKey, ignoreCase = true) }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MonoWhite else MonoSurfaceCard)
                            .border(1.dp, if (isSelected) MonoWhite else MonoBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedTypeFilter = typeKey }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$label ($count)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MonoBlack else MonoLightGrey
                            )
                        )
                    }
                }
            }
        }

        // Day of Week Selector Pills (if not viewing all days)
        if (!showAllDays) {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(days) { (dayNum, dayLabel) ->
                        val isSelected = selectedDayOfWeek == dayNum
                        val countForDay = timeSlots.count { it.dayOfWeek == dayNum }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) MonoWhite else MonoSurfaceCard)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) MonoWhite else MonoBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedDayOfWeek = dayNum }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("day_tab_$dayNum"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dayLabel,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MonoBlack else MonoWhite
                                    )
                                )
                                Text(
                                    text = "$countForDay",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        color = if (isSelected) MonoBlack.copy(alpha = 0.7f) else MonoGrey
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section Title
        item {
            val title = if (showAllDays) {
                "All Weekly Sessions (${displayedSlots.size})"
            } else {
                val dayName = days.firstOrNull { it.first == selectedDayOfWeek }?.second ?: "Day"
                "$dayName's Sessions (${displayedSlots.size})"
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MonoWhite
                )
            )
        }

        if (displayedSlots.isEmpty()) {
            item {
                GroovyCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Sessions Found",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MonoWhite
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add classes, postings, or labs using the Add Session button.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MonoGrey)
                        )
                    }
                }
            }
        } else {
            items(displayedSlots, key = { it.id }) { slot ->
                val course = courseMap[slot.courseId]
                if (course != null) {
                    val dayLabel = days.firstOrNull { it.first == slot.dayOfWeek }?.second ?: "Day"
                    val cStat = courseStatsMap[course.id]
                    val isBelowTarget = cStat != null && !cStat.isSafe && cStat.totalCount > 0

                    TimetableSlotCard(
                        slot = slot,
                        course = course,
                        isBelowTarget = isBelowTarget,
                        dayLabel = if (showAllDays) dayLabel else null,
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
            onConfirm = { name, code, target, sessionType, daysSelected, start, end ->
                onAddCourseWithSlots(name, code, target, sessionType, daysSelected, start, end)
                if (daysSelected.isNotEmpty()) {
                    selectedDayOfWeek = daysSelected.first()
                }
                showAddClassDialog = false
            }
        )
    }
}

@Composable
fun TimetableSlotCard(
    slot: TimeSlot,
    course: Course,
    isBelowTarget: Boolean,
    dayLabel: String?,
    onDeleteSlot: () -> Unit
) {
    GroovyCard(
        borderColor = if (isBelowTarget) MonoDangerRed else MonoBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (dayLabel != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MonoSurfaceElevated)
                                .border(0.5.dp, MonoBorderBright, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MonoWhite
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = course.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isBelowTarget) MonoDangerRed else MonoWhite
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val typeLabel = when (slot.sessionType.uppercase()) {
                        "POSTING" -> "POSTING"
                        "LAB" -> "LAB"
                        else -> "CLASS"
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isBelowTarget) MonoDangerRedBg else MonoSurfaceElevated)
                            .border(0.5.dp, if (isBelowTarget) MonoDangerRedBorder else MonoBorderBright, RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = typeLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isBelowTarget) MonoDangerRed else MonoLightGrey
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (course.code.isNotBlank()) {
                        Text(
                            text = course.code,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isBelowTarget) MonoDangerRed else MonoLightGrey
                            )
                        )
                    }
                    Text(
                        text = "${slot.startTime} - ${slot.endTime}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = MonoWhite
                        )
                    )
                    Text(
                        text = "• Min: ${course.targetPercent}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isBelowTarget) MonoDangerRed else MonoGrey
                        )
                    )
                }
            }

            IconButton(
                onClick = onDeleteSlot,
                modifier = Modifier.testTag("delete_slot_${slot.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Class Slot",
                    tint = MonoLightGrey
                )
            }
        }
    }
}

@Composable
fun AddClassDialog(
    initialDay: Int,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, String, List<Int>, String, String) -> Unit
) {
    var courseName by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf("") }
    var targetPercentText by remember { mutableStateOf("75") }
    var selectedSessionType by remember { mutableStateOf("CLASS") } // CLASS, POSTING, LAB
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:30") }
    var selectedDays by remember { mutableStateOf(setOf(initialDay)) }

    val daysList = listOf(1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu", 5 to "Fri", 6 to "Sat", 7 to "Sun")
    val sessionTypes = listOf("CLASS" to "Class", "POSTING" to "Posting", "LAB" to "Lab")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MonoSurfaceCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, MonoBorderBright),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .testTag("add_class_dialog")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "ADD NEW SESSION",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MonoWhite
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Type selector: Class / Posting / Lab
                Text(
                    text = "Type:",
                    style = MaterialTheme.typography.labelSmall.copy(color = MonoLightGrey, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    sessionTypes.forEach { (typeKey, typeLabel) ->
                        val isSelected = selectedSessionType == typeKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MonoWhite else MonoSurfaceElevated)
                                .border(1.dp, if (isSelected) MonoWhite else MonoBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedSessionType = typeKey }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = typeLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MonoBlack else MonoWhite
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = courseName,
                    onValueChange = { courseName = it },
                    label = { Text("Subject / Posting Name *") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MonoWhite,
                        unfocusedBorderColor = MonoBorder,
                        focusedTextColor = MonoWhite,
                        unfocusedTextColor = MonoWhite,
                        focusedContainerColor = MonoSurfaceElevated,
                        unfocusedContainerColor = MonoSurfaceElevated
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_course_name")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = courseCode,
                        onValueChange = { courseCode = it },
                        label = { Text("Code (optional)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MonoWhite,
                            unfocusedBorderColor = MonoBorder,
                            focusedTextColor = MonoWhite,
                            unfocusedTextColor = MonoWhite,
                            focusedContainerColor = MonoSurfaceElevated,
                            unfocusedContainerColor = MonoSurfaceElevated
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = targetPercentText,
                        onValueChange = { targetPercentText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Min Target %") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MonoWhite,
                            unfocusedBorderColor = MonoBorder,
                            focusedTextColor = MonoWhite,
                            unfocusedTextColor = MonoWhite,
                            focusedContainerColor = MonoSurfaceElevated,
                            unfocusedContainerColor = MonoSurfaceElevated
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
                            focusedBorderColor = MonoWhite,
                            unfocusedBorderColor = MonoBorder,
                            focusedTextColor = MonoWhite,
                            unfocusedTextColor = MonoWhite,
                            focusedContainerColor = MonoSurfaceElevated,
                            unfocusedContainerColor = MonoSurfaceElevated
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MonoWhite,
                            unfocusedBorderColor = MonoBorder,
                            focusedTextColor = MonoWhite,
                            unfocusedTextColor = MonoWhite,
                            focusedContainerColor = MonoSurfaceElevated,
                            unfocusedContainerColor = MonoSurfaceElevated
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Weekly Days:",
                    style = MaterialTheme.typography.labelSmall.copy(color = MonoLightGrey, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    daysList.forEach { (d, label) ->
                        val isChecked = selectedDays.contains(d)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isChecked) MonoWhite else MonoSurfaceElevated)
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
                                    color = if (isChecked) MonoBlack else MonoWhite
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = MonoSurfaceElevated)
                    ) {
                        Text("Cancel", color = MonoLightGrey)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (courseName.isNotBlank()) {
                                val target = targetPercentText.toIntOrNull()?.coerceIn(1, 100) ?: 75
                                onConfirm(
                                    courseName,
                                    courseCode,
                                    target,
                                    selectedSessionType,
                                    selectedDays.toList(),
                                    startTime,
                                    endTime
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MonoWhite),
                        modifier = Modifier.testTag("save_new_class_button")
                    ) {
                        Text("Add Session", color = MonoBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
