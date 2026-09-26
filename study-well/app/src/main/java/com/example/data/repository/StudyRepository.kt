package com.example.data.repository

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.firebase.FirebaseDatabaseService
import com.example.data.model.ChatMessageEntity
import com.example.data.model.HomeworkReminderEntity
import com.example.data.model.LibraryDocumentEntity
import com.example.data.model.QuizAttemptEntity
import com.example.data.model.StudyMaterialEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.SubjectItem
import com.example.data.model.UserActivityEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class StudyRepository(
    private val database: AppDatabase,
    private val context: Context? = null
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)
    private val userDao = database.userDao()
    private val subjectDao = database.subjectDao()
    private val materialDao = database.studyMaterialDao()
    private val quizAttemptDao = database.quizAttemptDao()
    private val userActivityDao = database.userActivityDao()
    private val chatMessageDao = database.chatMessageDao()
    private val libraryDao = database.libraryDocumentDao()
    private val homeworkReminderDao = database.homeworkReminderDao()

    suspend fun authenticateUser(username: String, password: String): UserEntity? {
        val trimmedUser = username.trim()
        val user = userDao.getUserByUsername(trimmedUser)
        
        // Specific Master Admin credentials
        if (trimmedUser == "selimamr" && password == "sosololo9102011") {
            if (user == null) {
                val admin = UserEntity(
                    username = "selimamr",
                    passwordHash = "sosololo9102011",
                    displayName = "Selim Amr (Admin)",
                    role = UserRole.ADMIN.name
                )
                userDao.insertUser(admin)
                return admin
            } else {
                // Ensure existing selimamr is marked as ADMIN
                val admin = user.copy(role = UserRole.ADMIN.name, passwordHash = "sosololo9102011")
                userDao.insertUser(admin)
                return admin
            }
        }

        if (user != null && user.passwordHash == password) {
            return user
        }

        // If not found locally, query Cloud Firestore for cross-device authentication
        if (context != null) {
            val cloudUser = FirebaseDatabaseService.fetchUserFromCloud(context, trimmedUser)
            if (cloudUser != null && cloudUser.passwordHash == password) {
                userDao.insertUser(cloudUser)
                return cloudUser
            }
        }
        return null
    }

    suspend fun registerUser(username: String, password: String, displayName: String, role: UserRole = UserRole.STUDENT): Boolean {
        val trimmedUser = username.trim()
        val existing = userDao.getUserByUsername(trimmedUser)
        if (existing != null) return false

        // Only selimamr can be ADMIN, all other registrations are regular Students
        val assignedRole = if (trimmedUser == "selimamr" && password == "sosololo9102011") {
            UserRole.ADMIN.name
        } else {
            UserRole.STUDENT.name
        }

        val newUser = UserEntity(
            username = trimmedUser,
            passwordHash = password,
            displayName = displayName.ifBlank { trimmedUser },
            role = assignedRole
        )
        userDao.insertUser(newUser)

        // Live sync new registered user to Firebase Cloud Firestore
        context?.let { ctx ->
            repositoryScope.launch {
                FirebaseDatabaseService.syncUser(ctx, newUser)
            }
        }
        return true
    }

    fun getAllUsers(): Flow<List<UserEntity>> {
        return userDao.getAllUsers()
    }

    // Dynamic Subject Management
    fun getAllSubjects(): Flow<List<SubjectItem>> {
        return subjectDao.getAllSubjects().map { list ->
            list.map { it.toSubjectItem() }
        }
    }

    suspend fun addSubject(subject: SubjectItem) {
        subjectDao.insertSubject(subject.toSubjectEntity())
    }

    suspend fun deleteSubject(subjectId: String) {
        subjectDao.deleteSubjectById(subjectId)
        materialDao.deleteMaterialsBySubject(subjectId)
    }

    suspend fun deleteAllMaterials() {
        materialDao.deleteAllMaterials()
    }

    fun getMaterialsForSubject(subjectId: String): Flow<List<StudyMaterialEntity>> {
        return materialDao.getMaterialsBySubject(subjectId)
    }

    fun getMaterialsForSubjectAndType(subjectId: String, type: String): Flow<List<StudyMaterialEntity>> {
        return materialDao.getMaterialsBySubjectAndType(subjectId, type)
    }

    fun getAllMaterials(): Flow<List<StudyMaterialEntity>> {
        return materialDao.getAllMaterials()
    }

    suspend fun getMaterialById(id: Long): StudyMaterialEntity? {
        return materialDao.getMaterialById(id)
    }

    suspend fun addStudyMaterial(material: StudyMaterialEntity): Long {
        val id = materialDao.insertMaterial(material)
        context?.let { ctx ->
            repositoryScope.launch {
                FirebaseDatabaseService.syncStudyMaterial(ctx, material.copy(id = id))
            }
        }
        return id
    }

    suspend fun updateStudyMaterial(material: StudyMaterialEntity) {
        materialDao.updateMaterial(material)
        context?.let { ctx ->
            repositoryScope.launch {
                FirebaseDatabaseService.syncStudyMaterial(ctx, material)
            }
        }
    }

    suspend fun deleteStudyMaterial(material: StudyMaterialEntity) {
        materialDao.deleteMaterial(material)
        context?.let { ctx ->
            repositoryScope.launch {
                FirebaseDatabaseService.deleteStudyMaterial(ctx, material.id)
            }
        }
    }

    suspend fun deleteStudyMaterialById(id: Long) {
        materialDao.deleteMaterialById(id)
        context?.let { ctx ->
            repositoryScope.launch {
                FirebaseDatabaseService.deleteStudyMaterial(ctx, id)
            }
        }
    }

    suspend fun backupAllMaterialsToStorage(): Boolean {
        return context?.let { ctx ->
            com.example.utils.PersistentDataBackupHelper.performPersistentBackup(ctx, database)
        } ?: false
    }

    suspend fun restoreAllMaterialsFromStorage(): Int {
        return context?.let { ctx ->
            com.example.utils.PersistentDataBackupHelper.autoRestoreIfAvailable(ctx, database)
        } ?: 0
    }

    fun getQuizAttempts(username: String): Flow<List<QuizAttemptEntity>> {
        return quizAttemptDao.getAttemptsForUser(username)
    }

    fun getQuizAttemptsForSubject(username: String, subjectId: String): Flow<List<QuizAttemptEntity>> {
        return quizAttemptDao.getAttemptsForUserAndSubject(username, subjectId)
    }

    fun getAllQuizAttempts(): Flow<List<QuizAttemptEntity>> {
        return quizAttemptDao.getAllAttempts()
    }

    suspend fun saveQuizAttempt(attempt: QuizAttemptEntity): Long {
        val id = quizAttemptDao.insertAttempt(attempt)
        context?.let { ctx ->
            repositoryScope.launch {
                FirebaseDatabaseService.syncQuizAttempt(ctx, attempt.copy(id = id))
            }
        }
        return id
    }

    suspend fun getAverageScore(username: String): Float {
        return quizAttemptDao.getAverageScore(username) ?: 0f
    }

    suspend fun getTotalQuizzesTaken(username: String): Int {
        return quizAttemptDao.getTotalQuizzesTaken(username)
    }

    // User Activity Tracking
    suspend fun recordUserActivity(activity: UserActivityEntity): Long {
        val id = userActivityDao.recordActivity(activity)
        context?.let { ctx ->
            repositoryScope.launch {
                FirebaseDatabaseService.syncUserActivity(ctx, activity.copy(id = id))
            }
        }
        return id
    }

    fun getAllUserActivities(): Flow<List<UserActivityEntity>> {
        return userActivityDao.getAllActivities()
    }

    fun getUserActivities(username: String): Flow<List<UserActivityEntity>> {
        return userActivityDao.getActivitiesForUser(username)
    }

    suspend fun getCountViewedMaterials(username: String): Int {
        return userActivityDao.getCountViewedMaterials(username)
    }

    // AI Tutor Chat History
    fun getChatHistory(username: String, subjectId: String): Flow<List<ChatMessageEntity>> {
        return chatMessageDao.getChatHistoryForSubject(username, subjectId)
    }

    fun getAllUserChatHistory(username: String): Flow<List<ChatMessageEntity>> {
        return chatMessageDao.getAllChatHistory(username)
    }

    suspend fun saveChatMessage(message: ChatMessageEntity): Long {
        val id = chatMessageDao.insertMessage(message)
        context?.let { ctx ->
            repositoryScope.launch {
                FirebaseDatabaseService.syncChatMessage(ctx, message.copy(id = id))
            }
        }
        return id
    }

    suspend fun clearChatForSubject(username: String, subjectId: String) {
        chatMessageDao.clearChatForSubject(username, subjectId)
    }

    suspend fun clearAllChatsForUser(username: String) {
        chatMessageDao.clearAllChatsForUser(username)
    }

    // Student Library Management
    fun getAllLibraryDocuments(): Flow<List<LibraryDocumentEntity>> {
        return libraryDao.getAllLibraryDocuments()
    }

    suspend fun addLibraryDocument(doc: LibraryDocumentEntity): Long {
        val id = libraryDao.insertDocument(doc)
        context?.let { ctx ->
            repositoryScope.launch {
                FirebaseDatabaseService.syncLibraryDocument(ctx, doc.copy(id = id))
            }
        }
        return id
    }

    suspend fun deleteLibraryDocument(doc: LibraryDocumentEntity) {
        try {
            val file = java.io.File(doc.filePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Exception) {}
        libraryDao.deleteDocument(doc)
    }

    suspend fun syncLibraryFromDisk(appContext: Context) {
        try {
            val downloadsDir = appContext.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
            if (downloadsDir != null && downloadsDir.exists()) {
                val pdfFiles = downloadsDir.listFiles { f -> f.isFile && f.extension.equals("pdf", ignoreCase = true) }
                pdfFiles?.forEach { file ->
                    val sizeKb = (file.length() / 1024.0)
                    val formattedSize = if (sizeKb >= 1024) String.format(java.util.Locale.US, "%.1f MB", sizeKb / 1024.0) else String.format(java.util.Locale.US, "%.0f KB", sizeKb)
                    val name = file.name
                    val isEdited = name.contains("Annotated", ignoreCase = true) || name.contains("Edited", ignoreCase = true)
                    // Only sync student edited/annotated documents into personal Student Library
                    if (isEdited) {
                        val cleanTitle = name.removeSuffix(".pdf").replace("_", " ")

                        val doc = LibraryDocumentEntity(
                            title = cleanTitle,
                            filePath = file.absolutePath,
                            fileName = file.name,
                            docType = "EDITED_PDF",
                            fileSizeFormatted = formattedSize,
                            timestamp = file.lastModified()
                        )
                        val id = libraryDao.insertDocument(doc)
                        repositoryScope.launch {
                            FirebaseDatabaseService.syncLibraryDocument(appContext, doc.copy(id = id))
                        }
                    }
                }
            }
        } catch (_: Exception) {}
    }

    // Homework Reminders Management
    fun getAllHomeworkReminders(): Flow<List<HomeworkReminderEntity>> {
        return homeworkReminderDao.getAllReminders()
    }

    suspend fun addHomeworkReminder(reminder: HomeworkReminderEntity): Long {
        val id = homeworkReminderDao.insertReminder(reminder)
        context?.let { ctx ->
            repositoryScope.launch {
                FirebaseDatabaseService.syncHomeworkReminder(ctx, reminder.copy(id = id))
            }
        }
        return id
    }

    suspend fun updateHomeworkReminder(reminder: HomeworkReminderEntity) {
        homeworkReminderDao.updateReminder(reminder)
        context?.let { ctx ->
            repositoryScope.launch {
                FirebaseDatabaseService.syncHomeworkReminder(ctx, reminder)
            }
        }
    }

    suspend fun deleteHomeworkReminder(reminder: HomeworkReminderEntity) {
        homeworkReminderDao.deleteReminder(reminder)
        context?.let { ctx ->
            repositoryScope.launch {
                FirebaseDatabaseService.deleteHomeworkReminder(ctx, reminder.id)
            }
        }
    }

    // Upgraded Cloud Firebase Synchronization
    suspend fun syncAllFromCloud(): com.example.data.firebase.SyncSummary {
        val ctx = context ?: return com.example.data.firebase.SyncSummary(false, "Context unavailable")
        return FirebaseDatabaseService.pullAllFromFirestore(ctx, database)
    }

    suspend fun backupAllToCloud(): com.example.data.firebase.SyncSummary {
        val ctx = context ?: return com.example.data.firebase.SyncSummary(false, "Context unavailable")
        return FirebaseDatabaseService.pushAllToFirestore(ctx, database)
    }
}
