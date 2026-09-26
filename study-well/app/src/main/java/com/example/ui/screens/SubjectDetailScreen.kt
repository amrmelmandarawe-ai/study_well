package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.*
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.Grading
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import com.example.data.repository.RealPastPaper
import com.example.data.repository.RealPastPaperRepository
import com.example.network.RealExamSearchService
import com.example.utils.NetworkUtils
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Rule
import com.example.ui.components.SectionIntroDialog
import com.example.utils.IntroPreferencesManager
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import java.io.File
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExamBoard
import com.example.data.model.StudyMaterialEntity
import com.example.data.model.SubjectEnum
import com.example.network.GeminiService
import com.example.ui.theme.SleekBlue400
import com.example.ui.theme.SleekBlue600
import com.example.ui.theme.SleekGold400
import com.example.ui.theme.SleekGold500
import com.example.ui.theme.SleekNavy900
import com.example.ui.theme.SleekNavy950
import com.example.utils.ExamPdfGenerator
import com.example.utils.BlankNotePdfGenerator
import com.example.utils.NotePageStyle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    subject: SubjectEnum,
    materials: List<StudyMaterialEntity>,
    selectedSection: String,
    onSelectSection: (String) -> Unit,
    onOpenVideo: (StudyMaterialEntity) -> Unit,
    onOpenDocument: (StudyMaterialEntity) -> Unit,
    onDownloadMaterial: ((StudyMaterialEntity) -> Unit)? = null,
    onStartQuiz: (topic: String, count: Int, difficulty: String, style: String, examBoard: ExamBoard, pastYear: String, session: String, paperVariant: String) -> Unit,
    isGeneratingQuiz: Boolean,
    onOpenAiTutor: () -> Unit,
    onOpenAdminUpload: () -> Unit,
    onOpenLibrary: () -> Unit = {},
    isAdmin: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val sections = listOf(
        "VIDEOS" to "Videos",
        "BOOKS" to "Books",
        "NOTES" to "Notes",
        "SHEETS" to "Sheets",
        "MARKSCHEMES" to "Mark Schemes",
        "DOWNLOADS" to "Downloaded 📥"
    )

    val currentTabIndex = when (selectedSection) {
        "VIDEOS" -> 0
        "BOOKS" -> 1
        "NOTES" -> 2
        "SHEETS" -> 3
        "MARKSCHEMES" -> 4
        "DOWNLOADS" -> 5
        else -> 0
    }

    var pageLimit by remember(selectedSection) { mutableIntStateOf(15) }
    var searchQuery by remember(selectedSection) { mutableStateOf("") }

    // Blank Note Creation Dialog State
    var showBlankNoteDialog by remember { mutableStateOf(false) }
    var blankNotePageCount by remember { mutableIntStateOf(5) }
    var blankNoteCustomInput by remember { mutableStateOf("5") }
    var blankNoteTitle by remember(subject) { mutableStateOf("${subject.title} Revision Notes") }
    var blankNoteStyle by remember { mutableStateOf(NotePageStyle.RULED) }
    var isGeneratingBlankNote by remember { mutableStateOf(false) }

    // Section Intro State (shown when entering Books, Notes, Mark Schemes)
    var showSectionIntroDialog by remember { mutableStateOf(false) }
    var activeIntroSection by remember { mutableStateOf("") }

    val sectionMaterials = remember(materials, selectedSection) {
        when (selectedSection) {
            "VIDEOS" -> materials.filter { it.materialType == "VIDEO" }
            "BOOKS" -> materials.filter { it.materialType == "BOOK" }
            "NOTES" -> materials.filter { it.materialType == "NOTE" }
            "SHEETS" -> materials.filter { it.materialType == "SHEET" }
            "MARKSCHEMES" -> {
                val databaseMarkSchemes = materials.filter { it.materialType == "MARKSCHEME" }
                val generatedMarkSchemes = materials.filter { it.materialType == "SHEET" }.mapNotNull { getMarkSchemeForSheet(it) }
                databaseMarkSchemes + generatedMarkSchemes
            }
            "DOWNLOADS" -> materials.filter { mat ->
                mat.isDownloaded || (mat.localFilePath != null && File(mat.localFilePath).exists())
            }
            "EXAMS", "QUIZ" -> materials.filter { it.materialType == "EXAM" }
            else -> materials
        }
    }

    val filteredMaterials = remember(sectionMaterials, searchQuery) {
        sectionMaterials.filter { mat ->
            if (searchQuery.isBlank()) {
                true
            } else {
                mat.title.contains(searchQuery, ignoreCase = true) ||
                mat.topic.contains(searchQuery, ignoreCase = true) ||
                mat.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("subject_detail_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = subject.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${subject.syllabusCode} Curriculum Hub",
                            style = MaterialTheme.typography.labelSmall,
                            color = subject.color,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("subject_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenLibrary,
                        modifier = Modifier.testTag("subject_library_action")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Student Library",
                            tint = SleekGold400
                        )
                    }
                    IconButton(
                        onClick = onOpenAiTutor,
                        modifier = Modifier.testTag("subject_ai_tutor_action")
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "AI Tutor",
                            tint = SleekGold400
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = onOpenAdminUpload,
                    containerColor = SleekGold400,
                    contentColor = SleekNavy950,
                    modifier = Modifier.testTag("admin_add_material_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Upload Material")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Scrollable 5 Tabs: Videos, Books, Notes, Sheets, Exams
            ScrollableTabRow(
                selectedTabIndex = currentTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = subject.color,
                edgePadding = 12.dp
            ) {
                sections.forEachIndexed { index, (key, label) ->
                    Tab(
                        selected = currentTabIndex == index,
                        onClick = {
                            onSelectSection(key)
                            if (key in listOf("VIDEOS", "BOOKS", "NOTES", "MARKSCHEMES")) {
                                if (!IntroPreferencesManager.hasSeenSectionIntro(context, key)) {
                                    activeIntroSection = key
                                    showSectionIntroDialog = true
                                    IntroPreferencesManager.markSectionIntroSeen(context, key)
                                }
                            }
                        },
                        modifier = Modifier.testTag("tab_${key.lowercase()}"),
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val icon = when (key) {
                                    "VIDEOS" -> Icons.Default.PlayCircleFilled
                                    "BOOKS" -> Icons.AutoMirrored.Filled.MenuBook
                                    "NOTES" -> Icons.Default.Description
                                    "SHEETS" -> Icons.AutoMirrored.Filled.Assignment
                                    "MARKSCHEMES" -> Icons.AutoMirrored.Filled.FactCheck
                                    "DOWNLOADS" -> Icons.Default.Download
                                    else -> Icons.Default.Quiz
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(17.dp),
                                    tint = if (currentTabIndex == index) subject.color else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = label,
                                    fontWeight = if (currentTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    )
                }
            }

            // Section Specific Body
            if (selectedSection == "EXAMS" || selectedSection == "QUIZ") {
                // Enhanced AI Exam Creator, Custom Mock Builder & Past Papers Hub
                EnhancedExamSectionView(
                    subject = subject,
                    materials = materials,
                    isGenerating = isGeneratingQuiz,
                    onStartQuiz = { topic, count, difficulty, style, board, year, sess, paper ->
                        onStartQuiz(topic, count, difficulty, style, board, year, sess, paper)
                    },
                    onOpenExamDocument = { mat -> onOpenDocument(mat) },
                    onOpenAdminUpload = onOpenAdminUpload,
                    isAdmin = isAdmin,
                    onDownloadMaterial = onDownloadMaterial
                )
            } else {
                // Search bar for uploaded materials
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search ${selectedSection.lowercase()} by title or keyword...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = subject.color
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = subject.color,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("materials_search_bar")
                )

                // List of Materials (Videos, Books, Notes, Sheets)
                if (sectionMaterials.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (selectedSection == "VIDEOS") {
                                VideosSectionIntroCard(
                                    subject = subject,
                                    onOpenIntroDialog = {
                                        activeIntroSection = "VIDEOS"
                                        showSectionIntroDialog = true
                                    },
                                    modifier = Modifier.padding(bottom = 20.dp)
                                )
                            } else if (selectedSection == "BOOKS") {
                                BooksSectionIntroCard(
                                    subject = subject,
                                    onOpenIntroDialog = {
                                        activeIntroSection = "BOOKS"
                                        showSectionIntroDialog = true
                                    },
                                    modifier = Modifier.padding(bottom = 20.dp)
                                )
                            } else if (selectedSection == "NOTES") {
                                NotesSectionIntroCard(
                                    subject = subject,
                                    onCreateBlankNote = { showBlankNoteDialog = true },
                                    onOpenIntroDialog = {
                                        activeIntroSection = "NOTES"
                                        showSectionIntroDialog = true
                                    },
                                    modifier = Modifier.padding(bottom = 20.dp)
                                )
                            } else if (selectedSection == "MARKSCHEMES") {
                                MarkSchemesSectionIntroCard(
                                    subject = subject,
                                    onOpenIntroDialog = {
                                        activeIntroSection = "MARKSCHEMES"
                                        showSectionIntroDialog = true
                                    },
                                    modifier = Modifier.padding(bottom = 20.dp)
                                )
                            }

                            Icon(
                                imageVector = when (selectedSection) {
                                    "VIDEOS" -> Icons.Default.Videocam
                                    "BOOKS" -> Icons.AutoMirrored.Filled.MenuBook
                                    "SHEETS" -> Icons.AutoMirrored.Filled.Assignment
                                    "DOWNLOADS" -> Icons.Default.Download
                                    else -> Icons.Default.Description
                                },
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            if (selectedSection == "DOWNLOADS") {
                                Text(
                                    text = "No offline downloads yet for ${subject.title}.",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tap the download icon (📥) next to any book, note, or sheet to read it offline anytime without internet.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Text(
                                    text = "No ${selectedSection.lowercase()} uploaded yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (selectedSection == "NOTES") {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { showBlankNoteDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = SleekGold400, contentColor = SleekNavy950),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Open Blank Note", fontWeight = FontWeight.Bold)
                                }
                            }

                            if (isAdmin) {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = onOpenAdminUpload,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Upload as Admin")
                                }
                            }
                        }
                    }
                } else if (filteredMaterials.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No results found matching \"$searchQuery\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try checking your spelling or search for another keyword.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            TextButton(
                                onClick = { searchQuery = "" },
                                colors = ButtonDefaults.textButtonColors(contentColor = subject.color)
                            ) {
                                Text("Clear Search")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (selectedSection == "VIDEOS") {
                            item(key = "videos_section_intro_card") {
                                VideosSectionIntroCard(
                                    subject = subject,
                                    onOpenIntroDialog = {
                                        activeIntroSection = "VIDEOS"
                                        showSectionIntroDialog = true
                                    }
                                )
                            }
                        } else if (selectedSection == "BOOKS") {
                            item(key = "books_section_intro_card") {
                                BooksSectionIntroCard(
                                    subject = subject,
                                    onOpenIntroDialog = {
                                        activeIntroSection = "BOOKS"
                                        showSectionIntroDialog = true
                                    }
                                )
                            }
                        } else if (selectedSection == "NOTES") {
                            item(key = "notes_section_intro_card") {
                                NotesSectionIntroCard(
                                    subject = subject,
                                    onCreateBlankNote = { showBlankNoteDialog = true },
                                    onOpenIntroDialog = {
                                        activeIntroSection = "NOTES"
                                        showSectionIntroDialog = true
                                    }
                                )
                            }
                        } else if (selectedSection == "MARKSCHEMES") {
                            item(key = "markschemes_section_intro_card") {
                                MarkSchemesSectionIntroCard(
                                    subject = subject,
                                    onOpenIntroDialog = {
                                        activeIntroSection = "MARKSCHEMES"
                                        showSectionIntroDialog = true
                                    }
                                )
                            }
                        }

                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = subject.color.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, subject.color.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when (selectedSection) {
                                            "VIDEOS" -> Icons.Default.PlayCircleFilled
                                            "BOOKS" -> Icons.AutoMirrored.Filled.MenuBook
                                            "SHEETS" -> Icons.AutoMirrored.Filled.Assignment
                                            "MARKSCHEMES" -> Icons.AutoMirrored.Filled.FactCheck
                                            else -> Icons.Default.Description
                                        },
                                        contentDescription = null,
                                        tint = subject.color,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val sectionLabel = sections.find { it.first == selectedSection }?.second ?: selectedSection
                                    Text(
                                        text = "${filteredMaterials.size} ${sectionLabel} available for ${subject.title}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = subject.color
                                    )
                                }
                            }
                        }

                        items(filteredMaterials.take(pageLimit), key = { it.id }, contentType = { "study_material" }) { material ->
                            StudyMaterialCard(
                                material = material,
                                subject = subject,
                                onOpen = {
                                    if (material.materialType == "VIDEO") onOpenVideo(material)
                                    else onOpenDocument(material)
                                },
                                onDownload = onDownloadMaterial?.let { { it(material) } }
                            )
                        }

                        if (filteredMaterials.size > pageLimit) {
                            item(key = "load_more_materials") {
                                OutlinedButton(
                                    onClick = { pageLimit += 15 },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Load More Materials (${filteredMaterials.size - pageLimit} remaining)")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // Blank Note Creation Dialog
    // ==========================================
    if (showBlankNoteDialog) {
        AlertDialog(
            onDismissRequest = { if (!isGeneratingBlankNote) showBlankNoteDialog = false },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = SleekGold400.copy(alpha = 0.15f),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = SleekGold400,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "Create Blank Note",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Subject: ${subject.title}",
                        style = MaterialTheme.typography.labelMedium,
                        color = subject.color,
                        fontWeight = FontWeight.Bold
                    )

                    // 1. Note Title Input
                    OutlinedTextField(
                        value = blankNoteTitle,
                        onValueChange = { blankNoteTitle = it },
                        label = { Text("Note Title") },
                        placeholder = { Text("${subject.title} Notes") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 2. Page Count Selector
                    Text(
                        text = "How many pages do you need?",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Quick Page Presets
                    val presetPages = listOf(1, 2, 3, 5, 10, 15, 20)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(presetPages) { count ->
                            val isSelected = blankNotePageCount == count
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) SleekGold400 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, if (isSelected) SleekGold400 else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .clickable {
                                        blankNotePageCount = count
                                        blankNoteCustomInput = count.toString()
                                    }
                            ) {
                                Text(
                                    text = "$count ${if (count == 1) "Page" else "Pages"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) SleekNavy950 else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    // Stepper / Custom Number Counter
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Selected Pages:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    if (blankNotePageCount > 1) {
                                        blankNotePageCount--
                                        blankNoteCustomInput = blankNotePageCount.toString()
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease")
                            }

                            Text(
                                text = "$blankNotePageCount",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SleekGold400,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            IconButton(
                                onClick = {
                                    if (blankNotePageCount < 50) {
                                        blankNotePageCount++
                                        blankNoteCustomInput = blankNotePageCount.toString()
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase")
                            }
                        }
                    }

                    // 3. Page Paper Style
                    Text(
                        text = "Page Style",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NotePageStyle.values().forEach { style ->
                            val isSelected = blankNoteStyle == style
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) SleekGold400.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(1.dp, if (isSelected) SleekGold400 else Color.Transparent),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { blankNoteStyle = style }
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = style.displayName.substringBefore(" "),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) SleekGold400 else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    if (isGeneratingBlankNote) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            CircularProgressIndicator(color = SleekGold400, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Generating $blankNotePageCount pages note...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!isGeneratingBlankNote) {
                            isGeneratingBlankNote = true
                            coroutineScope.launch {
                                val generatedFile = BlankNotePdfGenerator.generateBlankNotePdf(
                                    context = context,
                                    subject = subject,
                                    noteTitle = blankNoteTitle.ifBlank { "${subject.title} Revision Notes" },
                                    pageCount = blankNotePageCount,
                                    pageStyle = blankNoteStyle
                                )
                                isGeneratingBlankNote = false
                                showBlankNoteDialog = false

                                if (generatedFile != null && generatedFile.exists()) {
                                    val noteMaterial = StudyMaterialEntity(
                                        id = 0L,
                                        subjectId = subject.id,
                                        materialType = "NOTE",
                                        title = blankNoteTitle.ifBlank { "${subject.title} Revision Notes" },
                                        topic = "Personal Notes",
                                        description = "${blankNotePageCount}-page ${blankNoteStyle.displayName} blank notebook for ${subject.title}",
                                        contentUrl = generatedFile.absolutePath,
                                        documentContent = "### ${blankNoteTitle.ifBlank { "${subject.title} Revision Notes" }}\n\nBlank revision notebook (${blankNotePageCount} pages).",
                                        durationOrPages = "$blankNotePageCount pages",
                                        uploadedBy = "Student"
                                    )
                                    onOpenDocument(noteMaterial)
                                } else {
                                    Toast.makeText(context, "Failed to create blank note. Please try again.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    enabled = !isGeneratingBlankNote,
                    colors = ButtonDefaults.buttonColors(containerColor = SleekGold400, contentColor = SleekNavy950),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Note & Write", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { if (!isGeneratingBlankNote) showBlankNoteDialog = false },
                    enabled = !isGeneratingBlankNote
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSectionIntroDialog && activeIntroSection.isNotBlank()) {
        SectionIntroDialog(
            sectionKey = activeIntroSection,
            subject = subject,
            onDismiss = {
                IntroPreferencesManager.markSectionIntroSeen(context, activeIntroSection)
                showSectionIntroDialog = false
            }
        )
    }
}

@Composable
fun VideosSectionIntroCard(
    subject: SubjectEnum,
    onOpenIntroDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("videos_section_intro_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SleekNavy900),
        border = BorderStroke(1.dp, SleekBlue400.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SleekBlue600.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PlayCircleFilled,
                                contentDescription = null,
                                tint = SleekBlue400,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SleekBlue400.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, SleekBlue400.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "🎬 VIDEO MASTERCLASS INTRO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SleekBlue400,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onOpenIntroDialog,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("videos_intro_info_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Full Intro & Tips",
                        tint = SleekBlue400
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${subject.title} Video Masterclasses",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Structured visual lessons with step-by-step past paper problem walkthroughs, interactive speed controls, and exam tips.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.72f),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IntroFeatureChip(icon = Icons.Default.School, label = "Chapter Lessons", chipColor = SleekBlue400)
                IntroFeatureChip(icon = Icons.Default.Speed, label = "0.75x - 2.0x", chipColor = SleekBlue400)
                IntroFeatureChip(icon = Icons.Default.AutoAwesome, label = "Lesson Intro Roadmap", chipColor = SleekBlue400)
            }
        }
    }
}

@Composable
fun BooksSectionIntroCard(
    subject: SubjectEnum,
    onOpenIntroDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("books_section_intro_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SleekNavy900),
        border = BorderStroke(1.dp, SleekBlue400.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SleekBlue600.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = SleekBlue400,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SleekBlue400.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, SleekBlue400.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "📚 COURSEBOOKS INTRO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SleekBlue400,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onOpenIntroDialog,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("books_intro_info_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Full Intro & Tips",
                        tint = SleekBlue400
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${subject.title} Endorsed Coursebooks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Official Cambridge & Edexcel textbooks with complete chapter theory, practice questions, and worked examples.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.72f),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IntroFeatureChip(icon = Icons.AutoMirrored.Filled.MenuBook, label = "In-App Reader", chipColor = SleekBlue400)
                IntroFeatureChip(icon = Icons.Default.Edit, label = "Stylus Markup", chipColor = SleekBlue400)
                IntroFeatureChip(icon = Icons.Default.Download, label = "Offline Save", chipColor = SleekBlue400)
            }
        }
    }
}

@Composable
fun NotesSectionIntroCard(
    subject: SubjectEnum,
    onCreateBlankNote: () -> Unit,
    onOpenIntroDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("notes_section_intro_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SleekNavy900),
        border = BorderStroke(1.dp, SleekGold400.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SleekGold400.copy(alpha = 0.18f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = SleekGold400,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SleekGold400.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, SleekGold400.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "📝 HIGH-YIELD NOTES INTRO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SleekGold400,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onOpenIntroDialog,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("notes_intro_info_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Full Intro & Tips",
                        tint = SleekGold400
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${subject.title} Revision Notes & Cheatsheets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Distilled topic summaries, formula sheets, key definitions, and custom blank notebooks for active recall.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.72f),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onCreateBlankNote,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SleekGold400,
                        contentColor = SleekNavy950
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create Blank Note", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onOpenIntroDialog,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekGold400),
                    border = BorderStroke(1.dp, SleekGold400.copy(alpha = 0.4f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Intro & Tips", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun MarkSchemesSectionIntroCard(
    subject: SubjectEnum,
    onOpenIntroDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("markschemes_section_intro_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SleekNavy900),
        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF10B981).copy(alpha = 0.18f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.FactCheck,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "🎯 EXAMINER MARK SCHEMES INTRO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onOpenIntroDialog,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("markschemes_intro_info_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Full Intro & Tips",
                        tint = Color(0xFF10B981)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${subject.title} Official Examiner Mark Schemes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Official marking criteria with Method (M), Accuracy (A), and Independent (B) points to avoid losing marks on exam day.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.72f),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IntroFeatureChip(icon = Icons.Default.Grade, label = "M / A / B Marks", chipColor = Color(0xFF10B981))
                IntroFeatureChip(icon = Icons.Default.Rule, label = "Examiner Tips", chipColor = Color(0xFF10B981))
                IntroFeatureChip(icon = Icons.Default.CheckCircle, label = "Model Solutions", chipColor = Color(0xFF10B981))
            }
        }
    }
}

@Composable
private fun IntroFeatureChip(
    icon: ImageVector,
    label: String,
    chipColor: Color = SleekBlue400
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SleekNavy800,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = chipColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun StudyMaterialCard(
    material: StudyMaterialEntity,
    subject: SubjectEnum,
    onOpen: () -> Unit,
    onDownload: (() -> Unit)? = null
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .testTag("material_card_${material.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = subject.color.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = material.topic,
                        style = MaterialTheme.typography.labelSmall,
                        color = subject.color,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (material.materialType == "VIDEO") Icons.Default.Timer else Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = material.durationOrPages,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = material.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = material.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            val isMs = material.materialType == "MARKSCHEME" || material.topic.contains("Mark Scheme", ignoreCase = true)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onOpen,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (material.materialType == "VIDEO") subject.color else if (isMs) SleekGold400 else MaterialTheme.colorScheme.primary,
                        contentColor = if (isMs) SleekNavy950 else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = when (material.materialType) {
                            "VIDEO" -> Icons.Default.PlayArrow
                            "BOOK" -> Icons.AutoMirrored.Filled.MenuBook
                            "SHEET" -> Icons.AutoMirrored.Filled.Assignment
                            "MARKSCHEME" -> Icons.AutoMirrored.Filled.FactCheck
                            "EXAM" -> Icons.Default.Quiz
                            else -> if (isMs) Icons.AutoMirrored.Filled.FactCheck else Icons.Default.Description
                        },
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (material.materialType) {
                            "VIDEO" -> "Watch Video"
                            "BOOK" -> "Read Textbook"
                            "SHEET" -> "Open Sheet"
                            "MARKSCHEME" -> "Open Mark Scheme"
                            "EXAM" -> "Open Exam Paper"
                            else -> if (isMs) "Open Mark Scheme" else "Open Notes"
                        },
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (onDownload != null && material.materialType != "VIDEO") {
                    if (material.isDownloaded) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SuccessGreen.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f)),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Downloaded Offline",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Cached",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = onDownload,
                            modifier = Modifier.height(40.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download Material",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedExamSectionView(
    subject: SubjectEnum,
    materials: List<StudyMaterialEntity>,
    isGenerating: Boolean,
    onStartQuiz: (topic: String, count: Int, difficulty: String, style: String, examBoard: ExamBoard, pastYear: String, session: String, paperVariant: String) -> Unit,
    onOpenExamDocument: (StudyMaterialEntity) -> Unit,
    onOpenAdminUpload: () -> Unit,
    isAdmin: Boolean,
    onDownloadMaterial: ((StudyMaterialEntity) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isGeneratingPdf by remember { mutableStateOf(false) }

    val examMaterials = materials.filter { it.subjectId == subject.id && it.materialType == "EXAM" }

    // Exam Board & Past Year Configuration State
    var selectedExamBoard by remember { mutableStateOf(ExamBoard.CAMBRIDGE) }
    var selectedPastYear by remember { mutableStateOf("2023") }
    var selectedSession by remember { mutableStateOf("May/June Series") }
    var selectedPaperVariant by remember { mutableStateOf("Paper 2 (Multiple Choice / Theory)") }

    // Exam Creator Configuration State
    var selectedTopic by remember { mutableStateOf("All Comprehensive Curriculum Topics") }
    var selectedQuestionCount by remember { mutableIntStateOf(10) }
    var selectedDifficulty by remember { mutableStateOf("Extended (Standard)") }
    var selectedQuestionStyle by remember { mutableStateOf("Multiple Choice & Structured") }
    var activeSubTab by remember { mutableStateOf(0) } // 0: AI Creator, 1: Uploaded Papers

    // Exam Board Selection Prompt Dialog State
    var showBoardConfirmDialog by remember { mutableStateOf(false) }
    var pendingActionType by remember { mutableStateOf<String>("DOWNLOAD_PDF") } // "DOWNLOAD_PDF", "OPEN_SOLVE_PDF", "IN_APP_QUIZ"
    var downloadedExamFile by remember { mutableStateOf<File?>(null) }
    var showExamSuccessDialog by remember { mutableStateOf(false) }

    // Dialog asking the user for Cambridge, Edexcel, or Oxford before bringing the exam
    if (showBoardConfirmDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showBoardConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🎯 Select Exam Board",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Choose your Year 10 IGCSE / O-Level examination board:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    ExamBoard.values().forEach { board ->
                        val isSelected = selectedExamBoard == board
                        val boardColor = Color(board.badgeColorHex)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) boardColor.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) boardColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedExamBoard = board }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = board.logoEmoji,
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = board.displayName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) boardColor else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = boardColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = board.subtitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Select Past Year Series:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("2024 (Latest)", "2023", "2022", "2021", "2020", "2019", "All Past Years").forEach { yearOption ->
                            val cleanYear = yearOption.replace(" (Latest)", "")
                            FilterChip(
                                selected = selectedPastYear == cleanYear,
                                onClick = { selectedPastYear = cleanYear },
                                label = { Text(yearOption, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBoardConfirmDialog = false
                        if (pendingActionType == "IN_APP_QUIZ") {
                            onStartQuiz(
                                selectedTopic,
                                selectedQuestionCount,
                                selectedDifficulty,
                                selectedQuestionStyle,
                                selectedExamBoard,
                                selectedPastYear,
                                selectedSession,
                                selectedPaperVariant
                            )
                        } else if (pendingActionType == "DOWNLOAD_PDF" || pendingActionType == "OPEN_SOLVE_PDF") {
                            coroutineScope.launch {
                                isGeneratingPdf = true

                                val cleanYear = selectedPastYear.filter { it.isDigit() }
                                val realPaper = RealPastPaperRepository.findPaper(
                                    subjectId = subject.id,
                                    examBoard = selectedExamBoard,
                                    year = if (cleanYear.isNotBlank()) cleanYear else null,
                                    paperCodeKeyword = if (selectedPaperVariant.contains("Paper", ignoreCase = true)) selectedPaperVariant else null
                                )

                                if (realPaper != null) {
                                    Toast.makeText(
                                        context,
                                        "Bringing Authentic Real Past Paper (${realPaper.paperCode})...",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    val pdfFile = ExamPdfGenerator.generateRealExamPdf(
                                        context = context,
                                        paper = realPaper,
                                        includeMarkScheme = true
                                    )

                                    isGeneratingPdf = false

                                    if (pdfFile != null) {
                                        downloadedExamFile = pdfFile
                                        if (pendingActionType == "OPEN_SOLVE_PDF") {
                                            val examMaterial = realPaper.toStudyMaterialEntity(isMarkScheme = false)
                                            onOpenExamDocument(examMaterial)
                                        } else {
                                            showExamSuccessDialog = true
                                        }
                                    } else {
                                        Toast.makeText(context, "Failed to compile Official Past Paper PDF", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Compiling ${selectedExamBoard.shortName} $selectedPastYear Official Exam Paper...",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    val geminiService = GeminiService()
                                    val subjectMats = materials.filter { it.subjectId == subject.id }
                                    val contextText = subjectMats.joinToString("\n\n") { mat ->
                                        "### Title: ${mat.title} (${mat.topic} - ${mat.materialType})\n${mat.documentContent.take(1000)}"
                                    }

                                    val questions = geminiService.generateTopicalQuiz(
                                        subject = subject,
                                        topic = selectedTopic,
                                        materialsContext = contextText.ifBlank { "Full ${selectedExamBoard.displayName} Grade 10 curriculum for ${subject.title}" },
                                        questionCount = selectedQuestionCount,
                                        difficulty = selectedDifficulty,
                                        questionStyle = selectedQuestionStyle,
                                        examBoard = selectedExamBoard,
                                        pastYear = selectedPastYear,
                                        session = selectedSession,
                                        paperVariant = selectedPaperVariant
                                    )

                                    val pdfFile = ExamPdfGenerator.generateExamPdf(
                                        context = context,
                                        subject = subject,
                                        topic = if (selectedTopic.contains("All")) "Official ${selectedExamBoard.shortName} Assessment Paper" else selectedTopic,
                                        questions = questions,
                                        examBoard = selectedExamBoard,
                                        pastYear = selectedPastYear
                                    )

                                    isGeneratingPdf = false

                                    if (pdfFile != null) {
                                        downloadedExamFile = pdfFile
                                        if (pendingActionType == "OPEN_SOLVE_PDF") {
                                            val examMaterial = StudyMaterialEntity(
                                                id = -System.currentTimeMillis(),
                                                subjectId = subject.id,
                                                materialType = "EXAM",
                                                title = "${selectedExamBoard.shortName} $selectedPastYear Exam: ${subject.title}",
                                                topic = selectedTopic,
                                                description = "Official ${selectedExamBoard.displayName} Past Paper Assessment ($selectedPastYear)",
                                                contentUrl = pdfFile.absolutePath,
                                                documentContent = "Official ${selectedExamBoard.displayName} Exam Paper with Mark Scheme",
                                                durationOrPages = "45 mins",
                                                uploadedBy = "Exam Generator"
                                            )
                                            onOpenExamDocument(examMaterial)
                                        } else {
                                            showExamSuccessDialog = true
                                        }
                                    } else {
                                        Toast.makeText(context, "Failed to compile Exam PDF", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(selectedExamBoard.badgeColorHex),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (pendingActionType == "DOWNLOAD_PDF") "Download ${selectedExamBoard.shortName} Exam (PDF)"
                        else if (pendingActionType == "OPEN_SOLVE_PDF") "Open & Solve ${selectedExamBoard.shortName} Exam"
                        else "Start ${selectedExamBoard.shortName} Quiz",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showBoardConfirmDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Exam Downloaded Success Dialog (Allows direct opening or sharing)
    if (showExamSuccessDialog && downloadedExamFile != null) {
        val file = downloadedExamFile!!
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showExamSuccessDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SleekGold400,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Official Exam Paper Downloaded!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Your ${selectedExamBoard.displayName} $selectedPastYear authentic past paper has been compiled as an official real exam PDF and saved for direct offline access.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 13.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = SleekBlue400,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = file.name,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExamSuccessDialog = false
                        val cleanYear = selectedPastYear.filter { it.isDigit() }
                        val realPaper = RealPastPaperRepository.findPaper(
                            subjectId = subject.id,
                            examBoard = selectedExamBoard,
                            year = if (cleanYear.isNotBlank()) cleanYear else null
                        )
                        val examMaterial = realPaper?.toStudyMaterialEntity(isMarkScheme = false) ?: StudyMaterialEntity(
                            id = -System.currentTimeMillis(),
                            subjectId = subject.id,
                            materialType = "EXAM",
                            title = "${selectedExamBoard.shortName} $selectedPastYear Exam: ${subject.title}",
                            topic = selectedTopic,
                            description = "Official ${selectedExamBoard.displayName} Past Paper Assessment ($selectedPastYear)",
                            contentUrl = file.absolutePath,
                            documentContent = "Official ${selectedExamBoard.displayName} Exam Paper with Mark Scheme",
                            durationOrPages = "45 mins",
                            uploadedBy = "Exam Generator"
                        )
                        onOpenExamDocument(examMaterial)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekBlue600),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open & Solve in PDF Viewer", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = {
                            ExamPdfGenerator.shareExamPdf(context, file)
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = SleekGold400)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share PDF", color = SleekGold400, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { showExamSuccessDialog = false }) {
                        Text("Close")
                    }
                }
            },
            shape = RoundedCornerShape(18.dp)
        )
    }

    val realSubjectPapers = remember(subject.id) {
        RealPastPaperRepository.getPapersForSubject(subject.id)
    }
    var realPaperBoardFilter by remember { mutableStateOf<ExamBoard?>(null) }
    var realPaperYearFilter by remember { mutableStateOf<String>("All") }

    // Online Network Search State
    var onlineSearchQuery by remember { mutableStateOf("") }
    var isSearchingOnline by remember { mutableStateOf(false) }
    var onlineSearchResults by remember { mutableStateOf<List<RealPastPaper>>(emptyList()) }
    var hasRunOnlineSearch by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Segmented SubTab Selector
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = SleekNavy900,
            border = BorderStroke(1.dp, SleekBorderDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val subTabs = listOf(
                    "🏛️ Real Papers (${realSubjectPapers.size})",
                    "⚡ Custom Selector",
                    "📜 Uploaded (${examMaterials.size})"
                )
                subTabs.forEachIndexed { index, title ->
                    val isSelected = activeSubTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) SleekNavy800 else Color.Transparent)
                            .border(
                                1.dp,
                                if (isSelected) SleekBlue400.copy(alpha = 0.5f) else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { activeSubTab = index }
                            .padding(vertical = 10.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else TextSecondaryDark,
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        if (activeSubTab == 0) {
            // ================= SUBTAB 0: AUTHENTIC REAL PAST PAPERS =================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SleekNavy900),
                border = BorderStroke(1.dp, SleekBorderDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(SleekNavy900, SleekNavy800.copy(alpha = 0.8f))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = SleekGold400.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, SleekGold400.copy(alpha = 0.4f)),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.FactCheck,
                                contentDescription = "Real Past Papers",
                                tint = SleekGold400,
                                modifier = Modifier.padding(11.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Authentic Past Papers (2015 – 2024)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SuccessGreen.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = "VERIFIED",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SuccessGreen,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "10 Years of Cambridge & Edexcel Question Papers with Official Mark Schemes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryDark,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ================= NETWORK EXAM SEARCH ENGINE =================
            val isNetworkOnline = NetworkUtils.isNetworkAvailable(context)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SleekNavy900),
                border = BorderStroke(1.dp, SleekBorderDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isNetworkOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = if (isNetworkOnline) SuccessGreen else ErrorRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isNetworkOnline) "Network Exam Search" else "Offline Mode (Local Papers)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        if (isNetworkOnline) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SuccessGreen.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(SuccessGreen, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "LIVE ONLINE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SuccessGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = onlineSearchQuery,
                        onValueChange = { onlineSearchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search exams e.g. '0625/42 May/June 2024'", color = TextSecondaryDark) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SleekBlue400) },
                        trailingIcon = {
                            if (onlineSearchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    onlineSearchQuery = ""
                                    onlineSearchResults = emptyList()
                                    hasRunOnlineSearch = false
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search", tint = TextSecondaryDark)
                                }
                            }
                        },
                        singleLine = true,
                        colors = sleekTextFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isSearchingOnline = true
                                val service = RealExamSearchService()
                                val results = service.searchOnlineRealExams(
                                    context = context,
                                    query = onlineSearchQuery,
                                    subject = subject,
                                    examBoard = selectedExamBoard,
                                    year = selectedPastYear
                                )
                                onlineSearchResults = results
                                isSearchingOnline = false
                                hasRunOnlineSearch = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        enabled = !isSearchingOnline,
                        colors = ButtonDefaults.buttonColors(containerColor = SleekBlue600, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSearchingOnline) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Searching Live Database...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        } else {
                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Search & Find Real Exams on Network", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Board Filters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Examination Board",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = realPaperBoardFilter == null,
                    onClick = { realPaperBoardFilter = null },
                    label = { Text("All Boards (${realSubjectPapers.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SleekBlue600,
                        selectedLabelColor = Color.White,
                        containerColor = SleekNavy900,
                        labelColor = TextSecondaryDark
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = realPaperBoardFilter == null,
                        borderColor = SleekBorderDark,
                        selectedBorderColor = SleekBlue400
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                ExamBoard.values().forEach { board ->
                    val count = realSubjectPapers.count { it.examBoard == board }
                    val isSelected = realPaperBoardFilter == board
                    FilterChip(
                        selected = isSelected,
                        onClick = { realPaperBoardFilter = board },
                        label = { Text("${board.shortName} ($count)", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(board.badgeColorHex),
                            selectedLabelColor = Color.White,
                            containerColor = SleekNavy900,
                            labelColor = TextSecondaryDark
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = SleekBorderDark,
                            selectedBorderColor = Color(board.badgeColorHex)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Year Filters
            Text(
                text = "Exam Year Series",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "2024", "2023", "2022", "2021", "2020", "2019", "2018", "2017", "2016", "2015").forEach { yr ->
                    val isSelected = realPaperYearFilter == yr
                    FilterChip(
                        selected = isSelected,
                        onClick = { realPaperYearFilter = yr },
                        label = {
                            Text(
                                if (yr == "2024") "2024 (Latest)" else if (yr == "All") "All 10 Years" else yr,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SleekGold500,
                            selectedLabelColor = SleekNavy950,
                            containerColor = SleekNavy900,
                            labelColor = TextSecondaryDark
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = SleekBorderDark,
                            selectedBorderColor = SleekGold400
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            val displayedRealPapers = (onlineSearchResults + realSubjectPapers).distinctBy { it.id }.filter { p ->
                (realPaperBoardFilter == null || p.examBoard == realPaperBoardFilter) &&
                (realPaperYearFilter == "All" || p.year == realPaperYearFilter) &&
                (onlineSearchQuery.isBlank() || p.paperTitle.contains(onlineSearchQuery, ignoreCase = true) || p.paperCode.contains(onlineSearchQuery, ignoreCase = true) || p.fullPaperContent.contains(onlineSearchQuery, ignoreCase = true))
            }

            if (displayedRealPapers.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekNavy900),
                    border = BorderStroke(1.dp, SleekBorderDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.AutoMirrored.Filled.FactCheck, contentDescription = null, tint = TextSecondaryDark, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No past papers match the selected criteria.", color = TextSecondaryDark, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { realPaperBoardFilter = null; realPaperYearFilter = "All"; onlineSearchQuery = "" }) {
                            Text("Reset All Filters", color = SleekBlue400, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    displayedRealPapers.forEach { paper ->
                        val boardColor = Color(paper.examBoard.badgeColorHex)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = SleekNavy900),
                            border = BorderStroke(1.dp, SleekBorderDark)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                // Board & Paper Code Badges
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = boardColor.copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, boardColor.copy(alpha = 0.3f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(paper.examBoard.logoEmoji, fontSize = 13.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = paper.examBoard.displayName,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = boardColor
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = SleekGold400.copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, SleekGold400.copy(alpha = 0.3f))
                                        ) {
                                            Text(
                                                text = paper.paperCode,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = SleekGold400,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SuccessGreen.copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.25f))
                                    ) {
                                        Text(
                                            text = "✓ Official Exam",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessGreen,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Paper Title
                                Text(
                                    text = paper.paperTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    lineHeight = 22.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Metadata row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📅 ${paper.session} ${paper.year}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextSecondaryDark
                                    )
                                    Text(
                                        text = "⏱️ ${paper.duration}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextSecondaryDark
                                    )
                                    Text(
                                        text = "🎯 ${paper.maxMarks} Marks",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekGold400
                                    )
                                    Text(
                                        text = "📝 ${paper.questions.size} Qs",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextSecondaryDark
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Question Topics Summary
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SleekNavy800.copy(alpha = 0.6f),
                                    border = BorderStroke(1.dp, SleekBorderDark),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "Sample Authentic Exam Questions:",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        paper.questions.take(3).forEach { q ->
                                            Row(
                                                modifier = Modifier.padding(vertical = 2.dp),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Text("• ", style = MaterialTheme.typography.bodySmall, color = SleekBlue400, fontWeight = FontWeight.Bold)
                                                Text(
                                                    text = "Q${q.questionNumber}${if (q.subPart.isNotBlank()) " (${q.subPart})" else ""}: ${q.questionText.take(65)}... [${q.marks}m]",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextSecondaryDark,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        if (paper.questions.size > 3) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "+ ${paper.questions.size - 3} more authentic questions included",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SleekGold400,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // ACTION BUTTONS
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Button 1: Open & Solve (QP)
                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    isGeneratingPdf = true
                                                    Toast.makeText(context, "Compiling Real ${paper.paperCode} PDF...", Toast.LENGTH_SHORT).show()
                                                    val pdfFile = ExamPdfGenerator.generateRealExamPdf(context, paper, includeMarkScheme = false)
                                                    isGeneratingPdf = false
                                                    if (pdfFile != null) {
                                                        val examMat = paper.toStudyMaterialEntity(isMarkScheme = false).copy(
                                                            contentUrl = pdfFile.absolutePath
                                                        )
                                                        onOpenExamDocument(examMat)
                                                    } else {
                                                        Toast.makeText(context, "Failed to compile Exam PDF", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            },
                                            enabled = !isGeneratingPdf,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .testTag("open_solve_${paper.id}"),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = SleekBlue600, contentColor = Color.White)
                                        ) {
                                            if (isGeneratingPdf) {
                                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                            } else {
                                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Open & Solve", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                        }

                                        // Button 2: Mark Scheme (MS)
                                        OutlinedButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    isGeneratingPdf = true
                                                    Toast.makeText(context, "Compiling Mark Scheme PDF...", Toast.LENGTH_SHORT).show()
                                                    val pdfFile = ExamPdfGenerator.generateRealExamPdf(context, paper, includeMarkScheme = true)
                                                    isGeneratingPdf = false
                                                    if (pdfFile != null) {
                                                        val msMat = paper.toStudyMaterialEntity(isMarkScheme = true).copy(
                                                            contentUrl = pdfFile.absolutePath
                                                        )
                                                        onOpenExamDocument(msMat)
                                                    } else {
                                                        Toast.makeText(context, "Failed to compile Mark Scheme PDF", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            },
                                            enabled = !isGeneratingPdf,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .testTag("mark_scheme_${paper.id}"),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                            border = BorderStroke(1.dp, SleekBorderDark)
                                        ) {
                                            if (isGeneratingPdf) {
                                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = SuccessGreen, strokeWidth = 2.dp)
                                            } else {
                                                Icon(Icons.AutoMirrored.Filled.Grading, contentDescription = null, modifier = Modifier.size(16.dp), tint = SuccessGreen)
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Mark Scheme", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Button 3: Download PDF
                                        OutlinedButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    isGeneratingPdf = true
                                                    Toast.makeText(context, "Compiling Real ${paper.paperCode} PDF...", Toast.LENGTH_SHORT).show()
                                                    val pdfFile = ExamPdfGenerator.generateRealExamPdf(context, paper, includeMarkScheme = true)
                                                    isGeneratingPdf = false
                                                    if (pdfFile != null) {
                                                        downloadedExamFile = pdfFile
                                                        showExamSuccessDialog = true
                                                    } else {
                                                        Toast.makeText(context, "Failed to save PDF", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                            border = BorderStroke(1.dp, SleekBorderDark)
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = SleekBlue400)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Download PDF", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                        }

                                        // Button 4: Practice Real Exam Questions
                                        OutlinedButton(
                                            onClick = {
                                                onStartQuiz(
                                                    "${paper.paperCode} ${paper.paperTitle}",
                                                    paper.questions.size,
                                                    "Extended",
                                                    "Real Exam",
                                                    paper.examBoard,
                                                    paper.year,
                                                    paper.session,
                                                    paper.paperCode
                                                )
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                            border = BorderStroke(1.dp, SleekBorderDark)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = SleekGold400)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Interactive Quiz", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (activeSubTab == 1) {
            // ================= SUBTAB 1: SELECTOR & DOWNLOADER =================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = SleekNavy950),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = SleekGold400,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Exam Selector",
                                tint = SleekNavy950,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Past Paper Downloader & Selector",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Select your board, year, series, and paper variant to download or solve.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Configuration Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // STEP 1: EXAM BOARD SELECTION (CAMBRIDGE, EDEXCEL, OXFORD AQA)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "1. Choose Exam Board:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(selectedExamBoard.badgeColorHex).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = selectedExamBoard.shortName.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(selectedExamBoard.badgeColorHex),
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExamBoard.values().forEach { board ->
                            val isSelected = selectedExamBoard == board
                            val boardColor = Color(board.badgeColorHex)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) boardColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) boardColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedExamBoard = board }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(board.logoEmoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = board.shortName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) boardColor else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // STEP 2: PAST YEAR & SESSION SELECTOR
                    Text(
                        text = "2. Past Year & Series Session:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("2024", "2023", "2022", "2021", "2020", "2019", "All Past Years").forEach { yr ->
                            FilterChip(
                                selected = selectedPastYear == yr,
                                onClick = { selectedPastYear = yr },
                                label = { Text(if (yr == "2024") "2024 (Latest)" else "Year $yr") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("May/June Series", "Oct/Nov Series", "Jan Series", "Feb/March Series").forEach { sess ->
                            FilterChip(
                                selected = selectedSession == sess,
                                onClick = { selectedSession = sess },
                                label = { Text(sess, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // STEP 3: PAPER COMPONENT / VARIANT
                    Text(
                        text = "3. Paper Component / Variant:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        selectedExamBoard.standardPapers.forEach { paperName ->
                            FilterChip(
                                selected = selectedPaperVariant == paperName,
                                onClick = { selectedPaperVariant = paperName },
                                label = { Text(paperName, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // STEP 4: TARGET TOPIC / SCOPE
                    Text(
                        text = "4. Target Syllabus Topic:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedTopic == "All Comprehensive Curriculum Topics",
                            onClick = { selectedTopic = "All Comprehensive Curriculum Topics" },
                            label = { Text("All Curriculum Topics (Full Mock)") }
                        )
                        subject.defaultTopics.forEach { topic ->
                            FilterChip(
                                selected = selectedTopic == topic,
                                onClick = { selectedTopic = topic },
                                label = { Text(topic) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // STEP 5: QUESTION COUNT
                    Text(
                        text = "5. Number of Questions:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            5 to "5 Qs (Quick)",
                            10 to "10 Qs (Standard)",
                            15 to "15 Qs (Extended)",
                            20 to "20 Qs (Full Mock)"
                        ).forEach { (count, label) ->
                            FilterChip(
                                selected = selectedQuestionCount == count,
                                onClick = { selectedQuestionCount = count },
                                label = { Text(label, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // STEP 6: DIFFICULTY TIER
                    Text(
                        text = "6. Difficulty Tier:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Core (Grades C–G)",
                            "Extended (Standard)",
                            "Past-Paper Tough"
                        ).forEach { diff ->
                            FilterChip(
                                selected = selectedDifficulty == diff,
                                onClick = { selectedDifficulty = diff },
                                label = { Text(diff, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action 1: Download Official Exam Paper PDF
            Button(
                onClick = {
                    pendingActionType = "DOWNLOAD_PDF"
                    showBoardConfirmDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("download_exam_pdf_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(selectedExamBoard.badgeColorHex),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = !isGenerating && !isGeneratingPdf
            ) {
                if (isGeneratingPdf && pendingActionType == "DOWNLOAD_PDF") {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Compiling ${selectedExamBoard.shortName} Exam PDF...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Download, contentDescription = null, tint = SleekGold400)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Download ${selectedExamBoard.shortName} $selectedPastYear Exam Paper (PDF)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Authentic Assessment Paper + Full Examiner Mark Scheme",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action 2: Open and Solve Directly on PDF Viewer
            Button(
                onClick = {
                    pendingActionType = "OPEN_SOLVE_PDF"
                    showBoardConfirmDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("open_solve_exam_pdf_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SleekNavy900,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, SleekGold400.copy(alpha = 0.7f)),
                enabled = !isGenerating && !isGeneratingPdf
            ) {
                if (isGeneratingPdf && pendingActionType == "OPEN_SOLVE_PDF") {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = SleekGold400,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Opening Exam Paper...", fontWeight = FontWeight.Bold, color = SleekGold400)
                } else {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = SleekGold400, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "✍️ Open & Solve Exam in PDF Viewer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action 3: Practice as Interactive Quiz
            OutlinedButton(
                onClick = {
                    pendingActionType = "IN_APP_QUIZ"
                    showBoardConfirmDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("start_ai_quiz_button"),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                enabled = !isGenerating && !isGeneratingPdf
            ) {
                if (isGenerating && pendingActionType == "IN_APP_QUIZ") {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = SleekBlue400,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Loading Quiz Questions...", fontSize = 12.sp)
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = SleekBlue400)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Practice as Interactive Quiz ($selectedQuestionCount Questions)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // ================= SUBTAB 2: UPLOADED PAPERS =================
            if (examMaterials.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Quiz,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No custom past papers uploaded yet for ${subject.title}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You can access official past papers in the Real Past Papers tab.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B),
                            lineHeight = 18.sp
                        )
                        if (isAdmin) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onOpenAdminUpload,
                                colors = ButtonDefaults.buttonColors(containerColor = SleekGold400, contentColor = SleekNavy950)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upload Past Paper / Mark Scheme")
                            }
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    examMaterials.forEach { mat ->
                        StudyMaterialCard(
                            material = mat,
                            subject = subject,
                            onOpen = { onOpenExamDocument(mat) },
                            onDownload = onDownloadMaterial?.let { { it(mat) } }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

fun getMarkSchemeForSheet(sheet: StudyMaterialEntity): StudyMaterialEntity? {
    val d = "$"
    val markSchemeContent = when (sheet.title) {
        "IGCSE Physics Formula & Equations Reference Sheet" -> """
            # MARK SCHEME: Physics Formula & Equations Reference Sheet
            
            This document outlines the correct marking criteria, units, and equations expected in exam papers.
            
            ### 1. General Physics Formulas
            * **Speed calculation**: Must state unit as m/s (meters per second) or km/h. [1 mark]
            * **Acceleration**: ${d}a = \frac{v - u}{t}${d}. Correct unit is ${d}m/s^2${d}. [1 mark]
            * **Density**: ${d}\rho = \frac{m}{V}${d}. Standard SI unit: ${d}kg/m^3${d} or ${d}g/cm^3${d}. [1 mark]
            
            ### 2. Thermal Physics and Waves
            * **Specific Heat Capacity**: ${d}c = \frac{E}{m \cdot \Delta T}${d}. SI unit: ${d}J/(kg \cdot ^\circ C)${d}. [1 mark]
            * **Wave Speed**: ${d}v = f \cdot \lambda${d}. Ensure frequency is in Hertz (Hz) and wavelength in meters (m). [1 mark]
            
            ### 3. Electricity & Magnetism
            * **Ohm's Law**: ${d}V = I \cdot R${d}. Volt (V) = Ampere (A) * Ohm (${d}\Omega${d}). [1 mark]
            * **Electrical Power**: ${d}P = I \cdot V = I^2 \cdot R${d}. Unit: Watts (W). [1 mark]
        """.trimIndent()
        
        "Trigonometry & Geometry Practice Worksheets" -> """
            # MARK SCHEME: Trigonometry & Geometry Practice Worksheet
            
            ### Question 1: Length of Side AC (3 marks)
            * **Step 1**: Use the Cosine Rule:
              ${d}AC^2 = AB^2 + BC^2 - 2 \cdot AB \cdot BC \cdot \cos(ABC)${d} [1 mark]
            * **Step 2**: Substitute values:
              ${d}AC^2 = 8^2 + 11^2 - 2 \cdot 8 \cdot 11 \cdot \cos(42^\circ)${d}
              ${d}AC^2 = 64 + 121 - 176 \cdot 0.7431${d}
              ${d}AC^2 = 185 - 130.79 = 54.21${d} [1 mark]
            * **Step 3**: Calculate final length:
              ${d}AC = \sqrt{54.21} \approx 7.36 \text{ cm}${d} (correct to 3 significant figures) [1 mark]
              
            ### Question 2: Bearing Calculation (3 marks)
            * **Step 1**: Find internal angles using Sine Rule:
              ${d}\frac{\sin(ACB)}{8} = \frac{\sin(42^\circ)}{7.36}${d} [1 mark]
            * **Step 2**: Solve for angle ACB:
              ${d}\sin(ACB) = \frac{8 \cdot \sin(42^\circ)}{7.36} = \frac{8 \cdot 0.6691}{7.36} \approx 0.727${d}
              ${d}ACB = \arcsin(0.727) \approx 46.6^\circ${d} [1 mark]
            * **Step 3**: State final three-figure bearing:
              Bearing of C from A is ${d}180^\circ + 46.6^\circ = 226.6^\circ${d} [1 mark]
        """.trimIndent()

        "Algebraic Equations & Factoring Worksheets" -> """
            # MARK SCHEME: Algebraic Equations & Factoring Worksheet
            
            ### Section A: Core Algebra
            
            #### Question 1 (2 marks)
            * **Equation**: ${d}3x - 7 = 5x + 9${d}
            * **Step 1**: Group like terms:
              ${d}3x - 5x = 9 + 7 \implies -2x = 16${d} [1 mark]
            * **Step 2**: Solve for ${d}x${d}:
              ${d}x = -8${d} [1 mark]
              
            #### Question 2 (3 marks)
            * **Equation**: ${d}2(4x - 3) - 3(x + 1) = 11${d}
            * **Step 1**: Expand brackets correctly:
              ${d}8x - 6 - 3x - 3 = 11${d} [1 mark]
            * **Step 2**: Simplify:
              ${d}5x - 9 = 11 \implies 5x = 20${d} [1 mark]
            * **Step 3**: Solve for ${d}x${d}:
              ${d}x = 4${d} [1 mark]
              
            ### Section B: Quadratic Factoring
            
            #### Question 3 (2 marks)
            * **Expression**: ${d}x^2 - 5x - 14${d}
            * **Step 1**: Find two numbers that multiply to ${d}-14${d} and add to ${d}-5${d} (which are ${d}-7${d} and ${d}+2${d}). [1 mark]
            * **Step 2**: Write factors:
              ${d}(x - 7)(x + 2)${d} [1 mark]
        """.trimIndent()

        "English Literature Critical Analysis Sheets" -> """
            # MARK SCHEME: English Literature Critical Analysis Sheet
            
            ### General Marking Principles
            * **Band 1 (5-6 marks)**: Highly perceptive analysis of language and structure, integrated text evidence, precise vocabulary.
            * **Band 2 (3-4 marks)**: Competent analysis of text, relevant quotes, structured arguments.
            * **Band 3 (1-2 marks)**: Simple surface-level observations, limited evidence.
            
            ### Model Answers & Guidelines
            * **Question 1: Metaphors of the Storm**
              * Candidates should note the storm represents the protagonist's inner psychological conflict. [2 marks]
              * Identification of words like "gale", "blind darkness" to amplify theme of isolation. [2 marks]
              * Structural pacing of paragraph mirrors heartbeats. [2 marks]
        """.trimIndent()

        "Syllabus & Grammar Review Practice Sheets" -> """
            # MARK SCHEME: Syllabus & Grammar Review Practice Sheet
            
            ### Part 1: Subject-Verb Agreement (5 marks)
            * **Q1**: *The list of items (is/are) on the table.*
              * **Answer**: **is** (subject is singular: 'list') [1 mark]
            * **Q2**: *Neither the teacher nor the students (has/have) arrived.*
              * **Answer**: **have** (closest subject 'students' is plural) [1 mark]
              
            ### Part 2: Active vs. Passive Voice (5 marks)
            * **Q3**: Rewrite in passive: *The team completed the research project.*
              * **Answer**: *The research project was completed by the team.* [2 marks for correct syntax, 1 mark for tense consistency]
        """.trimIndent()

        "Acid, Bases & Salts Practice worksheets" -> """
            # MARK SCHEME: Acid, Bases & Salts Practice Sheet
            
            ### Question 1: Neutralization (3 marks)
            * **Word Equation**: ${d}Acid + Base \implies Salt + Water${d} [1 mark]
            * **Chemical Equation**: ${d}HCl(aq) + NaOH(aq) \implies NaCl(aq) + H_2O(l)${d} [1 mark]
            * **Observation**: Temperature rises (exothermic reaction). [1 mark]
            
            ### Question 2: pH Scale Indicators (3 marks)
            * **Indicator turns**:
              * Litmus in acid: Red [1 mark]
              * Litmus in alkali: Blue [1 mark]
              * Methyl orange in acid: Red [1 mark]
        """.trimIndent()

        "Cell Biology Structure & Function Sheets" -> """
            # MARK SCHEME: Cell Biology Structure & Function Sheet
            
            ### Section A: Diagram Identification (5 marks)
            * **Part A (Cell Wall)**: Protects and provides structural rigidity to plant cells. [1 mark]
            * **Part B (Mitochondria)**: Site of aerobic respiration, releasing ATP energy. [1 mark]
            * **Part C (Chloroplast)**: Contains chlorophyll to absorb light for photosynthesis. [1 mark]
            * **Part D (Ribosome)**: Synthesizes proteins. [1 mark]
            * **Part E (Vacuole)**: Stores cell sap, maintains turgor pressure. [1 mark]
        """.trimIndent()

        "Python Programming Syntax & Trace Worksheets" -> """
            # MARK SCHEME: Python Programming Syntax & Trace Sheet
            
            ### Question 1: Trace Output (3 marks)
            * **Code block execution**:
              ```python
              x = 5
              for i in range(3):
                  x += i
              print(x)
              ```
            * **Trace table**:
              * Start: ${d}x = 5${d}
              * Iteration 0 (${d}i=0${d}): ${d}x = 5 + 0 = 5${d} [1 mark]
              * Iteration 1 (${d}i=1${d}): ${d}x = 5 + 1 = 6${d} [1 mark]
              * Iteration 2 (${d}i=2${d}): ${d}x = 6 + 2 = 8${d} [1 mark]
            * **Final Output printed**: **8** [1 mark]
        """.trimIndent()

        else -> null
    }

    if (markSchemeContent == null) return null

    return sheet.copy(
        id = sheet.id + 100000, // offset id to prevent clashes
        title = "${sheet.title} - (Mark Scheme)",
        topic = "Mark Scheme Solutions",
        description = "Official, step-by-step marking guidelines, correct answers, and grading criteria for this practice worksheet.",
        documentContent = markSchemeContent
    )
}
