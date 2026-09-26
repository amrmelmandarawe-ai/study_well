package com.example.ui.components

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.HomeworkReminderEntity
import com.example.ui.theme.*
import com.example.utils.HomeworkNotificationHelper
import java.util.*

enum class ReminderFilterTab {
    PENDING,
    FINISHED,
    ALL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworkReminderManagementDialog(
    reminders: List<HomeworkReminderEntity>,
    onAddReminder: (subject: String, topic: String, dueTimestamp: Long) -> Unit,
    onUpdateReminder: (HomeworkReminderEntity) -> Unit,
    onDeleteReminder: (HomeworkReminderEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(ReminderFilterTab.PENDING) }

    val pendingReminders = remember(reminders) { reminders.filter { !it.isFinished }.sortedBy { it.dueDateTimestamp } }
    val finishedReminders = remember(reminders) { reminders.filter { it.isFinished }.sortedByDescending { it.dueDateTimestamp } }

    // Notification Permission Launcher for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Notification permission granted! 🔔", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Please allow notifications in system settings to receive due date alerts.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 700.dp),
            shape = RoundedCornerShape(24.dp),
            color = SleekNavy950,
            border = BorderStroke(1.dp, SleekGold400.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SleekGold400.copy(alpha = 0.2f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = SleekGold400,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Homework Reminders",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "24h & 2h alerts before due date",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_reminders_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.7f))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Bar: Add + Test Notification
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekBlue600),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("dialog_open_add_reminder_btn"),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Homework", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    return@OutlinedButton
                                }
                            }
                            HomeworkNotificationHelper.sendHomeworkNotification(
                                context = context,
                                title = "🔔 Homework Alert System Active!",
                                message = "StudyWell alarm service is active. You will receive 24h & 2h reminders automatically.",
                                notificationId = 9999
                            )
                            Toast.makeText(context, "Test notification sent! Check your notification bar.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekGold400),
                        border = BorderStroke(1.dp, SleekGold400.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("test_notification_btn")
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test Alert", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Two Dedicated Sections Selector (Pending vs Finished)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Pending Section Tab
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = ReminderFilterTab.PENDING },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedTab == ReminderFilterTab.PENDING) SleekBlue600 else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⏳ Pending",
                                fontWeight = if (selectedTab == ReminderFilterTab.PENDING) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (selectedTab == ReminderFilterTab.PENDING) Color.White else Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = if (selectedTab == ReminderFilterTab.PENDING) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "${pendingReminders.size}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    // Finished Section Tab
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = ReminderFilterTab.FINISHED },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedTab == ReminderFilterTab.FINISHED) Color(0xFF10B981) else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✅ Finished",
                                fontWeight = if (selectedTab == ReminderFilterTab.FINISHED) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (selectedTab == ReminderFilterTab.FINISHED) Color.White else Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = if (selectedTab == ReminderFilterTab.FINISHED) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "${finishedReminders.size}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    // All Tab
                    Surface(
                        modifier = Modifier
                            .weight(0.7f)
                            .clickable { selectedTab = ReminderFilterTab.ALL },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedTab == ReminderFilterTab.ALL) SleekNavy900 else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "All (${reminders.size})",
                                fontWeight = if (selectedTab == ReminderFilterTab.ALL) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (selectedTab == ReminderFilterTab.ALL) SleekGold400 else Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section Lists
                when (selectedTab) {
                    ReminderFilterTab.PENDING -> {
                        if (pendingReminders.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircleOutline,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(54.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "No Pending Homework 🎉",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "All your assignments are done! Tap '+ Add Homework' to set new deadlines.",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(pendingReminders, key = { it.id }) { reminder ->
                                    HomeworkItemCard(
                                        reminder = reminder,
                                        onUpdate = onUpdateReminder,
                                        onDelete = onDeleteReminder
                                    )
                                }
                            }
                        }
                    }

                    ReminderFilterTab.FINISHED -> {
                        if (finishedReminders.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.AssignmentLate,
                                        contentDescription = null,
                                        tint = Color.Gray.copy(alpha = 0.5f),
                                        modifier = Modifier.size(54.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "No Finished Homework Yet",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Mark assignments as finished to track your study progress.",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(finishedReminders, key = { it.id }) { reminder ->
                                    HomeworkItemCard(
                                        reminder = reminder,
                                        onUpdate = onUpdateReminder,
                                        onDelete = onDeleteReminder
                                    )
                                }
                            }
                        }
                    }

                    ReminderFilterTab.ALL -> {
                        if (reminders.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.EventAvailable,
                                        contentDescription = null,
                                        tint = Color.Gray.copy(alpha = 0.5f),
                                        modifier = Modifier.size(54.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "No homework reminders set",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Tap '+ Add Homework' to pick date & time from calendar",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (pendingReminders.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = "⏳ Pending Homework (${pendingReminders.size})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = SleekGold400,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }
                                    items(pendingReminders, key = { "pending_${it.id}" }) { reminder ->
                                        HomeworkItemCard(
                                            reminder = reminder,
                                            onUpdate = onUpdateReminder,
                                            onDelete = onDeleteReminder
                                        )
                                    }
                                }

                                if (finishedReminders.isNotEmpty()) {
                                    item {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "✅ Finished Homework (${finishedReminders.size})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF10B981),
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }
                                    items(finishedReminders, key = { "finished_${it.id}" }) { reminder ->
                                        HomeworkItemCard(
                                            reminder = reminder,
                                            onUpdate = onUpdateReminder,
                                            onDelete = onDeleteReminder
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddHomeworkDatePickerDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { subject, topic, dueTimestamp ->
                onAddReminder(subject, topic, dueTimestamp)
                showAddDialog = false
                val remaining = dueTimestamp - System.currentTimeMillis()
                val msg = when {
                    remaining > 24 * 3600 * 1000L -> "Reminder set for $subject! 24h & 2h alarms scheduled."
                    remaining > 2 * 3600 * 1000L -> "Reminder set for $subject! 2h & deadline alarms scheduled (due in under 24h)."
                    else -> "Reminder set for $subject! Deadline alarm scheduled (due in under 2h)."
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun AddHomeworkDatePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (subject: String, topic: String, dueTimestamp: Long) -> Unit
) {
    val context = LocalContext.current
    var subjectInput by remember { mutableStateOf("") }
    var topicInput by remember { mutableStateOf("") }

    // Calendar & 12-Hour AM/PM Time State
    val selectedCalendar = remember {
        Calendar.getInstance().apply {
            // Default to tomorrow at 04:00 PM
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 16)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    var dueTimestampState by remember { mutableStateOf(selectedCalendar.timeInMillis) }

    val quickSubjects = listOf("Mathematics", "Physics", "Chemistry", "Biology", "English", "ICT", "Arabic")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = SleekBlue600)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Set Homework Reminder", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select due date from the calendar and time in 12-hour (AM/PM) format. You will automatically receive alerts 24 hours and 2 hours before the deadline.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Quick Subject Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickSubjects) { sub ->
                        FilterChip(
                            selected = subjectInput == sub,
                            onClick = { subjectInput = sub },
                            label = { Text(sub, fontSize = 11.sp) }
                        )
                    }
                }

                // Subject Name Field
                OutlinedTextField(
                    value = subjectInput,
                    onValueChange = { subjectInput = it },
                    label = { Text("Subject Name *") },
                    placeholder = { Text("e.g. Physics / Mathematics") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_reminder_subject_field"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Topic Field
                OutlinedTextField(
                    value = topicInput,
                    onValueChange = { topicInput = it },
                    label = { Text("Topic / Chapter (Optional)") },
                    placeholder = { Text("e.g. Past Paper 4 Revision / Kinematics") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_reminder_topic_field"),
                    shape = RoundedCornerShape(12.dp)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Date and Time Pickers
                Text(
                    text = "🗓️ Due Date & 12-Hour AM/PM Time",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Date Picker Button (Calendar)
                    OutlinedButton(
                        onClick = {
                            val cal = Calendar.getInstance().apply { timeInMillis = dueTimestampState }
                            val year = cal.get(Calendar.YEAR)
                            val month = cal.get(Calendar.MONTH)
                            val day = cal.get(Calendar.DAY_OF_MONTH)

                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    cal.set(Calendar.YEAR, y)
                                    cal.set(Calendar.MONTH, m)
                                    cal.set(Calendar.DAY_OF_MONTH, d)
                                    dueTimestampState = cal.timeInMillis
                                },
                                year,
                                month,
                                day
                            ).apply {
                                datePicker.minDate = System.currentTimeMillis() - 1000
                                show()
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("select_calendar_date_btn"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        val dateFormatted = DateFormat.format("MMM dd, yyyy", Date(dueTimestampState)).toString()
                        Text(dateFormatted, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }

                    // Time Picker Button (12-Hour AM/PM)
                    OutlinedButton(
                        onClick = {
                            val cal = Calendar.getInstance().apply { timeInMillis = dueTimestampState }
                            val hour = cal.get(Calendar.HOUR_OF_DAY)
                            val minute = cal.get(Calendar.MINUTE)

                            TimePickerDialog(
                                context,
                                { _, h, m ->
                                    cal.set(Calendar.HOUR_OF_DAY, h)
                                    cal.set(Calendar.MINUTE, m)
                                    cal.set(Calendar.SECOND, 0)
                                    dueTimestampState = cal.timeInMillis
                                },
                                hour,
                                minute,
                                false // is24HourView = false for 12-Hour AM/PM
                            ).show()
                        },
                        modifier = Modifier.weight(1f).testTag("select_12hr_time_btn"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        val timeFormatted = DateFormat.format("hh:mm a", Date(dueTimestampState)).toString()
                        Text(timeFormatted, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }

                // Summary Pill
                val fullFormatted = DateFormat.format("EEEE, MMM dd, yyyy 'at' hh:mm a", Date(dueTimestampState)).toString()
                val diffMillis = dueTimestampState - System.currentTimeMillis()
                val hoursRemaining = diffMillis / (1000 * 60 * 60)
                val daysRemaining = hoursRemaining / 24

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "⏰ Selected Deadline (12-Hour):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = fullFormatted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (diffMillis > 0) {
                                if (daysRemaining >= 1) "⏳ Due in $daysRemaining day(s), ${hoursRemaining % 24} hr(s)"
                                else "⏳ Due in $hoursRemaining hour(s)"
                            } else {
                                "⚠️ Due date is in the past! Please select a future time."
                            },
                            fontSize = 11.sp,
                            color = if (diffMillis > 0) Color(0xFF10B981) else Color(0xFFEF4444),
                            fontWeight = FontWeight.Medium
                        )
                        if (diffMillis > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(4.dp))
                            if (diffMillis > 24 * 3600 * 1000L) {
                                val time24hStr = DateFormat.format("MMM dd 'at' hh:mm a", Date(dueTimestampState - 24 * 3600 * 1000L)).toString()
                                Text("🔔 24-Hour Alert: $time24hStr", fontSize = 10.sp, color = SleekGold400)
                            } else {
                                Text("ℹ️ 24-Hour Alert: Skipped (due in less than 24h)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                            }
                            if (diffMillis > 2 * 3600 * 1000L) {
                                val time2hStr = DateFormat.format("MMM dd 'at' hh:mm a", Date(dueTimestampState - 2 * 3600 * 1000L)).toString()
                                Text("⏰ 2-Hour Alert: $time2hStr", fontSize = 10.sp, color = SleekBlue400)
                            } else {
                                Text("ℹ️ 2-Hour Alert: Skipped (due in less than 2h)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (subjectInput.isBlank()) {
                        Toast.makeText(context, "Please enter a subject name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (dueTimestampState <= System.currentTimeMillis()) {
                        Toast.makeText(context, "Please choose a future due date & time", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    onConfirm(subjectInput.trim(), topicInput.trim(), dueTimestampState)
                },
                modifier = Modifier.testTag("submit_homework_reminder_btn")
            ) {
                Text("Set Reminder", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun HomeworkItemCard(
    reminder: HomeworkReminderEntity,
    onUpdate: (HomeworkReminderEntity) -> Unit,
    onDelete: (HomeworkReminderEntity) -> Unit
) {
    val now = System.currentTimeMillis()
    val dueFormatted = DateFormat.format("MMM dd, yyyy 'at' hh:mm a", Date(reminder.dueDateTimestamp)).toString()
    val diff = reminder.dueDateTimestamp - now
    val hoursRemaining = diff / (1000 * 60 * 60)
    val daysRemaining = hoursRemaining / 24

    val statusText = when {
        reminder.isFinished -> "Finished ✅"
        diff < 0 -> "Overdue ⚠️"
        daysRemaining >= 1 -> "Due in $daysRemaining d, ${hoursRemaining % 24} h"
        else -> "Due in $hoursRemaining hours"
    }

    val statusColor = when {
        reminder.isFinished -> Color(0xFF10B981)
        diff < 0 -> Color(0xFFEF4444)
        hoursRemaining <= 2 -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isFinished) Color(0xFF0F172A).copy(alpha = 0.6f) else SleekNavy900
        ),
        border = BorderStroke(1.dp, if (reminder.isFinished) Color(0xFF10B981).copy(alpha = 0.35f) else SleekBlue600.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = reminder.subjectName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (reminder.isFinished) Color(0xFF94A3B8) else Color.White,
                            textDecoration = if (reminder.isFinished) TextDecoration.LineThrough else TextDecoration.None,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (reminder.topicName.isNotBlank()) {
                            Text(
                                text = " • ${reminder.topicName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (reminder.isFinished) Color(0xFF64748B) else Color(0xFF94A3B8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📅 $dueFormatted",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (reminder.isFinished) Color(0xFF64748B) else SleekBlue400,
                        fontSize = 11.sp
                    )
                }

                IconButton(
                    onClick = { onDelete(reminder) },
                    modifier = Modifier.size(28.dp).testTag("delete_reminder_${reminder.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = Color.Gray.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                OutlinedButton(
                    onClick = {
                        onUpdate(reminder.copy(isFinished = !reminder.isFinished))
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (reminder.isFinished) Color(0xFF10B981).copy(alpha = 0.5f) else SleekBlue600),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (reminder.isFinished) Color(0xFF10B981) else SleekBlue600
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("toggle_finished_reminder_${reminder.id}")
                ) {
                    Icon(
                        imageVector = if (reminder.isFinished) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (reminder.isFinished) "Completed ✅" else "Mark Finished", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
