package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import coil.compose.AsyncImage
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Videocam
import com.example.R
import com.example.data.model.StudyMaterialEntity
import com.example.data.model.SubjectEnum
import com.example.data.model.SubjectItem
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.AutoStories
import com.example.ui.theme.SleekBlue400
import com.example.ui.theme.SleekBlue600
import com.example.ui.theme.SleekBorderDark
import com.example.ui.theme.SleekGold400
import com.example.ui.theme.SleekGold500
import com.example.ui.theme.SleekNavy800
import com.example.ui.theme.SleekNavy900
import com.example.ui.theme.SleekNavy950
import com.example.ui.theme.TextSecondaryDark

import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.text.format.DateFormat
import java.util.Date
import com.example.data.model.HomeworkReminderEntity
import com.example.ui.components.HomeworkReminderManagementDialog
import com.example.ui.components.FirebaseDatabaseConfigDialog
import androidx.compose.material.icons.filled.Storage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: UserEntity,
    materials: List<StudyMaterialEntity> = emptyList(),
    subjects: List<SubjectItem> = emptyList(),
    onSelectSubject: (SubjectEnum) -> Unit,
    onOpenAdminPanel: (Int) -> Unit = {},
    onOpenAiTutor: () -> Unit,
    onOpenVideo: (StudyMaterialEntity) -> Unit = {},
    onOpenDocument: (StudyMaterialEntity) -> Unit = {},
    onOpenLibrary: () -> Unit = {},
    onLogout: () -> Unit,
    averageScore: Float,
    totalQuizzes: Int,
    savedLibraryCount: Int = 0,
    homeworkReminders: List<HomeworkReminderEntity> = emptyList(),
    onAddReminder: (String, String, Long) -> Unit = { _, _, _ -> },
    onUpdateReminder: (HomeworkReminderEntity) -> Unit = {},
    onDeleteReminder: (HomeworkReminderEntity) -> Unit = {},
    onShowIntro: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isAdmin = user.role == UserRole.ADMIN.name
    val displaySubjects = if (subjects.isNotEmpty()) subjects else SubjectEnum.values().map { it.toSubjectItem() }
    var showRemindersDialog by remember { mutableStateOf(false) }
    var showFirebaseDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOpenAiTutor,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .testTag("fab_ai_tutor")
                    .padding(bottom = 8.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Tutor",
                        tint = SleekGold400
                    )
                },
                text = {
                    Text(
                        text = "AI Study Tutor",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    )
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // App Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.size(44.dp),
                            color = Color.Transparent,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        ) {
                            AsyncImage(
                                model = R.drawable.app_logo,
                                contentDescription = "Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "STUDY WELL",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = "IGCSE Grade 10",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Sleek Role Badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isAdmin) SleekGold400.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                            border = BorderStroke(
                                1.dp,
                                if (isAdmin) SleekGold400.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.School,
                                    contentDescription = null,
                                    tint = if (isAdmin) SleekGold500 else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isAdmin) "Admin" else "Student",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAdmin) SleekGold500 else MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // App Intro Walkthrough Button
                        IconButton(
                            onClick = onShowIntro,
                            modifier = Modifier.testTag("home_app_intro_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "App Intro & Walkthrough",
                                tint = SleekBlue400
                            )
                        }

                        // Upgraded Firebase Cloud Sync Status Button (Admin Account Only)
                        if (isAdmin) {
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { showFirebaseDialog = true },
                                modifier = Modifier.testTag("home_firebase_config_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = "Firebase Cloud Database",
                                    tint = SleekBlue400
                                )
                            }
                        }

                        // Log Out Button
                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier.testTag("open_library_header_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Log Out",
                                tint = SleekGold400
                            )
                        }
                    }
                }
            }

            // Sleek Welcome Hero Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SleekNavy950
                    ),
                    border = BorderStroke(1.dp, SleekBorderDark)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(
                            model = R.drawable.hero_banner,
                            contentDescription = "Hero",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(165.dp),
                            contentScale = ContentScale.Crop,
                            alpha = 0.25f
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(22.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SleekGold400.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = if (isAdmin) "ADMINISTRATION" else "GRADE 10 PORTAL",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SleekGold400,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Welcome back, ${user.displayName.ifBlank { user.username }}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (isAdmin)
                                    "Administrator access active: Upload curriculum videos, textbook chapters, and past-paper revision notes."
                                else
                                    "Ace your Grade 10 IGCSE exams with video lectures, interactive readers, and dynamic AI quizzes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8),
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Learning progress overview
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White.copy(alpha = 0.08f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Quiz,
                                            contentDescription = null,
                                            tint = SleekGold400,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "$totalQuizzes Completed",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                if (totalQuizzes > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.White.copy(alpha = 0.08f),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Psychology,
                                                contentDescription = null,
                                                tint = Color(0xFF34D399),
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${averageScore.toInt()}% Avg",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Homework & Exam Reminders Quick Access Card (ABOVE Student Library)
            item {
                val pendingReminders = homeworkReminders.filter { !it.isFinished }
                val nextReminder = pendingReminders.minByOrNull { it.dueDateTimestamp }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clickable { showRemindersDialog = true }
                        .testTag("home_homework_reminders_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SleekNavy900
                    ),
                    border = BorderStroke(1.dp, SleekBlue400.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SleekBlue600.copy(alpha = 0.2f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Alarm,
                                        contentDescription = null,
                                        tint = SleekBlue400,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Homework & Reminders",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (pendingReminders.isNotEmpty()) Color(0xFF10B981).copy(alpha = 0.25f) else Color.Gray.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = if (pendingReminders.isNotEmpty()) "${pendingReminders.size} Active" else "All Clear",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (pendingReminders.isNotEmpty()) Color(0xFF34D399) else Color(0xFF94A3B8),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = if (nextReminder != null) {
                                        val timeStr = DateFormat.format("MMM dd 'at' hh:mm a", Date(nextReminder.dueDateTimestamp)).toString()
                                        "Next: ${nextReminder.subjectName} • Due $timeStr"
                                    } else {
                                        "Pick dates from calendar & 12h AM/PM for 24h & 2h alerts"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = SleekBlue400,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Student Library Quick Access Card (Access edited PDFs & downloaded exams)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clickable { onOpenLibrary() }
                        .testTag("home_student_library_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SleekNavy900
                    ),
                    border = BorderStroke(1.dp, SleekGold400.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SleekGold400.copy(alpha = 0.15f),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = null,
                                        tint = SleekGold400,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Student Library",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SleekBlue600.copy(alpha = 0.35f)
                                    ) {
                                        Text(
                                            text = if (savedLibraryCount > 0) "$savedLibraryCount PDFs" else "Vault",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SleekBlue400,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Edited notes, annotations & downloaded official exam papers",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = SleekGold400,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Admin Upload Banner if Admin
            if (isAdmin) {
                item {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                            .clickable { onOpenAdminPanel(0) }
                            .testTag("admin_upload_panel_banner"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = SleekGold400.copy(alpha = 0.08f)
                        ),
                        border = BorderStroke(1.dp, SleekGold400.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = SleekGold400,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.UploadFile,
                                        contentDescription = "Upload",
                                        tint = SleekNavy950,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Admin Upload & Content Hub",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Upload videos, textbook PDFs & revision notes",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = SleekGold500
                            )
                        }
                    }
                }
            }

            // Section Title
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "IGCSE Curriculum Subjects",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.2.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "${displaySubjects.size} Active Courses",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Dynamic Subjects Cards
            items(displaySubjects, key = { it.id }) { subject ->
                val (vCount, bCount, nCount) = remember(materials, subject.id) {
                    var v = 0
                    var b = 0
                    var n = 0
                    for (m in materials) {
                        if (m.subjectId == subject.id) {
                            when (m.materialType) {
                                "VIDEO" -> v++
                                "BOOK" -> b++
                                "NOTE" -> n++
                            }
                        }
                    }
                    Triple(v, b, n)
                }

                val subjectIcon: ImageVector = when (subject.id.uppercase()) {
                    "PHYSICS" -> Icons.Default.Science
                    "ENGLISH" -> Icons.AutoMirrored.Filled.MenuBook
                    "MATH" -> Icons.Default.Calculate
                    "BIOLOGY" -> Icons.Default.Biotech
                    "CHEMISTRY" -> Icons.Default.Science
                    "ARABIC_OL", "ARABIC" -> Icons.Default.AutoStories
                    "ICT" -> Icons.Default.Computer
                    else -> Icons.Default.School
                }

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clickable { onSelectSubject(SubjectEnum.fromId(subject.id)) }
                        .testTag("subject_card_${subject.id.lowercase()}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = subject.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (subject.isCustom) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "Admin",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Section Badges inside Subject Card without numbers
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(
                                    "Videos",
                                    "Books",
                                    "Notes",
                                    "Sheets",
                                    "Exams"
                                ).forEach { sectionName ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                    ) {
                                        Text(
                                            text = sectionName,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open ${subject.title}",
                            tint = subject.color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Quick Add Subject for Admin if logged in
            if (isAdmin) {
                item {
                    OutlinedCard(
                        onClick = { onOpenAdminPanel(2) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                            .testTag("admin_add_subject_card"),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Subject",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "+ Add New Subject / Manage",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Add Math, English, Biology, or custom courses",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Subtle logout button placed at the bottom so it's not prominently seen
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(
                        onClick = onLogout,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("discreet_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sign out of your Study Well session",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    }

    if (showRemindersDialog) {
        HomeworkReminderManagementDialog(
            reminders = homeworkReminders,
            onAddReminder = onAddReminder,
            onUpdateReminder = onUpdateReminder,
            onDeleteReminder = onDeleteReminder,
            onDismiss = { showRemindersDialog = false }
        )
    }

    if (isAdmin && showFirebaseDialog) {
        FirebaseDatabaseConfigDialog(
            onDismiss = { showFirebaseDialog = false }
        )
    }
}
