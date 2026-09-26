package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.SubjectEnum
import com.example.data.model.UserRole
import com.example.ui.components.SharedFilesDialog
import com.example.ui.player.InAppDocumentViewer
import com.example.ui.player.InAppVideoPlayer
import com.example.ui.screens.AdminUploadScreen
import com.example.ui.screens.AiTutorDialog
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingIntroScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.StudentLibraryScreen
import com.example.ui.screens.SubjectDetailScreen
import com.example.ui.screens.DocumentScannerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.StudyViewModel

enum class CurrentScreen {
    INTRO,
    AUTH,
    HOME,
    SUBJECT_DETAIL,
    QUIZ,
    ADMIN_PANEL,
    AI_TUTOR,
    STUDENT_LIBRARY,
    DOCUMENT_SCANNER
}

class MainActivity : ComponentActivity() {

    private val viewModel: StudyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        intent?.let {
            viewModel.handleIncomingIntent(it)
        }

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StudyWellApp(viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.handleIncomingIntent(intent)
    }
}

@Composable
fun StudyWellApp(viewModel: StudyViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val isAuthenticating by viewModel.isAuthenticating.collectAsState()
    val isIntroCompleted by viewModel.isIntroCompleted.collectAsState()
    val isInitialSessionCheckDone by viewModel.isInitialSessionCheckDone.collectAsState()

    var currentScreen by remember { mutableStateOf<CurrentScreen?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.targetScreenEvent.collect { target ->
            currentScreen = target
        }
    }

    // Determine initial screen once session check completes
    val activeScreen = currentScreen ?: when {
        !isInitialSessionCheckDone -> CurrentScreen.AUTH
        currentUser != null -> CurrentScreen.HOME
        !isIntroCompleted -> CurrentScreen.INTRO
        else -> CurrentScreen.AUTH
    }

    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val selectedSection by viewModel.selectedSection.collectAsState()
    val currentMaterials by viewModel.currentSubjectMaterials.collectAsState()
    val allMaterials by viewModel.allMaterials.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allUserActivities by viewModel.allUserActivities.collectAsState()
    val allQuizAttempts by viewModel.allQuizAttempts.collectAsState()

    val activeVideo by viewModel.activeVideo.collectAsState()
    val activeDocument by viewModel.activeDocument.collectAsState()

    val activeQuizQuestions by viewModel.activeQuizQuestions.collectAsState()
    val isGeneratingQuiz by viewModel.isGeneratingQuiz.collectAsState()
    val quizTopic by viewModel.quizTopic.collectAsState()

    val allSubjects by viewModel.allSubjects.collectAsState()

    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAiChatting by viewModel.isAiChatting.collectAsState()

    val incomingShareBatch by viewModel.incomingShareBatch.collectAsState()
    val pendingAdminSharedFiles by viewModel.pendingAdminSharedFiles.collectAsState()
    val context = LocalContext.current

    val averageScore by viewModel.userAverageScore.collectAsState()
    val totalQuizzes by viewModel.userTotalQuizzes.collectAsState()
    val libraryDocuments by viewModel.libraryDocuments.collectAsState()
    val homeworkReminders by viewModel.homeworkReminders.collectAsState()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Screen Routing
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            activeScreen == CurrentScreen.INTRO -> {
                OnboardingIntroScreen(
                    onFinishIntro = {
                        viewModel.completeIntro()
                        currentScreen = CurrentScreen.AUTH
                    }
                )
            }
            currentUser == null || activeScreen == CurrentScreen.AUTH -> {
                AuthScreen(
                    onLogin = { username, password ->
                        viewModel.login(username, password) {
                            currentScreen = CurrentScreen.HOME
                        }
                    },
                    onRegister = { username, password, name, role ->
                        viewModel.register(username, password, name, role) {
                            currentScreen = CurrentScreen.HOME
                        }
                    },
                    isLoading = isAuthenticating,
                    errorMessage = authError,
                    onShowIntro = {
                        currentScreen = CurrentScreen.INTRO
                    }
                )
            }
            activeScreen == CurrentScreen.HOME -> {
                currentUser?.let { user ->
                    HomeScreen(
                        user = user,
                        materials = allMaterials,
                        subjects = allSubjects,
                        onSelectSubject = { subject ->
                            viewModel.selectSubject(subject)
                            currentScreen = CurrentScreen.SUBJECT_DETAIL
                        },
                        onOpenAdminPanel = {
                            currentScreen = CurrentScreen.ADMIN_PANEL
                        },
                        onOpenAiTutor = {
                            currentScreen = CurrentScreen.AI_TUTOR
                        },
                        onOpenVideo = { mat ->
                            val sub = SubjectEnum.fromId(mat.subjectId)
                            viewModel.selectSubject(sub)
                            viewModel.openVideoPlayer(mat)
                        },
                        onOpenDocument = { mat ->
                            val sub = SubjectEnum.fromId(mat.subjectId)
                            viewModel.selectSubject(sub)
                            viewModel.openDocumentViewer(mat)
                        },
                        onLogout = {
                            viewModel.logout()
                            currentScreen = CurrentScreen.AUTH
                        },
                        onOpenLibrary = {
                            currentScreen = CurrentScreen.STUDENT_LIBRARY
                        },
                        averageScore = averageScore,
                        totalQuizzes = totalQuizzes,
                        savedLibraryCount = libraryDocuments.size,
                        homeworkReminders = homeworkReminders,
                        onAddReminder = { subject, topic, dueTimestamp ->
                            viewModel.addHomeworkReminder(subject, topic, dueTimestamp)
                        },
                        onUpdateReminder = { reminder ->
                            viewModel.updateHomeworkReminder(reminder)
                        },
                        onDeleteReminder = { reminder ->
                            viewModel.deleteHomeworkReminder(reminder)
                        },
                        onShowIntro = {
                            currentScreen = CurrentScreen.INTRO
                        }
                    )
                }
            }
            activeScreen == CurrentScreen.SUBJECT_DETAIL -> {
                BackHandler {
                    currentScreen = CurrentScreen.HOME
                }
                SubjectDetailScreen(
                    subject = selectedSubject,
                    materials = currentMaterials,
                    selectedSection = selectedSection,
                    onSelectSection = { viewModel.selectSection(it) },
                    onOpenVideo = { mat -> viewModel.openVideoPlayer(mat) },
                    onOpenDocument = { mat -> viewModel.openDocumentViewer(mat) },
                    onDownloadMaterial = { mat ->
                        viewModel.downloadStudyMaterial(mat) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onStartQuiz = { topic, count, difficulty, style, examBoard, pastYear, session, paperVariant ->
                        viewModel.generateAiQuizForTopic(
                            subject = selectedSubject,
                            topic = topic,
                            questionCount = count,
                            difficulty = difficulty,
                            questionStyle = style,
                            examBoard = examBoard,
                            pastYear = pastYear,
                            session = session,
                            paperVariant = paperVariant
                        ) {
                            currentScreen = CurrentScreen.QUIZ
                        }
                    },
                    isGeneratingQuiz = isGeneratingQuiz,
                    onOpenAiTutor = { currentScreen = CurrentScreen.AI_TUTOR },
                    onOpenAdminUpload = { currentScreen = CurrentScreen.ADMIN_PANEL },
                    onOpenLibrary = { currentScreen = CurrentScreen.STUDENT_LIBRARY },
                    isAdmin = currentUser?.role == UserRole.ADMIN.name,
                    onBack = { currentScreen = CurrentScreen.HOME }
                )
            }
            activeScreen == CurrentScreen.QUIZ -> {
                BackHandler {
                    currentScreen = CurrentScreen.SUBJECT_DETAIL
                }
                QuizScreen(
                    subject = selectedSubject,
                    topic = quizTopic,
                    questions = activeQuizQuestions,
                    onSubmitResult = { score, total, details ->
                        viewModel.submitQuizResult(quizTopic, score, total, details)
                    },
                    onBack = { currentScreen = CurrentScreen.SUBJECT_DETAIL }
                )
            }
            activeScreen == CurrentScreen.ADMIN_PANEL -> {
                BackHandler {
                    currentScreen = CurrentScreen.HOME
                }
                AdminUploadScreen(
                    materials = allMaterials,
                    subjects = allSubjects,
                    users = allUsers,
                    activities = allUserActivities,
                    quizAttempts = allQuizAttempts,
                    currentUser = currentUser,
                    initialSharedFiles = pendingAdminSharedFiles,
                    onClearSharedFiles = { viewModel.clearPendingAdminSharedFiles() },
                    onUpload = { subId, type, title, topic, desc, url, doc, dur, onComp ->
                        viewModel.uploadMaterial(subId, type, title, topic, desc, url, doc, dur, onComp)
                    },
                    onDelete = { mat ->
                        viewModel.deleteMaterial(mat)
                    },
                    onAddSubject = { title, code, desc, topics, colorHex, onComp ->
                        viewModel.addSubject(title, code, desc, topics, colorHex, onComp)
                    },
                    onDeleteSubject = { subjectId ->
                        viewModel.deleteSubject(subjectId)
                    },
                    onClearAllMaterials = {
                        viewModel.clearAllMaterials()
                    },
                    onBack = { currentScreen = CurrentScreen.HOME }
                )
            }
            activeScreen == CurrentScreen.AI_TUTOR -> {
                BackHandler {
                    currentScreen = CurrentScreen.HOME
                }
                AiTutorDialog(
                    selectedSubject = selectedSubject,
                    onSelectSubject = { viewModel.selectSubject(it) },
                    messages = chatMessages,
                    isTyping = isAiChatting,
                    onSendMessage = { query, imgUri, imgBase64 ->
                        viewModel.sendAiTutorMessage(query, imgUri, imgBase64)
                    },
                    onClearChat = { viewModel.clearChat() },
                    onClose = { currentScreen = CurrentScreen.HOME }
                )
            }
            activeScreen == CurrentScreen.STUDENT_LIBRARY -> {
                BackHandler {
                    currentScreen = CurrentScreen.HOME
                }
                StudentLibraryScreen(
                    libraryDocuments = libraryDocuments,
                    homeworkReminders = homeworkReminders,
                    onAddReminder = { subject, topic, dueTime ->
                        viewModel.addHomeworkReminder(subject, topic, dueTime)
                    },
                    onUpdateReminder = { reminder ->
                        viewModel.updateHomeworkReminder(reminder)
                    },
                    onDeleteReminder = { reminder ->
                        viewModel.deleteHomeworkReminder(reminder)
                    },
                    onBack = { currentScreen = CurrentScreen.HOME },
                    onOpenDocument = { doc ->
                        viewModel.openLibraryDocument(doc)
                    },
                    onShareDocument = { doc ->
                        viewModel.shareLibraryDocument(doc, context)
                    },
                    onDeleteDocument = { doc ->
                        viewModel.deleteLibraryDocument(doc)
                    },
                    onUpdateDocument = { doc ->
                        viewModel.updateLibraryDocument(doc)
                    },
                    onSyncLibrary = {
                        viewModel.syncLibrary()
                    },
                    onImportExternalPdf = { uri ->
                        viewModel.openExternalPdfUri(uri)
                    },
                    onScanDocument = {
                        currentScreen = CurrentScreen.DOCUMENT_SCANNER
                    }
                )
            }
            activeScreen == CurrentScreen.DOCUMENT_SCANNER -> {
                BackHandler {
                    currentScreen = CurrentScreen.STUDENT_LIBRARY
                }
                DocumentScannerScreen(
                    onPdfGenerated = { file ->
                        viewModel.importScannedPdf(file)
                        currentScreen = CurrentScreen.STUDENT_LIBRARY
                    },
                    onBack = { currentScreen = CurrentScreen.STUDENT_LIBRARY }
                )
            }
        }

        // In-App Video Player Overlay
        AnimatedVisibility(
            visible = activeVideo != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(400)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(350))
        ) {
            val videoMat = activeVideo
            if (videoMat != null) {
                InAppVideoPlayer(
                    material = videoMat,
                    onClose = { viewModel.closeVideoPlayer() }
                )
            }
        }

        // In-App Document / PDF Viewer Overlay
        AnimatedVisibility(
            visible = activeDocument != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(400)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(350))
        ) {
            val docMat = activeDocument
            if (docMat != null) {
                InAppDocumentViewer(
                    material = docMat,
                    onClose = { viewModel.closeDocumentViewer() }
                )
            }
        }

        // Incoming Shared Files Dialog Overlay (Single or Multiple files from WhatsApp, Files, etc.)
        incomingShareBatch?.let { batch ->
            SharedFilesDialog(
                batch = batch,
                currentSelectedSubject = selectedSubject,
                onDismiss = { viewModel.clearIncomingShare() },
                onOpenAsDocument = { file, subject ->
                    viewModel.openSharedItemAsDocument(file, subject)
                    viewModel.clearIncomingShare()
                },
                onOpenWithAiTutor = { file, subject, base64 ->
                    viewModel.selectSubject(subject)
                    viewModel.sendAiTutorMessage("", file.localFilePath ?: file.uriString, base64)
                    currentScreen = CurrentScreen.AI_TUTOR
                    viewModel.clearIncomingShare()
                },
                onSaveToLibrary = { file, subject, type ->
                    viewModel.importSharedFile(file, subject, type) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                },
                onBatchImportAll = { files, subject ->
                    viewModel.batchImportAllSharedFiles(files, subject) { count ->
                        Toast.makeText(context, "Saved $count files to ${subject.title}!", Toast.LENGTH_LONG).show()
                        viewModel.clearIncomingShare()
                    }
                },
                onRouteToAdminUpload = { files ->
                    viewModel.routeSharedBatchToAdminUpload(files)
                },
                isAdmin = currentUser?.role == UserRole.ADMIN.name
            )
        }
    }
}
