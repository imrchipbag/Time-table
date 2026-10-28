package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.data.model.OverallAttendanceStats
import com.example.data.model.ScheduleItem
import com.example.ui.components.AttendanceQuickButton
import com.example.ui.components.GroovyCard
import com.example.ui.components.GroovyStreakBadge
import com.example.ui.theme.GroovyAttended
import com.example.ui.theme.GroovyBg
import com.example.ui.theme.GroovyBorder
import com.example.ui.theme.GroovyBorderBright
import com.example.ui.theme.GroovyCancelled
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TodayGrooveScreen(
    currentDateStr: String,
    scheduleItems: List<ScheduleItem>,
    stats: OverallAttendanceStats,
    onAnswerAttendance: (ScheduleItem, AttendanceStatus) -> Unit,
    onClearAttendance: (ScheduleItem) -> Unit,
    onShiftDate: (Long) -> Unit,
    onResetToday: () -> Unit,
    onNavigateToTimetable: () -> Unit,
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GroovyBg)
            .testTag("today_groove_screen"),
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
                        text = "GROOVETRACK",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            color = GroovyMagenta
                        )
                    )
                    Text(
                        text = "ATTENDANCE CHECK-IN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = GroovyCyan
                        )
                    )
                }
                GroovyStreakBadge(streakDays = stats.currentStreak)
            }
        }

        // Date Switcher Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GroovySurfaceCard)
                    .border(1.dp, GroovyBorder, RoundedCornerShape(16.dp))
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
                        tint = GroovyCyan
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onResetToday() }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isToday) "TODAY, $dayName" else dayName.uppercase(Locale.ROOT),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = if (isToday) GroovyYellow else GroovyTextPrimary
                            )
                        )
                        if (!isToday) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GroovyMagenta.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "RESET TO TODAY",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GroovyMagenta
                                    )
                                )
                            }
                        }
                    }
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall.copy(color = GroovyTextMuted)
                    )
                }

                IconButton(
                    onClick = { onShiftDate(1) },
                    modifier = Modifier.testTag("next_day_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Day",
                        tint = GroovyCyan
                    )
                }
            }
        }

        // Day Progress Banner
        if (totalToday > 0) {
            item {
                GroovyCard(glowEffect = answeredToday == totalToday) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (answeredToday == totalToday) "ALL CLASSES CHECKED! 🎉" else "DAILY PROGRESS",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = if (answeredToday == totalToday) GroovyAttended else GroovyCyan
                                )
                            )
                            Text(
                                text = "$answeredToday / $totalToday Answered",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GroovyTextSecondary
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = if (answeredToday == totalToday) GroovyAttended else GroovyMagenta,
                            trackColor = GroovySurfaceElevated,
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
                    text = "SCHEDULED CLASSES (${scheduleItems.size})",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = GroovyTextSecondary
                    )
                )
                Text(
                    text = "Tap to answer",
                    style = MaterialTheme.typography.labelSmall.copy(color = GroovyTextMuted)
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
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🏖️",
                            fontSize = 42.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "NO CLASSES SCHEDULED TODAY",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = GroovyYellow
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Kick back, relax and groove! Or check another day or upload your timetable.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = GroovyTextSecondary),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToTimetable,
                            colors = ButtonDefaults.buttonColors(containerColor = GroovySurfaceElevated),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("empty_open_timetable_button")
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, tint = GroovyCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Manage / Upload Timetable", color = GroovyCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Scheduled Class Cards
            items(scheduleItems, key = { it.slot.id }) { item ->
                ClassAttendanceCard(
                    item = item,
                    onAnswer = { status -> onAnswerAttendance(item, status) },
                    onClear = { onClearAttendance(item) }
                )
            }
        }

        // Bottom Groovy Stats Callout
        item {
            GroovyCard(
                borderColor = GroovyBorderBright,
                glowEffect = true
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "OVERALL ATTENDANCE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = GroovyTextMuted
                            )
                        )
                        Text(
                            text = "${stats.overallPercentage.toInt()}% Attendance Rate",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = if (stats.overallPercentage >= 75) GroovyCyan else GroovyMissed
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(GroovySurfaceElevated)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${stats.totalAttended} attended • ${stats.totalMissed} missed",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = GroovyYellow
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClassAttendanceCard(
    item: ScheduleItem,
    onAnswer: (AttendanceStatus) -> Unit,
    onClear: () -> Unit
) {
    val course = item.course
    val slot = item.slot
    val record = item.record

    val courseColor = remember(course.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(course.colorHex))
        } catch (_: Exception) {
            GroovyMagenta
        }
    }

    val currentStatus = record?.status

    GroovyCard(
        borderColor = if (currentStatus != null) {
            when (currentStatus) {
                AttendanceStatus.ATTENDED -> GroovyAttended
                AttendanceStatus.MISSED -> GroovyMissed
                AttendanceStatus.CANCELLED -> GroovyCancelled
            }
        } else GroovyBorder,
        glowEffect = currentStatus != null
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Color Tag, Course Name, Status Chip
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = course.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = GroovyTextPrimary
                            )
                        )
                        if (course.code.isNotBlank()) {
                            Text(
                                text = course.code,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = courseColor
                                )
                            )
                        }
                    }
                }

                // Current Status Badge if answered
                if (currentStatus != null) {
                    val (badgeText, badgeColor) = when (currentStatus) {
                        AttendanceStatus.ATTENDED -> "ATTENDED ✨" to GroovyAttended
                        AttendanceStatus.MISSED -> "MISSED 😴" to GroovyMissed
                        AttendanceStatus.CANCELLED -> "CANCELLED 🏖️" to GroovyCancelled
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeColor.copy(alpha = 0.2f))
                            .border(1.dp, badgeColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = badgeColor
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Time & Location details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Time",
                        tint = GroovyCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${slot.startTime} - ${slot.endTime}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = GroovyTextPrimary
                        )
                    )
                }

                val location = slot.roomOverride.ifEmpty { course.room }
                if (location.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = GroovyYellow,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodySmall.copy(color = GroovyTextSecondary)
                        )
                    }
                }

                if (course.professor.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Professor",
                            tint = GroovyMagenta,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = course.professor,
                            style = MaterialTheme.typography.bodySmall.copy(color = GroovyTextSecondary)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Attendance Question: "Did you attend this class?"
            Text(
                text = if (currentStatus == null) "Did you attend this class?" else "Change attendance answer:",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = GroovyTextSecondary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3 Quick Action Buttons
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
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Reset Answer ↺",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = GroovyTextMuted
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
