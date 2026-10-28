package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
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

@Composable
fun GroovyCard(
    modifier: Modifier = Modifier,
    borderColor: Color = GroovyBorder,
    glowEffect: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GroovySurfaceCard)
            .border(
                width = if (glowEffect) 1.5.dp else 1.dp,
                color = if (glowEffect) GroovyBorderBright else borderColor,
                shape = RoundedCornerShape(20.dp)
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
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(GroovyMagenta.copy(alpha = 0.25f), GroovyOrange.copy(alpha = 0.25f))
                )
            )
            .border(1.dp, GroovyMagenta.copy(alpha = 0.6f), RoundedCornerShape(30.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("streak_badge")
    ) {
        Icon(
            imageVector = Icons.Default.LocalFireDepartment,
            contentDescription = "Groove Streak",
            tint = GroovyOrange,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "$streakDays DAY GROOVE",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = GroovyYellow
            )
        )
    }
}

@Composable
fun GroovyCircularGauge(
    percentage: Float,
    targetPercentage: Int = 75,
    size: Dp = 150.dp,
    strokeWidth: Dp = 14.dp,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (percentage / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900),
        label = "gaugeProgress"
    )

    val gaugeColor = when {
        percentage >= targetPercentage -> GroovyCyan
        percentage >= (targetPercentage - 10) -> GroovyYellow
        else -> GroovyMissed
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val arcSize = this.size.width - stroke

            // Background track
            drawArc(
                color = GroovySurfaceElevated,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Neon Progress arc
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(GroovyMagenta, gaugeColor, GroovyCyan, GroovyMagenta)
                ),
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
                    fontWeight = FontWeight.Black,
                    color = GroovyTextPrimary
                )
            )
            Text(
                text = if (percentage >= targetPercentage) "ON TRACK ✨" else "ATTEND MORE!",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = gaugeColor,
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
    val (label, icon, activeColor, tag) = when (status) {
        AttendanceStatus.ATTENDED -> Quadruple("Attended", Icons.Default.Check, GroovyAttended, "btn_attend")
        AttendanceStatus.MISSED -> Quadruple("Missed", Icons.Default.Close, GroovyMissed, "btn_miss")
        AttendanceStatus.CANCELLED -> Quadruple("Cancelled", Icons.Default.EventBusy, GroovyCancelled, "btn_cancel")
    }

    val backgroundColor = if (isSelected) activeColor.copy(alpha = 0.22f) else GroovySurfaceElevated
    val borderColor = if (isSelected) activeColor else GroovyBorder

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
        modifier = modifier
            .height(44.dp)
            .testTag(tag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) activeColor else GroovyTextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (isSelected) activeColor else GroovyTextSecondary
                )
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun BunkMarginTag(
    bunkMargin: Int,
    isSafe: Boolean,
    targetPercent: Int = 75,
    modifier: Modifier = Modifier
) {
    val (text, color, bg) = if (isSafe) {
        if (bunkMargin > 0) {
            Triple("Can bunk $bunkMargin class${if (bunkMargin > 1) "es" else ""}", GroovyCyan, GroovyCyan.copy(alpha = 0.15f))
        } else {
            Triple("At minimum border ($targetPercent%)", GroovyYellow, GroovyYellow.copy(alpha = 0.15f))
        }
    } else {
        Triple("Need $bunkMargin to hit $targetPercent%", GroovyMissed, GroovyMissed.copy(alpha = 0.15f))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(0.8.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
    }
}
