package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LibraryDocumentEntity
import com.example.ui.theme.ArabicRose
import com.example.ui.theme.BiologyGreen
import com.example.ui.theme.ChemistryCyan
import com.example.ui.theme.EnglishPurple
import com.example.ui.theme.IctEmerald
import com.example.ui.theme.MathAmber
import com.example.ui.theme.PhysicsBlue
import com.example.ui.theme.SleekBlue400
import com.example.ui.theme.SleekBlue600
import com.example.ui.theme.SleekGold400
import com.example.ui.theme.SleekNavy800
import com.example.ui.theme.SleekNavy900
import com.example.ui.theme.SleekNavy950
import com.example.ui.theme.SubjectOrange
import com.example.ui.theme.sleekTextFieldColors
import com.example.utils.FileUtils
import java.io.File
import java.util.Date

private fun getIgcseSubjectColor(tag: String): Color {
    return when (tag.lowercase().trim()) {
        "physics" -> PhysicsBlue
        "math", "mathematics" -> MathAmber
        "chemistry" -> ChemistryCyan
        "biology" -> BiologyGreen
        "english" -> EnglishPurple
        "ict" -> IctEmerald
        "arabic", "arabic ol" -> ArabicRose
        "history" -> SubjectOrange
        else -> SleekBlue400
    }
}

private fun getIgcseSubjectEmoji(tag: String): String {
    return when (tag.lowercase().trim()) {
        "all" -> "📁"
        "physics" -> "⚛️"
        "math", "mathematics" -> "📐"
        "chemistry" -> "🧪"
        "biology" -> "🧬"
        "english" -> "📚"
        "ict" -> "💻"
        "arabic", "arabic ol" -> "📖"
        "history" -> "🏛️"
        else -> "🏷️"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentLibraryScreen(
    libraryDocuments: List<LibraryDocumentEntity>,
    homeworkReminders: List<com.example.data.model.HomeworkReminderEntity> = emptyList(),
    onAddReminder: (String, String, Long) -> Unit = { _, _, _ -> },
    onUpdateReminder: (com.example.data.model.HomeworkReminderEntity) -> Unit = {},
    onDeleteReminder: (com.example.data.model.HomeworkReminderEntity) -> Unit = {},
    onBack: () -> Unit,
    onOpenDocument: (LibraryDocumentEntity) -> Unit,
    onShareDocument: (LibraryDocumentEntity) -> Unit,
    onDeleteDocument: (LibraryDocumentEntity) -> Unit,
    onUpdateDocument: (LibraryDocumentEntity) -> Unit = {},
    onSyncLibrary: () -> Unit,
    onImportExternalPdf: (Uri) -> Unit = {},
    onScanDocument: () -> Unit = {}
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubjectTag by remember { mutableStateOf("ALL") }
    var libraryPageLimit by remember(searchQuery, selectedSubjectTag) { mutableIntStateOf(12) }
    var documentToDelete by remember { mutableStateOf<LibraryDocumentEntity?>(null) }
    var documentToTag by remember { mutableStateOf<LibraryDocumentEntity?>(null) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showAddFolderDialog by remember { mutableStateOf(false) }
    var customFolders by remember { mutableStateOf(listOf<String>()) }

    // Picker for adding external PDFs to library
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportExternalPdf(uri)
        }
    }

    // List of predefined and extracted subject folders
    val defaultSubjectList = remember { listOf("Math", "Physics", "Biology", "Chemistry", "History", "English", "ICT", "General") }
    val documentSubjectTags = remember(libraryDocuments) {
        libraryDocuments
            .map { doc -> if (doc.subjectId.isBlank() || doc.subjectId == "ALL") "General" else doc.subjectId }
            .distinct()
    }

    val allSubjectFolders = remember(libraryDocuments, customFolders, documentSubjectTags) {
        (listOf("ALL") + defaultSubjectList + documentSubjectTags + customFolders).distinct()
    }

    // Filter documents by search query AND selected subject folder
    val filteredDocs = remember(libraryDocuments, searchQuery, selectedSubjectTag) {
        libraryDocuments.filter { doc ->
            val matchesSearch = searchQuery.isBlank() ||
                    doc.title.contains(searchQuery, ignoreCase = true) ||
                    doc.fileName.contains(searchQuery, ignoreCase = true) ||
                    doc.notes.contains(searchQuery, ignoreCase = true) ||
                    doc.subjectId.contains(searchQuery, ignoreCase = true)

            val matchesSubject = when (selectedSubjectTag) {
                "ALL" -> true
                "General" -> doc.subjectId.isBlank() || doc.subjectId.equals("General", ignoreCase = true) || doc.subjectId.equals("ALL", ignoreCase = true)
                else -> doc.subjectId.equals(selectedSubjectTag, ignoreCase = true)
            }

            matchesSearch && matchesSubject
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Student Library",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Transparent,
                                border = BorderStroke(1.dp, SleekGold400.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "${libraryDocuments.size} Saved PDFs",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SleekGold400,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Your annotated revision documents and downloaded exam papers",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("student_library_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (libraryDocuments.isNotEmpty()) {
                        IconButton(
                            onClick = { showDeleteAllDialog = true },
                            modifier = Modifier.testTag("delete_all_documents_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete All Files",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            onSyncLibrary()
                            Toast.makeText(context, "Library refreshed from device storage", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("sync_library_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Library",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SleekNavy950
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Scan Document CamScanner FAB
                FloatingActionButton(
                    onClick = onScanDocument,
                    containerColor = SleekGold400,
                    contentColor = Color.Black,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("scan_pdf_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = "Scan Document",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan Document", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // Primary Import PDF FAB
                FloatingActionButton(
                    onClick = {
                        pdfPickerLauncher.launch(arrayOf("application/pdf"))
                    },
                    containerColor = SleekBlue600,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("import_pdf_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Import PDF",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Import PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("library_search_field"),
                    placeholder = { Text("Search file names, notes, or titles across all subjects...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (searchQuery.isNotEmpty()) SleekGold400 else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear Search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    colors = sleekTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Search Active Indicator
            if (searchQuery.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Found ${filteredDocs.size} matching file${if (filteredDocs.size != 1) "s" else ""} for \"$searchQuery\"",
                        style = MaterialTheme.typography.labelSmall,
                        color = SleekGold400,
                        fontWeight = FontWeight.Bold
                    )
                    if (selectedSubjectTag != "ALL") {
                        TextButton(
                            onClick = { selectedSubjectTag = "ALL" },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Search across ALL subjects",
                                style = MaterialTheme.typography.labelSmall,
                                color = SleekBlue400,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Subject Folders & Tag Filter Chips Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("subject_folder_chips_row"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(allSubjectFolders, key = { it }) { tag ->
                    val isSelected = selectedSubjectTag.equals(tag, ignoreCase = true)
                    val subjectColor = getIgcseSubjectColor(tag)
                    val emoji = getIgcseSubjectEmoji(tag)
                    val docCount = if (tag == "ALL") {
                        libraryDocuments.size
                    } else {
                        libraryDocuments.count { doc ->
                            if (tag == "General") {
                                doc.subjectId.isBlank() || doc.subjectId.equals("General", ignoreCase = true) || doc.subjectId.equals("ALL", ignoreCase = true)
                            } else {
                                doc.subjectId.equals(tag, ignoreCase = true)
                            }
                        }
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSubjectTag = tag },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "$emoji ${if (tag == "ALL") "All Files" else tag}",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) Color.White.copy(alpha = 0.25f) else subjectColor.copy(alpha = 0.2f),
                                    modifier = Modifier.padding(start = 2.dp)
                                ) {
                                    Text(
                                        text = "$docCount",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) Color.White else subjectColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (tag == "ALL") SleekBlue600 else subjectColor,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = subjectColor.copy(alpha = 0.35f),
                            selectedBorderColor = subjectColor,
                            enabled = true,
                            selected = isSelected
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("filter_chip_$tag")
                    )
                }

                item(key = "add_new_folder_chip") {
                    OutlinedButton(
                        onClick = { showAddFolderDialog = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("add_folder_chip_btn"),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, SleekGold400.copy(alpha = 0.7f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Folder Tag",
                            tint = SleekGold400,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "New Folder",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekGold400
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Documents List or Empty State
            if (filteredDocs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = SleekBlue600.copy(alpha = 0.15f),
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp),
                                        tint = SleekBlue400
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = if (searchQuery.isNotBlank() || selectedSubjectTag != "ALL") "No matching PDFs in '$selectedSubjectTag'" else "Your Library is Empty",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (searchQuery.isNotBlank() || selectedSubjectTag != "ALL")
                                    "Try adjusting your search query or choosing another subject folder above."
                                else
                                    "When you annotate study documents or download official IGCSE past papers, they will automatically be saved here so you can open, revise, and share them anytime.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    pdfPickerLauncher.launch(arrayOf("application/pdf"))
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SleekBlue600)
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Import PDF from Device")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredDocs.take(libraryPageLimit), key = { it.id }, contentType = { "library_document" }) { doc ->
                        LibraryDocumentCard(
                            document = doc,
                            onOpen = { onOpenDocument(doc) },
                            onShare = { onShareDocument(doc) },
                            onDelete = { documentToDelete = doc },
                            onChangeTag = { documentToTag = doc }
                        )
                    }

                    if (filteredDocs.size > libraryPageLimit) {
                        item(key = "load_more_docs") {
                            OutlinedButton(
                                onClick = { libraryPageLimit += 12 },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Load More Documents (${filteredDocs.size - libraryPageLimit} remaining)")
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(64.dp))
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    documentToDelete?.let { doc ->
        AlertDialog(
            onDismissRequest = { documentToDelete = null },
            title = {
                Text(
                    text = "Delete from Library?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove '${doc.title}' from your student library and storage?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteDocument(doc)
                        documentToDelete = null
                        Toast.makeText(context, "Deleted from library", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { documentToDelete = null }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Delete All Confirmation Dialog
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = {
                Text(
                    text = "Delete All ${libraryDocuments.size} Files?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete all saved files from your student library? This operation cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        libraryDocuments.toList().forEach { doc ->
                            onDeleteDocument(doc)
                        }
                        showDeleteAllDialog = false
                        Toast.makeText(context, "All files deleted from library", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Organize Subject Folder / Tag Dialog
    documentToTag?.let { doc ->
        var customTagInput by remember { mutableStateOf("") }
        var selectedTagChoice by remember { mutableStateOf(if (doc.subjectId.isBlank() || doc.subjectId == "ALL") "General" else doc.subjectId) }

        AlertDialog(
            onDismissRequest = { documentToTag = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Label,
                        contentDescription = null,
                        tint = SleekGold400,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Organize Subject Folder",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Assign '${doc.title}' to a subject folder or custom tag:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text("Select Subject Folder:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val availableFolderTags = (defaultSubjectList + customFolders + listOf("General")).distinct()
                        availableFolderTags.chunked(3).forEach { rowTags ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowTags.forEach { tag ->
                                    val isChosen = selectedTagChoice.equals(tag, ignoreCase = true)
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isChosen) SleekBlue600 else MaterialTheme.colorScheme.surfaceVariant,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isChosen) SleekGold400 else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                selectedTagChoice = tag
                                                customTagInput = ""
                                            }
                                    ) {
                                        Text(
                                            text = "🏷️ $tag",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isChosen) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 11.sp,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                repeat(3 - rowTags.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = customTagInput,
                        onValueChange = {
                            customTagInput = it
                            if (it.isNotBlank()) {
                                selectedTagChoice = it.trim()
                            }
                        },
                        label = { Text("Or enter custom folder/tag name...") },
                        placeholder = { Text("e.g. Economics, Exam Prep, Chapter 4") },
                        singleLine = true,
                        colors = sleekTextFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalSubjectTag = if (customTagInput.isNotBlank()) customTagInput.trim() else selectedTagChoice
                        val updatedDoc = doc.copy(subjectId = finalSubjectTag)
                        onUpdateDocument(updatedDoc)
                        documentToTag = null
                        Toast.makeText(context, "Organized into '$finalSubjectTag' folder", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekBlue600)
                ) {
                    Text("Save Folder Tag")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { documentToTag = null }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Add New Folder Dialog
    if (showAddFolderDialog) {
        var folderInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddFolderDialog = false },
            title = {
                Text("Create New Subject Folder", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Enter the subject name or folder tag (e.g., Economics, History, Formula Sheets):",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = folderInput,
                        onValueChange = { folderInput = it },
                        placeholder = { Text("Folder Tag Name") },
                        singleLine = true,
                        colors = sleekTextFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanFolder = folderInput.trim()
                        if (cleanFolder.isNotBlank()) {
                            if (!customFolders.contains(cleanFolder)) {
                                customFolders = customFolders + cleanFolder
                            }
                            selectedSubjectTag = cleanFolder
                            Toast.makeText(context, "Folder '$cleanFolder' created", Toast.LENGTH_SHORT).show()
                        }
                        showAddFolderDialog = false
                    },
                    enabled = folderInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = SleekBlue600)
                ) {
                    Text("Create Folder")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddFolderDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun LibraryDocumentCard(
    document: LibraryDocumentEntity,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onChangeTag: () -> Unit = {}
) {
    val isEdited = document.docType == "EDITED_PDF"
    val isExam = document.docType == "EXAM_PDF"

    val badgeColor = when {
        isEdited -> SleekGold400
        isExam -> SleekBlue400
        else -> Color(0xFF94A3B8)
    }

    val badgeText = when {
        isEdited -> "✏️ Annotated Notes"
        isExam -> "🏛️ Official Exam Paper"
        else -> "📥 Downloaded PDF"
    }

    val subjectLabel = if (document.subjectId.isBlank() || document.subjectId == "ALL") "General" else document.subjectId

    val formattedDate = try {
        DateFormat.format("MMM d, yyyy • h:mm a", Date(document.timestamp)).toString()
    } catch (_: Exception) {
        ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .testTag("library_doc_card_${document.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (isEdited) SleekGold400.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // PDF Type Icon
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, if (isEdited) SleekGold400.copy(alpha = 0.5f) else SleekBlue600.copy(alpha = 0.5f)),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isEdited) Icons.Default.Edit else Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = if (isEdited) SleekGold400 else SleekBlue400,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title & Metadata
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Transparent,
                            border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Subject Folder Tag Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SleekBlue600.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, SleekBlue400.copy(alpha = 0.5f)),
                            modifier = Modifier.clickable { onChangeTag() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Label,
                                    contentDescription = "Change Folder Tag",
                                    tint = SleekBlue400,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = subjectLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SleekBlue400,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        if (document.fileSizeFormatted.isNotBlank()) {
                            Text(
                                text = document.fileSizeFormatted,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (formattedDate.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    if (document.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = document.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Delete Icon
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_doc_${document.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: "Open", "Folder", "Share", and "Delete"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Open Button
                Button(
                    onClick = onOpen,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("open_doc_${document.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SleekBlue600
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Open",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }

                // Folder Tag Button
                OutlinedButton(
                    onClick = onChangeTag,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("tag_doc_${document.id}"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, SleekBlue400.copy(alpha = 0.7f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SleekBlue400
                    ),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Label,
                        contentDescription = "Folder Tag",
                        modifier = Modifier.size(14.dp),
                        tint = SleekBlue400
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Folder",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekBlue400,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }

                // Share Button
                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("share_doc_${document.id}"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, SleekGold400.copy(alpha = 0.7f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SleekGold400
                    ),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = SleekGold400
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Share",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekGold400,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }

                // Delete Button
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("delete_action_btn_doc_${document.id}"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete File",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Delete",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
