package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdminSelectedFile
import com.example.data.model.QuizAttemptEntity
import com.example.data.model.StudyMaterialEntity
import com.example.data.model.SubjectEnum
import com.example.data.model.SubjectItem
import com.example.data.model.UserActivityEntity
import com.example.data.model.UserEntity
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Science
import com.example.ui.theme.SleekBlue400
import com.example.ui.theme.SleekBlue600
import com.example.ui.theme.SleekGold400
import com.example.ui.theme.SleekNavy800
import com.example.ui.theme.SleekNavy900
import com.example.ui.theme.SleekNavy950
import com.example.ui.theme.sleekTextFieldColors
import com.example.utils.FileUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.ui.components.FirebaseDatabaseConfigDialog
import com.example.data.firebase.FirebaseDatabaseService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUploadScreen(
    materials: List<StudyMaterialEntity>,
    subjects: List<SubjectItem> = emptyList(),
    users: List<UserEntity> = emptyList(),
    activities: List<UserActivityEntity> = emptyList(),
    quizAttempts: List<QuizAttemptEntity> = emptyList(),
    currentUser: UserEntity? = null,
    initialSharedFiles: List<AdminSelectedFile> = emptyList(),
    onClearSharedFiles: () -> Unit = {},
    onUpload: (
        subjectId: String,
        materialType: String,
        title: String,
        topic: String,
        description: String,
        contentUrl: String,
        documentContent: String,
        durationOrPages: String,
        onComplete: (Boolean, String) -> Unit
    ) -> Unit,
    onDelete: (StudyMaterialEntity) -> Unit,
    onAddSubject: (
        title: String,
        syllabusCode: String,
        description: String,
        topics: List<String>,
        colorHex: Long,
        onComplete: (Boolean, String) -> Unit
    ) -> Unit = { _, _, _, _, _, _ -> },
    onDeleteSubject: (String) -> Unit = {},
    onClearAllMaterials: () -> Unit = {},
    onBack: () -> Unit,
    initialTab: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isUserAdmin = currentUser?.role == "ADMIN"

    if (!isUserAdmin) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .testTag("admin_upload_screen_denied"),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Admin Access",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Access Denied",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Administrative Access Required",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "This hub contains restricted curriculum management utilities and student tracking telemetry. Please log in with an Administrator account to upload documents or manage subjects.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF94A3B8),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SleekBlue600,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp).fillMaxWidth(0.6f)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Return to Home", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val displaySubjects = if (subjects.isNotEmpty()) subjects else SubjectEnum.values().map { it.toSubjectItem() }

    val scope = rememberCoroutineScope()
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) } // 0: Upload Form, 1: Library, 2: Subjects, 3: Student Tracker

    // Form fields
    var selectedSubjectId by remember { mutableStateOf(displaySubjects.firstOrNull()?.id ?: "PHYSICS") }
    val currentSelectedSubject = displaySubjects.find { it.id == selectedSubjectId } ?: displaySubjects.first()

    var selectedType by remember { mutableStateOf("VIDEO") } // "VIDEO", "BOOK", "NOTE"
    var title by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var contentUrl by remember { mutableStateOf("") }
    var documentContent by remember { mutableStateOf("") }
    var durationOrPages by remember { mutableStateOf("") }

    // Dialog States
    var materialToDelete by remember { mutableStateOf<StudyMaterialEntity?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var showFirebaseConfigDialog by remember { mutableStateOf(false) }
    var isPushingCloudDatabase by remember { mutableStateOf(false) }
    var isPullingCloudDatabase by remember { mutableStateOf(false) }
    var subjectToDelete by remember { mutableStateOf<SubjectItem?>(null) }

    // New Subject Dialog State
    var newSubjectTitle by remember { mutableStateOf("") }
    var newSubjectCode by remember { mutableStateOf("") }
    var newSubjectDesc by remember { mutableStateOf("") }
    var newSubjectTopics by remember { mutableStateOf("") }
    var newSubjectColorHex by remember { mutableStateOf(0xFF3B82F6L) }

    // Backup & Vault State (Max Level)
    var backupDiagnostics by remember { mutableStateOf<com.example.utils.PersistentDataBackupHelper.BackupDiagnostics?>(null) }
    var isBackupOperationActive by remember { mutableStateOf(false) }
    var backupOperationTitle by remember { mutableStateOf("") }
    var backupOperationProgress by remember { mutableStateOf(0f) }
    var backupOperationMessage by remember { mutableStateOf("") }
    var showBackupDiagnosticsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val db = com.example.data.db.AppDatabase.getDatabase(context)
        backupDiagnostics = com.example.utils.PersistentDataBackupHelper.getBackupDiagnostics(context, db)
    }

    // 1. Full Standalone Archive Launcher (.studywell / .zip)
    val exportZipArchiveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            isBackupOperationActive = true
            backupOperationTitle = "Creating Full .studywell Archive"
            backupOperationProgress = 0f
            backupOperationMessage = "Initializing archive streams..."
            scope.launch {
                val db = com.example.data.db.AppDatabase.getDatabase(context)
                val outStream = context.contentResolver.openOutputStream(uri, "wt")
                if (outStream != null) {
                    val success = com.example.utils.PersistentDataBackupHelper.createFullZipBackupStream(
                        context,
                        db,
                        outStream
                    ) { prog, msg ->
                        backupOperationProgress = prog
                        backupOperationMessage = msg
                    }
                    backupDiagnostics = com.example.utils.PersistentDataBackupHelper.getBackupDiagnostics(context, db)
                    isBackupOperationActive = false
                    if (success) {
                        Toast.makeText(context, "Full .studywell backup archive saved successfully!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Failed creating backup archive.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    isBackupOperationActive = false
                    Toast.makeText(context, "Unable to write to selected file location.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // 2. JSON Manifest Export Launcher
    val exportJsonBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val db = com.example.data.db.AppDatabase.getDatabase(context)
                val json = com.example.utils.PersistentDataBackupHelper.generateBackupJson(context, db)
                val success = com.example.utils.PersistentDataBackupHelper.writeBackupToUri(context, uri, json)
                backupDiagnostics = com.example.utils.PersistentDataBackupHelper.getBackupDiagnostics(context, db)
                if (success) {
                    Toast.makeText(context, "Backup JSON manifest exported successfully!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Failed writing backup JSON file.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // 3. Multi-Format Universal Restore Launcher (.studywell, .zip, .json)
    val restoreUniversalBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            isBackupOperationActive = true
            backupOperationTitle = "Restoring from Backup Package"
            backupOperationProgress = 0f
            backupOperationMessage = "Reading archive package..."
            scope.launch {
                val db = com.example.data.db.AppDatabase.getDatabase(context)
                val inStream = context.contentResolver.openInputStream(uri)
                if (inStream != null) {
                    val result = com.example.utils.PersistentDataBackupHelper.restoreArchiveFromStream(
                        context,
                        db,
                        inStream
                    ) { prog, msg ->
                        backupOperationProgress = prog
                        backupOperationMessage = msg
                    }
                    backupDiagnostics = com.example.utils.PersistentDataBackupHelper.getBackupDiagnostics(context, db)
                    isBackupOperationActive = false
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                } else {
                    isBackupOperationActive = false
                    Toast.makeText(context, "Unable to open backup file.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Selected file state from device / Google Drive
    var pickedFileName by remember { mutableStateOf<String?>(null) }
    var pickedFileSize by remember { mutableStateOf<String?>(null) }
    var isGoogleDriveLinkDetected by remember { mutableStateOf(false) }

    // Multi-PDF selection states
    var selectedFiles by remember(initialSharedFiles) { mutableStateOf<List<AdminSelectedFile>>(initialSharedFiles) }
    var isUploadingBatch by remember { mutableStateOf(false) }
    var uploadBatchProgress by remember { mutableStateOf("") }
    var isPreparingFiles by remember { mutableStateOf(false) }
    var preparingProgress by remember { mutableStateOf("") }

    // When files are shared from WhatsApp / external apps, prepare form fields automatically
    LaunchedEffect(initialSharedFiles) {
        if (initialSharedFiles.isNotEmpty()) {
            selectedTab = 0 // Switch directly to the Upload Form tab
            val firstFile = initialSharedFiles.first()
            if (title.isBlank()) {
                title = firstFile.customTitle
            }
            if (firstFile.isPdf && selectedType == "VIDEO") {
                selectedType = "BOOK" // Default to Book/PDF for shared documents
            }
            if (topic.isBlank()) {
                topic = "General Curriculum"
            }
            if (durationOrPages.isBlank()) {
                durationOrPages = firstFile.durationOrPages
            }
            if (contentUrl.isBlank()) {
                contentUrl = firstFile.contentUrl
            }
            if (documentContent.isBlank()) {
                documentContent = firstFile.documentContent
            }
        }
    }

    // Student Tracker Search & Filters
    var studentSearchQuery by remember { mutableStateOf("") }
    var selectedTrackerFilter by remember { mutableStateOf("ALL") } // "ALL", "VIDEOS", "DOCS", "QUIZZES"
    var expandedUsernames by remember { mutableStateOf(setOf<String>()) }
    var showLiveFeed by remember { mutableStateOf(false) }

    // File picker launcher supporting multiple selections asynchronously in parallel
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri>? ->
        if (!uris.isNullOrEmpty()) {
            isPreparingFiles = true
            preparingProgress = "Processing ${uris.size} selected file(s) in parallel..."
            scope.launch(Dispatchers.IO) {
                val deferreds = uris.mapIndexed { index, uri ->
                    async(Dispatchers.IO) {
                        // Persist URI read permission
                        runCatching {
                            context.contentResolver.takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        }

                        try {
                            val meta = FileUtils.getFileMeta(context, uri)
                            var fileContentUrl = uri.toString()
                            var fileDurationOrPages = ""
                            var fileDocContent = ""

                            if (meta.isPdf) {
                                val copiedPdf = FileUtils.copyUriToInternalStorage(context, uri, meta.name)
                                fileContentUrl = copiedPdf?.absolutePath ?: uri.toString()
                                val totalPages = meta.pageCount
                                fileDurationOrPages = if (totalPages > 0) "$totalPages Pages • ${meta.sizeFormatted}" else meta.sizeFormatted
                            } else {
                                fileDurationOrPages = if (selectedType == "VIDEO") {
                                    "Video (${meta.sizeFormatted})"
                                } else {
                                    "${meta.sizeFormatted} Doc"
                                }
                                val ext = meta.name.substringAfterLast(".", "").lowercase()
                                if (ext in listOf("txt", "md", "markdown", "csv", "json", "rtf", "html")) {
                                    val text = FileUtils.readTextContent(context, uri)
                                    if (text.isNotBlank()) {
                                        fileDocContent = text
                                    }
                                }
                            }

                            AdminSelectedFile(
                                uri = uri,
                                originalName = meta.name,
                                sizeFormatted = meta.sizeFormatted,
                                isPdf = meta.isPdf,
                                customTitle = FileUtils.cleanFileNameForTitle(meta.name),
                                contentUrl = fileContentUrl,
                                documentContent = fileDocContent,
                                durationOrPages = fileDurationOrPages,
                                customTopic = topic.ifBlank { currentSelectedSubject.defaultTopics.firstOrNull() ?: "" }
                            )
                        } catch (e: Exception) {
                            Log.e("AdminUpload", "Error processing file in parallel: ${e.message}", e)
                            null
                        }
                    }
                }

                val processedFiles = deferreds.awaitAll().filterNotNull()

                withContext(Dispatchers.Main) {
                    selectedFiles = selectedFiles + processedFiles
                    if (selectedType == "VIDEO" && processedFiles.any { it.isPdf }) {
                        selectedType = "BOOK"
                    }
                    isPreparingFiles = false
                    preparingProgress = ""
                    Toast.makeText(context, "Added ${processedFiles.size} file(s) to upload queue", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_upload_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = SleekGold400,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = SleekNavy950,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Admin Hub & Student Tracking",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("admin_panel_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showFirebaseConfigDialog = true },
                        modifier = Modifier.testTag("admin_firebase_config_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = "Firebase Database Settings",
                            tint = SleekBlue400
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Upload", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Library (${materials.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Subjects (${displaySubjects.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Students (${users.size})", fontWeight = FontWeight.Bold) }
                )
            }

            when (selectedTab) {
                0 -> {
                    // Upload Form
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = SleekNavy950
                            ),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = SleekBlue600.copy(alpha = 0.2f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = SleekBlue400,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Upload Videos, PDFs & Notes",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Pick files from Google Drive / Local Storage or paste Drive links directly.",
                                        color = Color(0xFF94A3B8),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        // Prominent WhatsApp / Shared Document Banner
                        if (selectedFiles.isNotEmpty() && initialSharedFiles.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF064E3B).copy(alpha = 0.9f)
                                ),
                                border = BorderStroke(1.5.dp, Color(0xFF10B981))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFF10B981).copy(alpha = 0.25f),
                                                modifier = Modifier.size(38.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Share,
                                                    contentDescription = null,
                                                    tint = Color(0xFF34D399),
                                                    modifier = Modifier.padding(8.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Document Shared from WhatsApp",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "Select subject and section below to publish to curriculum",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFFA7F3D0)
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                onClearSharedFiles()
                                                selectedFiles = emptyList()
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Dismiss",
                                                tint = Color(0xFF94A3B8),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    selectedFiles.forEach { file ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF042F2C),
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (file.isPdf) Icons.Default.PictureAsPdf else Icons.Default.Description,
                                                    contentDescription = null,
                                                    tint = Color(0xFF34D399),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = file.originalName,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = file.durationOrPages.ifBlank { file.sizeFormatted },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFFA7F3D0),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Quick direct upload button
                                    Button(
                                        onClick = {
                                            if (selectedFiles.isEmpty()) return@Button
                                            val targetSub = currentSelectedSubject.title
                                            val sectionName = when (selectedType) {
                                                "BOOK" -> "Textbooks"
                                                "NOTE" -> "Notes"
                                                "SHEET" -> "Worksheets"
                                                "MARKSCHEME" -> "Mark Schemes"
                                                "EXAM" -> "Past Papers"
                                                else -> "Videos"
                                            }

                                            isUploadingBatch = true
                                            uploadBatchProgress = "Uploading to $targetSub ($sectionName)..."

                                            fun uploadSharedFileAt(idx: Int) {
                                                if (idx >= selectedFiles.size) {
                                                    isUploadingBatch = false
                                                    uploadBatchProgress = ""
                                                    selectedFiles = emptyList()
                                                    onClearSharedFiles()
                                                    selectedTab = 1
                                                    Toast.makeText(context, "Uploaded to $targetSub > $sectionName successfully!", Toast.LENGTH_LONG).show()
                                                    return
                                                }

                                                val cur = selectedFiles[idx]
                                                val finalTitle = cur.customTitle.ifBlank { title.ifBlank { FileUtils.cleanFileNameForTitle(cur.originalName) } }
                                                val finalTopic = cur.customTopic.ifBlank { topic.ifBlank { "General Curriculum" } }
                                                val finalDuration = cur.durationOrPages.ifBlank { if (selectedType == "VIDEO") "15 mins" else "10 pages" }

                                                onUpload(
                                                    selectedSubjectId,
                                                    selectedType,
                                                    finalTitle,
                                                    finalTopic,
                                                    description.ifBlank { "Uploaded via WhatsApp share" },
                                                    cur.contentUrl,
                                                    cur.documentContent,
                                                    finalDuration
                                                ) { success, msg ->
                                                    if (success) {
                                                        uploadSharedFileAt(idx + 1)
                                                    } else {
                                                        isUploadingBatch = false
                                                        uploadBatchProgress = ""
                                                        Toast.makeText(context, "Upload failed: $msg", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            }

                                            uploadSharedFileAt(0)
                                        },
                                        enabled = !isUploadingBatch,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (isUploadingBatch) {
                                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Uploading...", color = Color.White)
                                        } else {
                                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            val sectionLabel = when (selectedType) {
                                                "BOOK" -> "Textbooks"
                                                "NOTE" -> "Revision Notes"
                                                "SHEET" -> "Worksheets"
                                                "MARKSCHEME" -> "Mark Schemes"
                                                "EXAM" -> "Past Papers"
                                                else -> "Videos"
                                            }
                                            Text(
                                                text = "Upload to ${currentSelectedSubject.title} > $sectionLabel",
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Text(
                            text = "1. Select Target Subject:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            displaySubjects.forEach { sub ->
                                FilterChip(
                                    selected = selectedSubjectId == sub.id,
                                    onClick = {
                                        selectedSubjectId = sub.id
                                        if (topic.isBlank() && sub.defaultTopics.isNotEmpty()) {
                                            topic = sub.defaultTopics.first()
                                        }
                                    },
                                    label = { Text(sub.title, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = sub.color.copy(alpha = 0.2f),
                                        selectedLabelColor = sub.color
                                    )
                                )
                            }
                        }

                        Text(
                            text = "2. Material Type:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "VIDEO" to "Video Lecture",
                                "BOOK" to "Textbook / Book",
                                "NOTE" to "Revision Notes",
                                "SHEET" to "Practice Sheet",
                                "MARKSCHEME" to "Mark Scheme",
                                "EXAM" to "Exam / Past Paper"
                            ).forEach { (typeKey, label) ->
                                OutlinedCard(
                                    modifier = Modifier
                                        .width(130.dp)
                                        .clickable { selectedType = typeKey },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        width = if (selectedType == typeKey) 2.dp else 1.dp,
                                        color = if (selectedType == typeKey) SleekBlue400 else Color(0xFF334155)
                                    ),
                                    colors = CardDefaults.outlinedCardColors(
                                        containerColor = if (selectedType == typeKey) SleekBlue600.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp, horizontal = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = when (typeKey) {
                                                "VIDEO" -> Icons.Default.Videocam
                                                "BOOK" -> Icons.Default.MenuBook
                                                "SHEET" -> Icons.Default.Assignment
                                                "MARKSCHEME" -> Icons.Default.FactCheck
                                                "EXAM" -> Icons.Default.Quiz
                                                else -> Icons.Default.Description
                                            },
                                            contentDescription = null,
                                            tint = if (selectedType == typeKey) SleekBlue400 else Color(0xFF94A3B8),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (selectedType == typeKey) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedType == typeKey) Color.White else Color(0xFF94A3B8),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "3. File Source (Upload PDF / Video / Drive Files):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Primary PDF Upload Button
                            Button(
                                onClick = {
                                    selectedType = "BOOK"
                                    filePickerLauncher.launch(arrayOf("application/pdf", "*/*"))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("admin_pick_pdf_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SleekBlue600,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = SleekGold400,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Upload PDF",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            // Secondary All Files / Drive Picker Button
                            OutlinedButton(
                                onClick = {
                                    val mimeTypes = when (selectedType) {
                                        "VIDEO" -> arrayOf("video/*")
                                        "BOOK" -> arrayOf("application/pdf", "text/*", "*/*")
                                        else -> arrayOf("application/pdf", "text/*", "text/plain", "*/*")
                                    }
                                    filePickerLauncher.launch(mimeTypes)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("admin_pick_file_button"),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, SleekBlue400.copy(alpha = 0.6f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = SleekGold400,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Drive & Files",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }
                        }

                        // Display selected files list
                        if (selectedFiles.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = SleekNavy950
                                ),
                                border = BorderStroke(1.dp, Color(0xFF334155))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Selected Files Queue (${selectedFiles.size})",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = SleekGold400
                                        )
                                        TextButton(
                                            onClick = { selectedFiles = emptyList() },
                                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                                        ) {
                                            Text("Clear All", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    selectedFiles.forEachIndexed { index, file ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = SleekBlue600.copy(alpha = 0.12f)
                                            ),
                                            border = BorderStroke(1.dp, SleekBlue400.copy(alpha = 0.3f))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.CheckCircle,
                                                            contentDescription = null,
                                                            tint = Color(0xFF10B981),
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = "#${index + 1}: ${file.originalName}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }

                                                    IconButton(
                                                        onClick = {
                                                            selectedFiles = selectedFiles.filterIndexed { idx, _ -> idx != index }
                                                        },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Clear,
                                                            contentDescription = "Remove file",
                                                            tint = Color(0xFF94A3B8),
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))

                                                OutlinedTextField(
                                                    value = file.customTitle,
                                                    onValueChange = { newTitle ->
                                                        selectedFiles = selectedFiles.mapIndexed { idx, f ->
                                                            if (idx == index) f.copy(customTitle = newTitle) else f
                                                        }
                                                    },
                                                    label = { Text("Rename PDF Display Title *", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SleekGold400) },
                                                    colors = sleekTextFieldColors(),
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth().testTag("admin_rename_input_$index")
                                                )

                                                Spacer(modifier = Modifier.height(6.dp))

                                                OutlinedTextField(
                                                    value = file.customTopic,
                                                    onValueChange = { newTopic ->
                                                        selectedFiles = selectedFiles.mapIndexed { idx, f ->
                                                            if (idx == index) f.copy(customTopic = newTopic) else f
                                                        }
                                                    },
                                                    label = { Text("Specific Topic / Chapter *", fontSize = 10.sp) },
                                                    colors = sleekTextFieldColors(),
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Text(
                            text = "4. Content Details:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Content Title *") },
                            placeholder = { Text("e.g. 02. Laws of Thermodynamics & Heat Capacity") },
                            colors = sleekTextFieldColors(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_input_title"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = topic,
                            onValueChange = { topic = it },
                            label = { Text("Topic / Chapter Name *") },
                            placeholder = { Text("e.g. Thermal Physics") },
                            colors = sleekTextFieldColors(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_input_topic"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = durationOrPages,
                            onValueChange = { durationOrPages = it },
                            label = { Text(if (selectedType == "VIDEO") "Video Duration (e.g. 18 mins / 45 MB)" else "Pages or Size (e.g. 24 pages / 2.5 MB)") },
                            colors = sleekTextFieldColors(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_input_duration"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = contentUrl,
                            onValueChange = { inputUrl ->
                                val (isDrive, formattedUrl) = FileUtils.parseGoogleDriveLink(inputUrl)
                                isGoogleDriveLinkDetected = isDrive
                                contentUrl = formattedUrl
                            },
                            label = { Text("Media Stream / Google Drive Share Link (Optional)") },
                            placeholder = { Text("Paste Google Drive link, MP4 stream, or picked URI") },
                            colors = sleekTextFieldColors(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_input_url"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (isGoogleDriveLinkDetected) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SleekGold400.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, SleekGold400.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CloudDone,
                                        contentDescription = null,
                                        tint = SleekGold400,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Google Drive share link detected & converted for direct in-app access",
                                        color = SleekGold400,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Syllabus Summary & Key Learning Objectives") },
                            placeholder = { Text("Covers Core & Extended Cambridge IGCSE learning goals...") },
                            colors = sleekTextFieldColors(),
                            maxLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_input_description"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = documentContent,
                            onValueChange = { documentContent = it },
                            label = { Text("Document Text / Notes / Revision Points") },
                            placeholder = { Text("Enter or paste lecture transcript, summary notes, formulas, or past-paper tips...") },
                            colors = sleekTextFieldColors(),
                            minLines = 4,
                            maxLines = 8,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_input_content"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (selectedFiles.isNotEmpty()) {
                                    isUploadingBatch = true
                                    
                                    fun uploadBatch(index: Int) {
                                        if (index >= selectedFiles.size) {
                                            isUploadingBatch = false
                                            uploadBatchProgress = ""
                                            selectedFiles = emptyList()
                                            title = ""
                                            topic = ""
                                            description = ""
                                            selectedTab = 1
                                            Toast.makeText(context, "All files uploaded successfully!", Toast.LENGTH_LONG).show()
                                            return
                                        }

                                        val file = selectedFiles[index]
                                        uploadBatchProgress = "Uploading file ${index + 1} of ${selectedFiles.size}:\n${file.customTitle}"

                                        onUpload(
                                            selectedSubjectId,
                                            selectedType,
                                            file.customTitle.trim().ifBlank { FileUtils.cleanFileNameForTitle(file.originalName) },
                                            file.customTopic.trim().ifBlank { topic.trim().ifBlank { "General" } },
                                            description.trim(),
                                            file.contentUrl.trim(),
                                            file.documentContent.trim(),
                                            file.durationOrPages.ifBlank { "10 pages" }
                                        ) { success, msg ->
                                            if (success) {
                                                uploadBatch(index + 1)
                                            } else {
                                                isUploadingBatch = false
                                                uploadBatchProgress = ""
                                                Toast.makeText(context, "Failed at file ${file.originalName}: $msg", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }

                                    uploadBatch(0)
                                } else {
                                    if (title.isBlank()) {
                                        Toast.makeText(context, "Please enter a title", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (topic.isBlank()) {
                                        Toast.makeText(context, "Please enter a topic", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    val finalDuration = durationOrPages.ifBlank {
                                        if (selectedType == "VIDEO") "15 mins" else "10 pages"
                                    }

                                    onUpload(
                                        selectedSubjectId,
                                        selectedType,
                                        title.trim(),
                                        topic.trim(),
                                        description.trim(),
                                        contentUrl.trim(),
                                        documentContent.trim(),
                                        finalDuration
                                    ) { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        if (success) {
                                            title = ""
                                            topic = ""
                                            description = ""
                                            contentUrl = ""
                                            documentContent = ""
                                            durationOrPages = ""
                                            pickedFileName = null
                                            pickedFileSize = null
                                            isGoogleDriveLinkDetected = false
                                            selectedTab = 1
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("admin_submit_upload_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SleekBlue600,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedFiles.isNotEmpty()) "Upload Batch (${selectedFiles.size} Files)" else "Upload to Cambridge Syllabus",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                1 -> {
                    // Manage Materials List
                    var manageMaterialTypeFilter by remember { mutableStateOf("ALL") }
                    val displayMaterials = if (manageMaterialTypeFilter == "ALL") {
                        materials
                    } else {
                        materials.filter { it.materialType == manageMaterialTypeFilter }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        // ==========================================
                        // MAX-LEVEL DATA VAULT & COMPLETE BACKUP SUITE
                        // ==========================================
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = SleekNavy950
                            ),
                            border = BorderStroke(1.5.dp, SleekGold400.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Header & Diagnostics Summary
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = SleekGold400.copy(alpha = 0.15f),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Security,
                                                contentDescription = null,
                                                tint = SleekGold400,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "StudyWell Data Vault",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = SleekGold400
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFF10B981).copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "MAX LEVEL",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFF10B981),
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Full snapshot + raw PDF archives (.studywell / .zip) with zero data loss guarantee.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                val db = com.example.data.db.AppDatabase.getDatabase(context)
                                                backupDiagnostics = com.example.utils.PersistentDataBackupHelper.getBackupDiagnostics(context, db)
                                                Toast.makeText(context, "Storage diagnostics refreshed.", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = "Refresh",
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Real-time Metrics Pill Strip
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SleekNavy900,
                                        border = BorderStroke(1.dp, Color(0xFF334155))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Storage, contentDescription = null, tint = SleekGold400, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${backupDiagnostics?.totalMaterials ?: materials.size} Materials",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SleekNavy900,
                                        border = BorderStroke(1.dp, Color(0xFF334155))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${backupDiagnostics?.totalPhysicalFiles ?: 0} PDFs (${backupDiagnostics?.formattedFilesSize ?: "0 KB"})",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SleekNavy900,
                                        border = BorderStroke(1.dp, Color(0xFF334155))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.History, contentDescription = null, tint = SleekBlue400, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = backupDiagnostics?.formattedLastBackupDate?.take(22) ?: "Ready",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // TIER 1: Full Standalone Package Actions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val dateStr = java.text.SimpleDateFormat("yyyyMMdd_HHmm", java.util.Locale.US).format(java.util.Date())
                                            exportZipArchiveLauncher.launch("StudyWell_FullArchive_$dateStr.studywell")
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SleekGold400,
                                            contentColor = SleekNavy950
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1.2f),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                    ) {
                                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Export .studywell Archive", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            isBackupOperationActive = true
                                            backupOperationTitle = "Packaging Archive for Share"
                                            backupOperationProgress = 0f
                                            backupOperationMessage = "Compressing database & PDF documents..."
                                            scope.launch {
                                                val db = com.example.data.db.AppDatabase.getDatabase(context)
                                                val success = com.example.utils.PersistentDataBackupHelper.shareFullBackupPackage(context, db)
                                                backupDiagnostics = com.example.utils.PersistentDataBackupHelper.getBackupDiagnostics(context, db)
                                                isBackupOperationActive = false
                                                if (!success) {
                                                    Toast.makeText(context, "Could not create shareable archive package.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SleekBlue600,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(0.9f),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Share Archive", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // TIER 2: Fast Local Persistence & Smart Auto-Restore
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                val db = com.example.data.db.AppDatabase.getDatabase(context)
                                                val success = com.example.utils.PersistentDataBackupHelper.performPersistentBackup(context, db)
                                                backupDiagnostics = com.example.utils.PersistentDataBackupHelper.getBackupDiagnostics(context, db)
                                                if (success) {
                                                    Toast.makeText(context, "Backup snapshot saved to Downloads/StudyWellBackup!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "No materials to backup or storage unavailable.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SleekNavy800,
                                            contentColor = SleekGold400
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Save to Downloads", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    Button(
                                        onClick = {
                                            scope.launch {
                                                val db = com.example.data.db.AppDatabase.getDatabase(context)
                                                val count = com.example.utils.PersistentDataBackupHelper.autoRestoreIfAvailable(context, db)
                                                backupDiagnostics = com.example.utils.PersistentDataBackupHelper.getBackupDiagnostics(context, db)
                                                if (count > 0) {
                                                    Toast.makeText(context, "Successfully restored $count materials from device backup!", Toast.LENGTH_LONG).show()
                                                } else {
                                                    Toast.makeText(context, "All materials in device storage are already loaded.", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SleekNavy800,
                                            contentColor = Color(0xFF10B981)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Auto-Restore Device", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // TIER 3: Universal Import, JSON Manifest & Inspection
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            restoreUniversalBackupLauncher.launch(
                                                arrayOf(
                                                    "application/zip",
                                                    "application/octet-stream",
                                                    "application/json",
                                                    "*/*"
                                                )
                                            )
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Import Archive/File", fontSize = 10.5.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val dateStr = java.text.SimpleDateFormat("yyyyMMdd_HHmm", java.util.Locale.US).format(java.util.Date())
                                            exportJsonBackupLauncher.launch("StudyWell_Manifest_$dateStr.json")
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Export JSON", fontSize = 10.5.sp)
                                    }

                                    OutlinedButton(
                                        onClick = { showBackupDiagnosticsDialog = true },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(0.7f),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Details", fontSize = 10.5.sp)
                                    }
                                }
                            }
                        }

                        // Upgraded Firebase Cloud Database Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .testTag("admin_firebase_cloud_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SleekNavy950),
                            border = BorderStroke(1.dp, Color(0xFF1E3A8A))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = SleekBlue600.copy(alpha = 0.25f),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Storage,
                                                    contentDescription = null,
                                                    tint = SleekBlue400,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Firebase Cloud Database",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Cloud Firestore Real-Time Sync",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SleekBlue400
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF0F2E22),
                                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.6f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(Color(0xFF34D399), CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Cloud Active",
                                                color = Color(0xFF34D399),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                isPushingCloudDatabase = true
                                                val db = com.example.data.db.AppDatabase.getDatabase(context)
                                                val summary = FirebaseDatabaseService.pushAllToFirestore(context, db)
                                                isPushingCloudDatabase = false
                                                Toast.makeText(context, summary.message, Toast.LENGTH_LONG).show()
                                            }
                                        },
                                        enabled = !isPushingCloudDatabase && !isPullingCloudDatabase,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SleekBlue600),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        if (isPushingCloudDatabase) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(13.dp),
                                                strokeWidth = 2.dp,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Pushing...", fontSize = 11.sp)
                                        } else {
                                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Push Cloud", fontSize = 11.sp)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            scope.launch {
                                                isPullingCloudDatabase = true
                                                val db = com.example.data.db.AppDatabase.getDatabase(context)
                                                val summary = FirebaseDatabaseService.pullAllFromFirestore(context, db)
                                                isPullingCloudDatabase = false
                                                Toast.makeText(context, summary.message, Toast.LENGTH_LONG).show()
                                            }
                                        },
                                        enabled = !isPushingCloudDatabase && !isPullingCloudDatabase,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        if (isPullingCloudDatabase) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(13.dp),
                                                strokeWidth = 2.dp,
                                                color = SleekBlue400
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Pulling...", fontSize = 11.sp)
                                        } else {
                                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Pull Cloud", fontSize = 11.sp)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { showFirebaseConfigDialog = true },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(0.8f),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Config", fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        // Filter Chips Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "ALL" to "All (${materials.size})",
                                "VIDEO" to "Videos",
                                "BOOK" to "Books",
                                "NOTE" to "Notes",
                                "SHEET" to "Sheets",
                                "MARKSCHEME" to "Mark Schemes",
                                "EXAM" to "Exams"
                            ).forEach { (typeKey, label) ->
                                FilterChip(
                                    selected = manageMaterialTypeFilter == typeKey,
                                    onClick = { manageMaterialTypeFilter = typeKey },
                                    label = { Text(label, fontSize = 12.sp) }
                                )
                            }
                        }

                        // Top Header Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${displayMaterials.size} Materials Shown",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (materials.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = { showClearAllDialog = true },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("admin_clear_all_materials_button")
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Remove All Materials", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (displayMaterials.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No study materials match this filter.",
                                        color = Color(0xFF94A3B8),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Switch to 'Upload' tab to upload videos, books, notes, sheets, or exams.",
                                        color = Color(0xFF64748B),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(displayMaterials, key = { it.id }) { mat ->
                                    val sub = displaySubjects.find { it.id == mat.subjectId } ?: SubjectEnum.fromId(mat.subjectId).toSubjectItem()
                                    ElevatedCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.elevatedCardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = sub.color.copy(alpha = 0.12f),
                                                    modifier = Modifier.size(42.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = when (mat.materialType) {
                                                            "VIDEO" -> Icons.Default.Videocam
                                                            "BOOK" -> Icons.Default.MenuBook
                                                            "SHEET" -> Icons.Default.Assignment
                                                            "EXAM" -> Icons.Default.Quiz
                                                            else -> Icons.Default.Description
                                                        },
                                                        contentDescription = null,
                                                        tint = sub.color,
                                                        modifier = Modifier.padding(10.dp)
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(12.dp))

                                                Column {
                                                    Text(
                                                        text = mat.title,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1
                                                    )
                                                    Text(
                                                        text = "${sub.title} • ${mat.materialType} • ${mat.durationOrPages}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    if (mat.contentUrl.isNotBlank()) {
                                                        Text(
                                                            text = if (mat.contentUrl.startsWith("content://")) "📁 Local / Drive File Attached" else "🔗 Drive / Web URL Linked",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = SleekGold400
                                                        )
                                                    }
                                                }
                                            }

                                            IconButton(
                                                onClick = { materialToDelete = mat },
                                                modifier = Modifier.testTag("delete_material_${mat.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Subjects Management Tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Header and Add Subject Button
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SleekNavy950),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Curriculum Subjects",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Manage core subjects & create custom courses (Math, English, Biology, etc.)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Button(
                                    onClick = { showAddSubjectDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = SleekBlue600),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("admin_open_add_subject_dialog_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Subject", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(displaySubjects, key = { it.id }) { subject ->
                                val subjectMaterialsCount = materials.count { it.subjectId == subject.id }
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = subject.color.copy(alpha = 0.15f),
                                                border = BorderStroke(1.dp, subject.color.copy(alpha = 0.3f)),
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Icon(
                                                    imageVector = when (subject.id.uppercase()) {
                                                        "PHYSICS" -> Icons.Default.Science
                                                        "ENGLISH" -> Icons.Default.MenuBook
                                                        "MATH" -> Icons.Default.Calculate
                                                        "BIOLOGY" -> Icons.Default.Biotech
                                                        "CHEMISTRY" -> Icons.Default.Science
                                                        "ARABIC_OL", "ARABIC" -> Icons.Default.AutoStories
                                                        "ICT" -> Icons.Default.Computer
                                                        else -> Icons.Default.School
                                                    },
                                                    contentDescription = null,
                                                    tint = subject.color,
                                                    modifier = Modifier.padding(12.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(14.dp))

                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = subject.title,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = MaterialTheme.colorScheme.surfaceVariant
                                                    ) {
                                                        Text(
                                                            text = subject.syllabusCode,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontSize = 10.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    }
                                                    if (subject.isCustom) {
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = SleekGold400.copy(alpha = 0.15f)
                                                        ) {
                                                            Text(
                                                                text = "Custom",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontSize = 9.sp,
                                                                color = SleekGold400,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(2.dp))

                                                Text(
                                                    text = "${subject.description} • $subjectMaterialsCount Materials",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                if (subject.defaultTopics.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "Topics: " + subject.defaultTopics.take(3).joinToString(", "),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color(0xFF64748B),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }

                                        if (subject.isCustom || displaySubjects.size > 1) {
                                            IconButton(
                                                onClick = { subjectToDelete = subject },
                                                modifier = Modifier.testTag("delete_subject_${subject.id.lowercase()}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete Subject",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Student Tracker & Activity Logs
                    val filteredUsers = users.filter { user ->
                        val matchesSearch = studentSearchQuery.isBlank() ||
                                user.displayName.contains(studentSearchQuery, ignoreCase = true) ||
                                user.username.contains(studentSearchQuery, ignoreCase = true)
                        matchesSearch
                    }

                    val totalMaterialsViewed = activities.count { it.actionType != "COMPLETED_QUIZ" }
                    val totalQuizzesTaken = quizAttempts.size
                    val overallAvgScore = if (quizAttempts.isNotEmpty()) {
                        quizAttempts.map { it.percentage }.average().toFloat()
                    } else 0f

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Overview Stats Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SleekNavy950),
                                border = BorderStroke(1.dp, Color(0xFF334155))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = CircleShape,
                                                color = SleekGold400.copy(alpha = 0.2f),
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Analytics,
                                                    contentDescription = null,
                                                    tint = SleekGold400,
                                                    modifier = Modifier.padding(8.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "Student Engagement Analytics",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        AdminMetricPill(
                                            title = "Students",
                                            value = "${users.size}",
                                            icon = Icons.Default.People,
                                            accentColor = SleekBlue400,
                                            modifier = Modifier.weight(1f)
                                        )
                                        AdminMetricPill(
                                            title = "Viewed Items",
                                            value = "$totalMaterialsViewed",
                                            icon = Icons.Default.Visibility,
                                            accentColor = Color(0xFF10B981),
                                            modifier = Modifier.weight(1f)
                                        )
                                        AdminMetricPill(
                                            title = "Quizzes",
                                            value = "$totalQuizzesTaken",
                                            icon = Icons.Default.Quiz,
                                            accentColor = SleekGold400,
                                            modifier = Modifier.weight(1f)
                                        )
                                        AdminMetricPill(
                                            title = "Avg Score",
                                            value = "${overallAvgScore.toInt()}%",
                                            icon = Icons.Default.AssignmentTurnedIn,
                                            accentColor = Color(0xFFA855F7),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        // Search & Live Feed Toggle Bar
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = studentSearchQuery,
                                    onValueChange = { studentSearchQuery = it },
                                    placeholder = { Text("Search by student name or username...") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Search, contentDescription = null, tint = SleekBlue400)
                                    },
                                    trailingIcon = {
                                        if (studentSearchQuery.isNotBlank()) {
                                            IconButton(onClick = { studentSearchQuery = "" }) {
                                                Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = Color(0xFF94A3B8))
                                            }
                                        }
                                    },
                                    colors = sleekTextFieldColors(),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("admin_search_students_input"),
                                    shape = RoundedCornerShape(14.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        FilterChip(
                                            selected = !showLiveFeed,
                                            onClick = { showLiveFeed = false },
                                            label = { Text("By User (${filteredUsers.size})", fontSize = 12.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = SleekBlue600.copy(alpha = 0.25f),
                                                selectedLabelColor = SleekBlue400
                                            )
                                        )
                                        FilterChip(
                                            selected = showLiveFeed,
                                            onClick = { showLiveFeed = true },
                                            label = { Text("Live Activity Stream (${activities.size})", fontSize = 12.sp) },
                                            leadingIcon = {
                                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp))
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = SleekGold400.copy(alpha = 0.25f),
                                                selectedLabelColor = SleekGold400
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        if (showLiveFeed) {
                            // Live Chronological Feed of all students
                            if (activities.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No user activity recorded yet. As students view videos and open study files, their activity appears here in real-time.",
                                            color = Color(0xFF94A3B8),
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                items(activities, key = { it.id }) { act ->
                                    ActivityLogItem(activity = act)
                                }
                            }
                        } else {
                            // Student Profile Cards with Activity Details
                            if (filteredUsers.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No matching students found.",
                                            color = Color(0xFF94A3B8),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            } else {
                                items(filteredUsers, key = { it.username }) { user ->
                                    val userActs = remember(activities, user.username) { activities.filter { it.username == user.username } }
                                    val userAttempts = remember(quizAttempts, user.username) { quizAttempts.filter { it.username == user.username } }
                                    val videosViewed = remember(userActs) { userActs.count { it.actionType == "VIEWED_VIDEO" } }
                                    val docsRead = remember(userActs) { userActs.count { it.actionType == "READ_DOCUMENT" } }
                                    val quizCount = userAttempts.size
                                    val avgScore = remember(userAttempts) {
                                        if (userAttempts.isNotEmpty()) userAttempts.map { it.percentage }.average().toFloat() else 0f
                                    }
                                    val isExpanded = expandedUsernames.contains(user.username)

                                    ElevatedCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("student_card_${user.username}"),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.elevatedCardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = if (user.role == "ADMIN") SleekGold400.copy(alpha = 0.25f) else SleekBlue600.copy(alpha = 0.25f),
                                                        modifier = Modifier.size(44.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text(
                                                                text = user.displayName.take(1).uppercase(),
                                                                style = MaterialTheme.typography.titleMedium,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (user.role == "ADMIN") SleekGold400 else SleekBlue400
                                                            )
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.width(12.dp))

                                                    Column {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(
                                                                text = user.displayName,
                                                                style = MaterialTheme.typography.titleSmall,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.White
                                                            )
                                                            if (user.role == "ADMIN") {
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Surface(
                                                                    shape = RoundedCornerShape(4.dp),
                                                                    color = SleekGold400.copy(alpha = 0.2f)
                                                                ) {
                                                                    Text(
                                                                        text = "ADMIN",
                                                                        style = MaterialTheme.typography.labelSmall,
                                                                        color = SleekGold400,
                                                                        fontSize = 9.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                        Text(
                                                            text = "@${user.username}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = Color(0xFF94A3B8)
                                                        )
                                                    }
                                                }

                                                IconButton(
                                                    onClick = {
                                                        expandedUsernames = if (isExpanded) {
                                                            expandedUsernames - user.username
                                                        } else {
                                                            expandedUsernames + user.username
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                        contentDescription = "Expand activity",
                                                        tint = SleekBlue400
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            // Quick Stats Badges
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                StudentStatBadge(
                                                    icon = Icons.Default.Videocam,
                                                    label = "$videosViewed Videos",
                                                    color = SleekBlue400
                                                )
                                                StudentStatBadge(
                                                    icon = Icons.Default.Description,
                                                    label = "$docsRead Docs",
                                                    color = Color(0xFF10B981)
                                                )
                                                StudentStatBadge(
                                                    icon = Icons.Default.Quiz,
                                                    label = "$quizCount Quizzes",
                                                    color = SleekGold400
                                                )
                                                if (quizCount > 0) {
                                                    StudentStatBadge(
                                                        icon = Icons.Default.AssignmentTurnedIn,
                                                        label = "${avgScore.toInt()}% Avg",
                                                        color = Color(0xFFA855F7)
                                                    )
                                                }
                                            }

                                            // Expanded Detailed Activity History
                                            AnimatedVisibility(
                                                visible = isExpanded,
                                                enter = fadeIn() + expandVertically(),
                                                exit = fadeOut() + shrinkVertically()
                                            ) {
                                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                                    Divider(color = Color(0xFF334155))
                                                    Spacer(modifier = Modifier.height(8.dp))

                                                    Text(
                                                        text = "Detailed Activity History (${userActs.size} events):",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = SleekGold400
                                                    )

                                                    Spacer(modifier = Modifier.height(6.dp))

                                                    if (userActs.isEmpty()) {
                                                        Text(
                                                            text = "No study material opened yet by this student.",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = Color(0xFF94A3B8),
                                                            modifier = Modifier.padding(vertical = 4.dp)
                                                        )
                                                    } else {
                                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                            userActs.forEach { act ->
                                                                UserHistoryRow(act)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Single Material Confirmation Dialog
    materialToDelete?.let { mat ->
        AlertDialog(
            onDismissRequest = { materialToDelete = null },
            title = { Text("Delete Material?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently delete \"${mat.title}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(mat)
                        materialToDelete = null
                        Toast.makeText(context, "Material deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { materialToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Preparing Files Progress Dialog
    if (isPreparingFiles) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Processing Files...", fontWeight = FontWeight.Bold, color = SleekGold400) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    CircularProgressIndicator(color = SleekGold400)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = preparingProgress,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            confirmButton = {},
            containerColor = SleekNavy900,
            tonalElevation = 6.dp
        )
    }

    // Batch Upload Loading Dialog
    if (isUploadingBatch) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Uploading Batch...", fontWeight = FontWeight.Bold, color = SleekGold400) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    CircularProgressIndicator(color = SleekGold400)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uploadBatchProgress,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            confirmButton = {},
            containerColor = SleekNavy900,
            tonalElevation = 6.dp
        )
    }

    // Clear All Materials Confirmation Dialog
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text("Remove All Materials?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to remove ALL study materials (${materials.size} files) from the app? This will reset the syllabus content.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllMaterials()
                        showClearAllDialog = false
                        Toast.makeText(context, "All materials removed successfully", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remove All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Subject Dialog
    subjectToDelete?.let { sub ->
        AlertDialog(
            onDismissRequest = { subjectToDelete = null },
            title = { Text("Delete Subject \"${sub.title}\"?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to delete this subject and all its related materials?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSubject(sub.id)
                        subjectToDelete = null
                        Toast.makeText(context, "Subject deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { subjectToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Subject Dialog
    if (showAddSubjectDialog) {
        val colorOptions = listOf(
            0xFF3B82F6L to "Blue",
            0xFF10B981L to "Emerald",
            0xFFF59E0BL to "Gold",
            0xFFEF4444L to "Crimson",
            0xFF8B5CF6L to "Purple",
            0xFF06B6D4L to "Teal",
            0xFFEC4899L to "Pink",
            0xFFF97316L to "Orange"
        )

        AlertDialog(
            onDismissRequest = { showAddSubjectDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = SleekBlue600.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            tint = SleekBlue400,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Add New Subject", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = newSubjectTitle,
                        onValueChange = { newSubjectTitle = it },
                        label = { Text("Subject Name (e.g. Math, English, Chemistry)") },
                        placeholder = { Text("e.g. Mathematics") },
                        singleLine = true,
                        colors = sleekTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newSubjectCode,
                        onValueChange = { newSubjectCode = it },
                        label = { Text("Syllabus Code (e.g. Cambridge 0580)") },
                        placeholder = { Text("e.g. IGCSE 0580") },
                        singleLine = true,
                        colors = sleekTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newSubjectTopics,
                        onValueChange = { newSubjectTopics = it },
                        label = { Text("Key Topics (comma separated)") },
                        placeholder = { Text("Algebra, Geometry, Trigonometry, Statistics") },
                        colors = sleekTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Accent Color:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colorOptions.forEach { (colorLong, name) ->
                            val color = Color(colorLong)
                            val isSelected = newSubjectColorHex == colorLong
                            Surface(
                                shape = CircleShape,
                                color = color,
                                border = if (isSelected) BorderStroke(3.dp, Color.White) else null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable { newSubjectColorHex = colorLong }
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = name,
                                        tint = Color.White,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSubjectTitle.isBlank()) {
                            Toast.makeText(context, "Please enter a subject name", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val topicsList = newSubjectTopics
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }

                        onAddSubject(
                            newSubjectTitle.trim(),
                            newSubjectCode.trim(),
                            newSubjectDesc.trim(),
                            topicsList,
                            newSubjectColorHex
                        ) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (success) {
                                newSubjectTitle = ""
                                newSubjectCode = ""
                                newSubjectDesc = ""
                                newSubjectTopics = ""
                                showAddSubjectDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekBlue600)
                ) {
                    Text("Create Subject")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubjectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Backup Operation Progress Modal
    if (isBackupOperationActive) {
        AlertDialog(
            onDismissRequest = { /* Non-dismissible during active streaming */ },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = SleekGold400,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = backupOperationTitle.ifBlank { "Processing Backup..." },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { backupOperationProgress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = SleekGold400,
                        trackColor = SleekNavy900
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = backupOperationMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${(backupOperationProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SleekGold400
                        )
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Backup Diagnostics Modal
    if (showBackupDiagnosticsDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDiagnosticsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = SleekGold400.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = SleekGold400,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Data Vault Diagnostics", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            text = {
                val diag = backupDiagnostics
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SleekNavy950,
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "VAULT STATUS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekGold400
                            )
                            Text(
                                text = diag?.backupStatusMessage ?: "Ready",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (diag?.isBackupHealthy == true) Color(0xFF10B981) else SleekGold400
                            )
                            Text(
                                text = "Last Snapshot: ${diag?.formattedLastBackupDate ?: "None"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SleekNavy950,
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "STORAGE & TABLE METRICS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekGold400
                            )
                            DiagnosticMetricRow("Study Materials", "${diag?.totalMaterials ?: materials.size} records")
                            DiagnosticMetricRow("Custom Subjects", "${diag?.totalSubjects ?: subjects.size} subjects")
                            DiagnosticMetricRow("Library Documents", "${diag?.totalLibraryDocs ?: 0} docs")
                            DiagnosticMetricRow("Quiz History", "${diag?.totalQuizAttempts ?: 0} attempts")
                            DiagnosticMetricRow("Chat History", "${diag?.totalChatMessages ?: 0} messages")
                            DiagnosticMetricRow("Attached Physical PDFs", "${diag?.totalPhysicalFiles ?: 0} files")
                            DiagnosticMetricRow("Total Files Size on Disk", diag?.formattedFilesSize ?: "0 KB")
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SleekNavy950,
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "PERSISTENT SYNC PATHS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekGold400
                            )
                            Text(
                                text = "• Public Snapshot: /Downloads/StudyWellBackup/\n• Standalone Archive: .studywell / .zip\n• Internal Storage: filesDir/study_pdfs/",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showBackupDiagnosticsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekBlue600)
                ) {
                    Text("Close")
                }
            }
        )
    }

    if (showFirebaseConfigDialog) {
        FirebaseDatabaseConfigDialog(
            onDismiss = { showFirebaseConfigDialog = false }
        )
    }
}

@Composable
private fun DiagnosticMetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun AdminMetricPill(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = SleekNavy900,
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = Color(0xFF94A3B8),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun StudentStatBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ActivityLogItem(activity: UserActivityEntity) {
    val sub = SubjectEnum.fromId(activity.subjectId)
    val timeFormatted = formatTime(activity.timestamp)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SleekNavy900),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = when (activity.actionType) {
                    "VIEWED_VIDEO" -> SleekBlue400.copy(alpha = 0.2f)
                    "READ_DOCUMENT" -> Color(0xFF10B981).copy(alpha = 0.2f)
                    else -> SleekGold400.copy(alpha = 0.2f)
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = when (activity.actionType) {
                        "VIEWED_VIDEO" -> Icons.Default.Videocam
                        "READ_DOCUMENT" -> Icons.Default.Description
                        else -> Icons.Default.Quiz
                    },
                    contentDescription = null,
                    tint = when (activity.actionType) {
                        "VIEWED_VIDEO" -> SleekBlue400
                        "READ_DOCUMENT" -> Color(0xFF10B981)
                        else -> SleekGold400
                    },
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = activity.userDisplayName.ifBlank { activity.username },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }

                Text(
                    text = when (activity.actionType) {
                        "VIEWED_VIDEO" -> "Watched Video: ${activity.materialTitle}"
                        "READ_DOCUMENT" -> "Opened Document: ${activity.materialTitle}"
                        else -> "Completed: ${activity.materialTitle}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = sub.color,
                    maxLines = 2
                )
                Text(
                    text = "Subject: ${sub.title}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
private fun UserHistoryRow(activity: UserActivityEntity) {
    val sub = SubjectEnum.fromId(activity.subjectId)
    val timeFormatted = formatTime(activity.timestamp)

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SleekNavy950,
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = when (activity.actionType) {
                        "VIEWED_VIDEO" -> Icons.Default.PlayCircle
                        "READ_DOCUMENT" -> Icons.Default.MenuBook
                        else -> Icons.Default.CheckCircle
                    },
                    contentDescription = null,
                    tint = when (activity.actionType) {
                        "VIEWED_VIDEO" -> SleekBlue400
                        "READ_DOCUMENT" -> Color(0xFF10B981)
                        else -> SleekGold400
                    },
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = activity.materialTitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(
                        text = "${sub.title} • ${activity.actionType.replace("_", " ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = sub.color,
                        fontSize = 10.sp
                    )
                }
            }

            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8),
                fontSize = 10.sp
            )
        }
    }
}

private fun formatTime(millis: Long): String {
    val sdf = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}
