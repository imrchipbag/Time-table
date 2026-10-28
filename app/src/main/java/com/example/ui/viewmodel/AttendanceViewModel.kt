package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
            db.attendanceRecordDao()
        )
        // Seed initial sample schedule so the user immediately has a working experience
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    private val _currentDate = MutableStateFlow(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _groovyToast = MutableSharedFlow<String>()
    val groovyToast: SharedFlow<String> = _groovyToast.asSharedFlow()

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
                AttendanceStatus.ATTENDED -> "Groovy! Marked as Attended ✨"
                AttendanceStatus.MISSED -> "Logged as Missed / Bunked 😴"
                AttendanceStatus.CANCELLED -> "Class Cancelled logged 🏖️"
            }
            _groovyToast.emit(msg)
        }
    }

    fun clearAttendance(slot: TimeSlot) {
        viewModelScope.launch {
            repository.removeAttendance(
                date = _currentDate.value,
                timeSlotId = slot.id
            )
            _groovyToast.emit("Attendance reset for class ↺")
        }
    }

    fun importParsedSchedule(slots: List<ParsedCourseSlot>, replaceExisting: Boolean) {
        viewModelScope.launch {
            repository.importParsedSlots(slots, replaceExisting)
            _groovyToast.emit("Groovy! Uploaded ${slots.size} class slots 🚀")
        }
    }

    fun importFromText(text: String, replaceExisting: Boolean): Boolean {
        val parsed = TimetableParser.parseTimetableText(text)
        if (parsed.isEmpty()) return false
        viewModelScope.launch {
            repository.importParsedSlots(parsed, replaceExisting)
            _groovyToast.emit("Imported ${parsed.size} class slots successfully! 🎸")
        }
        return true
    }

    fun loadPreset(presetName: String, replaceExisting: Boolean) {
        val presets = TimetableParser.getSamplePresets()
        val slots = presets[presetName] ?: return
        viewModelScope.launch {
            repository.importParsedSlots(slots, replaceExisting)
            _groovyToast.emit("Loaded '$presetName' timetable! ✌️")
        }
    }

    fun addCourseWithSlots(
        name: String,
        code: String,
        professor: String,
        room: String,
        colorHex: String,
        targetPercent: Int,
        days: List<Int>,
        startTime: String,
        endTime: String
    ) {
        viewModelScope.launch {
            val course = Course(
                name = name,
                code = code,
                professor = professor,
                room = room,
                colorHex = colorHex,
                targetPercent = targetPercent
            )
            val courseId = repository.addCourse(course)
            days.forEach { day ->
                val slot = TimeSlot(
                    courseId = courseId,
                    dayOfWeek = day,
                    startTime = startTime,
                    endTime = endTime,
                    roomOverride = room
                )
                repository.addTimeSlot(slot)
            }
            _groovyToast.emit("Added $name with ${days.size} weekly classes! ⚡")
        }
    }

    fun deleteCourse(courseId: Long) {
        viewModelScope.launch {
            repository.deleteCourse(courseId)
            _groovyToast.emit("Course removed from timetable 🗑️")
        }
    }

    fun deleteTimeSlot(slotId: Long) {
        viewModelScope.launch {
            repository.deleteTimeSlot(slotId)
            _groovyToast.emit("Class time slot deleted 🗑️")
        }
    }
}
