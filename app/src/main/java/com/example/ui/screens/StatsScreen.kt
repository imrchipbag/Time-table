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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CourseAttendanceStats
import com.example.data.model.OverallAttendanceStats
import com.example.ui.components.BunkMarginTag
import com.example.ui.components.GroovyCard
import com.example.ui.components.GroovyCircularGauge
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
import com.example.ui.theme.MetricSuccessGreen
import com.example.ui.theme.MetricSuccessGreenBg
import com.example.ui.theme.MetricSuccessGreenBorder
import com.example.ui.theme.MetricDangerRed
import com.example.ui.theme.MetricDangerRedBg
import com.example.ui.theme.MetricDangerRedBorder

@Composable
fun StatsScreen(
    stats: OverallAttendanceStats,
    onUpdateCourseTarget: (Long, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var editingCourseStat by remember { mutableStateOf<CourseAttendanceStats?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MonoBlack)
            .testTag("stats_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "METRICS",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        color = MonoWhite
                    )
                )
                Text(
                    text = "INDIVIDUAL SUBJECT GOALS & BUNK MARGINS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = MonoGrey
                    )
                )
            }
        }

        // Circular Gauge Card
        item {
            GroovyCard(
                borderColor = MonoBorderBright
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "OVERALL ATTENDANCE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MonoLightGrey
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    GroovyCircularGauge(
                        percentage = stats.overallPercentage,
                        targetPercentage = 75,
                        size = 150.dp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Each subject has its own customizable minimum attendance goal below.",
                        style = MaterialTheme.typography.labelSmall.copy(color = MonoGrey),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        // Summary Metric Tiles (Green for Attended, Red for Missed)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricTile(
                    title = "ATTENDED",
                    value = stats.totalAttended.toString(),
                    valueColor = MetricSuccessGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    title = "MISSED",
                    value = stats.totalMissed.toString(),
                    valueColor = MetricDangerRed,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    title = "CANCELLED",
                    value = stats.totalCancelled.toString(),
                    valueColor = MonoLightGrey,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Course Breakdown Title
        item {
            Text(
                text = "SUBJECT BREAKDOWN & TARGETS",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MonoWhite
                )
            )
        }

        if (stats.coursesStats.isEmpty()) {
            item {
                GroovyCard {
                    Text(
                        text = "No course stats available yet. Answer your attendance to see live margins.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MonoGrey)
                    )
                }
            }
        } else {
            items(stats.coursesStats, key = { it.course.id }) { courseStat ->
                CourseStatCard(
                    stat = courseStat,
                    onEditTarget = { editingCourseStat = courseStat }
                )
            }
        }
    }

    // Dialog to edit individual subject target percentage
    if (editingCourseStat != null) {
        val course = editingCourseStat!!.course
        EditTargetDialog(
            courseName = course.name,
            currentTarget = course.targetPercent,
            onDismiss = { editingCourseStat = null },
            onSave = { newTarget ->
                onUpdateCourseTarget(course.id, newTarget)
                editingCourseStat = null
            }
        )
    }
}

@Composable
fun MetricTile(
    title: String,
    value: String,
    valueColor: Color = MonoWhite,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MonoSurfaceCard)
            .border(1.dp, MonoBorder, RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = MonoGrey
                )
            )
        }
    }
}

@Composable
fun CourseStatCard(
    stat: CourseAttendanceStats,
    onEditTarget: () -> Unit
) {
    val course = stat.course
    val target = course.targetPercent
    val isSafe = stat.isSafe
    val isBelowTarget = !isSafe && stat.totalCount > 0
    val gradeColor = if (isSafe) MetricSuccessGreen else MetricDangerRed
    val cardBorder = if (isBelowTarget) MetricDangerRedBorder else if (isSafe && stat.totalCount > 0) MetricSuccessGreenBorder else MonoBorder

    GroovyCard(
        borderColor = cardBorder,
        glowEffect = isBelowTarget
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isBelowTarget) MetricDangerRed else MonoWhite
                        )
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (course.code.isNotBlank()) {
                            Text(
                                text = course.code,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isBelowTarget) MetricDangerRed.copy(alpha = 0.8f) else MonoGrey
                                )
                            )
                        }
                        // Individual target indicator pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MonoSurfaceElevated)
                                .clickable { onEditTarget() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Target: $target%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MonoWhite
                                    )
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit target",
                                    tint = MonoGrey,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "${stat.percentage.toInt()}%",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (stat.totalCount == 0) MonoWhite else gradeColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dynamic Progress Bar (Green if above threshold, Red if below)
            LinearProgressIndicator(
                progress = { (stat.percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
                color = if (stat.totalCount == 0) MonoWhite else gradeColor,
                trackColor = MonoSurfaceElevated,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stat.attendedCount} attended / ${stat.totalCount} held",
                    style = MaterialTheme.typography.bodySmall.copy(color = MonoLightGrey)
                )

                BunkMarginTag(
                    bunkMargin = stat.bunkMargin,
                    isSafe = isSafe,
                    targetPercent = target
                )
            }
        }
    }
}

@Composable
fun EditTargetDialog(
    courseName: String,
    currentTarget: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var targetText by remember { mutableStateOf(currentTarget.toString()) }
    val presets = listOf(60, 70, 75, 80, 85, 90)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MonoSurfaceCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, MonoBorderBright),
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "SET MINIMUM ATTENDANCE",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MonoWhite
                    )
                )
                Text(
                    text = "For $courseName",
                    style = MaterialTheme.typography.bodySmall.copy(color = MonoGrey)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Required Percentage (%)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MonoWhite,
                        unfocusedBorderColor = MonoBorder,
                        focusedTextColor = MonoWhite,
                        unfocusedTextColor = MonoWhite,
                        focusedContainerColor = MonoSurfaceElevated,
                        unfocusedContainerColor = MonoSurfaceElevated
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Quick Presets:",
                    style = MaterialTheme.typography.labelSmall.copy(color = MonoGrey)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presets.forEach { p ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (targetText == p.toString()) MonoWhite else MonoSurfaceElevated)
                                .clickable { targetText = p.toString() }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$p%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (targetText == p.toString()) MonoBlack else MonoWhite
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
                        colors = ButtonDefaults.buttonColors(containerColor = MonoSurfaceElevated)
                    ) {
                        Text("Cancel", color = MonoLightGrey)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val value = targetText.toIntOrNull()?.coerceIn(1, 100) ?: currentTarget
                            onSave(value)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MonoWhite)
                    ) {
                        Text("Save Target", color = MonoBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
