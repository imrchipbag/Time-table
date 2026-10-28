package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.ai.ExtractedTimetableResult
import com.example.data.ai.GeminiTimetableExtractor
import com.example.data.repository.ParsedCourseSlot
import com.example.data.repository.TimetableParser
import com.example.ui.components.GroovyCard
import com.example.ui.theme.MetricDangerRed
import com.example.ui.theme.MetricSuccessGreen
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoBorder
import com.example.ui.theme.MonoBorderBright
import com.example.ui.theme.MonoDangerRed
import com.example.ui.theme.MonoGrey
import com.example.ui.theme.MonoLightGrey
import com.example.ui.theme.MonoSurfaceCard
import com.example.ui.theme.MonoSurfaceElevated
import com.example.ui.theme.MonoWhite
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun UploadTimetableDialog(
    initialStartDate: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
    initialEndDate: String = LocalDate.now().plusMonths(4).format(DateTimeFormatter.ISO_LOCAL_DATE),
    onDismiss: () -> Unit,
    onImportConfirmed: (List<ParsedCourseSlot>, Boolean, String, String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Timetable validity range: user can set start date and end date
    var timetableStartDate by remember { mutableStateOf(initialStartDate) }
    var timetableEndDate by remember { mutableStateOf(initialEndDate) }

    // Completely empty by default - no mock data or pre-seeded entries!
    var showPasteSection by remember { mutableStateOf(false) }
    var pastedText by remember { mutableStateOf("") }
    var replaceExisting by remember { mutableStateOf(true) }

    val editableSlots = remember { mutableStateListOf<ParsedCourseSlot>() }

    // Photo extraction states
    var isExtracting by remember { mutableStateOf(false) }
    var extractionStatusMessage by remember { mutableStateOf<String?>(null) }
    var lastSelectedUri by remember { mutableStateOf<Uri?>(null) }
    var lastCapturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    fun handleResult(result: ExtractedTimetableResult) {
        isExtracting = false
        extractionStatusMessage = result.statusMessage
        if (result.isSuccess && result.slots.isNotEmpty()) {
            if (result.startDate.isNotBlank()) timetableStartDate = result.startDate
            if (result.endDate.isNotBlank()) timetableEndDate = result.endDate
            editableSlots.clear()
            editableSlots.addAll(result.slots)
        }
    }

    fun extractUri(uri: Uri) {
        lastSelectedUri = uri
        lastCapturedBitmap = null
        isExtracting = true
        extractionStatusMessage = "Gemini 3.8 Flash is analyzing timetable photo..."
        coroutineScope.launch {
            val result = GeminiTimetableExtractor.extractFromUri(context, uri)
            handleResult(result)
        }
    }

    fun extractBitmap(bitmap: Bitmap) {
        lastCapturedBitmap = bitmap
        lastSelectedUri = null
        isExtracting = true
        extractionStatusMessage = "Gemini 3.8 Flash is analyzing timetable photo..."
        coroutineScope.launch {
            val result = GeminiTimetableExtractor.extractFromBitmap(bitmap)
            handleResult(result)
        }
    }

    // Gallery photo picker (zero permission photo picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            extractUri(uri)
        }
    }

    // Camera snapshot (zero permission built-in camera contract)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            extractBitmap(bitmap)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MonoSurfaceCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, MonoBorderBright),
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
                .fillMaxHeight(0.96f)
                .testTag("upload_timetable_dialog")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SCHEDULE SETUP",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = MonoWhite
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MonoWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Extract from photo or enter your classes, postings & labs",
                            style = MaterialTheme.typography.bodySmall.copy(color = MonoGrey)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_upload_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MonoLightGrey)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Photo Extraction & Capture Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MonoSurfaceElevated)
                        .border(1.dp, MonoBorderBright, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ADD SCHEDULE THROUGH PHOTO",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp,
                                        color = MonoWhite
                                    )
                                )
                            }
                            Text(
                                text = "Gemini 3.8 Flash",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MetricSuccessGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }

                        // Photo Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Gallery Button
                            Button(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                enabled = !isExtracting,
                                colors = ButtonDefaults.buttonColors(containerColor = MonoSurfaceCard),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MonoBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("pick_gallery_photo_button")
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = MonoWhite, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Gallery", color = MonoWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            // Camera Button
                            Button(
                                onClick = { cameraLauncher.launch(null) },
                                enabled = !isExtracting,
                                colors = ButtonDefaults.buttonColors(containerColor = MonoSurfaceCard),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MonoBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("snap_camera_photo_button")
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = MonoWhite, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Camera", color = MonoWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Photo Preview & Extraction Status Banner
                        if (lastSelectedUri != null || lastCapturedBitmap != null || isExtracting) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MonoSurfaceCard)
                                    .border(0.5.dp, MonoBorder, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Thumbnail
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MonoBlack)
                                ) {
                                    if (lastSelectedUri != null) {
                                        AsyncImage(
                                            model = lastSelectedUri,
                                            contentDescription = "Timetable photo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else if (lastCapturedBitmap != null) {
                                        Image(
                                            bitmap = lastCapturedBitmap!!.asImageBitmap(),
                                            contentDescription = "Timetable photo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    if (isExtracting) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(12.dp),
                                                strokeWidth = 2.dp,
                                                color = MonoWhite
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Extracting with Gemini 3.8 Flash...",
                                                style = MaterialTheme.typography.bodySmall.copy(color = MonoWhite)
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = if (editableSlots.isNotEmpty()) "${editableSlots.size} sessions loaded" else "Scan Complete",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MonoWhite,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }

                                    if (extractionStatusMessage != null) {
                                        Text(
                                            text = extractionStatusMessage!!,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = if (extractionStatusMessage!!.contains("error", ignoreCase = true)) MetricDangerRed else MonoLightGrey,
                                                fontSize = 11.sp
                                            ),
                                            maxLines = 2
                                        )
                                    }
                                }

                                // Retry Button
                                if (!isExtracting && (lastSelectedUri != null || lastCapturedBitmap != null)) {
                                    IconButton(
                                        onClick = {
                                            if (lastSelectedUri != null) extractUri(lastSelectedUri!!)
                                            else if (lastCapturedBitmap != null) extractBitmap(lastCapturedBitmap!!)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Retry extraction", tint = MonoWhite, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Timetable Validity / Active Term Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MonoSurfaceElevated)
                        .border(1.dp, MonoBorderBright, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "SCHEDULE DURATION / TERM",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = MonoWhite
                                )
                            )
                            Text(
                                text = "Applicable Period",
                                style = MaterialTheme.typography.labelSmall.copy(color = MonoLightGrey)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Start Date
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "START DATE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MonoGrey,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MonoSurfaceCard)
                                        .border(1.dp, MonoBorder, RoundedCornerShape(8.dp))
                                        .clickable {
                                            val parts = timetableStartDate.split("-").mapNotNull { it.toIntOrNull() }
                                            val y = parts.getOrElse(0) { LocalDate.now().year }
                                            val m = parts.getOrElse(1) { LocalDate.now().monthValue } - 1
                                            val d = parts.getOrElse(2) { LocalDate.now().dayOfMonth }
                                            DatePickerDialog(context, { _, year, month, dayOfMonth ->
                                                timetableStartDate = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)
                                            }, y, m, d).show()
                                        }
                                        .padding(horizontal = 8.dp, vertical = 8.dp)
                                        .testTag("pick_timetable_start_date")
                                ) {
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = MonoWhite,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = timetableStartDate,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MonoWhite,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }

                            // Applicable Until Date
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "APPLICABLE UNTIL",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MonoGrey,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MonoSurfaceCard)
                                        .border(1.dp, MonoBorder, RoundedCornerShape(8.dp))
                                        .clickable {
                                            val parts = timetableEndDate.split("-").mapNotNull { it.toIntOrNull() }
                                            val y = parts.getOrElse(0) { LocalDate.now().year }
                                            val m = parts.getOrElse(1) { LocalDate.now().monthValue } - 1
                                            val d = parts.getOrElse(2) { LocalDate.now().dayOfMonth }
                                            DatePickerDialog(context, { _, year, month, dayOfMonth ->
                                                timetableEndDate = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)
                                            }, y, m, d).show()
                                        }
                                        .padding(horizontal = 8.dp, vertical = 8.dp)
                                        .testTag("pick_timetable_end_date")
                                ) {
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = MonoWhite,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = timetableEndDate,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MonoWhite,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sessions Editor Section
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header Bar for Sessions with Quick Action Buttons
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SESSIONS (${editableSlots.size})",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = MonoWhite
                                )
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { showPasteSection = !showPasteSection },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (showPasteSection) MonoWhite else MonoSurfaceElevated
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp).testTag("toggle_paste_section_button")
                                ) {
                                    Icon(
                                        Icons.Default.ContentPaste,
                                        contentDescription = null,
                                        tint = if (showPasteSection) MonoBlack else MonoWhite,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Paste Text",
                                        fontSize = 11.sp,
                                        color = if (showPasteSection) MonoBlack else MonoWhite
                                    )
                                }

                                Button(
                                    onClick = {
                                        editableSlots.add(
                                            ParsedCourseSlot(
                                                courseName = "",
                                                courseCode = "",
                                                dayOfWeek = 1,
                                                startTime = "09:00",
                                                endTime = "10:30",
                                                sessionType = "CLASS"
                                            )
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MonoWhite),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp).testTag("add_slot_to_preview_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = MonoBlack, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Session", fontSize = 11.sp, color = MonoBlack, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Optional Quick Text Importer
                    if (showPasteSection) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MonoSurfaceElevated)
                                    .border(1.dp, MonoBorder, RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Paste Timetable Text (Day, Start, End, Name, [Type])",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MonoLightGrey)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = pastedText,
                                        onValueChange = {
                                            pastedText = it
                                            val parsed = TimetableParser.parseTimetableText(it)
                                            editableSlots.clear()
                                            editableSlots.addAll(parsed)
                                        },
                                        placeholder = {
                                            Text(
                                                "e.g. Monday, 09:00, 11:30, Internal Medicine, POSTING\nTuesday, 14:00, 16:00, Microbiology, LAB",
                                                color = MonoGrey,
                                                fontSize = 12.sp
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(95.dp)
                                            .testTag("timetable_csv_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MonoWhite,
                                            unfocusedBorderColor = MonoBorder,
                                            focusedTextColor = MonoWhite,
                                            unfocusedTextColor = MonoWhite,
                                            focusedContainerColor = MonoSurfaceCard,
                                            unfocusedContainerColor = MonoSurfaceCard
                                        ),
                                        textStyle = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    // Empty State if no sessions have been added or extracted yet
                    if (editableSlots.isEmpty()) {
                        item {
                            GroovyCard {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No Sessions Added Yet",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MonoWhite
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Snap or choose a timetable photo above to auto-extract with Gemini 3.8 Flash, or tap '+ Add Session' to configure manually.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MonoGrey),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                photoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MonoSurfaceElevated),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = MonoWhite, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Upload Photo", color = MonoWhite)
                                        }

                                        Button(
                                            onClick = {
                                                editableSlots.add(
                                                    ParsedCourseSlot(
                                                        courseName = "",
                                                        courseCode = "",
                                                        dayOfWeek = 1,
                                                        startTime = "09:00",
                                                        endTime = "10:30",
                                                        sessionType = "CLASS"
                                                    )
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MonoWhite),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.testTag("empty_add_first_session_button")
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = MonoBlack, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Add Manually", color = MonoBlack, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Editable slot cards
                        itemsIndexed(editableSlots) { index, slot ->
                            EditableSlotCard(
                                slot = slot,
                                onUpdate = { updated ->
                                    editableSlots[index] = updated
                                },
                                onDelete = {
                                    editableSlots.removeAt(index)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Replace existing checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { replaceExisting = !replaceExisting }
                ) {
                    Checkbox(
                        checked = replaceExisting,
                        onCheckedChange = { replaceExisting = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MonoWhite,
                            checkmarkColor = MonoBlack,
                            uncheckedColor = MonoGrey
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Replace existing timetable schedule completely",
                        style = MaterialTheme.typography.bodySmall.copy(color = MonoLightGrey)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Apply Button
                Button(
                    onClick = {
                        val validSlots = editableSlots.filter { it.courseName.isNotBlank() }
                        onImportConfirmed(
                            validSlots.ifEmpty { editableSlots.toList() },
                            replaceExisting,
                            timetableStartDate,
                            timetableEndDate
                        )
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MonoWhite),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_import_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = MonoBlack, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (editableSlots.isEmpty()) "SAVE SCHEDULE" else "APPLY SCHEDULE (${editableSlots.size} SESSIONS)",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MonoBlack,
                            fontWeight = FontWeight.Black
                        )
                    )
                }
            }
        }
    }
}

/**
 * Interactive card allowing user to edit day, start/end time, type (CLASS, POSTING, LAB), and subject name.
 */
@Composable
private fun EditableSlotCard(
    slot: ParsedCourseSlot,
    onUpdate: (ParsedCourseSlot) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    val days = listOf(
        1 to "Mon",
        2 to "Tue",
        3 to "Wed",
        4 to "Thu",
        5 to "Fri",
        6 to "Sat",
        7 to "Sun"
    )

    val sessionTypes = listOf("CLASS", "POSTING", "LAB")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MonoSurfaceElevated)
            .border(0.5.dp, MonoBorder, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Row 1: Session Type Selector + Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Session Type Segmented Toggle (CLASS, POSTING, LAB)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MonoSurfaceCard)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    sessionTypes.forEach { type ->
                        val isSelected = slot.sessionType.equals(type, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) MonoWhite else MonoSurfaceCard)
                                .clickable {
                                    onUpdate(slot.copy(sessionType = type))
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (isSelected) MonoBlack else MonoLightGrey
                                )
                            )
                        }
                    }
                }

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Remove Slot",
                        tint = MonoDangerRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Row 2: Subject / Posting / Lab Name Field
            OutlinedTextField(
                value = slot.courseName,
                onValueChange = { onUpdate(slot.copy(courseName = it)) },
                placeholder = {
                    Text(
                        when (slot.sessionType) {
                            "POSTING" -> "e.g. General Surgery Clinical Posting"
                            "LAB" -> "e.g. Microbiology Practical Lab"
                            else -> "e.g. Pathology Lecture"
                        },
                        fontSize = 11.sp,
                        color = MonoGrey
                    )
                },
                label = { Text("Subject / Posting / Lab Name", fontSize = 10.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MonoWhite,
                    unfocusedBorderColor = MonoBorder,
                    focusedTextColor = MonoWhite,
                    unfocusedTextColor = MonoWhite,
                    focusedLabelColor = MonoWhite,
                    unfocusedLabelColor = MonoGrey,
                    focusedContainerColor = MonoSurfaceCard,
                    unfocusedContainerColor = MonoSurfaceCard
                ),
                textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
            )

            // Row 3: Day Selector Pills
            Column {
                Text(
                    text = "DAY OF THE WEEK",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MonoGrey,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(days.size) { i ->
                        val (dayNum, dayLabel) = days[i]
                        val isDaySelected = slot.dayOfWeek == dayNum
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isDaySelected) MonoWhite else MonoSurfaceCard)
                                .border(
                                    0.5.dp,
                                    if (isDaySelected) MonoWhite else MonoBorder,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    onUpdate(slot.copy(dayOfWeek = dayNum))
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isDaySelected) MonoBlack else MonoLightGrey
                                )
                            )
                        }
                    }
                }
            }

            // Row 4: Start Time & End Time with TimePicker dialog triggers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Start Time
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "START TIME",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MonoGrey,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MonoSurfaceCard)
                            .border(1.dp, MonoBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                val parts = slot.startTime.split(":")
                                val initialH = parts.getOrNull(0)?.toIntOrNull() ?: 9
                                val initialM = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                TimePickerDialog(context, { _, hourOfDay, minute ->
                                    val formatted = "%02d:%02d".format(hourOfDay, minute)
                                    onUpdate(slot.copy(startTime = formatted))
                                }, initialH, initialM, true).show()
                            }
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = MonoLightGrey, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = slot.startTime.ifBlank { "09:00" },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MonoWhite,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // End Time
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "END TIME",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MonoGrey,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MonoSurfaceCard)
                            .border(1.dp, MonoBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                val parts = slot.endTime.split(":")
                                val initialH = parts.getOrNull(0)?.toIntOrNull() ?: 10
                                val initialM = parts.getOrNull(1)?.toIntOrNull() ?: 30
                                TimePickerDialog(context, { _, hourOfDay, minute ->
                                    val formatted = "%02d:%02d".format(hourOfDay, minute)
                                    onUpdate(slot.copy(endTime = formatted))
                                }, initialH, initialM, true).show()
                            }
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = MonoLightGrey, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = slot.endTime.ifBlank { "10:30" },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MonoWhite,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}
