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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.CourseAttendanceStats
import com.example.data.model.OverallAttendanceStats
import com.example.ui.components.BunkMarginTag
import com.example.ui.components.GroovyCard
import com.example.ui.components.GroovyCircularGauge
import com.example.ui.theme.GroovyAttended
import com.example.ui.theme.GroovyBg
import com.example.ui.theme.GroovyBorder
import com.example.ui.theme.GroovyBorderBright
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
fun StatsScreen(
    stats: OverallAttendanceStats,
    modifier: Modifier = Modifier
) {
    var targetPercent by remember { mutableIntStateOf(75) }
    val targets = listOf(70, 75, 80, 85)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GroovyBg)
            .testTag("stats_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "GROOVE METRICS",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        color = GroovyMagenta
                    )
                )
                Text(
                    text = "ATTENDANCE & BUNK MARGIN CALCULATOR",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = GroovyCyan
                    )
                )
            }
        }

        // Circular Gauge Card
        item {
            GroovyCard(
                borderColor = GroovyBorderBright,
                glowEffect = true
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "OVERALL ATTENDANCE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = GroovyTextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    GroovyCircularGauge(
                        percentage = stats.overallPercentage,
                        targetPercentage = targetPercent,
                        size = 160.dp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Target Selector Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Target:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = GroovyTextMuted
                            )
                        )
                        targets.forEach { targetVal ->
                            val isSelected = targetPercent == targetVal
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) GroovyMagenta else GroovySurfaceElevated)
                                    .clickable { targetPercent = targetVal }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .testTag("target_$targetVal"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$targetVal%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = if (isSelected) GroovyBg else GroovyTextPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Summary Metric Tiles
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricTile(
                    title = "ATTENDED",
                    value = stats.totalAttended.toString(),
                    color = GroovyAttended,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    title = "MISSED",
                    value = stats.totalMissed.toString(),
                    color = GroovyMissed,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    title = "CANCELLED",
                    value = stats.totalCancelled.toString(),
                    color = GroovyCancelled,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Course Breakdown Title
        item {
            Text(
                text = "SUBJECT BREAKDOWN & BUNK MARGINS",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = GroovyYellow
                )
            )
        }

        if (stats.coursesStats.isEmpty()) {
            item {
                GroovyCard {
                    Text(
                        text = "No course stats available yet. Answer attendance for classes to see live bunk calculations!",
                        style = MaterialTheme.typography.bodyMedium.copy(color = GroovyTextSecondary)
                    )
                }
            }
        } else {
            items(stats.coursesStats, key = { it.course.id }) { courseStat ->
                CourseStatCard(stat = courseStat, defaultTarget = targetPercent)
            }
        }
    }
}

@Composable
fun MetricTile(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GroovySurfaceCard)
            .border(1.dp, GroovyBorder, RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = color
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = GroovyTextMuted
                )
            )
        }
    }
}

@Composable
fun CourseStatCard(
    stat: CourseAttendanceStats,
    defaultTarget: Int
) {
    val course = stat.course
    val courseColor = remember(course.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(course.colorHex))
        } catch (_: Exception) {
            GroovyMagenta
        }
    }

    val target = defaultTarget
    val isSafe = stat.percentage >= target

    GroovyCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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

                Text(
                    text = "${stat.percentage.toInt()}%",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = if (isSafe) GroovyCyan else GroovyMissed
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { (stat.percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = if (isSafe) courseColor else GroovyMissed,
                trackColor = GroovySurfaceElevated,
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
                    style = MaterialTheme.typography.bodySmall.copy(color = GroovyTextSecondary)
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
