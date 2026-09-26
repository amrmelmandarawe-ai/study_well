package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Constants for color schemes aligning with Sleek design system
private val SleekNavy950 = Color(0xFF0F172A)
private val SleekNavy900 = Color(0xFF1E293B)
private val SleekBlue600 = Color(0xFF2563EB)
private val SleekBlue400 = Color(0xFF60A5FA)
private val SleekGold400 = Color(0xFFF59E0B)

data class ScannedPage(
    val originalBitmap: Bitmap,
    val croppedBitmap: Bitmap? = null,
    val filter: String = "MAGIC_COLOR",
    val isAutoCropped: Boolean = false
) {
    fun getActiveBitmap(): Bitmap {
        return croppedBitmap ?: originalBitmap
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentScannerScreen(
    onPdfGenerated: (File) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var scannedPages by remember { mutableStateOf<List<ScannedPage>>(emptyList()) }
    var selectedPageIndex by remember { mutableIntStateOf(0) }
    var showCompilerDialog by remember { mutableStateOf(false) }
    var pdfTitle by remember { mutableStateOf("") }
    var isCompiling by remember { mutableStateOf(false) }

    // Manual crop dialog state
    var showManualCropDialog by remember { mutableStateOf(false) }
    var manualCropLeft by remember { mutableFloatStateOf(0.02f) }
    var manualCropTop by remember { mutableFloatStateOf(0.02f) }
    var manualCropRight by remember { mutableFloatStateOf(0.98f) }
    var manualCropBottom by remember { mutableFloatStateOf(0.98f) }

    // Uri holder for full resolution camera captures
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraFile by remember { mutableStateOf<File?>(null) }

    // Filter list
    val filters = listOf(
        Pair("ORIGINAL", "Original"),
        Pair("MAGIC_COLOR", "Magic Color"),
        Pair("BW", "B&W"),
        Pair("GRAYSCALE", "Grayscale")
    )

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraFile?.let { file ->
                val bitmap = FileUtils.loadBitmapFromFile(file)
                if (bitmap != null) {
                    val rotatedBitmap = FileUtils.fixImageRotationIfRequired(file.absolutePath, bitmap)
                    // Automatically auto-crop the captured document photo instantly
                    val cropped = autoCropBitmap(rotatedBitmap)
                    val isCropped = cropped != rotatedBitmap
                    scannedPages = scannedPages + ScannedPage(
                        originalBitmap = rotatedBitmap,
                        croppedBitmap = if (isCropped) cropped else null,
                        isAutoCropped = isCropped
                    )
                    selectedPageIndex = scannedPages.lastIndex
                    Toast.makeText(
                        context,
                        if (isCropped) "Page captured & automatically cropped!" else "Page captured successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val triggerCameraDirectly = {
        try {
            val file = File.createTempFile("cam_scan_", ".jpg", context.cacheDir)
            tempCameraFile = file
            val authority = "${context.packageName}.provider"
            tempCameraUri = FileProvider.getUriForFile(context, authority, file)
            cameraLauncher.launch(tempCameraUri!!)
        } catch (e: Exception) {
            Toast.makeText(context, "Error setting up camera: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            triggerCameraDirectly()
        } else {
            Toast.makeText(context, "Camera permission is required to scan documents", Toast.LENGTH_LONG).show()
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (!uris.isNullOrEmpty()) {
            var loadedCount = 0
            val loadedPages = uris.mapNotNull { uri ->
                val bitmap = FileUtils.loadBitmapFromUri(context, uri)
                if (bitmap != null) {
                    loadedCount++
                    // Automatically auto-crop photos selected from gallery
                    val cropped = autoCropBitmap(bitmap)
                    val isCropped = cropped != bitmap
                    ScannedPage(
                        originalBitmap = bitmap,
                        croppedBitmap = if (isCropped) cropped else null,
                        isAutoCropped = isCropped
                    )
                } else {
                    null
                }
            }
            if (loadedPages.isNotEmpty()) {
                scannedPages = scannedPages + loadedPages
                selectedPageIndex = scannedPages.lastIndex
                Toast.makeText(context, "Loaded & auto-cropped $loadedCount pictures", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to load any images from gallery", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun triggerCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            triggerCameraDirectly()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("document_scanner_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Study Well CamScanner",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SleekNavy950,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = SleekNavy950
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Display Screen
            if (scannedPages.isEmpty()) {
                // Empty state instructions
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekNavy900),
                    border = BorderStroke(1.dp, SleekBlue600.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SleekBlue600.copy(alpha = 0.15f),
                            modifier = Modifier.size(90.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DocumentScanner,
                                contentDescription = null,
                                tint = SleekGold400,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "CamScanner Document Engine",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Capture handouts, past papers, or homework. The scanning engine applies image filters to enhance visibility, contrast, and clarity, compiles them into a single PDF, and stamps the official watermark.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = { triggerCamera() },
                            colors = ButtonDefaults.buttonColors(containerColor = SleekBlue600),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Take First Photo", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                galleryLauncher.launch("image/*")
                            },
                            border = BorderStroke(1.dp, SleekBlue400.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = SleekGold400)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Import from Gallery", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Active Preview Screen
                val currentPage = scannedPages.getOrNull(selectedPageIndex)
                if (currentPage != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header controller
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Page ${selectedPageIndex + 1} of ${scannedPages.size}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = SleekGold400
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = {
                                        if (selectedPageIndex > 0) {
                                            // Move page left (reorder)
                                            val list = scannedPages.toMutableList()
                                            val temp = list[selectedPageIndex]
                                            list[selectedPageIndex] = list[selectedPageIndex - 1]
                                            list[selectedPageIndex - 1] = temp
                                            scannedPages = list
                                            selectedPageIndex -= 1
                                        }
                                    },
                                    enabled = selectedPageIndex > 0
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = "Move page up",
                                        tint = if (selectedPageIndex > 0) Color.White else Color.Gray
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        if (selectedPageIndex < scannedPages.lastIndex) {
                                            // Move page right (reorder)
                                            val list = scannedPages.toMutableList()
                                            val temp = list[selectedPageIndex]
                                            list[selectedPageIndex] = list[selectedPageIndex + 1]
                                            list[selectedPageIndex + 1] = temp
                                            scannedPages = list
                                            selectedPageIndex += 1
                                        }
                                    },
                                    enabled = selectedPageIndex < scannedPages.lastIndex
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "Move page down",
                                        tint = if (selectedPageIndex < scannedPages.lastIndex) Color.White else Color.Gray
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val list = scannedPages.toMutableList()
                                        list.removeAt(selectedPageIndex)
                                        scannedPages = list
                                        if (selectedPageIndex >= list.size && list.isNotEmpty()) {
                                            selectedPageIndex = list.lastIndex
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Page", tint = Color.Red)
                                }
                            }
                        }

                        // Preview box showing image + filter
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black)
                                .border(1.dp, SleekBlue600.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            val filteredBitmap = remember(currentPage.getActiveBitmap(), currentPage.filter) {
                                applyScannerFilter(currentPage.getActiveBitmap(), currentPage.filter)
                            }

                            val filteredImageBitmap = remember(filteredBitmap) { filteredBitmap.asImageBitmap() }

                            Image(
                                bitmap = filteredImageBitmap,
                                contentDescription = "Scanned Page Preview",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                contentScale = ContentScale.Fit
                            )

                            // Overlay showing water mark preview on bottom right
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.Black.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        text = "Watermark: Study Well App Icon & Title",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Smart Edge & Manual Crop Action Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isCropped = currentPage.isAutoCropped
                            Button(
                                onClick = {
                                    scannedPages = scannedPages.mapIndexed { idx, page ->
                                        if (idx == selectedPageIndex) {
                                            if (page.isAutoCropped) {
                                                page.copy(croppedBitmap = null, isAutoCropped = false)
                                            } else {
                                                val cropped = autoCropBitmap(page.originalBitmap)
                                                page.copy(croppedBitmap = cropped, isAutoCropped = true)
                                            }
                                        } else {
                                            page
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCropped) SleekGold400 else SleekNavy900
                                ),
                                border = BorderStroke(1.dp, if (isCropped) SleekGold400 else SleekBlue400.copy(alpha = 0.3f)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCropped) Icons.Default.Crop else Icons.Default.AutoFixHigh,
                                    contentDescription = null,
                                    tint = if (isCropped) Color.Black else SleekGold400,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isCropped) "Auto Cropped (Reset)" else "Auto Crop",
                                    color = if (isCropped) Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Button(
                                onClick = {
                                    manualCropLeft = 0.02f
                                    manualCropTop = 0.02f
                                    manualCropRight = 0.98f
                                    manualCropBottom = 0.98f
                                    showManualCropDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SleekNavy900),
                                border = BorderStroke(1.dp, SleekBlue400.copy(alpha = 0.5f)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Crop,
                                    contentDescription = null,
                                    tint = SleekGold400,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Manual Crop",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Filter selections
                        Text(
                            text = "Filter Mode (CamScanner FX):",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            filters.forEach { (filterKey, filterName) ->
                                val isSelected = currentPage.filter == filterKey
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) SleekBlue600 else SleekNavy900)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) SleekGold400 else SleekBlue400.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            scannedPages = scannedPages.mapIndexed { idx, page ->
                                                if (idx == selectedPageIndex) page.copy(filter = filterKey) else page
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = filterName,
                                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lower Action Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = SleekNavy900,
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (scannedPages.isNotEmpty()) {
                        // Thumbnail row
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            itemsIndexed(scannedPages) { index, page ->
                                val isSelected = index == selectedPageIndex
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) SleekGold400 else Color(0xFF334155),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedPageIndex = index }
                                ) {
                                    val pageImageBitmap = remember(page.getActiveBitmap()) { page.getActiveBitmap().asImageBitmap() }
                                    Image(
                                        bitmap = pageImageBitmap,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = if (isSelected) 0f else 0.4f)),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            modifier = Modifier
                                                .background(Color.Black.copy(alpha = 0.6f))
                                                .padding(horizontal = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { triggerCamera() },
                            modifier = Modifier
                                .background(SleekBlue600, CircleShape)
                                .size(48.dp)
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = "Add Photo", tint = Color.White)
                        }

                        IconButton(
                            onClick = {
                                galleryLauncher.launch("image/*")
                            },
                            modifier = Modifier
                                .background(SleekNavy950, CircleShape)
                                .border(1.dp, SleekBlue400.copy(alpha = 0.5f), CircleShape)
                                .size(44.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Add Gallery", tint = SleekGold400)
                        }

                        if (scannedPages.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                    pdfTitle = "Scanned_Doc_$timeStamp"
                                    showCompilerDialog = true
                                },
                                modifier = Modifier
                                    .background(Color(0xFF10B981), CircleShape)
                                    .size(48.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Compile PDF", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    // Manual Crop Dialog
    if (showManualCropDialog) {
        val currentPage = scannedPages.getOrNull(selectedPageIndex)
        if (currentPage != null) {
            val previewCropBitmap = remember(currentPage.originalBitmap, manualCropLeft, manualCropTop, manualCropRight, manualCropBottom) {
                manualCropBitmap(currentPage.originalBitmap, manualCropLeft, manualCropTop, manualCropRight, manualCropBottom)
            }

            AlertDialog(
                onDismissRequest = { showManualCropDialog = false },
                title = { Text("Manual Crop Page ${selectedPageIndex + 1}", fontWeight = FontWeight.Bold, color = SleekGold400) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Adjust margins to crop out backgrounds and isolate the document.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            val cropImageBitmap = remember(previewCropBitmap) { previewCropBitmap.asImageBitmap() }
                            Image(
                                bitmap = cropImageBitmap,
                                contentDescription = "Crop Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Left: ${(manualCropLeft * 100).toInt()}% | Right: ${( (1f - manualCropRight) * 100).toInt()}%", fontSize = 11.sp, color = Color.White)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Slider(
                                value = manualCropLeft,
                                onValueChange = { manualCropLeft = it },
                                valueRange = 0f..0.4f,
                                modifier = Modifier.weight(1f)
                            )
                            Slider(
                                value = manualCropRight,
                                onValueChange = { manualCropRight = it },
                                valueRange = 0.6f..1f,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Text("Top: ${(manualCropTop * 100).toInt()}% | Bottom: ${( (1f - manualCropBottom) * 100).toInt()}%", fontSize = 11.sp, color = Color.White)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Slider(
                                value = manualCropTop,
                                onValueChange = { manualCropTop = it },
                                valueRange = 0f..0.4f,
                                modifier = Modifier.weight(1f)
                            )
                            Slider(
                                value = manualCropBottom,
                                onValueChange = { manualCropBottom = it },
                                valueRange = 0.6f..1f,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val finalCropped = manualCropBitmap(currentPage.originalBitmap, manualCropLeft, manualCropTop, manualCropRight, manualCropBottom)
                            scannedPages = scannedPages.mapIndexed { idx, page ->
                                if (idx == selectedPageIndex) {
                                    page.copy(croppedBitmap = finalCropped, isAutoCropped = true)
                                } else {
                                    page
                                }
                            }
                            showManualCropDialog = false
                            Toast.makeText(context, "Page manually cropped successfully!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekBlue600)
                    ) {
                        Text("Apply Crop", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showManualCropDialog = false }) {
                        Text("Cancel")
                    }
                },
                containerColor = SleekNavy900
            )
        }
    }

    // PDF Compile Dialog
    if (showCompilerDialog) {
        AlertDialog(
            onDismissRequest = { if (!isCompiling) showCompilerDialog = false },
            title = { Text("Compile PDF Document", fontWeight = FontWeight.Bold, color = SleekGold400) },
            text = {
                Column {
                    Text(
                        text = "Enter a title for the compiled PDF. All pages will be enhanced, watermarked, and saved into your library.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pdfTitle,
                        onValueChange = { pdfTitle = it },
                        label = { Text("Document Title *") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SleekGold400,
                            unfocusedBorderColor = SleekBlue400.copy(alpha = 0.4f)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isCompiling
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pdfTitle.isBlank()) {
                            Toast.makeText(context, "Please enter a title", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isCompiling = true
                        
                        // Run PDF compiler asynchronously in IO dispatcher
                        coroutineScope.launch {
                            try {
                                val outputFile = File(context.cacheDir, "${FileUtils.cleanFileNameForTitle(pdfTitle)}.pdf")
                                
                                withContext(Dispatchers.IO) {
                                    val pdfDoc = PdfDocument()

                                    scannedPages.forEachIndexed { idx, page ->
                                        val activeBitmap = page.getActiveBitmap()
                                        val enhanced = applyScannerFilter(activeBitmap, page.filter)
                                        
                                        // Standard A4: 595 x 842 PostScript Points
                                        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, idx + 1).create()
                                        val pdfPage = pdfDoc.startPage(pageInfo)
                                        val canvas = pdfPage.canvas

                                        // White sheet
                                        canvas.drawColor(android.graphics.Color.WHITE)

                                        // Calculate proportional rect fitting standard A4 (595 x 842) without distortion
                                        val imgWidth = enhanced.width.toFloat()
                                        val imgHeight = enhanced.height.toFloat()
                                        val maxPdfWidth = 595f
                                        val maxPdfHeight = 842f

                                        val scale = minOf(maxPdfWidth / imgWidth, maxPdfHeight / imgHeight)
                                        val finalWidth = imgWidth * scale
                                        val finalHeight = imgHeight * scale

                                        val left = (maxPdfWidth - finalWidth) / 2f
                                        val top = (maxPdfHeight - finalHeight) / 2f

                                        val src = android.graphics.Rect(0, 0, enhanced.width, enhanced.height)
                                        val dst = android.graphics.RectF(left, top, left + finalWidth, top + finalHeight)
                                        val paint = Paint().apply { isFilterBitmap = true }
                                        canvas.drawBitmap(enhanced, src, dst, paint)

                                        // Draw the watermark (app logo and text) at the bottom right
                                        FileUtils.drawAppWatermark(context, canvas, 595f, 842f)

                                        pdfDoc.finishPage(pdfPage)

                                        // Immediately recycle intermediate filtered bitmap to free memory
                                        if (enhanced != activeBitmap && !enhanced.isRecycled) {
                                            enhanced.recycle()
                                        }
                                    }

                                    FileOutputStream(outputFile).use { out ->
                                        pdfDoc.writeTo(out)
                                    }
                                    pdfDoc.close()
                                }

                                isCompiling = false
                                showCompilerDialog = false
                                onPdfGenerated(outputFile)
                                Toast.makeText(context, "PDF Compiled successfully (${scannedPages.size} pages)!", Toast.LENGTH_SHORT).show()
                            } catch (e: Throwable) {
                                isCompiling = false
                                Toast.makeText(context, "Failed compiling PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekBlue600),
                    enabled = !isCompiling
                ) {
                    if (isCompiling) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                    } else {
                        Text("Compile", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                if (!isCompiling) {
                    TextButton(onClick = { showCompilerDialog = false }) {
                        Text("Cancel")
                    }
                }
            },
            containerColor = SleekNavy900
        )
    }
}

/**
 * High quality image scanner filter calculations.
 */
fun applyScannerFilter(bitmap: Bitmap, filter: String): Bitmap {
    if (filter == "ORIGINAL") {
        return bitmap
    }
    return try {
        val width = bitmap.width
        val height = bitmap.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(result)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)

        when (filter) {
            "MAGIC_COLOR" -> {
                // High-contrast, bright color matrix (classic CamScanner)
                val cm = ColorMatrix().apply {
                    set(floatArrayOf(
                        1.45f, 0f, 0f, 0f, 25f,
                        0f, 1.45f, 0f, 0f, 25f,
                        0f, 0f, 1.45f, 0f, 25f,
                        0f, 0f, 0f, 1f, 0f
                    ))
                }
                paint.colorFilter = ColorMatrixColorFilter(cm)
                canvas.drawBitmap(bitmap, 0f, 0f, paint)
            }
            "BW" -> {
                // Crisp high-contrast black and white for readability (document scanning mode)
                val cm = ColorMatrix()
                cm.setSaturation(0f)
                val cmContrast = ColorMatrix().apply {
                    set(floatArrayOf(
                        2.3f, 0f, 0f, 0f, -125f,
                        0f, 2.3f, 0f, 0f, -125f,
                        0f, 0f, 2.3f, 0f, -125f,
                        0f, 0f, 0f, 1f, 0f
                    ))
                }
                cm.postConcat(cmContrast)
                paint.colorFilter = ColorMatrixColorFilter(cm)
                canvas.drawBitmap(bitmap, 0f, 0f, paint)
            }
            "GRAYSCALE" -> {
                val cm = ColorMatrix()
                cm.setSaturation(0f)
                paint.colorFilter = ColorMatrixColorFilter(cm)
                canvas.drawBitmap(bitmap, 0f, 0f, paint)
            }
            else -> {
                canvas.drawBitmap(bitmap, 0f, 0f, paint)
            }
        }
        result
    } catch (e: Throwable) {
        bitmap
    }
}

/**
 * High-accuracy intelligent edge detection and auto-cropping for document scans.
 * Analyzes contrast between background surfaces (desks/tables) and document sheets,
 * automatically detects sheet margins and crops out unnecessary surroundings.
 */
fun autoCropBitmap(bitmap: Bitmap): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    if (width < 80 || height < 80) return bitmap

    try {
        val samplesX = 50
        val samplesY = 50
        val stepX = (width / samplesX).coerceAtLeast(1)
        val stepY = (height / samplesY).coerceAtLeast(1)

        var borderLumSum = 0.0
        var borderCount = 0
        for (x in 0 until width step stepX) {
            borderLumSum += getLuminance(bitmap.getPixel(x, 0)) + getLuminance(bitmap.getPixel(x, height - 1))
            borderCount += 2
        }
        for (y in 0 until height step stepY) {
            borderLumSum += getLuminance(bitmap.getPixel(0, y)) + getLuminance(bitmap.getPixel(width - 1, y))
            borderCount += 2
        }
        val avgBorderLum = if (borderCount > 0) borderLumSum / borderCount else 100.0

        var minX = width
        var maxX = 0
        var minY = height
        var maxY = 0

        for (y in 0 until height step stepY) {
            for (x in 0 until width step stepX) {
                val lum = getLuminance(bitmap.getPixel(x, y))
                if (kotlin.math.abs(lum - avgBorderLum) > 22.0) {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
            }
        }

        if (minX < maxX && minY < maxY) {
            val padX = (width * 0.02f).toInt()
            val padY = (height * 0.02f).toInt()
            val cropX = (minX - padX).coerceAtLeast(0)
            val cropY = (minY - padY).coerceAtLeast(0)
            val cropW = (maxX + padX).coerceAtMost(width) - cropX
            val cropH = (maxY + padY).coerceAtMost(height) - cropY

            if (cropW > width * 0.2f && cropH > height * 0.2f) {
                return Bitmap.createBitmap(bitmap, cropX, cropY, cropW, cropH)
            }
        }
    } catch (_: Throwable) {}

    // Guaranteed smart fallback crop: trim 4% outer border
    try {
        val cropX = (width * 0.04f).toInt()
        val cropY = (height * 0.04f).toInt()
        val cropW = width - (cropX * 2)
        val cropH = height - (cropY * 2)
        if (cropW > 100 && cropH > 100) {
            return Bitmap.createBitmap(bitmap, cropX, cropY, cropW, cropH)
        }
    } catch (_: Throwable) {}

    return bitmap
}

/**
 * Manual crop utility applying specific margin sliders (left, top, right, bottom).
 */
fun manualCropBitmap(bitmap: Bitmap, leftRatio: Float, topRatio: Float, rightRatio: Float, bottomRatio: Float): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    try {
        val left = (width * leftRatio.coerceIn(0f, 0.45f)).toInt()
        val top = (height * topRatio.coerceIn(0f, 0.45f)).toInt()
        val right = (width * rightRatio.coerceIn(0.55f, 1f)).toInt()
        val bottom = (height * bottomRatio.coerceIn(0.55f, 1f)).toInt()

        val cropW = (right - left).coerceIn(100, width - left)
        val cropH = (bottom - top).coerceIn(100, height - top)

        if (cropW > 50 && cropH > 50) {
            return Bitmap.createBitmap(bitmap, left, top, cropW, cropH)
        }
    } catch (_: Throwable) {}
    return bitmap
}

private fun getLuminance(color: Int): Float {
    val r = (color shr 16) and 0xff
    val g = (color shr 8) and 0xff
    val b = color and 0xff
    return 0.299f * r + 0.587f * g + 0.114f * b
}
