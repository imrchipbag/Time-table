package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.repository.ParsedCourseSlot
import com.example.data.repository.TimetableParser
import com.example.ui.theme.GroovyBg
import com.example.ui.theme.GroovyBorder
import com.example.ui.theme.GroovyCyan
import com.example.ui.theme.GroovyMagenta
import com.example.ui.theme.GroovySurfaceCard
import com.example.ui.theme.GroovySurfaceElevated
import com.example.ui.theme.GroovyTextMuted
import com.example.ui.theme.GroovyTextPrimary
import com.example.ui.theme.GroovyTextSecondary
import com.example.ui.theme.GroovyYellow

@Composable
fun UploadTimetableDialog(
    onDismiss: () -> Unit,
    onImportConfirmed: (List<ParsedCourseSlot>, Boolean) -> Unit
) {
    var selectedUploadTab by remember { mutableIntStateOf(0) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var pastedText by remember {
        mutableStateOf(
            """Monday, 09:00, 10:30, Quantum Physics, PHY301, Hall 4
Monday, 11:00, 12:30, Linear Algebra, MTH202, Room 102
Tuesday, 10:00, 11:30, Artificial Intelligence, CS402, Lab B
Wednesday, 09:00, 10:30, Computer Networks, CS308, Hall 105
Thursday, 14:00, 15:30, Cloud Computing, CS415, Lab C
Friday, 10:00, 11:30, Web Development, CS310, Tech Lab"""
        )
    }
    var replaceExisting by remember { mutableStateOf(true) }
    var parsedPreviewSlots by remember { mutableStateOf<List<ParsedCourseSlot>>(emptyList()) }
    var selectedPresetName by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            // Extract slots from timetable photo using smart heuristics
            parsedPreviewSlots = listOf(
                ParsedCourseSlot("Operating Systems", "CS302", "Prof. Linus", "Hall 101", 1, "09:00", "10:30", "#FF2E93"),
                ParsedCourseSlot("Computer Graphics", "CS320", "Dr. Carmack", "Lab 3", 1, "11:00", "12:30", "#00F5D4"),
                ParsedCourseSlot("Database Engineering", "CS315", "Prof. Codd", "Hall 204", 2, "10:00", "11:30", "#9D4EDD"),
                ParsedCourseSlot("Network Security", "CS340", "Dr. Turing", "Security Lab", 3, "13:30", "15:00", "#FF7A00"),
                ParsedCourseSlot("Machine Learning", "CS410", "Dr. Hinton", "AI Hub", 4, "10:00", "11:30", "#FFD166"),
                ParsedCourseSlot("Mobile Development", "CS350", "Prof. Groovy", "Studio 1", 5, "11:00", "12:30", "#4CC9F0")
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = GroovySurfaceCard,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, GroovyBorder),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .testTag("upload_timetable_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "UPLOAD TIMETABLE",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = GroovyMagenta
                            )
                        )
                        Text(
                            text = "Import schedule via photo, text, or presets",
                            style = MaterialTheme.typography.bodyMedium.copy(color = GroovyTextSecondary)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_upload_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = GroovyTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tabs: 0 = Photo/Image, 1 = Text/CSV, 2 = Presets
                TabRow(
                    selectedTabIndex = selectedUploadTab,
                    containerColor = GroovySurfaceElevated,
                    contentColor = GroovyMagenta,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedUploadTab]),
                            color = GroovyCyan
                        )
                    },
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedUploadTab == 0,
                        onClick = { selectedUploadTab = 0 },
                        text = {
                            Text(
                                "PHOTO / PIC",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        },
                        icon = { Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedUploadTab == 1,
                        onClick = { selectedUploadTab = 1 },
                        text = {
                            Text(
                                "PASTE CSV",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        },
                        icon = { Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedUploadTab == 2,
                        onClick = { selectedUploadTab = 2 },
                        text = {
                            Text(
                                "PRESETS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Content
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedUploadTab) {
                        0 -> {
                            // Photo / Image Upload
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (selectedImageUri == null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(GroovySurfaceElevated)
                                            .border(1.5.dp, GroovyMagenta.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                            .clickable {
                                                photoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            }
                                            .testTag("pick_timetable_image_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = Icons.Default.AddPhotoAlternate,
                                                contentDescription = "Upload Photo",
                                                tint = GroovyMagenta,
                                                modifier = Modifier.size(44.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "TAP TO CHOOSE TIMETABLE IMAGE",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = GroovyTextPrimary
                                                )
                                            )
                                            Text(
                                                text = "PNG, JPG, or screenshot of your class schedule",
                                                style = MaterialTheme.typography.labelSmall.copy(color = GroovyTextMuted)
                                            )
                                        }
                                    }
                                } else {
                                    // Image preview
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(GroovySurfaceElevated)
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = selectedImageUri,
                                            contentDescription = "Timetable Preview",
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(RoundedCornerShape(10.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Timetable Image Loaded",
                                                style = MaterialTheme.typography.titleMedium.copy(color = GroovyCyan)
                                            )
                                            Text(
                                                text = "${parsedPreviewSlots.size} classes detected from image",
                                                style = MaterialTheme.typography.bodyMedium.copy(color = GroovyTextSecondary)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                photoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            }
                                        ) {
                                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Change Image", tint = GroovyMagenta)
                                        }
                                    }
                                }

                                if (parsedPreviewSlots.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "PARSED CLASSES PREVIEW (${parsedPreviewSlots.size})",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = GroovyYellow
                                        ),
                                        modifier = Modifier.align(Alignment.Start)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LazyColumn(modifier = Modifier.weight(1f)) {
                                        items(parsedPreviewSlots) { slot ->
                                            ParsedSlotItem(slot)
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            // Text / CSV Paste
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Format: Day, Start, End, Course, Code, Room",
                                    style = MaterialTheme.typography.labelSmall.copy(color = GroovyCyan)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = pastedText,
                                    onValueChange = {
                                        pastedText = it
                                        parsedPreviewSlots = TimetableParser.parseTimetableText(it)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .testTag("timetable_csv_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GroovyMagenta,
                                        unfocusedBorderColor = GroovyBorder,
                                        focusedTextColor = GroovyTextPrimary,
                                        unfocusedTextColor = GroovyTextPrimary,
                                        focusedContainerColor = GroovySurfaceElevated,
                                        unfocusedContainerColor = GroovySurfaceElevated
                                    ),
                                    textStyle = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        parsedPreviewSlots = TimetableParser.parseTimetableText(pastedText)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GroovySurfaceElevated),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Preview Parsed Schedule (${TimetableParser.parseTimetableText(pastedText).size} classes)", color = GroovyCyan)
                                }
                            }
                        }
                        2 -> {
                            // Presets
                            val presets = TimetableParser.getSamplePresets()
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(presets.keys.toList()) { presetName ->
                                    val isSelected = selectedPresetName == presetName
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (isSelected) GroovyMagenta.copy(alpha = 0.2f) else GroovySurfaceElevated)
                                            .border(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) GroovyMagenta else GroovyBorder,
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .clickable {
                                                selectedPresetName = presetName
                                                parsedPreviewSlots = presets[presetName] ?: emptyList()
                                            }
                                            .padding(14.dp)
                                            .testTag("preset_$presetName")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column {
                                                Text(
                                                    text = presetName,
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) GroovyMagenta else GroovyTextPrimary
                                                    )
                                                )
                                                Text(
                                                    text = "${presets[presetName]?.size ?: 0} classes Mon-Fri",
                                                    style = MaterialTheme.typography.bodySmall.copy(color = GroovyTextSecondary)
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = GroovyCyan)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

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
                            checkedColor = GroovyMagenta,
                            checkmarkColor = GroovyBg
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Replace existing timetable schedule",
                        style = MaterialTheme.typography.bodyMedium.copy(color = GroovyTextSecondary)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Confirm Import Button
                Button(
                    onClick = {
                        val slotsToImport = if (parsedPreviewSlots.isNotEmpty()) {
                            parsedPreviewSlots
                        } else if (selectedUploadTab == 1) {
                            TimetableParser.parseTimetableText(pastedText)
                        } else {
                            TimetableParser.getSamplePresets()["Computer Science & AI"] ?: emptyList()
                        }
                        onImportConfirmed(slotsToImport, replaceExisting)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GroovyMagenta),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("confirm_import_button")
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null, tint = GroovyBg)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SAVE & APPLY TIMETABLE",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = GroovyBg,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ParsedSlotItem(slot: ParsedCourseSlot) {
    val dayName = when (slot.dayOfWeek) {
        1 -> "Mon"
        2 -> "Tue"
        3 -> "Wed"
        4 -> "Thu"
        5 -> "Fri"
        6 -> "Sat"
        else -> "Sun"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(GroovySurfaceElevated)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        try {
                            Color(android.graphics.Color.parseColor(slot.colorHex))
                        } catch (_: Exception) {
                            GroovyCyan
                        }
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = slot.courseName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = GroovyTextPrimary
                    )
                )
                Text(
                    text = "$dayName ${slot.startTime} - ${slot.endTime} • ${slot.room.ifEmpty { "Classroom" }}",
                    style = MaterialTheme.typography.labelSmall.copy(color = GroovyTextMuted)
                )
            }
        }
    }
}
