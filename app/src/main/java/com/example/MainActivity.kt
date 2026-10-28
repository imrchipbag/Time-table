package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.TimetableScreen
import com.example.ui.screens.TodayGrooveScreen
import com.example.ui.screens.UploadTimetableDialog
import com.example.ui.theme.GroovyBg
import com.example.ui.theme.GroovyBorder
import com.example.ui.theme.GroovyCyan
import com.example.ui.theme.GroovyMagenta
import com.example.ui.theme.GroovySurface
import com.example.ui.theme.GroovySurfaceCard
import com.example.ui.theme.GroovySurfaceElevated
import com.example.ui.theme.GroovyTextMuted
import com.example.ui.theme.GroovyTextPrimary
import com.example.ui.theme.GroovyTextSecondary
import com.example.ui.theme.GrooveTrackTheme
import com.example.ui.viewmodel.AttendanceViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: AttendanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GrooveTrackTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: AttendanceViewModel) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val currentDate by viewModel.currentDate.collectAsStateWithLifecycle()
    val scheduleItems by viewModel.scheduleItems.collectAsStateWithLifecycle()
    val overallStats by viewModel.overallStats.collectAsStateWithLifecycle()
    val allCourses by viewModel.allCourses.collectAsStateWithLifecycle()
    val allTimeSlots by viewModel.allTimeSlots.collectAsStateWithLifecycle()
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()

    var showUploadDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.groovyToast.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(GroovyBg),
        containerColor = GroovyBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            GroovyBottomBar(
                selectedTab = selectedTab,
                onSelectTab = { viewModel.selectTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { tab ->
                when (tab) {
                    0 -> TodayGrooveScreen(
                        currentDateStr = currentDate,
                        scheduleItems = scheduleItems,
                        stats = overallStats,
                        onAnswerAttendance = { item, status ->
                            viewModel.answerAttendance(item.slot, status)
                        },
                        onClearAttendance = { item ->
                            viewModel.clearAttendance(item.slot)
                        },
                        onShiftDate = { delta ->
                            viewModel.shiftDate(delta)
                        },
                        onResetToday = {
                            viewModel.resetToToday()
                        },
                        onNavigateToTimetable = {
                            viewModel.selectTab(1)
                        }
                    )
                    1 -> TimetableScreen(
                        courses = allCourses,
                        timeSlots = allTimeSlots,
                        onOpenUploadDialog = { showUploadDialog = true },
                        onAddCourseWithSlots = { name, code, prof, room, color, target, days, start, end ->
                            viewModel.addCourseWithSlots(name, code, prof, room, color, target, days, start, end)
                        },
                        onDeleteSlot = { slotId ->
                            viewModel.deleteTimeSlot(slotId)
                        },
                        onDeleteCourse = { courseId ->
                            viewModel.deleteCourse(courseId)
                        }
                    )
                    2 -> StatsScreen(
                        stats = overallStats
                    )
                    3 -> HistoryScreen(
                        records = allRecords,
                        courses = allCourses
                    )
                }
            }
        }
    }

    if (showUploadDialog) {
        UploadTimetableDialog(
            onDismiss = { showUploadDialog = false },
            onImportConfirmed = { slots, replaceExisting ->
                viewModel.importParsedSchedule(slots, replaceExisting)
            }
        )
    }
}

@Composable
fun GroovyBottomBar(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit
) {
    val items = listOf(
        NavigationItem("Today", Icons.Default.Today, 0, "nav_today"),
        NavigationItem("Timetable", Icons.Default.CalendarMonth, 1, "nav_timetable"),
        NavigationItem("Metrics", Icons.Default.Analytics, 2, "nav_stats"),
        NavigationItem("History", Icons.Default.FormatListBulleted, 3, "nav_history")
    )

    Surface(
        color = GroovySurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, GroovyBorder),
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = selectedTab == item.index
                val activeColor = if (item.index % 2 == 0) GroovyMagenta else GroovyCyan

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectTab(item.index) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag(item.tag)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) activeColor.copy(alpha = 0.2f) else Color.Transparent)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected) activeColor else GroovyTextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            fontSize = 11.sp,
                            color = if (isSelected) activeColor else GroovyTextMuted
                        )
                    )
                }
            }
        }
    }
}

private data class NavigationItem(
    val label: String,
    val icon: ImageVector,
    val index: Int,
    val tag: String
)
