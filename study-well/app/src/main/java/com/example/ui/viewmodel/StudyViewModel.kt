package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.ChatMessageEntity
import com.example.data.model.HomeworkReminderEntity
import com.example.data.model.LibraryDocumentEntity
import com.example.data.model.MaterialType
import com.example.data.model.QuizAttemptEntity
import com.example.data.model.QuizQuestion
import com.example.data.model.SharedIncomingBatch
import com.example.data.model.SharedIncomingFile
import com.example.data.model.StudyMaterialEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.SubjectEnum
import com.example.data.model.SubjectItem
import com.example.data.model.UserActivityEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.repository.StudyRepository
import com.example.network.GeminiService
import com.example.utils.FileUtils
import com.example.utils.ExamPdfGenerator
import java.io.File
import android.content.Intent
import android.net.Uri
import com.example.CurrentScreen
import com.example.data.model.AdminSelectedFile
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyRepository
    private val geminiService = GeminiService()
    private val prefs = application.getSharedPreferences("study_well_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SAVED_USERNAME = "saved_username"
        private const val KEY_SAVED_PASSWORD = "saved_password"
        private const val KEY_INTRO_COMPLETED = "intro_completed"
    }

    private val _isIntroCompleted = MutableStateFlow(prefs.getBoolean(KEY_INTRO_COMPLETED, false))
    val isIntroCompleted: StateFlow<Boolean> = _isIntroCompleted.asStateFlow()

    private val _isInitialSessionCheckDone = MutableStateFlow(false)
    val isInitialSessionCheckDone: StateFlow<Boolean> = _isInitialSessionCheckDone.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = StudyRepository(db, application)

        // Try automatic login if saved credentials exist
        checkSavedSession()

        // Sync student library from downloaded files on disk
        syncLibrary()

        // Start background homework reminder notification checker
        startHomeworkReminderChecker()
    }

    private fun checkSavedSession() {
        viewModelScope.launch {
            val savedUser = prefs.getString(KEY_SAVED_USERNAME, null)
            val savedPass = prefs.getString(KEY_SAVED_PASSWORD, null)
            var user: UserEntity? = null
            if (!savedUser.isNullOrBlank() && !savedPass.isNullOrBlank()) {
                user = repository.authenticateUser(savedUser, savedPass)
            }
            if (user == null) {
                try {
                    repository.registerUser("student", "", "Student", UserRole.STUDENT)
                } catch (_: Exception) {}
                user = repository.authenticateUser("student", "") ?: UserEntity(
                    username = "student",
                    passwordHash = "",
                    displayName = "Student",
                    role = UserRole.STUDENT.name
                )
            }
            _currentUser.value = user
            refreshUserStats(user.username)
            _isInitialSessionCheckDone.value = true
        }
    }

    fun completeIntro() {
        prefs.edit().putBoolean(KEY_INTRO_COMPLETED, true).apply()
        _isIntroCompleted.value = true
    }

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating: StateFlow<Boolean> = _isAuthenticating.asStateFlow()

    private val _selectedSubject = MutableStateFlow(SubjectEnum.PHYSICS)
    val selectedSubject: StateFlow<SubjectEnum> = _selectedSubject.asStateFlow()

    // Dynamic subjects list from database (defaults seeded with Physics, English, Math, Biology, Chemistry, Arabic OL, ICT)
    val allSubjects: StateFlow<List<SubjectItem>> = repository.getAllSubjects()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SubjectEnum.values().map { it.toSubjectItem() }
        )

    private val _selectedExamBoard = MutableStateFlow(com.example.data.model.ExamBoard.CAMBRIDGE)
    val selectedExamBoard: StateFlow<com.example.data.model.ExamBoard> = _selectedExamBoard.asStateFlow()

    fun selectExamBoard(board: com.example.data.model.ExamBoard) {
        _selectedExamBoard.value = board
    }

    private val _selectedSection = MutableStateFlow<String>("VIDEOS") // "VIDEOS", "BOOKS", "NOTES", "SHEETS", "EXAMS"
    val selectedSection: StateFlow<String> = _selectedSection.asStateFlow()

    // Active media viewers
    private val _activeVideo = MutableStateFlow<StudyMaterialEntity?>(null)
    val activeVideo: StateFlow<StudyMaterialEntity?> = _activeVideo.asStateFlow()

    private val _activeDocument = MutableStateFlow<StudyMaterialEntity?>(null)
    val activeDocument: StateFlow<StudyMaterialEntity?> = _activeDocument.asStateFlow()

    // Materials for selected subject
    val currentSubjectMaterials: StateFlow<List<StudyMaterialEntity>> = _selectedSubject
        .flatMapLatest { subject -> repository.getMaterialsForSubject(subject.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMaterials: StateFlow<List<StudyMaterialEntity>> = repository.getAllMaterials()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin Analytics & Users Tracking
    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUserActivities: StateFlow<List<UserActivityEntity>> = repository.getAllUserActivities()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQuizAttempts: StateFlow<List<QuizAttemptEntity>> = repository.getAllQuizAttempts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Quiz State
    private val _isGeneratingQuiz = MutableStateFlow(false)
    val isGeneratingQuiz: StateFlow<Boolean> = _isGeneratingQuiz.asStateFlow()

    private val _activeQuizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val activeQuizQuestions: StateFlow<List<QuizQuestion>> = _activeQuizQuestions.asStateFlow()

    private val _quizTopic = MutableStateFlow("")
    val quizTopic: StateFlow<String> = _quizTopic.asStateFlow()

    // Chat / AI Tutor State (Persisted in Room Database)
    private val _isAiChatting = MutableStateFlow(false)
    val isAiChatting: StateFlow<Boolean> = _isAiChatting.asStateFlow()

    val chatMessages: StateFlow<List<ChatMessageEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) {
            repository.getAllUserChatHistory(user.username)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUserChatHistory: StateFlow<List<ChatMessageEntity>> = chatMessages

    // User Quiz Stats
    private val _userAverageScore = MutableStateFlow(0f)
    val userAverageScore: StateFlow<Float> = _userAverageScore.asStateFlow()

    private val _userTotalQuizzes = MutableStateFlow(0)
    val userTotalQuizzes: StateFlow<Int> = _userTotalQuizzes.asStateFlow()

    // Student Library: Downloaded & Edited PDFs
    val libraryDocuments: StateFlow<List<LibraryDocumentEntity>> = repository.getAllLibraryDocuments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Homework Reminders
    val homeworkReminders: StateFlow<List<HomeworkReminderEntity>> = repository.getAllHomeworkReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addHomeworkReminder(subjectName: String, topicName: String, dueDateTimestamp: Long) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val timeUntilDue = dueDateTimestamp - now

            // If the homework deadline is already less than 24 hours (or 2 hours) away at creation,
            // mark those notification flags as already passed so we NEVER send a false "24h" or "2h" alert!
            val alreadyPast24h = timeUntilDue <= 24 * 60 * 60 * 1000L
            val alreadyPast2h = timeUntilDue <= 2 * 60 * 60 * 1000L

            val reminder = HomeworkReminderEntity(
                subjectName = subjectName,
                topicName = topicName,
                dueDateTimestamp = dueDateTimestamp,
                notified24h = alreadyPast24h,
                notified2h = alreadyPast2h
            )
            val newId = repository.addHomeworkReminder(reminder)
            val appContext = getApplication<Application>().applicationContext
            com.example.utils.HomeworkNotificationHelper.scheduleHomeworkAlarms(
                context = appContext,
                reminderId = newId,
                subject = subjectName,
                topic = topicName,
                dueTimestamp = dueDateTimestamp
            )
        }
    }

    fun updateHomeworkReminder(reminder: HomeworkReminderEntity) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val timeUntilDue = reminder.dueDateTimestamp - now

            // If deadline changed, update notified flags appropriately
            val adjustedReminder = if (!reminder.isFinished) {
                reminder.copy(
                    notified24h = reminder.notified24h || timeUntilDue <= 24 * 60 * 60 * 1000L,
                    notified2h = reminder.notified2h || timeUntilDue <= 2 * 60 * 60 * 1000L
                )
            } else reminder

            repository.updateHomeworkReminder(adjustedReminder)
            val appContext = getApplication<Application>().applicationContext
            if (adjustedReminder.isFinished) {
                com.example.utils.HomeworkNotificationHelper.cancelHomeworkAlarms(appContext, adjustedReminder.id)
            } else {
                com.example.utils.HomeworkNotificationHelper.scheduleHomeworkAlarms(
                    context = appContext,
                    reminderId = adjustedReminder.id,
                    subject = adjustedReminder.subjectName,
                    topic = adjustedReminder.topicName,
                    dueTimestamp = adjustedReminder.dueDateTimestamp
                )
            }
        }
    }

    fun deleteHomeworkReminder(reminder: HomeworkReminderEntity) {
        viewModelScope.launch {
            repository.deleteHomeworkReminder(reminder)
            val appContext = getApplication<Application>().applicationContext
            com.example.utils.HomeworkNotificationHelper.cancelHomeworkAlarms(appContext, reminder.id)
        }
    }

    private fun startHomeworkReminderChecker() {
        // System AlarmManager is used via HomeworkNotificationHelper for exact scheduled reminders.
        // Unnecessary background DB polling is removed for optimal CPU and battery performance.
    }

    fun syncLibrary() {
        viewModelScope.launch {
            repository.syncLibraryFromDisk(getApplication())
        }
    }

    fun openLibraryDocument(doc: LibraryDocumentEntity) {
        val sub = SubjectEnum.fromId(doc.subjectId)
        _selectedSubject.value = sub
        val tempMaterial = StudyMaterialEntity(
            id = -doc.id,
            subjectId = doc.subjectId,
            materialType = if (doc.docType == "EXAM_PDF") "EXAM" else "NOTE",
            title = doc.title,
            topic = if (doc.docType == "EXAM_PDF") "Official Exam Paper" else "Edited Revision Document",
            description = "Saved in Student Library (${doc.fileSizeFormatted})",
            contentUrl = doc.filePath,
            documentContent = if (doc.notes.isNotBlank()) doc.notes else "Official Student Library Document: ${doc.title}",
            durationOrPages = "${doc.pageCount} pages",
            uploadedBy = "Student Library"
        )
        openDocumentViewer(tempMaterial)
    }

    fun shareLibraryDocument(doc: LibraryDocumentEntity, context: Context) {
        val file = java.io.File(doc.filePath)
        if (file.exists()) {
            com.example.utils.EditedDocumentPdfExporter.shareDownloadedPdf(context, file)
        } else {
            android.widget.Toast.makeText(context, "File not found: ${doc.fileName}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteLibraryDocument(doc: LibraryDocumentEntity) {
        viewModelScope.launch {
            repository.deleteLibraryDocument(doc)
        }
    }

    fun updateLibraryDocument(doc: LibraryDocumentEntity) {
        viewModelScope.launch {
            repository.addLibraryDocument(doc)
        }
    }

    fun openExternalPdfUri(uri: android.net.Uri) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val fileMeta = FileUtils.getFileMeta(app, uri)
            val tempMaterial = StudyMaterialEntity(
                id = -System.currentTimeMillis(),
                subjectId = _selectedSubject.value.id,
                materialType = "NOTE",
                title = fileMeta.name,
                topic = "Imported PDF",
                description = "External document (${fileMeta.sizeFormatted})",
                contentUrl = uri.toString(),
                documentContent = "Imported PDF: ${fileMeta.name}",
                durationOrPages = if (fileMeta.pageCount > 0) "${fileMeta.pageCount} pages" else "PDF Document",
                uploadedBy = "Local File"
            )
            openDocumentViewer(tempMaterial)
        }
    }

    fun importScannedPdf(file: java.io.File) {
        viewModelScope.launch {
            val totalPages = FileUtils.countPdfPagesFromFile(file)
            val sizeKb = file.length() / 1024.0
            val sizeFormatted = if (sizeKb > 1024) String.format("%.1f MB", sizeKb / 1024.0) else String.format("%.0f KB", sizeKb)
            val doc = LibraryDocumentEntity(
                fileName = file.name,
                title = file.nameWithoutExtension.replace("_", " "),
                subjectId = _selectedSubject.value.id,
                docType = "DOWNLOADED_PDF",
                filePath = file.absolutePath,
                fileSizeFormatted = sizeFormatted,
                pageCount = if (totalPages > 0) totalPages else 1,
                timestamp = System.currentTimeMillis()
            )
            repository.addLibraryDocument(doc)
            syncLibrary()
        }
    }

    fun login(username: String, pass: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isAuthenticating.value = true
            _authError.value = null
            val user = repository.authenticateUser(username, pass)
            _isAuthenticating.value = false
            if (user != null) {
                _currentUser.value = user
                // Save session for auto-login
                prefs.edit()
                    .putString(KEY_SAVED_USERNAME, username.trim())
                    .putString(KEY_SAVED_PASSWORD, pass)
                    .apply()
                refreshUserStats(user.username)
                onSuccess()
            } else {
                _authError.value = "Invalid username or password. Please try again."
            }
        }
    }

    fun register(username: String, pass: String, displayName: String, role: UserRole, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isAuthenticating.value = true
            _authError.value = null
            if (username.isBlank() || pass.isBlank()) {
                _authError.value = "Username and password cannot be empty."
                _isAuthenticating.value = false
                return@launch
            }
            val success = repository.registerUser(username, pass, displayName, role)
            _isAuthenticating.value = false
            if (success) {
                val user = repository.authenticateUser(username, pass)
                _currentUser.value = user
                // Save session for auto-login
                prefs.edit()
                    .putString(KEY_SAVED_USERNAME, username.trim())
                    .putString(KEY_SAVED_PASSWORD, pass)
                    .apply()
                user?.let { refreshUserStats(it.username) }
                onSuccess()
            } else {
                _authError.value = "Username already exists. Please choose another."
            }
        }
    }

    fun logout() {
        // Clear saved session
        prefs.edit()
            .remove(KEY_SAVED_USERNAME)
            .remove(KEY_SAVED_PASSWORD)
            .apply()
        _currentUser.value = null
        _activeVideo.value = null
        _activeDocument.value = null
        _activeQuizQuestions.value = emptyList()
    }

    fun selectSubject(subject: SubjectEnum) {
        _selectedSubject.value = subject
        _selectedSection.value = "VIDEOS"
    }

    fun selectSection(section: String) {
        _selectedSection.value = section
    }

    fun openVideoPlayer(material: StudyMaterialEntity) {
        _activeVideo.value = material
        _currentUser.value?.let { user ->
            viewModelScope.launch {
                repository.recordUserActivity(
                    UserActivityEntity(
                        username = user.username,
                        userDisplayName = user.displayName,
                        materialId = material.id,
                        materialTitle = material.title,
                        materialType = "VIDEO",
                        subjectId = material.subjectId,
                        actionType = "VIEWED_VIDEO"
                    )
                )
            }
        }
    }

    fun closeVideoPlayer() {
        _activeVideo.value = null
    }

    fun openDocumentViewer(material: StudyMaterialEntity) {
        _activeDocument.value = material
        _currentUser.value?.let { user ->
            viewModelScope.launch {
                repository.recordUserActivity(
                    UserActivityEntity(
                        username = user.username,
                        userDisplayName = user.displayName,
                        materialId = material.id,
                        materialTitle = material.title,
                        materialType = material.materialType,
                        subjectId = material.subjectId,
                        actionType = "READ_DOCUMENT"
                    )
                )
            }
        }
    }

    fun downloadStudyMaterial(material: StudyMaterialEntity, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                
                // 1. Resolve or pre-generate the local PDF file using FileUtils
                val localFile = withContext(Dispatchers.IO) {
                    FileUtils.ensurePdfFile(context, material)
                }
                
                if (localFile != null && localFile.exists()) {
                    // 2. Mark the material as cached/downloaded in Room Database
                    val updatedMaterial = material.copy(
                        isDownloaded = true,
                        localFilePath = localFile.absolutePath
                    )
                    repository.updateStudyMaterial(updatedMaterial)
                    
                    onComplete(true, "Successfully downloaded '${material.title}' for direct offline access!")
                } else {
                    onComplete(false, "Could not cache the PDF file. Please try again.")
                }
            } catch (e: Exception) {
                onComplete(false, "Failed to download: ${e.localizedMessage}")
            }
        }
    }

    fun closeDocumentViewer() {
        _activeDocument.value = null
    }

    fun startRealPastPaperQuiz(paper: com.example.data.repository.RealPastPaper, onReady: () -> Unit) {
        _selectedExamBoard.value = paper.examBoard
        _quizTopic.value = "${paper.paperCode} ${paper.paperTitle} (${paper.year})"
        _activeQuizQuestions.value = paper.toQuizQuestions()
        _isGeneratingQuiz.value = false
        onReady()
    }

    fun openRealPastPaperAsDocument(paper: com.example.data.repository.RealPastPaper, isMarkScheme: Boolean = false) {
        val mat = paper.toStudyMaterialEntity(isMarkScheme = isMarkScheme)
        openDocumentViewer(mat)
    }

    fun downloadRealExamPdf(
        paper: com.example.data.repository.RealPastPaper,
        includeMarkScheme: Boolean = true,
        onComplete: (File?) -> Unit
    ) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val file = ExamPdfGenerator.generateRealExamPdf(app, paper, includeMarkScheme)
            onComplete(file)
        }
    }

    fun generateAiQuizForTopic(
        subject: SubjectEnum,
        topic: String,
        questionCount: Int = 5,
        difficulty: String = "Extended",
        questionStyle: String = "Multiple Choice & Reasoning",
        examBoard: com.example.data.model.ExamBoard = _selectedExamBoard.value,
        pastYear: String = "2023",
        session: String = "May/June",
        paperVariant: String = "Paper 2",
        onReady: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _selectedExamBoard.value = examBoard
            _isGeneratingQuiz.value = true
            _quizTopic.value = if (topic.contains(examBoard.shortName, ignoreCase = true)) topic else "${examBoard.shortName} $topic ($pastYear)"

            // Check if an authentic real past paper matches this subject & board from RealPastPaperRepository
            val cleanYear = pastYear.filter { it.isDigit() }
            val matchingRealPaper = com.example.data.repository.RealPastPaperRepository.findPaper(
                subjectId = subject.id,
                examBoard = examBoard,
                year = if (cleanYear.isNotBlank()) cleanYear else null,
                paperCodeKeyword = if (paperVariant.contains("Paper", ignoreCase = true)) paperVariant else null
            )

            if (matchingRealPaper != null) {
                _activeQuizQuestions.value = matchingRealPaper.toQuizQuestions()
                _isGeneratingQuiz.value = false
                onReady()
                return@launch
            }

            // Extract context from books, notes, sheets, and exams for this subject and topic
            val subjectMats = currentSubjectMaterials.value.filter { it.subjectId == subject.id }
            val studyDocs = subjectMats.filter { it.materialType in listOf("BOOK", "NOTE", "SHEET", "EXAM") }
            val contextText = studyDocs.joinToString("\n\n") { mat ->
                "### Title: ${mat.title} (${mat.topic} - ${mat.materialType})\n${mat.documentContent.take(1500)}"
            }

            val questions = geminiService.generateTopicalQuiz(
                subject = subject,
                topic = topic,
                materialsContext = contextText.ifBlank { "Official ${examBoard.displayName} Grade 10 ${subject.title} curriculum on $topic" },
                questionCount = questionCount,
                difficulty = difficulty,
                questionStyle = questionStyle,
                examBoard = examBoard,
                pastYear = pastYear,
                session = session,
                paperVariant = paperVariant
            )

            _activeQuizQuestions.value = questions
            _isGeneratingQuiz.value = false
            onReady()
        }
    }

    fun submitQuizResult(topic: String, score: Int, total: Int, details: String = "") {
        val user = _currentUser.value ?: return
        val percentage = if (total > 0) (score.toFloat() / total) * 100f else 0f
        viewModelScope.launch {
            repository.saveQuizAttempt(
                QuizAttemptEntity(
                    username = user.username,
                    subjectId = _selectedSubject.value.id,
                    topic = topic,
                    score = score,
                    totalQuestions = total,
                    percentage = percentage,
                    details = details
                )
            )
            repository.recordUserActivity(
                UserActivityEntity(
                    username = user.username,
                    userDisplayName = user.displayName,
                    materialId = 0L,
                    materialTitle = "Quiz: $topic (${score}/$total)",
                    materialType = "QUIZ",
                    subjectId = _selectedSubject.value.id,
                    actionType = "COMPLETED_QUIZ"
                )
            )
            refreshUserStats(user.username)
        }
    }

    private fun refreshUserStats(username: String) {
        viewModelScope.launch {
            _userAverageScore.value = repository.getAverageScore(username)
            _userTotalQuizzes.value = repository.getTotalQuizzesTaken(username)
        }
    }

    fun sendAiTutorMessage(userMsg: String, imageUri: String? = null, imageBase64: String? = null) {
        val currentSub = _selectedSubject.value
        val trimmed = userMsg.trim()
        if (trimmed.isBlank() && imageBase64.isNullOrBlank()) return

        val displayPrompt = if (trimmed.isNotBlank()) {
            trimmed
        } else {
            "📸 [Analyzing problem from photo...]"
        }

        val detectedSub = if (trimmed.isNotBlank()) geminiService.detectSubject(trimmed) else currentSub
        _selectedSubject.value = detectedSub

        viewModelScope.launch {
            try {
                var user = _currentUser.value
                if (user == null) {
                    try {
                        repository.registerUser("student", "", "Student", UserRole.STUDENT)
                    } catch (_: Exception) {}
                    user = repository.authenticateUser("student", "") ?: UserEntity(
                        username = "student",
                        passwordHash = "",
                        displayName = "Student",
                        role = UserRole.STUDENT.name
                    )
                    _currentUser.value = user
                }

                // Save user message with imageUri immediately to database
                val userChatEntity = ChatMessageEntity(
                    username = user.username,
                    subjectId = detectedSub.id,
                    sender = "user",
                    text = displayPrompt,
                    imageUri = imageUri
                )
                repository.saveChatMessage(userChatEntity)

                _isAiChatting.value = true
                // Read prior history for context (exclude current prompt to prevent duplicates)
                val history = chatMessages.value.filter { it.text != displayPrompt }.takeLast(4).map { it.sender to it.text }
                val reply = geminiService.chatWithTutor(
                    subject = detectedSub,
                    history = history,
                    userQuery = trimmed,
                    imageBase64 = imageBase64
                )
                _isAiChatting.value = false

                // Save AI reply to database
                val aiChatEntity = ChatMessageEntity(
                    username = user.username,
                    subjectId = detectedSub.id,
                    sender = "gemini",
                    text = reply.ifBlank { "I have received your query. Please review the core concepts for ${detectedSub.title} and apply step-by-step reasoning." }
                )
                repository.saveChatMessage(aiChatEntity)
            } catch (e: Exception) {
                _isAiChatting.value = false
                val user = _currentUser.value ?: UserEntity(username = "student", passwordHash = "", displayName = "Student", role = UserRole.STUDENT.name)
                val fallbackReply = "🎯 **Final Answer**:\nHere is the step-by-step guide for your ${detectedSub.title} query:\n\n1. **Step 1**: Review the core definitions and principles.\n2. **Step 2**: Apply standard formulas and systematic problem-solving steps.\n3. **Step 3**: Verify your calculations with correct units."
                val aiChatEntity = ChatMessageEntity(
                    username = user.username,
                    subjectId = detectedSub.id,
                    sender = "gemini",
                    text = fallbackReply
                )
                try {
                    repository.saveChatMessage(aiChatEntity)
                } catch (_: Exception) {}
            }
        }
    }

    fun generateFullSubjectExam(
        subject: SubjectEnum,
        examBoard: com.example.data.model.ExamBoard = _selectedExamBoard.value,
        pastYear: String = "2023",
        onReady: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _selectedExamBoard.value = examBoard
            _isGeneratingQuiz.value = true
            _quizTopic.value = "${examBoard.shortName} Full Official Past Paper ($pastYear)"

            val cleanYear = pastYear.filter { it.isDigit() }
            val realPaper = com.example.data.repository.RealPastPaperRepository.findPaper(
                subjectId = subject.id,
                examBoard = examBoard,
                year = if (cleanYear.isNotBlank()) cleanYear else null
            )

            if (realPaper != null) {
                _activeQuizQuestions.value = realPaper.toQuizQuestions()
                _quizTopic.value = "${realPaper.paperCode} ${realPaper.paperTitle} (${realPaper.year})"
                _isGeneratingQuiz.value = false
                onReady()
                return@launch
            }

            val subjectMats = currentSubjectMaterials.value.filter { it.subjectId == subject.id }
            val contextText = subjectMats.joinToString("\n\n") { mat ->
                "### Title: ${mat.title} (${mat.topic} - ${mat.materialType})\n${mat.documentContent.take(1000)}"
            }

            val questions = geminiService.generateTopicalQuiz(
                subject = subject,
                topic = "${examBoard.displayName} ${subject.title} Full Official Examination ($pastYear)",
                materialsContext = contextText.ifBlank { "Full ${examBoard.displayName} Grade 10 curriculum for ${subject.title}" },
                examBoard = examBoard,
                pastYear = pastYear
            )

            _activeQuizQuestions.value = questions
            _isGeneratingQuiz.value = false
            onReady()
        }
    }

    fun clearChat() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.clearAllChatsForUser(user.username)
        }
    }

    fun clearAllUserChats() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.clearAllChatsForUser(user.username)
        }
    }

    // Target screen navigation event to allow ViewModel to navigate to specific screens (e.g. ADMIN_PANEL on WhatsApp share)
    private val _targetScreenEvent = MutableSharedFlow<CurrentScreen>(extraBufferCapacity = 1)
    val targetScreenEvent: SharedFlow<CurrentScreen> = _targetScreenEvent.asSharedFlow()

    fun navigateToScreen(screen: CurrentScreen) {
        _targetScreenEvent.tryEmit(screen)
    }

    // Pending files shared from WhatsApp / external apps for Admin Upload
    private val _pendingAdminSharedFiles = MutableStateFlow<List<AdminSelectedFile>>(emptyList())
    val pendingAdminSharedFiles: StateFlow<List<AdminSelectedFile>> = _pendingAdminSharedFiles.asStateFlow()

    fun clearPendingAdminSharedFiles() {
        _pendingAdminSharedFiles.value = emptyList()
    }

    private fun SharedIncomingFile.toAdminSelectedFile(): AdminSelectedFile {
        val fileUri = try {
            if (localFilePath != null) {
                val f = File(localFilePath)
                if (f.exists()) Uri.fromFile(f) else Uri.parse(uriString)
            } else {
                Uri.parse(uriString)
            }
        } catch (_: Exception) {
            Uri.EMPTY
        }
        val cleanTitle = FileUtils.cleanFileNameForTitle(fileName)
        val pagesOrDuration = if (pageCount > 0) "$pageCount Pages • $sizeFormatted" else sizeFormatted
        return AdminSelectedFile(
            uri = fileUri,
            originalName = fileName,
            sizeFormatted = sizeFormatted,
            isPdf = isPdf,
            customTitle = cleanTitle,
            contentUrl = localFilePath ?: uriString,
            documentContent = textContent.ifBlank { "### $cleanTitle\n\nShared from external application ($fileName)." },
            durationOrPages = pagesOrDuration,
            customTopic = ""
        )
    }

    fun routeSharedBatchToAdminUpload(files: List<SharedIncomingFile>) {
        val adminFiles = files.map { it.toAdminSelectedFile() }
        _pendingAdminSharedFiles.value = adminFiles
        _incomingShareBatch.value = null
        _targetScreenEvent.tryEmit(CurrentScreen.ADMIN_PANEL)
    }

    // Incoming Share State (Single or Multiple files from WhatsApp, Files, etc.)
    private val _incomingShareBatch = MutableStateFlow<SharedIncomingBatch?>(null)
    val incomingShareBatch: StateFlow<SharedIncomingBatch?> = _incomingShareBatch.asStateFlow()

    fun handleIncomingIntent(intent: Intent) {
        viewModelScope.launch {
            // Wait briefly for initial session authentication if not yet complete
            var attempts = 0
            while (!_isInitialSessionCheckDone.value && attempts < 20) {
                delay(50)
                attempts++
            }

            val batch = FileUtils.extractSharedBatchFromIntent(getApplication(), intent)
            if (batch != null && batch.items.isNotEmpty()) {
                val action = intent.action
                val user = _currentUser.value
                val isAdmin = user?.role == UserRole.ADMIN.name

                if (isAdmin) {
                    // Admin User: Directly navigate to Upload Screen with the shared document ready
                    val adminFiles = batch.items.map { it.toAdminSelectedFile() }
                    _pendingAdminSharedFiles.value = adminFiles
                    _incomingShareBatch.value = null
                    _targetScreenEvent.emit(CurrentScreen.ADMIN_PANEL)
                } else {
                    // If opened directly from external app (ACTION_VIEW) or a single PDF is opened and user is NOT an admin, open directly as document
                    if (!isAdmin && (action == Intent.ACTION_VIEW || (batch.items.size == 1 && batch.items.first().isPdf))) {
                        val singleItem = batch.items.first()
                        val targetSubject = _selectedSubject.value
                        openSharedItemAsDocument(singleItem, targetSubject)
                    } else {
                        _incomingShareBatch.value = batch
                    }
                }
            }
        }
    }

    fun clearIncomingShare() {
        _incomingShareBatch.value = null
    }

    fun openSharedItemAsDocument(file: SharedIncomingFile, subject: SubjectEnum) {
        val mat = StudyMaterialEntity(
            id = 0L,
            subjectId = subject.id,
            materialType = if (file.isPdf) "BOOK" else "NOTE",
            title = FileUtils.cleanFileNameForTitle(file.fileName),
            topic = "External Document",
            description = "Opened directly • No local copy stored (${file.sizeFormatted})",
            contentUrl = file.uriString.ifBlank { file.localFilePath ?: "" },
            documentContent = file.textContent.ifBlank { "### ${file.fileName}\n\nDocument opened directly from external application without creating a local copy." },
            durationOrPages = if (file.pageCount > 0) "${file.pageCount} pages" else file.sizeFormatted,
            uploadedBy = "External"
        )
        selectSubject(subject)
        openDocumentViewer(mat)
    }

    fun importSharedFile(
        file: SharedIncomingFile,
        subject: SubjectEnum,
        materialType: String,
        customTitle: String? = null,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val user = _currentUser.value
        val title = (customTitle?.takeIf { it.isNotBlank() } ?: FileUtils.cleanFileNameForTitle(file.fileName))
        val topic = when (subject) {
            SubjectEnum.PHYSICS -> "General Mechanics & Past Papers"
            SubjectEnum.ENGLISH -> "Comprehension & Directed Writing"
            SubjectEnum.MATH -> "Algebra & Past Paper Practice"
            SubjectEnum.BIOLOGY -> "Cell Biology & Genetics"
            SubjectEnum.CHEMISTRY -> "Stoichiometry & Chemical Bonding"
            SubjectEnum.ARABIC_OL -> "البلاغة والقواعد والنصوص"
            SubjectEnum.ICT -> "Theory & Practical Notes"
        }

        viewModelScope.launch {
            try {
                val material = StudyMaterialEntity(
                    subjectId = subject.id,
                    materialType = materialType,
                    title = title,
                    topic = topic,
                    description = "Imported from shared files (${file.sizeFormatted})",
                    contentUrl = file.localFilePath ?: file.uriString,
                    documentContent = file.textContent.ifBlank { "### $title\n\nImported shared document (${file.sizeFormatted})." },
                    durationOrPages = if (file.pageCount > 0) "${file.pageCount} pages" else file.sizeFormatted,
                    uploadedBy = user?.username ?: "Student"
                )
                repository.addStudyMaterial(material)
                onComplete(true, "Successfully added '$title' to ${subject.title}!")
            } catch (e: Exception) {
                onComplete(false, "Failed to import: ${e.message}")
            }
        }
    }

    fun batchImportAllSharedFiles(
        files: List<SharedIncomingFile>,
        subject: SubjectEnum,
        onComplete: (Int) -> Unit
    ) {
        val user = _currentUser.value
        viewModelScope.launch {
            var importedCount = 0
            for (file in files) {
                try {
                    val matType = when {
                        file.isPdf -> "BOOK"
                        file.isVideo -> "VIDEO"
                        else -> "NOTE"
                    }
                    val title = FileUtils.cleanFileNameForTitle(file.fileName)
                    val topic = "${subject.title} Shared Library"
                    val material = StudyMaterialEntity(
                        subjectId = subject.id,
                        materialType = matType,
                        title = title,
                        topic = topic,
                        description = "Batch imported (${file.sizeFormatted})",
                        contentUrl = file.localFilePath ?: file.uriString,
                        documentContent = file.textContent.ifBlank { "### $title\n\nShared file ($matType)." },
                        durationOrPages = if (file.pageCount > 0) "${file.pageCount} pages" else file.sizeFormatted,
                        uploadedBy = user?.username ?: "Student"
                    )
                    repository.addStudyMaterial(material)
                    importedCount++
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            onComplete(importedCount)
        }
    }

    // Admin Operations
    fun uploadMaterial(
        subjectId: String,
        materialType: String,
        title: String,
        topic: String,
        description: String,
        contentUrl: String,
        documentContent: String,
        durationOrPages: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        val user = _currentUser.value
        if (user?.role != UserRole.ADMIN.name) {
            onComplete(false, "Unauthorized: Admin privileges required.")
            return
        }

        if (title.isBlank()) {
            onComplete(false, "Title is required.")
            return
        }

        val safeTopic = topic.trim().ifBlank { "General Curriculum" }

        viewModelScope.launch {
            try {
                val material = StudyMaterialEntity(
                    subjectId = subjectId,
                    materialType = materialType,
                    title = title.trim(),
                    topic = safeTopic,
                    description = description.trim(),
                    contentUrl = contentUrl.trim().ifBlank {
                        if (materialType == "VIDEO") "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                        else "https://example.com/document.pdf"
                    },
                    documentContent = documentContent.trim(),
                    durationOrPages = durationOrPages.trim().ifBlank {
                        if (materialType == "VIDEO") "15 mins" else "24 pages"
                    },
                    uploadedBy = user.username
                )
                repository.addStudyMaterial(material)
                onComplete(true, "Material uploaded successfully!")
            } catch (e: Exception) {
                onComplete(false, "Failed to upload: ${e.message}")
            }
        }
    }

    fun deleteMaterial(material: StudyMaterialEntity, onComplete: (Boolean) -> Unit = {}) {
        val user = _currentUser.value
        if (user?.role != UserRole.ADMIN.name) {
            onComplete(false)
            return
        }
        viewModelScope.launch {
            repository.deleteStudyMaterial(material)
            onComplete(true)
        }
    }

    // Dynamic Admin Subject Management
    fun addSubject(
        title: String,
        syllabusCode: String,
        description: String,
        topics: List<String>,
        colorHex: Long,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val user = _currentUser.value
        if (user?.role != UserRole.ADMIN.name) {
            onComplete(false, "Admin credentials required.")
            return
        }
        if (title.isBlank()) {
            onComplete(false, "Subject title is required.")
            return
        }

        viewModelScope.launch {
            try {
                val subjectId = title.trim().uppercase().replace(" ", "_")
                val newSubject = SubjectItem(
                    id = subjectId,
                    title = title.trim(),
                    syllabusCode = syllabusCode.trim().ifBlank { "Curriculum Study" },
                    description = description.trim().ifBlank { "Study materials, past papers and exams for ${title.trim()}" },
                    defaultTopics = if (topics.isNotEmpty()) topics else listOf("Core Principles", "Advanced Applications", "Past Paper Questions"),
                    color = androidx.compose.ui.graphics.Color(colorHex),
                    isCustom = true
                )
                repository.addSubject(newSubject)
                onComplete(true, "Subject '${title.trim()}' added successfully!")
            } catch (e: Exception) {
                onComplete(false, "Failed to add subject: ${e.message}")
            }
        }
    }

    fun deleteSubject(subjectId: String, onComplete: (Boolean) -> Unit = {}) {
        val user = _currentUser.value
        if (user?.role != UserRole.ADMIN.name) {
            onComplete(false)
            return
        }
        viewModelScope.launch {
            try {
                repository.deleteSubject(subjectId)
                if (_selectedSubject.value.id == subjectId) {
                    _selectedSubject.value = SubjectEnum.PHYSICS
                }
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    fun clearAllMaterials(onComplete: (Boolean) -> Unit = {}) {
        val user = _currentUser.value
        if (user?.role != UserRole.ADMIN.name) {
            onComplete(false)
            return
        }
        viewModelScope.launch {
            try {
                repository.deleteAllMaterials()
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    // --- Persistent Backup & Restore Operations (Max Level) ---

    private val _backupDiagnostics = MutableStateFlow<com.example.utils.PersistentDataBackupHelper.BackupDiagnostics?>(null)
    val backupDiagnostics: StateFlow<com.example.utils.PersistentDataBackupHelper.BackupDiagnostics?> = _backupDiagnostics.asStateFlow()

    fun refreshBackupDiagnostics(context: Context) {
        viewModelScope.launch {
            try {
                val db = com.example.data.db.AppDatabase.getDatabase(context)
                val diag = com.example.utils.PersistentDataBackupHelper.getBackupDiagnostics(context, db)
                _backupDiagnostics.value = diag
            } catch (_: Exception) {}
        }
    }

    fun performDeviceBackup(context: Context, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val db = com.example.data.db.AppDatabase.getDatabase(context)
                val success = com.example.utils.PersistentDataBackupHelper.performPersistentBackup(context, db)
                refreshBackupDiagnostics(context)
                if (success) {
                    onResult(true, "Full backup saved to device Downloads (StudyWellBackup)!")
                } else {
                    onResult(false, "No materials to backup or storage unavailable.")
                }
            } catch (e: Exception) {
                onResult(false, "Backup failed: ${e.message}")
            }
        }
    }

    fun shareFullBackupArchive(context: Context, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val db = com.example.data.db.AppDatabase.getDatabase(context)
                val success = com.example.utils.PersistentDataBackupHelper.shareFullBackupPackage(context, db)
                refreshBackupDiagnostics(context)
                if (success) {
                    onResult(true, "Opening Share Sheet with complete backup package...")
                } else {
                    onResult(false, "Failed generating shareable backup archive.")
                }
            } catch (e: Exception) {
                onResult(false, "Share failed: ${e.message}")
            }
        }
    }

    fun exportFullArchiveToSafUri(
        context: Context,
        uri: android.net.Uri,
        onProgress: (Float, String) -> Unit,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val db = com.example.data.db.AppDatabase.getDatabase(context)
                val outStream = context.contentResolver.openOutputStream(uri, "wt")
                if (outStream == null) {
                    onResult(false, "Could not open target location.")
                    return@launch
                }
                val success = com.example.utils.PersistentDataBackupHelper.createFullZipBackupStream(
                    context,
                    db,
                    outStream,
                    onProgress
                )
                refreshBackupDiagnostics(context)
                if (success) {
                    onResult(true, "Complete .studywell archive exported successfully!")
                } else {
                    onResult(false, "Failed writing archive to chosen location.")
                }
            } catch (e: Exception) {
                onResult(false, "Archive export failed: ${e.message}")
            }
        }
    }

    fun exportBackupToSafUri(context: Context, uri: android.net.Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val db = com.example.data.db.AppDatabase.getDatabase(context)
                val json = com.example.utils.PersistentDataBackupHelper.generateBackupJson(context, db)
                val success = com.example.utils.PersistentDataBackupHelper.writeBackupToUri(context, uri, json)
                refreshBackupDiagnostics(context)
                if (success) {
                    onResult(true, "Backup JSON manifest exported successfully!")
                } else {
                    onResult(false, "Failed writing backup file.")
                }
            } catch (e: Exception) {
                onResult(false, "Export failed: ${e.message}")
            }
        }
    }

    fun restoreBackupFromSafUri(
        context: Context,
        uri: android.net.Uri,
        onProgress: (Float, String) -> Unit = { _, _ -> },
        onResult: (Int, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val db = com.example.data.db.AppDatabase.getDatabase(context)
                val inStream = context.contentResolver.openInputStream(uri)
                if (inStream == null) {
                    onResult(0, "Could not read selected backup file.")
                    return@launch
                }
                val result = com.example.utils.PersistentDataBackupHelper.restoreArchiveFromStream(
                    context,
                    db,
                    inStream,
                    onProgress
                )
                refreshBackupDiagnostics(context)
                if (result.success && result.restoredMaterialsCount > 0) {
                    onResult(result.restoredMaterialsCount, result.message)
                } else if (result.success) {
                    onResult(0, "All items in this backup are already up to date.")
                } else {
                    onResult(0, result.message)
                }
            } catch (e: Exception) {
                onResult(0, "Restore failed: ${e.message}")
            }
        }
    }

    fun autoRestoreFromDevice(context: Context, onResult: (Int, String) -> Unit) {
        viewModelScope.launch {
            try {
                val db = com.example.data.db.AppDatabase.getDatabase(context)
                val restored = com.example.utils.PersistentDataBackupHelper.autoRestoreIfAvailable(context, db)
                refreshBackupDiagnostics(context)
                if (restored > 0) {
                    onResult(restored, "Successfully restored $restored materials from device backup!")
                } else {
                    onResult(0, "All materials from device backup are already up to date.")
                }
            } catch (e: Exception) {
                onResult(0, "Restore error: ${e.message}")
            }
        }
    }

    // Upgraded Firebase Cloud Sync
    fun syncAllFromCloud(onResult: (com.example.data.firebase.SyncSummary) -> Unit) {
        viewModelScope.launch {
            try {
                val result = repository.syncAllFromCloud()
                onResult(result)
            } catch (e: Exception) {
                onResult(com.example.data.firebase.SyncSummary(false, "Sync error: ${e.message}"))
            }
        }
    }

    fun backupAllToCloud(onResult: (com.example.data.firebase.SyncSummary) -> Unit) {
        viewModelScope.launch {
            try {
                val result = repository.backupAllToCloud()
                onResult(result)
            } catch (e: Exception) {
                onResult(com.example.data.firebase.SyncSummary(false, "Cloud backup error: ${e.message}"))
            }
        }
    }
}
