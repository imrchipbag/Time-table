package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.screens.AIScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.TimetableScreen
import com.example.ui.screens.TodayGrooveScreen
import com.example.ui.screens.UploadTimetableDialog
import com.example.ui.theme.GrooveTrackTheme
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoGlassBg
import com.example.ui.theme.MonoGlassBorder
import com.example.ui.theme.MonoGrey
import com.example.ui.theme.MonoWhite
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
    val aiMessages by viewModel.aiMessages.collectAsStateWithLifecycle()

    val timetableValidity by viewModel.timetableValidity.collectAsStateWithLifecycle()

    var showUploadDialog by remember { mutableStateOf(false) }

    // Request notification permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.scheduleClassReminders(context)
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.scheduleClassReminders(context)
        }
    }

    LaunchedEffect(allTimeSlots.size) {
        if (allTimeSlots.isNotEmpty()) {
            viewModel.scheduleClassReminders(context)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.toast.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MonoBlack),
        containerColor = MonoBlack,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AppleGlassBottomBar(
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
                        onDateSelected = { selectedDate ->
                            viewModel.setSelectedDate(selectedDate)
                        },
                        onNavigateToTimetable = {
                            viewModel.selectTab(1)
                        },
                        onTestNotification = {
                            viewModel.triggerTestNotification(context)
                        }
                    )
                    1 -> TimetableScreen(
                        courses = allCourses,
                        timeSlots = allTimeSlots,
                        stats = overallStats,
                        timetableValidity = timetableValidity,
                        onOpenUploadDialog = { showUploadDialog = true },
                        onAddCourseWithSlots = { name, code, target, sessionType, days, start, end ->
                            viewModel.addCourseWithSlots(name, code, target, sessionType, days, start, end)
                        },
                        onDeleteSlot = { slotId ->
                            viewModel.deleteTimeSlot(slotId)
                        },
                        onDeleteCourse = { courseId ->
                            viewModel.deleteCourse(courseId)
                        },
                        onClearAll = {
                            viewModel.clearAllTimetable()
                        }
                    )
                    2 -> StatsScreen(
                        stats = overallStats,
                        onUpdateCourseTarget = { courseId, newTarget ->
                            viewModel.updateCourseTarget(courseId, newTarget)
                        }
                    )
                    3 -> HistoryScreen(
                        records = allRecords,
                        courses = allCourses
                    )
                    4 -> AIScreen(
                        messages = aiMessages,
                        onSendMessage = { query ->
                            viewModel.askAI(query)
                        }
                    )
                }
            }
        }
    }

    if (showUploadDialog) {
        UploadTimetableDialog(
            initialStartDate = timetableValidity.first,
            initialEndDate = timetableValidity.second,
            onDismiss = { showUploadDialog = false },
            onImportConfirmed = { slots, replaceExisting, startDate, endDate ->
                viewModel.importParsedSchedule(slots, replaceExisting, startDate, endDate)
            }
        )
    }
}

/**
 * Apple-style translucent glass floating bottom navigation bar
 */
@Composable
fun AppleGlassBottomBar(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit
) {
    val items = listOf(
        NavigationItem("Today", Icons.Default.Today, 0, "nav_today"),
        NavigationItem("Timetable", Icons.Default.CalendarMonth, 1, "nav_timetable"),
        NavigationItem("Metrics", Icons.Default.Analytics, 2, "nav_stats"),
        NavigationItem("History", Icons.Default.FormatListBulleted, 3, "nav_history"),
        NavigationItem("Ask Tally", com.example.ui.components.KiteIcon, 4, "nav_tally")
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Translucent frosted glass container
        Surface(
            color = MonoGlassBg,
            shape = RoundedCornerShape(28.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MonoGlassBorder),
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = selectedTab == item.index

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onSelectTab(item.index) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag(item.tag)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) Color(0x30FFFFFF) else Color.Transparent)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) MonoWhite else Color(0x88FFFFFF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp,
                                color = if (isSelected) MonoWhite else MonoGrey
                            )
                        )
                    }
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
