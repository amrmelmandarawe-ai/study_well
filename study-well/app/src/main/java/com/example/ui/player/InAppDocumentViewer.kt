package com.example.ui.player

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixNormal
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LastPage
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import com.example.ui.components.DocumentStudyIntroOverlay
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.utils.IntroPreferencesManager
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.text.style.TextAlign
import com.example.data.model.StudyMaterialEntity
import com.example.data.model.SubjectEnum
import com.example.ui.theme.SleekBlue400
import com.example.ui.theme.SleekBlue600
import com.example.ui.theme.SleekGold400
import com.example.ui.theme.SleekNavy800
import com.example.ui.theme.SleekNavy900
import com.example.ui.theme.SleekNavy950
import com.example.ui.theme.sleekTextFieldColors
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import com.example.utils.EditedDocumentPdfExporter
import com.example.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

enum class ReaderTheme(val bg: Color, val textColor: Color, val label: String, val colorMatrix: ColorMatrix?) {
    LIGHT(Color(0xFFFFFFFF), Color(0xFF1E293B), "Light", null),
    SEPIA(
        Color(0xFFFBF0D9),
        Color(0xFF5F4B32),
        "Sepia",
        ColorMatrix(
            floatArrayOf(
                0.90f, 0.05f, 0.05f, 0f, 0f,
                0.05f, 0.85f, 0.05f, 0f, 0f,
                0.05f, 0.05f, 0.70f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    ),
    DARK(
        Color(0xFF0F172A),
        Color(0xFFF1F5F9),
        "Dark",
        ColorMatrix(
            floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    )
}

data class HandStroke(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float,
    val isHighlighter: Boolean = false,
    val canvasWidth: Float = 0f,
    val canvasHeight: Float = 0f
) {
    val path: Path = Path().apply {
        if (points.isNotEmpty()) {
            moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InAppDocumentViewer(
    material: StudyMaterialEntity,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val subject = remember(material.subjectId) { SubjectEnum.fromId(material.subjectId) }

    // Inspect if material is PDF
    val isPdfCandidate = remember(material) {
        val url = material.contentUrl
        val isPdfPath = url.endsWith(".pdf", ignoreCase = true)
        val isLocalFile = url.startsWith("/") && File(url).exists()
        val isContentUri = url.startsWith("content://")
        val isBookType = material.materialType in listOf("BOOK", "NOTE", "EXAM")
        isPdfPath || (isLocalFile && File(url).extension.equals("pdf", ignoreCase = true)) || isContentUri || isBookType
    }

    var selectedViewMode by remember { mutableIntStateOf(if (isPdfCandidate) 0 else 1) } // 0: PDF Viewer, 1: Text Notes
    var showDocumentIntro by remember {
        mutableStateOf(!IntroPreferencesManager.hasSeenDocumentIntro(context))
    }
    var readerTheme by remember { mutableStateOf(ReaderTheme.LIGHT) }
    var isFullscreen by remember { mutableStateOf(false) }
    var isFocusReadingMode by remember { mutableStateOf(false) }
    var showThumbnailsDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    // Handwriting on PDF State
    var isHandwritingMode by remember { mutableStateOf(false) }
    var selectedPenColor by remember { mutableStateOf(Color(0xFFDC2626)) } // default Red for marking
    var selectedStrokeWidth by remember { mutableFloatStateOf(4.5f) }
    var isHighlighterMode by remember { mutableStateOf(false) }
    var isEraserMode by remember { mutableStateOf(false) }
    val pageStrokes = remember { mutableStateMapOf<Int, MutableList<HandStroke>>() }

    // Download Edited PDF Dialog States
    var showDownloadEditsDialog by remember { mutableStateOf(false) }
    var showJumpToPageDialog by remember { mutableStateOf(false) }
    var jumpPageInput by remember { mutableStateOf("") }
    var exportOnlyEditedPages by remember { mutableStateOf(false) }
    var isExportingPdf by remember { mutableStateOf(false) }
    var exportedPdfFile by remember { mutableStateOf<File?>(null) }
    var pendingCloseAfterExport by remember { mutableStateOf(false) }

    val hasHandwritingEdits by remember {
        derivedStateOf { pageStrokes.values.any { it.isNotEmpty() } }
    }
    val editedPageNumbers by remember {
        derivedStateOf {
            pageStrokes.filter { it.value.isNotEmpty() }.keys.map { it + 1 }.sorted()
        }
    }

    // PDF state
    var pdfTotalPages by remember { mutableIntStateOf(0) }
    var isPdfLoading by remember { mutableStateOf(true) }
    var pdfLoadError by remember { mutableStateOf<String?>(null) }
    var isPaginatedViewMode by remember { mutableStateOf(false) }
    var currentSinglePage by remember { mutableIntStateOf(0) }
    val pdfListState = rememberLazyListState()
    var useNativeEngine by remember { mutableStateOf(true) }
    var nativeCurrentPage by remember { mutableIntStateOf(0) }

    LaunchedEffect(isHandwritingMode) {
        if (isHandwritingMode) {
            useNativeEngine = false
        }
    }

    // Text reader state
    var fontSize by remember { mutableFloatStateOf(16f) }
    var isBookmarked by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }
    var ttsRef by remember { mutableStateOf<TextToSpeech?>(null) }

    // PDF Zoom & Pan state
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var effectivePdfFile by remember { mutableStateOf<File?>(null) }

    // Load PDF page count or resolve image document
    LaunchedEffect(material.contentUrl, material.id, material.title) {
        isPdfLoading = true
        pdfLoadError = null
        withContext(Dispatchers.IO) {
            val url = material.contentUrl
            val path = material.localFilePath
            val isImage = url.endsWith(".jpg", ignoreCase = true) || url.endsWith(".jpeg", ignoreCase = true) || url.endsWith(".png", ignoreCase = true) || url.endsWith(".webp", ignoreCase = true) ||
                          (path != null && (path.endsWith(".jpg", ignoreCase = true) || path.endsWith(".jpeg", ignoreCase = true) || path.endsWith(".png", ignoreCase = true) || path.endsWith(".webp", ignoreCase = true))) ||
                          material.materialType == "PHOTO" || material.materialType == "IMAGE" || url.contains("image/")

            var pageCount = 0

            try {
                if (isImage) {
                    pageCount = 1
                } else {
                    // Ensure there is a real, renderable PDF file on disk
                    val file = FileUtils.ensurePdfFile(context, material)
                    effectivePdfFile = file

                    if (file != null && file.exists()) {
                        pageCount = FileUtils.countPdfPagesFromFile(file)
                    } else if (url.startsWith("content://")) {
                        pageCount = FileUtils.countPdfPagesFromUri(context, Uri.parse(url))
                    }
                }

                // If no local file but has duration/pages indication
                if (pageCount == 0 && material.durationOrPages.contains("page", ignoreCase = true)) {
                    val count = Regex("(\\d+)\\s*page", RegexOption.IGNORE_CASE).find(material.durationOrPages)?.groupValues?.get(1)?.toIntOrNull()
                    if (count != null && count > 0) {
                        pageCount = count
                    }
                }

                pdfTotalPages = pageCount.coerceAtLeast(1)
                isPdfLoading = false
            } catch (e: Exception) {
                pdfLoadError = e.message ?: "Failed to open document"
                pdfTotalPages = 1
                isPdfLoading = false
            }
        }
    }

    // Load persisted annotations/handwriting edits for this document
    LaunchedEffect(material.contentUrl, material.title, effectivePdfFile) {
        withContext(Dispatchers.IO) {
            val keys = listOfNotNull(
                material.contentUrl.takeIf { it.isNotBlank() },
                material.title.takeIf { it.isNotBlank() },
                effectivePdfFile?.absolutePath,
                effectivePdfFile?.name
            )
            for (k in keys) {
                val loaded = com.example.utils.AnnotationPersistenceManager.loadStrokes(context, k)
                if (loaded.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        loaded.forEach { (pIdx, strokeList) ->
                            val existing = pageStrokes.getOrPut(pIdx) { mutableListOf() }
                            strokeList.forEach { s ->
                                if (!existing.contains(s)) {
                                    existing.add(s)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // TTS Cleanup
    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsRef?.language = Locale.UK
            }
        }
        ttsRef = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    // Current visible page calculation
    val composeFirstVisiblePage by remember {
        derivedStateOf {
            if (pdfTotalPages > 0) {
                (pdfListState.firstVisibleItemIndex + 1).coerceIn(1, pdfTotalPages)
            } else 1
        }
    }

    val firstVisiblePage by remember(useNativeEngine, nativeCurrentPage, isPaginatedViewMode, currentSinglePage, composeFirstVisiblePage) {
        derivedStateOf {
            if (useNativeEngine) {
                (nativeCurrentPage + 1).coerceIn(1, pdfTotalPages.coerceAtLeast(1))
            } else if (isPaginatedViewMode) {
                (currentSinglePage + 1).coerceIn(1, pdfTotalPages.coerceAtLeast(1))
            } else {
                composeFirstVisiblePage
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("in_app_document_viewer_screen"),
        color = readerTheme.bg
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
            // Header Bar (Hidden in Fullscreen or Focus Reading Mode) - Ultra Compact Single Row
            AnimatedVisibility(visible = !isFullscreen && !isFocusReadingMode) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Navigation Button
                        IconButton(
                            onClick = {
                                ttsRef?.stop()
                                if (hasHandwritingEdits) {
                                    pendingCloseAfterExport = true
                                    showDownloadEditsDialog = true
                                } else {
                                    onClose()
                                }
                            },
                            modifier = Modifier.testTag("close_document_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Close Document"
                            )
                        }

                        // Title & Subject Subtitle Column
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = material.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${subject.title} • ${material.materialType}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = subject.color,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp
                                )
                                if (material.id == 0L || material.uploadedBy == "External") {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(External)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = SleekGold400
                                    )
                                }
                            }
                        }

                        // Compact Actions Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            // Open Real PDF App Button (Pill)
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = SleekGold400.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, SleekGold400),
                                modifier = Modifier
                                    .padding(end = 3.dp)
                                    .clickable {
                                        val targetPathOrUri = effectivePdfFile?.absolutePath ?: material.localFilePath ?: material.contentUrl
                                        if (targetPathOrUri.isNotBlank()) {
                                            FileUtils.openPdfInExternalViewer(context, targetPathOrUri)
                                        } else {
                                            Toast.makeText(context, "Opening PDF...", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .testTag("open_real_pdf_app_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInNew,
                                        contentDescription = "Open Real PDF in System Viewer",
                                        tint = SleekGold400,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Open PDF",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = SleekNavy900
                                    )
                                }
                            }

                            // Focus Reading Mode Toggle Button (Pill)
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = SleekBlue600.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, SleekBlue400.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .padding(end = 3.dp)
                                    .clickable {
                                        isFocusReadingMode = true
                                        isHandwritingMode = false
                                        Toast.makeText(context, "Focus Reading Mode Enabled • Navigation hidden", Toast.LENGTH_SHORT).show()
                                    }
                                    .testTag("focus_reading_mode_toggle_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fullscreen,
                                        contentDescription = "Focus Reading Mode",
                                        tint = SleekBlue400,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Focus",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = SleekBlue400
                                    )
                                }
                            }

                            // Direct Edit / Handwrite Toggle Button (Pill)
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isHandwritingMode) SleekGold400 else SleekBlue600.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, if (isHandwritingMode) SleekGold400 else SleekBlue400.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .padding(end = 2.dp)
                                    .clickable {
                                        if (isHandwritingMode) {
                                            isHandwritingMode = false
                                            if (hasHandwritingEdits) {
                                                showDownloadEditsDialog = true
                                            }
                                        } else {
                                            isHandwritingMode = true
                                            zoomScale = 1f
                                            offsetX = 0f
                                            offsetY = 0f
                                        }
                                    }
                                    .testTag("pdf_edit_handwrite_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isHandwritingMode) Icons.Default.Check else Icons.Default.Edit,
                                        contentDescription = "Handwrite on PDF",
                                        tint = if (isHandwritingMode) SleekNavy950 else SleekBlue400,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (isHandwritingMode) "Done" else "Edit",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isHandwritingMode) SleekNavy950 else SleekBlue400
                                    )
                                }
                            }

                            // Quick Reset Zoom if zoomed
                            if (zoomScale != 1f) {
                                IconButton(
                                    onClick = {
                                        zoomScale = 1f
                                        offsetX = 0f
                                        offsetY = 0f
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RestartAlt,
                                        contentDescription = "Reset Zoom",
                                        tint = SleekGold400,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Document Intro & Tips Info Button
                            IconButton(
                                onClick = { showDocumentIntro = true },
                                modifier = Modifier.size(36.dp).testTag("document_intro_info_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Document Intro & Guidelines",
                                    tint = SleekBlue400,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Overflow Menu Button
                            Box {
                                IconButton(
                                    onClick = { showOverflowMenu = true },
                                    modifier = Modifier.size(38.dp).testTag("document_viewer_more_options")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More Options"
                                    )
                                }

                                DropdownMenu(
                                    expanded = showOverflowMenu,
                                    onDismissRequest = { showOverflowMenu = false }
                                ) {
                                    // 0. Intro & Guidelines
                                    DropdownMenuItem(
                                        text = { Text("Intro & Study Tips", fontSize = 13.sp) },
                                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = SleekBlue400, modifier = Modifier.size(18.dp)) },
                                        onClick = {
                                            showOverflowMenu = false
                                            showDocumentIntro = true
                                        }
                                    )

                                    // 1. Share PDF
                                    DropdownMenuItem(
                                        text = { Text("Share PDF", fontSize = 13.sp) },
                                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = SleekGold400, modifier = Modifier.size(18.dp)) },
                                        onClick = {
                                            showOverflowMenu = false
                                            if (hasHandwritingEdits) {
                                                showDownloadEditsDialog = true
                                            } else {
                                                coroutineScope.launch {
                                                    val currentFile = effectivePdfFile
                                                    val targetFile: File? = if (currentFile != null && currentFile.exists()) {
                                                        currentFile
                                                    } else {
                                                        FileUtils.ensurePdfFile(context, material)
                                                    }
                                                    if (targetFile != null && targetFile.exists()) {
                                                        EditedDocumentPdfExporter.shareDownloadedPdf(context, targetFile)
                                                    } else {
                                                        val temp = EditedDocumentPdfExporter.exportEditedNotesPdf(
                                                            context,
                                                            material,
                                                            material.documentContent
                                                        )
                                                        if (temp != null) {
                                                            EditedDocumentPdfExporter.shareDownloadedPdf(context, temp)
                                                        } else {
                                                            Toast.makeText(context, "Opening share options...", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    )

                                    // 2. Download Annotated PDF (if edited)
                                    if (hasHandwritingEdits) {
                                        DropdownMenuItem(
                                            text = { Text("Download Annotated PDF", fontSize = 13.sp) },
                                            leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null, tint = SleekGold400, modifier = Modifier.size(18.dp)) },
                                            onClick = {
                                                showOverflowMenu = false
                                                showDownloadEditsDialog = true
                                            }
                                        )
                                    }

                                    // 3. Page Grid Overview
                                    if (pdfTotalPages > 1) {
                                        DropdownMenuItem(
                                            text = { Text("Page Grid Overview", fontSize = 13.sp) },
                                            leadingIcon = { Icon(Icons.Default.GridView, contentDescription = null, tint = SleekBlue400, modifier = Modifier.size(18.dp)) },
                                            onClick = {
                                                showOverflowMenu = false
                                                showThumbnailsDialog = true
                                            }
                                        )
                                    }

                                    // 4. Open in External PDF Viewer
                                    if (material.contentUrl.isNotBlank() || effectivePdfFile != null) {
                                        DropdownMenuItem(
                                            text = { Text("Open External PDF App", fontSize = 13.sp) },
                                            leadingIcon = { Icon(Icons.Default.OpenInNew, contentDescription = null, tint = SleekGold400, modifier = Modifier.size(18.dp)) },
                                            onClick = {
                                                showOverflowMenu = false
                                                val target = effectivePdfFile?.absolutePath ?: material.contentUrl
                                                FileUtils.openPdfInExternalViewer(context, target)
                                            }
                                        )
                                    }

                                    // 5. Toggle Fullscreen
                                    DropdownMenuItem(
                                        text = { Text(if (isFullscreen) "Exit Fullscreen" else "Fullscreen View", fontSize = 13.sp) },
                                        leadingIcon = { Icon(if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                        onClick = {
                                            showOverflowMenu = false
                                            isFullscreen = !isFullscreen
                                        }
                                    )

                                    // 6. Bookmark
                                    DropdownMenuItem(
                                        text = { Text(if (isBookmarked) "Bookmarked ✓" else "Bookmark Document", fontSize = 13.sp) },
                                        leadingIcon = { Icon(if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, contentDescription = null, tint = if (isBookmarked) SleekGold400 else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) },
                                        onClick = {
                                            showOverflowMenu = false
                                            isBookmarked = !isBookmarked
                                        }
                                    )

                                    // 7. Toggle PDF Rendering Engine
                                    DropdownMenuItem(
                                        text = { Text(if (useNativeEngine) "Switch to Editor Mode" else "Switch to Native Smooth Reader", fontSize = 13.sp) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = if (useNativeEngine) Icons.Default.Edit else Icons.Default.MenuBook,
                                                contentDescription = null,
                                                tint = SleekBlue400,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        onClick = {
                                            showOverflowMenu = false
                                            useNativeEngine = !useNativeEngine
                                            if (useNativeEngine) {
                                                isHandwritingMode = false
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Dedicated Handwriting Studio Toolbar
            AnimatedVisibility(visible = isHandwritingMode && !isFocusReadingMode) {
                HandwritingToolbar(
                    selectedColor = selectedPenColor,
                    onColorSelected = {
                        selectedPenColor = it
                        isEraserMode = false
                    },
                    strokeWidth = selectedStrokeWidth,
                    onStrokeWidthSelected = { selectedStrokeWidth = it },
                    isHighlighter = isHighlighterMode,
                    onToggleHighlighter = {
                        isHighlighterMode = it
                        if (it) {
                            isEraserMode = false
                            if (selectedStrokeWidth < 8f) selectedStrokeWidth = 14f
                        } else {
                            if (selectedStrokeWidth >= 8f) selectedStrokeWidth = 4.5f
                        }
                    },
                    isEraser = isEraserMode,
                    onToggleEraser = { isEraserMode = it },
                    onUndo = {
                        val pg = (firstVisiblePage - 1).coerceAtLeast(0)
                        pageStrokes[pg]?.let { list ->
                            if (list.isNotEmpty()) list.removeAt(list.size - 1)
                        }
                    },
                    onClear = {
                        val pg = (firstVisiblePage - 1).coerceAtLeast(0)
                        pageStrokes[pg]?.clear()
                    },
                    currentPage = firstVisiblePage,
                    hasEdits = hasHandwritingEdits,
                    onFinishAndDownload = {
                        isHandwritingMode = false
                        showDownloadEditsDialog = true
                    }
                )
            }

            // Thumbnail Page Selector Dialog
            if (showThumbnailsDialog && pdfTotalPages > 0) {
                AlertDialog(
                    onDismissRequest = { showThumbnailsDialog = false },
                    title = {
                        Text("Jump to Page (1 - $pdfTotalPages)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    },
                    text = {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            contentPadding = PaddingValues(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.height(300.dp)
                        ) {
                            items(pdfTotalPages, key = { it }) { idx ->
                                val pageNum = idx + 1
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (pageNum == firstVisiblePage) SleekBlue600 else MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, if (pageNum == firstVisiblePage) SleekGold400 else MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .aspectRatio(0.8f)
                                        .clickable {
                                            if (useNativeEngine) {
                                                nativeCurrentPage = pageNum - 1
                                            } else {
                                                coroutineScope.launch {
                                                    pdfListState.animateScrollToItem(pageNum - 1)
                                                }
                                            }
                                            showThumbnailsDialog = false
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                Icons.Default.PictureAsPdf,
                                                contentDescription = null,
                                                tint = if (pageNum == firstVisiblePage) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "p.$pageNum",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (pageNum == firstVisiblePage) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showThumbnailsDialog = false }) {
                            Text("Close")
                        }
                    }
                )
            }

            if (showJumpToPageDialog) {
                AlertDialog(
                    onDismissRequest = { showJumpToPageDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = SleekBlue600.copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = SleekBlue400,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Go to Page Number", fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        Column {
                            Text(
                                "Enter a page number between 1 and $pdfTotalPages:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = jumpPageInput,
                                onValueChange = { input ->
                                    if (input.isEmpty() || input.all { it.isDigit() }) {
                                        jumpPageInput = input
                                    }
                                },
                                label = { Text("Page Number (1-$pdfTotalPages)") },
                                placeholder = { Text("e.g. 1") },
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                ),
                                colors = sleekTextFieldColors(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val target = jumpPageInput.toIntOrNull()
                                if (target != null && target in 1..pdfTotalPages) {
                                    if (useNativeEngine) {
                                        nativeCurrentPage = target - 1
                                    } else if (isPaginatedViewMode) {
                                        currentSinglePage = target - 1
                                    } else {
                                        coroutineScope.launch {
                                            pdfListState.scrollToItem(target - 1)
                                        }
                                    }
                                    showJumpToPageDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SleekBlue600)
                        ) {
                            Text("Jump to Page")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showJumpToPageDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Main Viewer Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clipToBounds()
                    .background(readerTheme.bg)
            ) {
                if (selectedViewMode == 0) {
                    // PDF Renderer View
                    if (isPdfLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = SleekGold400)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Loading PDF document...", color = readerTheme.textColor, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    } else if (pdfTotalPages > 0) {
                        // Render pages
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .then(
                                    if (!isHandwritingMode && !useNativeEngine) {
                                        Modifier.pointerInput(Unit) {
                                            detectTransformGestures { _, pan, zoom, _ ->
                                                zoomScale = (zoomScale * zoom).coerceIn(0.8f, 3.5f)
                                                if (zoomScale > 1.0f) {
                                                    offsetX += pan.x
                                                    offsetY += pan.y
                                                } else {
                                                    offsetX = 0f
                                                    offsetY = 0f
                                                }
                                            }
                                        }
                                    } else Modifier
                                )
                        ) {
                            if (useNativeEngine && effectivePdfFile != null) {
                                NativePdfRendererView(
                                    pdfFile = effectivePdfFile!!,
                                    readerTheme = readerTheme,
                                    targetPage = nativeCurrentPage,
                                    onPageChanged = { page ->
                                        nativeCurrentPage = page
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else if (isPaginatedViewMode) {
                                // Single-Page Paginated Mode for large documents
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp, vertical = 16.dp),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    val safePageIdx = currentSinglePage.coerceIn(0, (pdfTotalPages - 1).coerceAtLeast(0))
                                    PdfPageCard(
                                        material = material,
                                        pdfFile = effectivePdfFile,
                                        pageIndex = safePageIdx,
                                        pageNumber = safePageIdx + 1,
                                        totalPages = pdfTotalPages,
                                        readerTheme = readerTheme,
                                        context = context,
                                        isHandwritingMode = isHandwritingMode,
                                        selectedPenColor = selectedPenColor,
                                        selectedStrokeWidth = selectedStrokeWidth,
                                        isHighlighterMode = isHighlighterMode,
                                        isEraserMode = isEraserMode,
                                        pageStrokes = pageStrokes[safePageIdx] ?: emptyList(),
                                        onSaveStroke = { completedStroke ->
                                            val list = pageStrokes.getOrPut(safePageIdx) { mutableListOf() }
                                            list.add(completedStroke)
                                            val keys = listOfNotNull(
                                                material.contentUrl.takeIf { it.isNotBlank() },
                                                material.title.takeIf { it.isNotBlank() },
                                                effectivePdfFile?.absolutePath,
                                                effectivePdfFile?.name
                                            )
                                            keys.forEach { k ->
                                                com.example.utils.AnnotationPersistenceManager.saveStrokes(context, k, pageStrokes.toMap())
                                            }
                                        },
                                        onEraseNear = { pt ->
                                            pageStrokes[safePageIdx]?.let { list ->
                                                list.removeAll { stroke ->
                                                    stroke.points.any { p ->
                                                        val dx = p.x - pt.x
                                                        val dy = p.y - pt.y
                                                        (dx * dx + dy * dy) < (35f * 35f)
                                                    }
                                                }
                                            }
                                        },
                                        onEraseDone = {
                                            val keys = listOfNotNull(
                                                material.contentUrl.takeIf { it.isNotBlank() },
                                                material.title.takeIf { it.isNotBlank() },
                                                effectivePdfFile?.absolutePath,
                                                effectivePdfFile?.name
                                            )
                                            keys.forEach { k ->
                                                com.example.utils.AnnotationPersistenceManager.saveStrokes(context, k, pageStrokes.toMap())
                                            }
                                        }
                                    )
                                }
                            } else {
                                // Continuous Scroll Lazy Loading Mode
                                LazyColumn(
                                    state = pdfListState,
                                    userScrollEnabled = !isHandwritingMode,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            scaleX = zoomScale
                                            scaleY = zoomScale
                                            translationX = offsetX
                                            translationY = offsetY
                                        },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(pdfTotalPages, key = { it }, contentType = { "pdf_page_card" }) { pageIdx ->
                                        val pageNumber = pageIdx + 1
                                        PdfPageCard(
                                            material = material,
                                            pdfFile = effectivePdfFile,
                                            pageIndex = pageIdx,
                                            pageNumber = pageNumber,
                                            totalPages = pdfTotalPages,
                                            readerTheme = readerTheme,
                                            context = context,
                                            isHandwritingMode = isHandwritingMode,
                                            selectedPenColor = selectedPenColor,
                                            selectedStrokeWidth = selectedStrokeWidth,
                                            isHighlighterMode = isHighlighterMode,
                                            isEraserMode = isEraserMode,
                                            pageStrokes = pageStrokes[pageIdx] ?: emptyList(),
                                            onSaveStroke = { completedStroke ->
                                                val list = pageStrokes.getOrPut(pageIdx) { mutableListOf() }
                                                list.add(completedStroke)
                                                val keys = listOfNotNull(
                                                    material.contentUrl.takeIf { it.isNotBlank() },
                                                    material.title.takeIf { it.isNotBlank() },
                                                    effectivePdfFile?.absolutePath,
                                                    effectivePdfFile?.name
                                                )
                                                keys.forEach { k ->
                                                    com.example.utils.AnnotationPersistenceManager.saveStrokes(context, k, pageStrokes.toMap())
                                                }
                                            },
                                            onEraseNear = { pt ->
                                                pageStrokes[pageIdx]?.let { list ->
                                                    list.removeAll { stroke ->
                                                        stroke.points.any { p ->
                                                            val dx = p.x - pt.x
                                                            val dy = p.y - pt.y
                                                            (dx * dx + dy * dy) < (35f * 35f)
                                                        }
                                                    }
                                                }
                                            },
                                            onEraseDone = {
                                                val keys = listOfNotNull(
                                                    material.contentUrl.takeIf { it.isNotBlank() },
                                                    material.title.takeIf { it.isNotBlank() },
                                                    effectivePdfFile?.absolutePath,
                                                    effectivePdfFile?.name
                                                )
                                                keys.forEach { k ->
                                                    com.example.utils.AnnotationPersistenceManager.saveStrokes(context, k, pageStrokes.toMap())
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            // Floating Bottom Page Navigator (Hidden in Focus Reading Mode)
                            if (!isFocusReadingMode) {
                                Surface(
                                    shape = RoundedCornerShape(24.dp),
                                    color = SleekNavy950.copy(alpha = 0.92f),
                                    border = BorderStroke(1.dp, SleekBlue400.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 16.dp)
                                ) {
                                    val displayPage = if (isPaginatedViewMode) currentSinglePage + 1 else firstVisiblePage
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (useNativeEngine) {
                                                    nativeCurrentPage = 0
                                                } else if (isPaginatedViewMode) {
                                                    currentSinglePage = 0
                                                } else {
                                                    coroutineScope.launch {
                                                        pdfListState.animateScrollToItem(0)
                                                    }
                                                }
                                            },
                                            enabled = displayPage > 1,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.FirstPage, contentDescription = "First Page", tint = Color.White)
                                        }

                                        IconButton(
                                            onClick = {
                                                if (useNativeEngine) {
                                                    nativeCurrentPage = (nativeCurrentPage - 1).coerceAtLeast(0)
                                                } else if (isPaginatedViewMode) {
                                                    currentSinglePage = (currentSinglePage - 1).coerceAtLeast(0)
                                                } else {
                                                    coroutineScope.launch {
                                                        val prev = (firstVisiblePage - 2).coerceAtLeast(0)
                                                        pdfListState.animateScrollToItem(prev)
                                                    }
                                                }
                                            },
                                            enabled = displayPage > 1,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.NavigateBefore, contentDescription = "Prev Page", tint = Color.White)
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = SleekBlue600.copy(alpha = 0.45f),
                                            modifier = Modifier
                                                .padding(horizontal = 4.dp)
                                                .clickable {
                                                    jumpPageInput = displayPage.toString()
                                                    showJumpToPageDialog = true
                                                }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "Jump to page",
                                                    tint = SleekGold400,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Page $displayPage / $pdfTotalPages",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                if (useNativeEngine) {
                                                    nativeCurrentPage = (nativeCurrentPage + 1).coerceAtMost(pdfTotalPages - 1)
                                                } else if (isPaginatedViewMode) {
                                                    currentSinglePage = (currentSinglePage + 1).coerceAtMost(pdfTotalPages - 1)
                                                } else {
                                                    coroutineScope.launch {
                                                        val next = (firstVisiblePage).coerceAtMost(pdfTotalPages - 1)
                                                        pdfListState.animateScrollToItem(next)
                                                    }
                                                }
                                            },
                                            enabled = displayPage < pdfTotalPages,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.NavigateNext, contentDescription = "Next Page", tint = Color.White)
                                        }

                                        IconButton(
                                            onClick = {
                                                if (useNativeEngine) {
                                                    nativeCurrentPage = pdfTotalPages - 1
                                                } else if (isPaginatedViewMode) {
                                                    currentSinglePage = pdfTotalPages - 1
                                                } else {
                                                    coroutineScope.launch {
                                                        pdfListState.animateScrollToItem(pdfTotalPages - 1)
                                                    }
                                                }
                                            },
                                            enabled = displayPage < pdfTotalPages,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.LastPage, contentDescription = "Last Page", tint = Color.White)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 4.dp)
                                                .size(width = 1.dp, height = 20.dp)
                                                .background(Color.White.copy(alpha = 0.3f))
                                        )

                                        // View Mode Switcher (Continuous vs Paginated)
                                        IconButton(
                                            onClick = {
                                                if (!isPaginatedViewMode) {
                                                    currentSinglePage = (firstVisiblePage - 1).coerceIn(0, (pdfTotalPages - 1).coerceAtLeast(0))
                                                }
                                                isPaginatedViewMode = !isPaginatedViewMode
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isPaginatedViewMode) Icons.Default.FormatListBulleted else Icons.Default.MenuBook,
                                                contentDescription = if (isPaginatedViewMode) "Switch to Continuous Scroll" else "Switch to Single Page Mode",
                                                tint = SleekGold400
                                            )
                                        }
                                    }
                                }
                            }

                            // Floating Unobtrusive Exit Focus Button Overlay
                            if (isFocusReadingMode) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = SleekNavy950.copy(alpha = 0.85f),
                                    border = BorderStroke(1.dp, SleekGold400.copy(alpha = 0.6f)),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 16.dp, end = 16.dp)
                                        .clickable {
                                            isFocusReadingMode = false
                                            Toast.makeText(context, "Restored standard reading controls", Toast.LENGTH_SHORT).show()
                                        }
                                        .testTag("exit_focus_reading_mode_overlay_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FullscreenExit,
                                            contentDescription = "Exit Focus Reading Mode",
                                            tint = SleekGold400,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Exit Focus",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Fallback PDF Info Banner
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = SleekBlue600.copy(alpha = 0.15f),
                                modifier = Modifier.size(72.dp)
                            ) {
                                Icon(
                                    Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = SleekBlue400,
                                    modifier = Modifier.padding(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = material.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = readerTheme.textColor,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Subject: ${subject.title} • Topic: ${material.topic}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = subject.color,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = material.description.ifBlank { "Full IGCSE Cambridge study guide and document notes." },
                                style = MaterialTheme.typography.bodySmall,
                                color = readerTheme.textColor.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            if (material.contentUrl.isNotBlank()) {
                                ButtonWithIcon(
                                    text = "Open PDF in Google Drive / Reader",
                                    icon = Icons.Default.OpenInNew,
                                    onClick = {
                                        FileUtils.openExternalUriOrUrl(context, material.contentUrl)
                                    }
                                )
                            }


                        }
                    }
                } else {
                    // Text / Notes Reader Canvas
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Surface(
                            color = subject.color.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "CAMBRIDGE IGCSE SYLLABUS: ${subject.syllabusCode}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = subject.color
                                    )
                                    Text(
                                        text = material.topic,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = readerTheme.textColor
                                    )
                                }
                                Text(
                                    text = material.durationOrPages,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = subject.color,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val contentText = material.documentContent.ifBlank {
                            "Cambridge IGCSE ${subject.title} Topic Notes:\n\n• Unit: ${material.topic}\n• Summary: ${material.description}\n\nKey Concepts & Core Mark Scheme Objectives:\n1. Understand fundamental definitions and units.\n2. Apply core formulas with step-by-step working.\n3. Verify exam past paper question patterns."
                        }

                        Text(
                            text = contentText,
                            color = readerTheme.textColor,
                            fontSize = fontSize.sp,
                            lineHeight = (fontSize * 1.55f).sp,
                            fontFamily = FontFamily.Default,
                            modifier = Modifier.testTag("document_reader_text")
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Divider(modifier = Modifier.weight(1f))
                            Text(
                                text = "  End of Unit • Study Well IGCSE  ",
                                style = MaterialTheme.typography.labelSmall,
                                color = readerTheme.textColor.copy(alpha = 0.6f)
                            )
                            Divider(modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Fullscreen floating exit button
                if (isFullscreen) {
                    IconButton(
                        onClick = { isFullscreen = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .background(SleekNavy950.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(Icons.Default.FullscreenExit, contentDescription = "Exit Fullscreen", tint = Color.White)
                    }
                }
            }
        }

        // ==========================================
        // 1. Download Edited Part as PDF Dialog
        // ==========================================
        if (showDownloadEditsDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDownloadEditsDialog = false
                    if (pendingCloseAfterExport) {
                        pendingCloseAfterExport = false
                        onClose()
                    }
                },
                icon = {
                    Surface(
                        shape = CircleShape,
                        color = SleekGold400.copy(alpha = 0.15f),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = SleekGold400,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                },
                title = {
                    Text(
                        text = "Download Edited Document?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (editedPageNumbers.isNotEmpty()) {
                                "You made annotations on ${editedPageNumbers.size} page(s) (${editedPageNumbers.joinToString { "p.$it" }}). Would you like to download the edited part to your device as a PDF?"
                            } else {
                                "Would you like to download this document as a PDF to your device?"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Option 1: Only edited pages (The exact user request: "the part that he makes a edite")
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (exportOnlyEditedPages) SleekBlue600.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.5.dp, if (exportOnlyEditedPages) SleekBlue600 else Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { exportOnlyEditedPages = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = exportOnlyEditedPages,
                                    onClick = { exportOnlyEditedPages = true }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Download Edited Part Only",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "${editedPageNumbers.size.coerceAtLeast(1)} edited page(s) with your drawings & notes",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Option 2: Full Document with edits
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (!exportOnlyEditedPages) SleekBlue600.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.5.dp, if (!exportOnlyEditedPages) SleekBlue600 else Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { exportOnlyEditedPages = false }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = !exportOnlyEditedPages,
                                    onClick = { exportOnlyEditedPages = false }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Download Full Document",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "All $pdfTotalPages pages including your edits",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDownloadEditsDialog = false
                            isExportingPdf = true
                            coroutineScope.launch {
                                val exported = EditedDocumentPdfExporter.exportAnnotatedPdf(
                                    context = context,
                                    material = material,
                                    pdfFile = effectivePdfFile,
                                    totalPages = pdfTotalPages,
                                    pageStrokes = pageStrokes.toMap(),
                                    onlyEditedPages = exportOnlyEditedPages
                                )
                                isExportingPdf = false
                                if (exported != null) {
                                    exportedPdfFile = exported
                                } else {
                                    android.widget.Toast.makeText(context, "Failed to create PDF. Please try again.", android.widget.Toast.LENGTH_SHORT).show()
                                    if (pendingCloseAfterExport) {
                                        pendingCloseAfterExport = false
                                        onClose()
                                    }
                                }
                            }
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = SleekGold400,
                            contentColor = SleekNavy950
                        )
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download PDF", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDownloadEditsDialog = false
                            if (pendingCloseAfterExport) {
                                pendingCloseAfterExport = false
                                onClose()
                            }
                        }
                    ) {
                        Text(if (pendingCloseAfterExport) "Exit Without Saving" else "Cancel")
                    }
                }
            )
        }

        // ==========================================
        // 2. Exporting / Generating Progress Dialog
        // ==========================================
        if (isExportingPdf) {
            Dialog(onDismissRequest = {}) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = SleekGold400)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Generating Edited PDF...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Rendering your annotations and saving to Downloads...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // ==========================================
        // 3. Download Success Dialog
        // ==========================================
        exportedPdfFile?.let { savedFile ->
            AlertDialog(
                onDismissRequest = {
                    exportedPdfFile = null
                    if (pendingCloseAfterExport) {
                        pendingCloseAfterExport = false
                        onClose()
                    }
                },
                icon = {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                },
                title = {
                    Text(
                        text = "PDF Downloaded to Device!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Your edited PDF has been successfully generated and saved to your device's Downloads folder.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = savedFile.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Saved in Downloads folder",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            EditedDocumentPdfExporter.openDownloadedPdf(context, savedFile)
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = SleekBlue600,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open PDF")
                    }
                },
                dismissButton = {
                    Row {
                        OutlinedButton(
                            onClick = {
                                EditedDocumentPdfExporter.shareDownloadedPdf(context, savedFile)
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                exportedPdfFile = null
                                if (pendingCloseAfterExport) {
                                    pendingCloseAfterExport = false
                                    onClose()
                                }
                            }
                        ) {
                            Text("Done")
                        }
                    }
                }
            )
        }

            // Document Study Intro Overlay (Presented when opening any document/material)
            if (showDocumentIntro) {
                DocumentStudyIntroOverlay(
                    material = material,
                    subject = subject,
                    onStartReading = {
                        IntroPreferencesManager.markDocumentIntroSeen(context)
                        showDocumentIntro = false
                    },
                    onClose = onClose
                )
            }
        }
    }
}

@Composable
private fun PdfPageCard(
    material: StudyMaterialEntity,
    pdfFile: File? = null,
    pageIndex: Int,
    pageNumber: Int,
    totalPages: Int,
    readerTheme: ReaderTheme,
    context: Context,
    isHandwritingMode: Boolean,
    selectedPenColor: Color,
    selectedStrokeWidth: Float,
    isHighlighterMode: Boolean,
    isEraserMode: Boolean,
    pageStrokes: List<HandStroke>,
    onSaveStroke: (HandStroke) -> Unit,
    onEraseNear: (Offset) -> Unit,
    onEraseDone: () -> Unit = {}
) {
    var pageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isRendering by remember { mutableStateOf(true) }
    val localActivePoints = remember { mutableListOf<Offset>() }
    var drawTrigger by remember { mutableIntStateOf(0) }
    val activePath = remember { Path() }
    var cardWidthPx by remember { mutableFloatStateOf(0f) }
    var cardHeightPx by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(pageIndex, material.contentUrl, material.title, pdfFile) {
        withContext(Dispatchers.IO) {
            val url = material.contentUrl
            val path = material.localFilePath
            val isImage = url.endsWith(".jpg", ignoreCase = true) || url.endsWith(".jpeg", ignoreCase = true) || url.endsWith(".png", ignoreCase = true) || url.endsWith(".webp", ignoreCase = true) ||
                          (path != null && (path.endsWith(".jpg", ignoreCase = true) || path.endsWith(".jpeg", ignoreCase = true) || path.endsWith(".png", ignoreCase = true) || path.endsWith(".webp", ignoreCase = true))) ||
                          material.materialType == "PHOTO" || material.materialType == "IMAGE" || url.contains("image/")

            var bmp: Bitmap? = null
            if (isImage) {
                if (!path.isNullOrBlank() && File(path).exists()) {
                    bmp = FileUtils.loadBitmapFromFile(File(path), maxDim = 1200)?.let { FileUtils.fixImageRotationIfRequired(path, it) }
                } else if (url.startsWith("content://")) {
                    bmp = FileUtils.loadBitmapFromUri(context, Uri.parse(url), maxDim = 1200)
                } else if (url.startsWith("/")) {
                    val f = File(url)
                    if (f.exists()) {
                        bmp = FileUtils.loadBitmapFromFile(f, maxDim = 1200)?.let { FileUtils.fixImageRotationIfRequired(url, it) }
                    }
                }
            } else {
                val resolvedFile = pdfFile?.takeIf { it.exists() && it.length() > 50 }
                    ?: FileUtils.ensurePdfFile(context, material)

                if (resolvedFile != null && resolvedFile.exists()) {
                    bmp = FileUtils.renderPdfPage(resolvedFile, pageIndex, targetWidth = 1080)
                }
                if (bmp == null) {
                    if (url.startsWith("/") && File(url).exists()) {
                        bmp = FileUtils.renderPdfPage(File(url), pageIndex, targetWidth = 1080)
                    } else if (url.startsWith("content://")) {
                        bmp = FileUtils.renderPdfPageFromUri(context, Uri.parse(url), pageIndex, targetWidth = 1080)
                    }
                }
            }

            pageBitmap = bmp
            isRendering = false

            // Asynchronously prefetch next page into LRU cache for 0ms scroll latency (if PDF)
            if (!isImage && pageIndex < totalPages - 1) {
                val resolvedFile = pdfFile?.takeIf { it.exists() && it.length() > 50 }
                if (resolvedFile != null && resolvedFile.exists()) {
                    FileUtils.renderPdfPage(resolvedFile, pageIndex + 1, targetWidth = 1080)
                }
            }
        }
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Page Number Indicator Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF1F5F9))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Page $pageNumber of $totalPages",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF475569),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isHandwritingMode) "✍️ Draw / Handwrite Active" else "IGCSE Study Well",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isHandwritingMode) SleekGold400 else Color(0xFF94A3B8),
                    fontWeight = if (isHandwritingMode) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 10.sp
                )
            }

            if (isRendering) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), color = SleekBlue600)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Rendering page $pageNumber...", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clipToBounds()
                        .onSizeChanged { sz ->
                            if (sz.width > 0 && sz.height > 0) {
                                cardWidthPx = sz.width.toFloat()
                                cardHeightPx = sz.height.toFloat()
                            }
                        }
                        .then(
                            if (isHandwritingMode) {
                                Modifier.pointerInput(pageIndex, isEraserMode, selectedPenColor, selectedStrokeWidth, isHighlighterMode, cardWidthPx, cardHeightPx) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            val clampedPos = Offset(
                                                x = offset.x.coerceIn(0f, cardWidthPx.coerceAtLeast(1f)),
                                                y = offset.y.coerceIn(0f, cardHeightPx.coerceAtLeast(1f))
                                            )
                                            if (isEraserMode) {
                                                onEraseNear(clampedPos)
                                            } else {
                                                localActivePoints.clear()
                                                localActivePoints.add(clampedPos)
                                                drawTrigger++
                                            }
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            val clampedPos = Offset(
                                                x = change.position.x.coerceIn(0f, cardWidthPx.coerceAtLeast(1f)),
                                                y = change.position.y.coerceIn(0f, cardHeightPx.coerceAtLeast(1f))
                                            )
                                            if (isEraserMode) {
                                                onEraseNear(clampedPos)
                                            } else {
                                                localActivePoints.add(clampedPos)
                                                drawTrigger++
                                            }
                                        },
                                        onDragEnd = {
                                            if (!isEraserMode && localActivePoints.isNotEmpty()) {
                                                val completedStroke = HandStroke(
                                                    points = localActivePoints.toList(),
                                                    color = selectedPenColor,
                                                    strokeWidth = selectedStrokeWidth,
                                                    isHighlighter = isHighlighterMode,
                                                    canvasWidth = if (cardWidthPx > 0f) cardWidthPx else 850f,
                                                    canvasHeight = if (cardHeightPx > 0f) cardHeightPx else 1200f
                                                )
                                                onSaveStroke(completedStroke)
                                                localActivePoints.clear()
                                                drawTrigger++
                                            } else if (isEraserMode) {
                                                onEraseDone()
                                            }
                                        },
                                        onDragCancel = {
                                            localActivePoints.clear()
                                            drawTrigger++
                                            if (isEraserMode) {
                                                onEraseDone()
                                            }
                                        }
                                    )
                                }
                            } else Modifier
                        )
                ) {
                    if (isRendering) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .background(Color(0xFFF8FAFC)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 2.5.dp,
                                    color = SleekGold400
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Rendering Page $pageNumber...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    } else if (pageBitmap != null) {
                        val rememberImageBitmap = remember(pageBitmap) { pageBitmap?.asImageBitmap() }
                        if (rememberImageBitmap != null) {
                            Image(
                                bitmap = rememberImageBitmap,
                                contentDescription = "Page $pageNumber",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)),
                                contentScale = ContentScale.FillWidth,
                                colorFilter = readerTheme.colorMatrix?.let { ColorFilter.colorMatrix(it) }
                            )
                        }
                    } else {
                        val isImage = material.contentUrl.endsWith(".jpg", ignoreCase = true) || material.contentUrl.endsWith(".png", ignoreCase = true) || material.materialType == "PHOTO" || material.contentUrl.contains("image/")
                        if (isImage) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(350.dp)
                                    .background(Color(0xFFF8FAFC)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = SleekGold400,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Unable to display photo page.",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Check file permissions or re-upload the image.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF64748B),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            // Real PDF page fallback card with one-click launch into system PDF viewer app
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(380.dp)
                                    .background(Color(0xFFF8FAFC)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        tint = SleekBlue600,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "PDF Page $pageNumber of $totalPages",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Tap below to view this page directly in your device's native PDF reader app.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF64748B),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            val targetPath = pdfFile?.absolutePath ?: material.localFilePath ?: material.contentUrl
                                            FileUtils.openPdfInExternalViewer(context, targetPath)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SleekBlue600,
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.testTag("open_external_pdf_viewer_page_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInNew,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Open Real PDF Viewer")
                                    }
                                }
                            }
                        }
                    }

                    // Drawing Canvas overlay for handwriting strokes (zero GC allocation during playback)
                    Canvas(modifier = Modifier.matchParentSize().clipToBounds()) {
                        @Suppress("UNUSED_VARIABLE")
                        val tick = drawTrigger

                        // Draw saved strokes using precomputed Cached Path
                        pageStrokes.forEach { stroke ->
                            if (stroke.points.size > 1) {
                                drawPath(
                                    path = stroke.path,
                                    color = if (stroke.isHighlighter) stroke.color.copy(alpha = 0.38f) else stroke.color,
                                    style = Stroke(
                                        width = stroke.strokeWidth,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            } else if (stroke.points.size == 1) {
                                drawCircle(
                                    color = if (stroke.isHighlighter) stroke.color.copy(alpha = 0.38f) else stroke.color,
                                    radius = stroke.strokeWidth / 2,
                                    center = stroke.points[0]
                                )
                            }
                        }

                        // Draw active stroke locally while drawing
                        if (localActivePoints.size > 1) {
                            activePath.reset()
                            activePath.moveTo(localActivePoints[0].x, localActivePoints[0].y)
                            for (i in 1 until localActivePoints.size) {
                                activePath.lineTo(localActivePoints[i].x, localActivePoints[i].y)
                            }
                            drawPath(
                                path = activePath,
                                color = if (isHighlighterMode) selectedPenColor.copy(alpha = 0.38f) else selectedPenColor,
                                style = Stroke(
                                    width = selectedStrokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        } else if (localActivePoints.size == 1) {
                            drawCircle(
                                color = if (isHighlighterMode) selectedPenColor.copy(alpha = 0.38f) else selectedPenColor,
                                radius = selectedStrokeWidth / 2,
                                center = localActivePoints[0]
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DirectPdfPageContent(
    material: StudyMaterialEntity,
    pageNumber: Int,
    totalPages: Int,
    readerTheme: ReaderTheme,
    modifier: Modifier = Modifier
) {
    val subject = remember(material.subjectId) { SubjectEnum.fromId(material.subjectId) }
    val isDark = readerTheme == ReaderTheme.DARK
    val isSepia = readerTheme == ReaderTheme.SEPIA

    val backgroundColor = when {
        isDark -> Color(0xFF0F172A)
        isSepia -> Color(0xFFFBF0D9)
        else -> Color.White
    }

    val primaryTextColor = when {
        isDark -> Color(0xFFF1F5F9)
        isSepia -> Color(0xFF3B2F1E)
        else -> Color(0xFF0F172A)
    }

    val secondaryTextColor = when {
        isDark -> Color(0xFF94A3B8)
        isSepia -> Color(0xFF6E5D46)
        else -> Color(0xFF475569)
    }

    val headerBannerBg = when {
        isDark -> Color(0xFF1E293B)
        isSepia -> Color(0xFFEFE2C5)
        else -> Color(0xFF0F172A)
    }

    val headerTextColor = when {
        isDark -> Color.White
        isSepia -> Color(0xFF3B2F1E)
        else -> Color.White
    }

    // Extract content for this specific page
    val rawText = remember(material.documentContent, material.description, material.title) {
        if (material.documentContent.isNotBlank()) {
            material.documentContent
        } else {
            "${material.title}\n\n${material.description}"
        }
    }

    val pageContentLines = remember(rawText, pageNumber, totalPages) {
        val lines = rawText.lines()
        if (totalPages <= 1 || lines.size <= 25) {
            lines
        } else {
            val linesPerPage = (lines.size + totalPages - 1) / totalPages
            val start = ((pageNumber - 1) * linesPerPage).coerceIn(0, lines.size)
            val end = (start + linesPerPage).coerceIn(0, lines.size)
            if (start < lines.size) lines.subList(start, end) else emptyList()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(bottom = 16.dp)
    ) {
        // 1. Official Cambridge / Edexcel Header Banner
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBannerBg)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CAMBRIDGE IGCSE • ${subject.title.uppercase()}",
                    color = headerTextColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = SleekGold400,
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    Text(
                        text = subject.syllabusCode,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${material.topic} • ${material.title}",
                color = if (isDark || !isSepia) Color(0xFFCBD5E1) else Color(0xFF78654C),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Gold Accent Line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(SleekGold400)
        )

        // 2. Page Body Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            pageContentLines.forEach { line ->
                val trimmed = line.trim()
                when {
                    trimmed.isEmpty() -> {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    trimmed.startsWith("# ") -> {
                        Text(
                            text = trimmed.removePrefix("# ").trim(),
                            color = primaryTextColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp
                        )
                    }
                    trimmed.startsWith("## ") -> {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isDark) SleekNavy800.copy(alpha = 0.5f) else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, SleekBlue400.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = trimmed.removePrefix("## ").trim(),
                                color = if (isDark) SleekBlue400 else SleekNavy900,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                    trimmed.startsWith("### ") -> {
                        Text(
                            text = trimmed.removePrefix("### ").trim(),
                            color = primaryTextColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    trimmed.startsWith("•") || trimmed.startsWith("-") || trimmed.startsWith("* ") -> {
                        val bulletText = trimmed.drop(1).trim()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "• ",
                                color = SleekBlue600,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(start = 4.dp, end = 6.dp)
                            )
                            Text(
                                text = bulletText.replace("**", ""),
                                color = primaryTextColor,
                                fontSize = 12.5.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    trimmed.startsWith("---") || trimmed.startsWith("***") -> {
                        Divider(
                            color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                            thickness = 1.dp
                        )
                    }
                    trimmed.contains("[") && (trimmed.contains("Mark") || trimmed.contains("marks")) -> {
                        // Exam mark callout
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, SleekGold400.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = trimmed.replace("**", ""),
                                    color = primaryTextColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    else -> {
                        Text(
                            text = trimmed.replace("**", ""),
                            color = if (trimmed.startsWith("Formula:") || trimmed.startsWith("Calculation:")) SleekNavy900 else secondaryTextColor,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // 3. Footer
        Spacer(modifier = Modifier.height(12.dp))
        Divider(
            color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 18.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Page $pageNumber of $totalPages",
                color = secondaryTextColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "IGCSE Study Well • Official Curriculum",
                color = secondaryTextColor,
                fontSize = 10.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandwritingToolbar(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    strokeWidth: Float,
    onStrokeWidthSelected: (Float) -> Unit,
    isHighlighter: Boolean,
    onToggleHighlighter: (Boolean) -> Unit,
    isEraser: Boolean,
    onToggleEraser: (Boolean) -> Unit,
    onUndo: () -> Unit,
    onClear: () -> Unit,
    currentPage: Int,
    hasEdits: Boolean = false,
    onFinishAndDownload: () -> Unit = {}
) {
    val penColors = listOf(
        Color(0xFFDC2626), // Red
        Color(0xFF2563EB), // Blue
        Color(0xFF059669), // Green
        Color(0xFFD97706), // Amber
        Color(0xFF7C3AED), // Purple
        Color(0xFF0F172A)  // Slate Black
    )

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tools: Pen, Highlighter, Eraser
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pen
                    FilterChip(
                        selected = !isHighlighter && !isEraser,
                        onClick = {
                            onToggleHighlighter(false)
                            onToggleEraser(false)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Brush, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = { Text("Pen", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SleekGold400,
                            selectedLabelColor = SleekNavy950,
                            selectedLeadingIconColor = SleekNavy950
                        )
                    )

                    // Highlighter
                    FilterChip(
                        selected = isHighlighter && !isEraser,
                        onClick = {
                            onToggleEraser(false)
                            onToggleHighlighter(true)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Highlight, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = { Text("Highlighter", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFBBF24),
                            selectedLabelColor = Color(0xFF78350F),
                            selectedLeadingIconColor = Color(0xFF78350F)
                        )
                    )

                    // Eraser
                    FilterChip(
                        selected = isEraser,
                        onClick = {
                            onToggleEraser(true)
                            onToggleHighlighter(false)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.AutoFixNormal, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = { Text("Eraser", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    )
                }

                // Undo & Clear Actions and Finish & Save
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasEdits) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = SleekGold400,
                            modifier = Modifier
                                .clickable { onFinishAndDownload() }
                                .padding(end = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null,
                                    tint = SleekNavy950,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Finish & Save",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekNavy950
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = onUndo,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = "Undo stroke", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Page", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Palette & Stroke Thickness Row (if not erasing)
            if (!isEraser) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Color dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        penColors.forEach { col ->
                            val isSelected = selectedColor == col
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 26.dp else 22.dp)
                                    .clip(CircleShape)
                                    .background(col)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color(0x33000000),
                                        shape = CircleShape
                                    )
                                    .clickable { onColorSelected(col) }
                            )
                        }
                    }

                    // Stroke width options: Fine, Med, Thick
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            Triple("Fine", 3f, 8f),
                            Triple("Med", 5.5f, 14f),
                            Triple("Thick", 10f, 22f)
                        ).forEach { (label, penW, highW) ->
                            val targetWidth = if (isHighlighter) highW else penW
                            val isSelected = Math.abs(strokeWidth - targetWidth) < 1f
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) SleekBlue600 else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, if (isSelected) SleekBlue600 else MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .clickable { onStrokeWidthSelected(targetWidth) }
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "Touch or drag across any handwriting stroke on Page $currentPage to erase it.",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun ButtonWithIcon(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = SleekBlue600,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.Bold)
    }
}
