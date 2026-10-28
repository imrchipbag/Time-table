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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.Course
import com.example.ui.components.GroovyCard
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoBorder
import com.example.ui.theme.MonoBorderBright
import com.example.ui.theme.MonoGrey
import com.example.ui.theme.MonoLightGrey
import com.example.ui.theme.MonoSurfaceCard
import com.example.ui.theme.MonoSurfaceElevated
import com.example.ui.theme.MonoWhite

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
            .background(MonoBlack)
            .testTag("history_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "LOG",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        color = MonoWhite
                    )
                )
                Text(
                    text = "ALL LOGGED SESSIONS (${records.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = MonoGrey
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
                        onClick = { selectedFilter = null }
                    )
                }
                item {
                    val count = records.count { it.status == AttendanceStatus.ATTENDED }
                    FilterChip(
                        label = "ATTENDED ($count)",
                        isSelected = selectedFilter == AttendanceStatus.ATTENDED,
                        onClick = { selectedFilter = AttendanceStatus.ATTENDED }
                    )
                }
                item {
                    val count = records.count { it.status == AttendanceStatus.MISSED }
                    FilterChip(
                        label = "MISSED ($count)",
                        isSelected = selectedFilter == AttendanceStatus.MISSED,
                        onClick = { selectedFilter = AttendanceStatus.MISSED }
                    )
                }
                item {
                    val count = records.count { it.status == AttendanceStatus.CANCELLED }
                    FilterChip(
                        label = "CANCELLED ($count)",
                        isSelected = selectedFilter == AttendanceStatus.CANCELLED,
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
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Records Found",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MonoWhite
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Answer your class attendance on the Today tab to build your log.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MonoGrey)
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
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) MonoWhite else MonoSurfaceCard)
            .border(1.dp, if (isSelected) MonoWhite else MonoBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
            .testTag("filter_${label.substringBefore(" ")}")
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MonoBlack else MonoLightGrey
            )
        )
    }
}

@Composable
fun HistoryRecordCard(
    record: AttendanceRecord,
    course: Course?
) {
    val (badgeText, isAttended) = when (record.status) {
        AttendanceStatus.ATTENDED -> "ATTENDED" to true
        AttendanceStatus.MISSED -> "MISSED" to false
        AttendanceStatus.CANCELLED -> "CANCELLED" to false
    }

    GroovyCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course?.name ?: "Class",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MonoWhite
                    )
                )
                Text(
                    text = "${record.date} ${if (course?.code?.isNotBlank() == true) "• ${course.code}" else ""}",
                    style = MaterialTheme.typography.labelSmall.copy(color = MonoGrey)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isAttended) MonoWhite else MonoSurfaceElevated)
                    .border(1.dp, MonoBorderBright, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isAttended) MonoBlack else MonoWhite
                    )
                )
            }
        }
    }
}
