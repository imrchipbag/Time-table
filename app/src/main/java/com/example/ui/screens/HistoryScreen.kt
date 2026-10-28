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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.Course
import com.example.ui.components.GroovyCard
import com.example.ui.theme.GroovyAttended
import com.example.ui.theme.GroovyBg
import com.example.ui.theme.GroovyBorder
import com.example.ui.theme.GroovyCancelled
import com.example.ui.theme.GroovyCyan
import com.example.ui.theme.GroovyMagenta
import com.example.ui.theme.GroovyMissed
import com.example.ui.theme.GroovySurfaceCard
import com.example.ui.theme.GroovySurfaceElevated
import com.example.ui.theme.GroovyTextMuted
import com.example.ui.theme.GroovyTextPrimary
import com.example.ui.theme.GroovyTextSecondary
import com.example.ui.theme.GroovyYellow

@Composable
fun HistoryScreen(
    records: List<AttendanceRecord>,
    courses: List<Course>,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf<AttendanceStatus?>(null) }
    val courseMap = remember(courses) { courses.associateBy { it.id } }

    val filteredRecords = remember(records, selectedFilter) {
        if (selectedFilter == null) records else records.filter { it.status == selectedFilter }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GroovyBg)
            .testTag("history_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "ATTENDANCE LOG",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        color = GroovyMagenta
                    )
                )
                Text(
                    text = "RECORD OF ALL CHECKED SESSIONS (${records.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = GroovyCyan
                    )
                )
            }
        }

        // Filter Chips Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        label = "ALL (${records.size})",
                        isSelected = selectedFilter == null,
                        color = GroovyMagenta,
                        onClick = { selectedFilter = null }
                    )
                }
                item {
                    val count = records.count { it.status == AttendanceStatus.ATTENDED }
                    FilterChip(
                        label = "ATTENDED ($count)",
                        isSelected = selectedFilter == AttendanceStatus.ATTENDED,
                        color = GroovyAttended,
                        onClick = { selectedFilter = AttendanceStatus.ATTENDED }
                    )
                }
                item {
                    val count = records.count { it.status == AttendanceStatus.MISSED }
                    FilterChip(
                        label = "MISSED ($count)",
                        isSelected = selectedFilter == AttendanceStatus.MISSED,
                        color = GroovyMissed,
                        onClick = { selectedFilter = AttendanceStatus.MISSED }
                    )
                }
                item {
                    val count = records.count { it.status == AttendanceStatus.CANCELLED }
                    FilterChip(
                        label = "CANCELLED ($count)",
                        isSelected = selectedFilter == AttendanceStatus.CANCELLED,
                        color = GroovyCancelled,
                        onClick = { selectedFilter = AttendanceStatus.CANCELLED }
                    )
                }
            }
        }

        if (filteredRecords.isEmpty()) {
            item {
                GroovyCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "📜", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "NO RECORDS FOUND",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = GroovyTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Answer your class attendance on the Today tab to start building your log!",
                            style = MaterialTheme.typography.bodySmall.copy(color = GroovyTextSecondary)
                        )
                    }
                }
            }
        } else {
            items(filteredRecords, key = { it.id }) { record ->
                val course = courseMap[record.courseId]
                HistoryRecordCard(record = record, course = course)
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) color.copy(alpha = 0.25f) else GroovySurfaceCard)
            .border(1.dp, if (isSelected) color else GroovyBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("filter_${label.substringBefore(" ")}")
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                color = if (isSelected) color else GroovyTextSecondary
            )
        )
    }
}

@Composable
fun HistoryRecordCard(
    record: AttendanceRecord,
    course: Course?
) {
    val courseColor = remember(course?.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(course?.colorHex ?: "#FF2E93"))
        } catch (_: Exception) {
            GroovyMagenta
        }
    }

    val (badgeText, badgeColor) = when (record.status) {
        AttendanceStatus.ATTENDED -> "ATTENDED ✨" to GroovyAttended
        AttendanceStatus.MISSED -> "MISSED 😴" to GroovyMissed
        AttendanceStatus.CANCELLED -> "CANCELLED 🏖️" to GroovyCancelled
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
                        text = course?.name ?: "Unknown Course",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = GroovyTextPrimary
                        )
                    )
                    Text(
                        text = "Date: ${record.date} ${if (course?.code?.isNotBlank() == true) "• ${course.code}" else ""}",
                        style = MaterialTheme.typography.labelSmall.copy(color = GroovyTextMuted)
                    )
                }
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
}
