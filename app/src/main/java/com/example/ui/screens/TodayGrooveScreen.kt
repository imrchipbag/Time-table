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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.data.model.CourseAttendanceStats
import com.example.data.model.OverallAttendanceStats
import com.example.data.model.ScheduleItem
import com.example.ui.components.AttendanceQuickButton
import com.example.ui.components.GroovyCard
import com.example.ui.components.GroovyStreakBadge
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayGrooveScreen(
    currentDateStr: String,
    scheduleItems: List<ScheduleItem>,
    stats: OverallAttendanceStats,
    onAnswerAttendance: (ScheduleItem, AttendanceStatus) -> Unit,
    onClearAttendance: (ScheduleItem) -> Unit,
    onShiftDate: (Long) -> Unit,
    onResetToday: () -> Unit,
    onDateSelected: (String) -> Unit = {},
    onNavigateToTimetable: () -> Unit,
    onTestNotification: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val parsedDate = try {
        LocalDate.parse(currentDateStr, DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (_: Exception) {
        LocalDate.now()
    }

    val isToday = parsedDate.isEqual(LocalDate.now())
    val dayName = parsedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val formattedDate = parsedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))

    val totalToday = scheduleItems.size
    val answeredToday = scheduleItems.count { it.record != null }
    val progress = if (totalToday > 0) answeredToday.toFloat() / totalToday else 0f

    var showDatePicker by remember { mutableStateOf(false) }

    val courseStatsMap = remember(stats.coursesStats) {
        stats.coursesStats.associateBy { it.course.id }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MonoBlack)
            .testTag("today_groove_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Minimal Header with Calendar icon in the corner
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TALLY",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = MonoWhite
                        )
                    )
                    Text(
                        text = "CLASS CHECK-IN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            color = MonoGrey
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GroovyStreakBadge(streakDays = stats.currentStreak)

                    // Calendar Icon in Corner
                    IconButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MonoSurfaceElevated)
                            .border(1.dp, MonoBorder, RoundedCornerShape(10.dp))
                            .testTag("corner_calendar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Pick Date from Calendar",
                            tint = MonoWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Date Switcher Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MonoSurfaceCard)
                    .border(1.dp, MonoBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { onShiftDate(-1) },
                    modifier = Modifier.testTag("prev_day_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Day",
                        tint = MonoWhite
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onResetToday() }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isToday) "Today, $dayName" else dayName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MonoWhite
                            )
                        )
                        if (!isToday) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MonoSurfaceElevated)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "RESET",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MonoLightGrey
                                    )
                                )
                            }
                        }
                    }
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall.copy(color = MonoGrey)
                    )
                }

                IconButton(
                    onClick = { onShiftDate(1) },
                    modifier = Modifier.testTag("next_day_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Day",
                        tint = MonoWhite
                    )
                }
            }
        }

        // Day Progress Banner
        if (totalToday > 0) {
            item {
                GroovyCard {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (answeredToday == totalToday) "All Classes Checked" else "Daily Progress",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MonoWhite
                                )
                            )
                            Text(
                                text = "$answeredToday / $totalToday Answered",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MonoLightGrey
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape),
                            color = MonoWhite,
                            trackColor = MonoSurfaceElevated,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        // Section Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CLASSES TODAY (${scheduleItems.size})",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MonoLightGrey
                    )
                )
                Text(
                    text = "Tap to answer",
                    style = MaterialTheme.typography.labelSmall.copy(color = MonoGrey)
                )
            }
        }

        // Empty State if no classes scheduled
        if (scheduleItems.isEmpty()) {
            item {
                GroovyCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Classes Scheduled Today",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MonoWhite
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You have no classes scheduled for $dayName.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MonoGrey)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onNavigateToTimetable,
                            colors = ButtonDefaults.buttonColors(containerColor = MonoSurfaceElevated),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("empty_open_timetable_button")
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MonoWhite, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Set Up Schedule", color = MonoWhite)
                        }
                    }
                }
            }
        } else {
            items(scheduleItems, key = { it.slot.id }) { item ->
                val courseStat = courseStatsMap[item.course.id]
                ClassAttendanceCard(
                    item = item,
                    courseStat = courseStat,
                    onAnswer = { status -> onAnswerAttendance(item, status) },
                    onClear = { onClearAttendance(item) }
                )
            }
        }
    }

    // Material 3 Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = parsedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val selectedLocalDate = Instant.ofEpochMilli(selectedMillis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            onDateSelected(selectedLocalDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Select", color = MonoWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = MonoGrey)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun ClassAttendanceCard(
    item: ScheduleItem,
    courseStat: CourseAttendanceStats?,
    onAnswer: (AttendanceStatus) -> Unit,
    onClear: () -> Unit
) {
    val course = item.course
    val slot = item.slot
    val record = item.record
    val currentStatus = record?.status

    // Check if below individual target percentage
    val isBelowTarget = courseStat != null && !courseStat.isSafe && courseStat.totalCount > 0

    val cardBorderColor = when {
        isBelowTarget -> MonoDangerRed
        currentStatus != null -> MonoWhite
        else -> MonoBorder
    }

    GroovyCard(
        borderColor = cardBorderColor,
        glowEffect = isBelowTarget || currentStatus != null
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Course Name, Code, and Red Danger Tag if below target
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = course.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isBelowTarget) MonoDangerRed else MonoWhite
                            )
                        )
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (course.code.isNotBlank()) {
                            Text(
                                text = course.code,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isBelowTarget) MonoDangerRed.copy(alpha = 0.8f) else MonoLightGrey
                                )
                            )
                        }
                        Text(
                            text = "Min: ${course.targetPercent}%",
                            style = MaterialTheme.typography.labelSmall.copy(color = MonoGrey)
                        )
                    }
                }

                // If below target percentage, show red indicator tag
                if (isBelowTarget) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MonoDangerRedBg)
                            .border(1.dp, MonoDangerRedBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${courseStat?.percentage?.toInt()}% DANGER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MonoDangerRed
                            )
                        )
                    }
                } else if (currentStatus != null) {
                    val badgeText = when (currentStatus) {
                        AttendanceStatus.ATTENDED -> "ATTENDED"
                        AttendanceStatus.MISSED -> "MISSED"
                        AttendanceStatus.CANCELLED -> "CANCELLED"
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (currentStatus == AttendanceStatus.ATTENDED) MonoWhite else MonoSurfaceElevated)
                            .border(1.dp, MonoBorderBright, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (currentStatus == AttendanceStatus.ATTENDED) MonoBlack else MonoWhite
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Time details strictly (location and faculty completely removed)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Time",
                    tint = if (isBelowTarget) MonoDangerRed else MonoLightGrey,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${slot.startTime} - ${slot.endTime}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isBelowTarget) MonoDangerRed else MonoWhite,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Attendance Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AttendanceQuickButton(
                    status = AttendanceStatus.ATTENDED,
                    isSelected = currentStatus == AttendanceStatus.ATTENDED,
                    onClick = { onAnswer(AttendanceStatus.ATTENDED) },
                    modifier = Modifier.weight(1f)
                )

                AttendanceQuickButton(
                    status = AttendanceStatus.MISSED,
                    isSelected = currentStatus == AttendanceStatus.MISSED,
                    onClick = { onAnswer(AttendanceStatus.MISSED) },
                    modifier = Modifier.weight(1f)
                )

                AttendanceQuickButton(
                    status = AttendanceStatus.CANCELLED,
                    isSelected = currentStatus == AttendanceStatus.CANCELLED,
                    onClick = { onAnswer(AttendanceStatus.CANCELLED) },
                    modifier = Modifier.weight(1f)
                )
            }

            if (currentStatus != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Reset ↺",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MonoGrey
                        ),
                        modifier = Modifier
                            .clickable { onClear() }
                            .padding(4.dp)
                            .testTag("reset_attendance_${slot.id}")
                    )
                }
            }
        }
    }
}
