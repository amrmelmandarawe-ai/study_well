package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.model.ChatMessageEntity
import com.example.data.model.SubjectEnum
import com.example.ui.theme.SleekBlue600
import com.example.ui.theme.SleekGold400
import com.example.ui.theme.SleekNavy900
import com.example.ui.theme.SleekNavy950
import com.example.ui.theme.sleekTextFieldColors
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiTutorDialog(
    selectedSubject: SubjectEnum,
    onSelectSubject: (SubjectEnum) -> Unit,
    messages: List<ChatMessageEntity>,
    isTyping: Boolean,
    onSendMessage: (query: String, imageUri: String?, imageBase64: String?) -> Unit,
    onClearChat: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputQuery by remember { mutableStateOf("") }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showPermissionRationale by remember { mutableStateOf(false) }
    var viewingImageUri by remember { mutableStateOf<String?>(null) }

    // Attached Image State for multimodal problem solving
    var attachedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var attachedImageUri by remember { mutableStateOf<String?>(null) }
    var attachedBase64 by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    // Text-to-Speech Engine for Gemini voice answers
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var speakingMessageId by remember { mutableStateOf<Long?>(null) }

    DisposableEffect(context) {
        val engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.ENGLISH
            }
        }
        tts = engine
        onDispose {
            try {
                engine.stop()
                engine.shutdown()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    val onSpeakClick: (Long, String) -> Unit = { id, text ->
        if (speakingMessageId == id) {
            tts?.stop()
            speakingMessageId = null
        } else {
            tts?.stop()
            speakingMessageId = id
            val cleanText = text.replace(Regex("[#*`•🎯]"), "")
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, id.toString())
        }
    }

    val onCopyClick: (String) -> Unit = { text ->
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("AI Tutor Solution", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Explanation copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    val onShareClick: (String) -> Unit = { text ->
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Cambridge IGCSE Tutor Solution:\n\n$text")
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share Solution"))
    }

    // Process Bitmap helper
    fun processBitmap(bitmap: Bitmap) {
        attachedBitmap = bitmap
        try {
            val base64 = bitmapToBase64(bitmap)
            attachedBase64 = base64
            val localPath = saveBitmapToCache(context, bitmap)
            attachedImageUri = localPath
        } catch (e: Exception) {
            Toast.makeText(context, "Could not process image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Camera Capture Launcher
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            processBitmap(bitmap)
        }
    }

    // Gallery Picker Launcher
    val pickGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = uriToBitmap(context, it)
            if (bitmap != null) {
                processBitmap(bitmap)
            } else {
                Toast.makeText(context, "Could not load image from gallery.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePhotoLauncher.launch(null)
        } else {
            showPermissionRationale = true
        }
    }

    fun requestCameraOrCapture() {
        val permission = Manifest.permission.CAMERA
        when {
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
                takePhotoLauncher.launch(null)
            }
            else -> {
                cameraPermissionLauncher.launch(permission)
            }
        }
    }

    // Clear confirmation dialog
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear Chat History?") },
            text = { Text("Are you sure you want to delete conversation history for ${selectedSubject.title}? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirm = false
                        onClearChat()
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Camera Permission Rationale Dialog
    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text("Camera Permission Needed") },
            text = {
                Text(
                    "To take pictures of difficult math, physics, or ICT exam problems, the AI Tutor needs camera access. Please grant camera permission to snap questions."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionRationale = false
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                ) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationale = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Fullscreen Image Dialog Viewer
    viewingImageUri?.let { uri ->
        Dialog(onDismissRequest = { viewingImageUri = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = SleekNavy950
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Question Photo",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { viewingImageUri = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    AsyncImage(
                        model = uri,
                        contentDescription = "Question Photo Fullscreen",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }

    val quickQuestions = when (selectedSubject) {
        SubjectEnum.PHYSICS -> listOf(
            "Explain Newton's 2nd Law",
            "Formula for Snell's Law",
            "How do transformers work?",
            "Speed-time graph vs Distance-time"
        )
        SubjectEnum.ENGLISH -> listOf(
            "Tips for Question 2(d) Writer's Effect",
            "How to write a summary in 120 words?",
            "Directed Writing formal speech format",
            "Descriptive writing sensory details"
        )
        SubjectEnum.MATH -> listOf(
            "How to use the quadratic formula?",
            "Sine Rule vs Cosine Rule guide",
            "Compound interest formula example",
            "Conditional probability tree diagram"
        )
        SubjectEnum.BIOLOGY -> listOf(
            "Difference between mitosis and meiosis",
            "How does enzyme denaturation occur?",
            "Structure and function of xylem vs phloem",
            "Aerobic vs anaerobic respiration equations"
        )
        SubjectEnum.CHEMISTRY -> listOf(
            "How to balance redox equations?",
            "Ionic vs Covalent bonding properties",
            "Electrolysis of molten vs aqueous electrolytes",
            "Periodic trends: electronegativity & atomic radius"
        )
        SubjectEnum.ARABIC_OL -> listOf(
            "قواعد الإعراب الشائعة في امتحان أكسفورد/كامبردج",
            "أساليب البلاغة: التشبيه والاستعارة والكناية",
            "كيفية كتابة المقال التعبيري والرسالة الرسمية",
            "تحليل النصوص الأدبية والشعرية للاختبار"
        )
        SubjectEnum.ICT -> listOf(
            "Difference between RAM and ROM",
            "What is Pharming vs Phishing?",
            "Primary Key vs Foreign Key in SQL",
            "NAND & NOR logic truth tables"
        )
        else -> listOf(
            "Explain key syllabus concepts",
            "Give me a step-by-step revision breakdown",
            "What are common past-paper exam traps?",
            "Quiz me on core fundamentals"
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_tutor_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = SleekGold400,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SleekNavy950,
                                modifier = Modifier.padding(7.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Study Well AI Tutor",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "✨ Auto-Detects Subject • Fast & Simple Answers",
                                style = MaterialTheme.typography.labelSmall,
                                color = SleekGold400,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("ai_tutor_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Close Tutor")
                    }
                },
                actions = {
                    if (messages.isNotEmpty()) {
                        IconButton(onClick = { showClearConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear Chat")
                        }
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
            // Chat Message List
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SleekGold400.copy(alpha = 0.15f),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SleekGold400,
                                modifier = Modifier.padding(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Ask Any Cambridge IGCSE Question!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Type your question or snap a photo of any exam problem 📸. The AI will automatically detect the subject (Physics, Math, English, or ICT) and give you a simple, direct answer with step-by-step reasoning!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { requestCameraOrCapture() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Snap Question with Camera")
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        val isUser = msg.sender == "user"
                        val timeStr = remember(msg.timestamp) {
                            timeFormatter.format(Date(msg.timestamp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            if (!isUser) {
                                Surface(
                                    shape = CircleShape,
                                    color = SleekGold400,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = SleekNavy950,
                                        modifier = Modifier.padding(7.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Column(
                                horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                            ) {
                                if (isUser) {
                                    Card(
                                        shape = RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = 16.dp,
                                            bottomEnd = 4.dp
                                        ),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier.widthIn(max = 300.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            if (!msg.imageUri.isNullOrBlank()) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(160.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Color.Black.copy(alpha = 0.2f))
                                                        .clickable { viewingImageUri = msg.imageUri }
                                                ) {
                                                    AsyncImage(
                                                        model = msg.imageUri,
                                                        contentDescription = "Question Photo",
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                            }

                                            Text(
                                                text = msg.text,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.White,
                                                lineHeight = 20.sp
                                            )
                                        }
                                    }
                                } else {
                                    // AI Response Bubble formatted like Google Gemini with full step-by-step visibility & TTS
                                    AiResponseBubble(
                                        msg = msg,
                                        selectedSubject = selectedSubject,
                                        isSpeaking = speakingMessageId == msg.id,
                                        onSpeak = { onSpeakClick(msg.id, msg.text) },
                                        onCopy = { onCopyClick(msg.text) },
                                        onShare = { onShareClick(msg.text) },
                                        onViewImage = { viewingImageUri = it }
                                    )
                                }

                                Text(
                                    text = timeStr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    if (isTyping) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 38.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = SleekGold400
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI Tutor is analyzing your question...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Quick Prompt Pills
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(quickQuestions) { question ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier.clickable {
                            onSendMessage(question, null, null)
                        }
                    ) {
                        Text(
                            text = question,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Input Bar & Attached Photo Preview
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .navigationBarsPadding()
                ) {
                    // Attached Photo Preview Bar
                    AnimatedVisibility(
                        visible = attachedBitmap != null,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        attachedBitmap?.let { bmp ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val bmpImage = remember(bmp) { bmp.asImageBitmap() }
                                    Image(
                                        bitmap = bmpImage,
                                        contentDescription = "Attached Photo Preview",
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "📸 Question Photo Attached",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = selectedSubject.color
                                        )
                                        Text(
                                            text = "AI will solve and explain step-by-step",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            attachedBitmap = null
                                            attachedImageUri = null
                                            attachedBase64 = null
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove photo", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Input Text and Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Camera Button
                        IconButton(
                            onClick = { requestCameraOrCapture() },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = selectedSubject.color.copy(alpha = 0.15f),
                                    shape = CircleShape
                                )
                                .testTag("ai_tutor_camera_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Take Photo of Question",
                                tint = selectedSubject.color,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Gallery Button
                        IconButton(
                            onClick = { pickGalleryLauncher.launch("image/*") },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape
                                )
                                .testTag("ai_tutor_gallery_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Choose Image from Gallery",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = inputQuery,
                            onValueChange = { inputQuery = it },
                            placeholder = {
                                Text(
                                    if (attachedBitmap != null) "Add instructions (optional)..."
                                    else "Ask or snap a question..."
                                )
                            },
                            maxLines = 3,
                            colors = sleekTextFieldColors(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_tutor_input"),
                            shape = RoundedCornerShape(24.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        val canSend = (inputQuery.isNotBlank() || attachedBase64 != null) && !isTyping

                        IconButton(
                            onClick = {
                                if (canSend) {
                                    val queryToSend = inputQuery.trim()
                                    val uriToSend = attachedImageUri
                                    val base64ToSend = attachedBase64

                                    onSendMessage(queryToSend, uriToSend, base64ToSend)

                                    // Reset input state
                                    inputQuery = ""
                                    attachedBitmap = null
                                    attachedImageUri = null
                                    attachedBase64 = null
                                }
                            },
                            modifier = Modifier
                                .background(
                                    color = if (canSend) SleekBlue600 else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape
                                )
                                .testTag("ai_tutor_send_button"),
                            enabled = canSend
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = if (canSend) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helpers for image handling
private fun bitmapToBase64(bitmap: Bitmap): String {
    val stream = ByteArrayOutputStream()
    // Scale down slightly if very large to prevent memory overhead and fit Gemini token payload
    val maxDimension = 1024
    val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
        val ratio = Math.min(maxDimension.toFloat() / bitmap.width, maxDimension.toFloat() / bitmap.height)
        Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
    } else {
        bitmap
    }
    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
    val bytes = stream.toByteArray()
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
}

private fun saveBitmapToCache(context: Context, bitmap: Bitmap): String {
    val dir = File(context.cacheDir, "tutor_images")
    if (!dir.exists()) dir.mkdirs()
    val file = File(dir, "problem_${System.currentTimeMillis()}.jpg")
    file.outputStream().use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    return file.absolutePath
}

private fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (e: Exception) {
        null
    }
}

@Composable
private fun AiResponseBubble(
    msg: ChatMessageEntity,
    selectedSubject: SubjectEnum,
    isSpeaking: Boolean,
    onSpeak: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onViewImage: (String) -> Unit
) {
    val rawText = msg.text
    val hasExplanationDelimiter = rawText.contains("--- EXPLANATION ---")

    val (keyAnswer, fullExplanation) = remember(rawText) {
        if (hasExplanationDelimiter) {
            val split = rawText.split("--- EXPLANATION ---", limit = 2)
            val answer = split[0].replace("🎯 **Final Answer**:", "").replace("🎯 Final Answer:", "").trim()
            Pair(answer, split[1].trim())
        } else if (rawText.contains("🎯 **Final Answer**:")) {
            val split = rawText.split("🎯 **Final Answer**:", limit = 2)
            Pair("", split[1].trim())
        } else {
            Pair("", rawText.trim())
        }
    }

    Card(
        shape = RoundedCornerShape(
            topStart = 18.dp,
            topEnd = 18.dp,
            bottomStart = 4.dp,
            bottomEnd = 18.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.96f)
        ),
        border = BorderStroke(1.dp, SleekGold400.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth(0.96f)
            .testTag("gemini_ai_response_bubble")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Gemini Identity Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = SleekGold400,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gemini",
                            tint = SleekNavy950,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gemini AI Tutor",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SleekBlue600.copy(alpha = 0.15f),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Text(
                            text = "Grade 10",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SleekBlue600,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Audio status chip
                if (isSpeaking) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SleekGold400.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, SleekGold400)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = SleekGold400,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Speaking...",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = SleekGold400,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Attached question image if any
            if (!msg.imageUri.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.25f))
                        .clickable { onViewImage(msg.imageUri) }
                ) {
                    AsyncImage(
                        model = msg.imageUri,
                        contentDescription = "Question Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        shape = RoundedCornerShape(bottomStart = 8.dp),
                        color = SleekNavy950.copy(alpha = 0.75f),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ZoomIn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tap to zoom", color = Color.White, fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Direct Final Answer Highlight Box (if present)
            if (keyAnswer.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SleekGold400.copy(alpha = 0.15f),
                    border = BorderStroke(1.2.dp, SleekGold400.copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🎯 Key Answer / Result",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SleekGold400
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = keyAnswer,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 22.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Full Step-by-Step Gemini Explanation - ALWAYS OPEN & VISIBLE
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val contentToShow = if (fullExplanation.isNotBlank()) fullExplanation else rawText
                    GeminiFormattedContent(content = contentToShow)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Gemini Action Bar (Copy, Listen/Speak, Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Copy action button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .clickable { onCopy() }
                            .testTag("ai_copy_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy solution",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Copy",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // TTS Listen / Stop action button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSpeaking) SleekGold400 else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isSpeaking) SleekGold400 else MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .clickable { onSpeak() }
                            .testTag("ai_listen_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                                contentDescription = if (isSpeaking) "Stop voice" else "Listen aloud",
                                tint = if (isSpeaking) SleekNavy950 else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSpeaking) "Stop" else "Listen",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                fontWeight = if (isSpeaking) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSpeaking) SleekNavy950 else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Share action button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .clickable { onShare() }
                            .testTag("ai_share_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share solution",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Share",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Text(
                    text = "Cambridge IGCSE",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun GeminiFormattedContent(content: String) {
    val lines = remember(content) { content.lines() }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        lines.forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isNotBlank()) {
                when {
                    trimmed.startsWith("### ") || trimmed.startsWith("## ") || trimmed.startsWith("# ") -> {
                        val headerText = trimmed.replace(Regex("^#+\\s*"), "")
                        Text(
                            text = headerText,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SleekGold400,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    trimmed.startsWith("• ") || trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                        val bulletText = trimmed.substring(2).trim()
                        Row(modifier = Modifier.padding(start = 4.dp)) {
                            Text(
                                text = "• ",
                                color = SleekGold400,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = bulletText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 21.sp
                            )
                        }
                    }
                    trimmed.matches(Regex("^\\d+\\..*")) -> {
                        // Numbered step
                        val parts = trimmed.split(".", limit = 2)
                        val num = parts.getOrNull(0)?.trim() ?: ""
                        val text = parts.getOrNull(1)?.trim() ?: ""
                        Row(modifier = Modifier.padding(start = 4.dp)) {
                            Surface(
                                shape = CircleShape,
                                color = SleekBlue600.copy(alpha = 0.15f),
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = num,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekBlue600
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 21.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = trimmed,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    }
}
