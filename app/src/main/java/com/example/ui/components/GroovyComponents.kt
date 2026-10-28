package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoBorder
import com.example.ui.theme.MonoBorderBright
import com.example.ui.theme.MonoDarkGrey
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
fun GroovyCard(
    modifier: Modifier = Modifier,
    borderColor: Color = MonoBorder,
    glowEffect: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MonoSurfaceCard)
            .border(
                width = 1.dp,
                color = if (glowEffect) MonoBorderBright else borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
fun GroovyStreakBadge(
    streakDays: Int,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MonoSurfaceElevated)
            .border(1.dp, MonoBorderBright, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .testTag("streak_badge")
    ) {
        Icon(
            imageVector = Icons.Default.LocalFireDepartment,
            contentDescription = "Streak",
            tint = MonoWhite,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "$streakDays DAY STREAK",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MonoWhite
            )
        )
    }
}

@Composable
fun GroovyCircularGauge(
    percentage: Float,
    targetPercentage: Int = 75,
    size: Dp = 150.dp,
    strokeWidth: Dp = 10.dp,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (percentage / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "gaugeProgress"
    )

    val isAboveThreshold = percentage >= targetPercentage
    val gradeColor = if (isAboveThreshold) MetricSuccessGreen else MetricDangerRed

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = strokeWidth.toPx()

            // Background track
            drawArc(
                color = Color(0xFF222222),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Dynamic Progress arc (Green if above threshold, Red if below)
            drawArc(
                color = gradeColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${percentage.toInt()}%",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = gradeColor
                )
            )
            Text(
                text = if (isAboveThreshold) "ON TRACK" else "BELOW TARGET",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = gradeColor,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}

@Composable
fun AttendanceQuickButton(
    status: AttendanceStatus,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (label, icon, tag) = when (status) {
        AttendanceStatus.ATTENDED -> Triple("Attended", Icons.Default.Check, "btn_attend")
        AttendanceStatus.MISSED -> Triple("Missed", Icons.Default.Close, "btn_miss")
        AttendanceStatus.CANCELLED -> Triple("Cancelled", Icons.Default.EventBusy, "btn_cancel")
    }

    val backgroundColor = if (isSelected) MonoWhite else MonoSurfaceElevated
    val contentColor = if (isSelected) MonoBlack else MonoLightGrey
    val borderColor = if (isSelected) MonoWhite else MonoBorder

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = modifier
            .height(44.dp)
            .testTag(tag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = contentColor
                )
            )
        }
    }
}

@Composable
fun BunkMarginTag(
    bunkMargin: Int,
    isSafe: Boolean,
    targetPercent: Int = 75,
    modifier: Modifier = Modifier
) {
    val text = if (isSafe) {
        if (bunkMargin > 0) {
            "Can skip $bunkMargin class${if (bunkMargin > 1) "es" else ""}"
        } else {
            "On target ($targetPercent%)"
        }
    } else {
        "Need $bunkMargin to reach $targetPercent%"
    }

    val tagBg = if (isSafe) MetricSuccessGreenBg else MetricDangerRedBg
    val tagBorder = if (isSafe) MetricSuccessGreenBorder else MetricDangerRedBorder
    val tagText = if (isSafe) MetricSuccessGreen else MetricDangerRed

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(tagBg)
            .border(0.8.dp, tagBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = tagText
            )
        )
    }
}
