package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AIAttendanceAdvisor
import com.example.data.ai.AIMessage
import com.example.data.db.GrooveTrackDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.Course
import com.example.data.model.OverallAttendanceStats
import com.example.data.model.ScheduleItem
import com.example.data.model.TimeSlot
import com.example.data.repository.AttendanceRepository
import com.example.data.repository.ParsedCourseSlot
import com.example.data.repository.TimetableParser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AttendanceRepository

    init {
        val db = GrooveTrackDatabase.getDatabase(application)
        repository = AttendanceRepository(
            db.courseDao(),
            db.timeSlotDao(),
            db.attendanceRecordDao(),
            application
        )
    }

    val timetableValidity: StateFlow<Pair<String, String>> = repository.timetableValidity

    private val _currentDate = MutableStateFlow(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _toast = MutableSharedFlow<String>()
    val toast: SharedFlow<String> = _toast.asSharedFlow()

    // AI Messages
    private val _aiMessages = MutableStateFlow<List<AIMessage>>(
        listOf(
            AIMessage(
                isUser = false,
                text = "Hello! I am your AI Attendance Advisor. Ask me anything like:\n• \"If I skip classes, when can I?\"\n• \"Can I skip classes today?\"\n• \"Which subject has the safest bunk margin?\""
            )
        )
    )
    val aiMessages: StateFlow<List<AIMessage>> = _aiMessages.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val scheduleItems: StateFlow<List<ScheduleItem>> = _currentDate
        .flatMapLatest { date -> repository.getScheduleForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overallStats: StateFlow<OverallAttendanceStats> = repository.overallStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OverallAttendanceStats())

    val allCourses: StateFlow<List<Course>> = repository.allCourses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTimeSlots: StateFlow<List<TimeSlot>> = repository.allTimeSlots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecords: StateFlow<List<AttendanceRecord>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun setSelectedDate(date: String) {
        _currentDate.value = date
    }

    fun shiftDate(days: Long) {
        try {
            val parsed = LocalDate.parse(_currentDate.value, DateTimeFormatter.ISO_LOCAL_DATE)
            val newDate = parsed.plusDays(days)
            _currentDate.value = newDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (_: Exception) {
            _currentDate.value = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        }
    }

    fun resetToToday() {
        _currentDate.value = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    fun answerAttendance(slot: TimeSlot, status: AttendanceStatus, note: String = "") {
        viewModelScope.launch {
            repository.setAttendance(
                courseId = slot.courseId,
                timeSlotId = slot.id,
                date = _currentDate.value,
                status = status,
                note = note
            )
            val msg = when (status) {
                AttendanceStatus.ATTENDED -> "Marked as Attended"
                AttendanceStatus.MISSED -> "Marked as Missed"
                AttendanceStatus.CANCELLED -> "Marked as Cancelled"
            }
            _toast.emit(msg)
        }
    }

    fun clearAttendance(slot: TimeSlot) {
        viewModelScope.launch {
            repository.removeAttendance(
                date = _currentDate.value,
                timeSlotId = slot.id
            )
            _toast.emit("Attendance reset for class")
        }
    }

    fun importParsedSchedule(
        slots: List<ParsedCourseSlot>,
        replaceExisting: Boolean,
        startDate: String = "",
        endDate: String = ""
    ) {
        viewModelScope.launch {
            repository.importParsedSlots(slots, replaceExisting, startDate, endDate)
            val validityStr = if (startDate.isNotBlank() && endDate.isNotBlank()) " ($startDate to $endDate)" else ""
            _toast.emit("Applied ${slots.size} sessions to timetable$validityStr")
        }
    }

    fun clearAllTimetable() {
        viewModelScope.launch {
            repository.clearAllTimetable()
            _toast.emit("All timetable classes cleared")
        }
    }

    fun addCourseWithSlots(
        name: String,
        code: String,
        targetPercent: Int,
        sessionType: String,
        days: List<Int>,
        startTime: String,
        endTime: String
    ) {
        viewModelScope.launch {
            val course = Course(
                name = name,
                code = code,
                professor = "",
                room = "",
                colorHex = "#FFFFFF",
                targetPercent = targetPercent,
                sessionType = sessionType
            )
            val courseId = repository.addCourse(course)
            days.forEach { day ->
                val slot = TimeSlot(
                    courseId = courseId,
                    dayOfWeek = day,
                    startTime = startTime,
                    endTime = endTime,
                    roomOverride = "",
                    sessionType = sessionType
                )
                repository.addTimeSlot(slot)
            }
            _toast.emit("Added $sessionType: $name (${days.size} weekly)")
        }
    }

    fun updateCourseTarget(courseId: Long, newTarget: Int) {
        viewModelScope.launch {
            repository.updateCourseTargetPercent(courseId, newTarget)
            _toast.emit("Updated minimum required attendance to $newTarget%")
        }
    }

    fun scheduleClassReminders(context: android.content.Context) {
        com.example.util.NotificationScheduler.scheduleRemindersForSlots(
            context = context,
            slots = allTimeSlots.value,
            courses = allCourses.value
        )
    }

    fun triggerTestNotification(context: android.content.Context) {
        com.example.util.NotificationScheduler.triggerTestNotification(context)
    }

    fun deleteCourse(courseId: Long) {
        viewModelScope.launch {
            repository.deleteCourse(courseId)
            _toast.emit("Course removed")
        }
    }

    fun deleteTimeSlot(slotId: Long) {
        viewModelScope.launch {
            repository.deleteTimeSlot(slotId)
            _toast.emit("Class slot removed")
        }
    }

    fun askAI(query: String) {
        if (query.isBlank()) return
        val userMsg = AIMessage(isUser = true, text = query.trim())
        _aiMessages.value = _aiMessages.value + userMsg

        viewModelScope.launch {
            val answer = AIAttendanceAdvisor.generateAdvice(
                userQuery = query,
                todaySchedule = scheduleItems.value,
                allSlots = allTimeSlots.value,
                stats = overallStats.value
            )
            val aiMsg = AIMessage(isUser = false, text = answer)
            _aiMessages.value = _aiMessages.value + aiMsg
        }
    }
}
