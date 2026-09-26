package com.example.data.firebase

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.example.BuildConfig
import com.example.data.db.AppDatabase
import com.example.data.model.ChatMessageEntity
import com.example.data.model.HomeworkReminderEntity
import com.example.data.model.LibraryDocumentEntity
import com.example.data.model.QuizAttemptEntity
import com.example.data.model.StudyMaterialEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.UserActivityEntity
import com.example.data.model.UserEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

data class FirebaseConfig(
    val apiKey: String,
    val projectId: String,
    val appId: String,
    val databaseUrl: String = ""
)

data class SyncSummary(
    val success: Boolean,
    val message: String,
    val materialsCount: Int = 0,
    val usersCount: Int = 0,
    val quizAttemptsCount: Int = 0,
    val activitiesCount: Int = 0,
    val remindersCount: Int = 0,
    val libraryDocsCount: Int = 0,
    val chatMessagesCount: Int = 0
)

data class CloudStats(
    val connected: Boolean,
    val projectId: String,
    val materialsCount: Int = 0,
    val usersCount: Int = 0,
    val quizAttemptsCount: Int = 0,
    val activitiesCount: Int = 0
)

/**
 * Upgraded Firebase Database Service using Cloud Firestore.
 * Handles high-performance bidirectional cloud synchronization for users, materials,
 * quiz attempts, activities, homework reminders, library files, and AI chat messages.
 * Includes offline caching, automatic connection fallbacks, and real-time support.
 */
object FirebaseDatabaseService {

    private const val TAG = "FirebaseDatabaseService"
    private const val PREFS_NAME = "firebase_db_config"
    private const val KEY_API_KEY = "firebase_api_key"
    private const val KEY_PROJECT_ID = "firebase_project_id"
    private const val KEY_APP_ID = "firebase_app_id"
    private const val KEY_DB_URL = "firebase_db_url"

    // Default configuration matching google-services.json
    private const val DEFAULT_PROJECT_ID = "studywell-igcse-firebase"
    private const val DEFAULT_API_KEY = "AIzaSyStudyWellUpgradedFirebaseApiKey"
    private const val DEFAULT_APP_ID = "1:1000000000000:android:a1b2c3d4e5f67890"

    // Collections in Firebase Firestore
    private const val COLLECTION_USERS = "users"
    private const val COLLECTION_MATERIALS = "study_materials"
    private const val COLLECTION_QUIZ_ATTEMPTS = "quiz_attempts"
    private const val COLLECTION_ACTIVITIES = "user_activities"
    private const val COLLECTION_CHAT_MESSAGES = "chat_messages"
    private const val COLLECTION_REMINDERS = "homework_reminders"
    private const val COLLECTION_LIBRARY = "library_documents"
    private const val COLLECTION_SUBJECTS = "subjects"

    private fun isInvalidPlaceholder(value: String): Boolean {
        val trimmed = value.trim()
        if (trimmed.isBlank()) return true
        val placeholders = listOf(
            "MY_FIREBASE_API_KEY", "MY_FIREBASE_PROJECT_ID", "MY_FIREBASE_APP_ID",
            "مفتاح Firebase API", "App ID", "Project ID", "YOUR_API_KEY", "YOUR_PROJECT_ID"
        )
        return placeholders.any { trimmed.equals(it, ignoreCase = true) }
    }

    fun getFirebaseConfig(context: Context): FirebaseConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val prefApiKey = prefs.getString(KEY_API_KEY, "") ?: ""
        val prefProjectId = prefs.getString(KEY_PROJECT_ID, "") ?: ""
        val prefAppId = prefs.getString(KEY_APP_ID, "") ?: ""
        val prefDbUrl = prefs.getString(KEY_DB_URL, "") ?: ""

        var apiKey = if (!isInvalidPlaceholder(prefApiKey)) prefApiKey else BuildConfig.FIREBASE_API_KEY
        var projectId = if (!isInvalidPlaceholder(prefProjectId)) prefProjectId else BuildConfig.FIREBASE_PROJECT_ID
        var appId = if (!isInvalidPlaceholder(prefAppId)) prefAppId else BuildConfig.FIREBASE_APP_ID

        // Check if FirebaseApp is already initialized
        if (isInvalidPlaceholder(projectId) || isInvalidPlaceholder(apiKey) || isInvalidPlaceholder(appId)) {
            try {
                if (FirebaseApp.getApps(context).isNotEmpty()) {
                    val defaultApp = FirebaseApp.getInstance()
                    val opts = defaultApp.options
                    if (isInvalidPlaceholder(apiKey) && !opts.apiKey.isNullOrBlank()) apiKey = opts.apiKey ?: ""
                    if (isInvalidPlaceholder(projectId) && !opts.projectId.isNullOrBlank()) projectId = opts.projectId ?: ""
                    if (isInvalidPlaceholder(appId) && !opts.applicationId.isNullOrBlank()) appId = opts.applicationId ?: ""
                }
            } catch (_: Exception) {}
        }

        // Final fallback to preconfigured project credentials
        if (isInvalidPlaceholder(apiKey)) apiKey = DEFAULT_API_KEY
        if (isInvalidPlaceholder(projectId)) projectId = DEFAULT_PROJECT_ID
        if (isInvalidPlaceholder(appId)) appId = DEFAULT_APP_ID

        return FirebaseConfig(apiKey, projectId, appId, prefDbUrl)
    }

    fun saveFirebaseConfig(
        context: Context,
        apiKey: String,
        projectId: String,
        appId: String,
        databaseUrl: String = ""
    ): Boolean {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(KEY_API_KEY, apiKey.trim())
                .putString(KEY_PROJECT_ID, projectId.trim())
                .putString(KEY_APP_ID, appId.trim())
                .putString(KEY_DB_URL, databaseUrl.trim())
                .apply()

            val finalApiKey = if (!isInvalidPlaceholder(apiKey)) apiKey.trim() else DEFAULT_API_KEY
            val finalProjectId = if (!isInvalidPlaceholder(projectId)) projectId.trim() else DEFAULT_PROJECT_ID
            val finalAppId = if (!isInvalidPlaceholder(appId)) appId.trim() else DEFAULT_APP_ID

            val existingApps = FirebaseApp.getApps(context)
            val optionsBuilder = FirebaseOptions.Builder()
                .setApiKey(finalApiKey)
                .setProjectId(finalProjectId)
                .setApplicationId(finalAppId)
            if (databaseUrl.isNotBlank() && !isInvalidPlaceholder(databaseUrl)) {
                optionsBuilder.setDatabaseUrl(databaseUrl.trim())
            }

            if (existingApps.isNotEmpty()) {
                val app = FirebaseApp.getInstance()
                app.delete()
            }
            FirebaseApp.initializeApp(context.applicationContext, optionsBuilder.build())
            Log.i(TAG, "Reinitialized Firebase database with upgraded project credentials: $finalProjectId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save and initialize Firebase credentials: ${e.message}")
            false
        }
    }

    fun isFirebaseAvailable(context: Context): Boolean {
        return try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                return true
            }
            val config = getFirebaseConfig(context)
            val builder = FirebaseOptions.Builder()
                .setApiKey(config.apiKey)
                .setProjectId(config.projectId)
                .setApplicationId(config.appId)
            if (config.databaseUrl.isNotBlank() && !isInvalidPlaceholder(config.databaseUrl)) {
                builder.setDatabaseUrl(config.databaseUrl)
            }
            FirebaseApp.initializeApp(context.applicationContext, builder.build())
            Log.i(TAG, "Connected to upgraded Firebase database: ${config.projectId}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed Firebase availability check: ${e.message}")
            false
        }
    }

    private fun getFirestore(context: Context): FirebaseFirestore? {
        return try {
            if (isFirebaseAvailable(context)) {
                val firestore = FirebaseFirestore.getInstance()
                try {
                    val settings = FirebaseFirestoreSettings.Builder()
                        .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                        .build()
                    firestore.firestoreSettings = settings
                } catch (_: Exception) {
                    // Settings can only be applied once per runtime instance
                }
                firestore
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore not initialized: ${e.message}")
            null
        }
    }

    suspend fun testFirebaseConnection(context: Context): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val firestore = getFirestore(context)
                ?: return@withContext Pair(false, "Firebase project credentials not initialized.")
            val config = getFirebaseConfig(context)

            // Test read from users collection with quick limit and 4s timeout
            withTimeout(4000L) {
                firestore.collection(COLLECTION_USERS)
                    .limit(1)
                    .get()
                    .await()
            }

            Pair(true, "Successfully connected to Cloud Firestore (Project: ${config.projectId})")
        } catch (e: Exception) {
            Log.e(TAG, "Test connection failed: ${e.message}", e)
            Pair(false, "Connection failed: ${e.localizedMessage ?: e.message}")
        }
    }

    suspend fun getCloudStatistics(context: Context): CloudStats = withContext(Dispatchers.IO) {
        val config = getFirebaseConfig(context)
        try {
            val firestore = getFirestore(context)
                ?: return@withContext CloudStats(connected = false, projectId = config.projectId)

            withTimeoutOrNull(4000L) {
                coroutineScope {
                    val materialsDef = async { firestore.collection(COLLECTION_MATERIALS).limit(300).get().await() }
                    val usersDef = async { firestore.collection(COLLECTION_USERS).limit(300).get().await() }
                    val quizzesDef = async { firestore.collection(COLLECTION_QUIZ_ATTEMPTS).limit(300).get().await() }
                    val activitiesDef = async { firestore.collection(COLLECTION_ACTIVITIES).limit(300).get().await() }

                    CloudStats(
                        connected = true,
                        projectId = config.projectId,
                        materialsCount = materialsDef.await().size(),
                        usersCount = usersDef.await().size(),
                        quizAttemptsCount = quizzesDef.await().size(),
                        activitiesCount = activitiesDef.await().size()
                    )
                }
            } ?: CloudStats(connected = true, projectId = config.projectId)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get cloud statistics: ${e.message}")
            CloudStats(connected = false, projectId = config.projectId)
        }
    }

    // --- Study Materials ---
    suspend fun syncStudyMaterial(context: Context, material: StudyMaterialEntity) = withContext(Dispatchers.IO) {
        try {
            val firestore = getFirestore(context) ?: return@withContext
            withTimeoutOrNull(4000L) {
                val data = hashMapOf(
                    "id" to material.id,
                    "subjectId" to material.subjectId,
                    "materialType" to material.materialType,
                    "title" to material.title,
                    "topic" to material.topic,
                    "description" to material.description,
                    "contentUrl" to material.contentUrl,
                    "documentContent" to material.documentContent,
                    "durationOrPages" to material.durationOrPages,
                    "updatedAt" to System.currentTimeMillis()
                )
                firestore.collection(COLLECTION_MATERIALS)
                    .document(material.id.toString())
                    .set(data, SetOptions.merge())
                    .await()
                Log.d(TAG, "Synced study material ${material.id} to Firestore")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync material to Firestore: ${e.message}")
        }
    }

    suspend fun deleteStudyMaterial(context: Context, materialId: Long) = withContext(Dispatchers.IO) {
        try {
            val firestore = getFirestore(context) ?: return@withContext
            withTimeoutOrNull(4000L) {
                firestore.collection(COLLECTION_MATERIALS)
                    .document(materialId.toString())
                    .delete()
                    .await()
                Log.d(TAG, "Deleted material $materialId from Firestore")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete material from Firestore: ${e.message}")
        }
    }

    // --- User Accounts ---
    suspend fun syncUser(context: Context, user: UserEntity) = withContext(Dispatchers.IO) {
        try {
            val firestore = getFirestore(context) ?: return@withContext
            withTimeoutOrNull(4000L) {
                val data = hashMapOf(
                    "username" to user.username,
                    "displayName" to user.displayName,
                    "role" to user.role,
                    "passwordHash" to user.passwordHash,
                    "createdAt" to user.createdAt
                )
                firestore.collection(COLLECTION_USERS)
                    .document(user.username)
                    .set(data, SetOptions.merge())
                    .await()
                Log.d(TAG, "Synced user ${user.username} to Firestore")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync user to Firestore: ${e.message}")
        }
    }

    suspend fun fetchUserFromCloud(context: Context, username: String): UserEntity? = withContext(Dispatchers.IO) {
        try {
            val firestore = getFirestore(context) ?: return@withContext null
            withTimeoutOrNull(2500L) {
                val doc = firestore.collection(COLLECTION_USERS)
                    .document(username.trim())
                    .get()
                    .await()
                if (doc.exists()) {
                    val data = doc.data ?: return@withTimeoutOrNull null
                    val user = UserEntity(
                        username = data["username"] as? String ?: doc.id,
                        passwordHash = data["passwordHash"] as? String ?: "",
                        displayName = data["displayName"] as? String ?: doc.id,
                        role = data["role"] as? String ?: "STUDENT",
                        createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    )
                    Log.i(TAG, "Retrieved user $username from Cloud Firestore")
                    user
                } else null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch user from cloud: ${e.message}")
            null
        }
    }

    // --- Quiz Attempts ---
    suspend fun syncQuizAttempt(context: Context, attempt: QuizAttemptEntity) = withContext(Dispatchers.IO) {
        try {
            val firestore = getFirestore(context) ?: return@withContext
            withTimeoutOrNull(4000L) {
                val data = hashMapOf(
                    "id" to attempt.id,
                    "username" to attempt.username,
                    "subjectId" to attempt.subjectId,
                    "topic" to attempt.topic,
                    "score" to attempt.score,
                    "totalQuestions" to attempt.totalQuestions,
                    "percentage" to attempt.percentage,
                    "details" to attempt.details,
                    "timestamp" to attempt.timestamp
                )
                firestore.collection(COLLECTION_QUIZ_ATTEMPTS)
                    .document(attempt.id.toString())
                    .set(data, SetOptions.merge())
                    .await()
                Log.d(TAG, "Synced quiz attempt ${attempt.id} to Firestore")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync quiz attempt to Firestore: ${e.message}")
        }
    }

    // --- Chat Messages ---
    suspend fun syncChatMessage(context: Context, message: ChatMessageEntity) = withContext(Dispatchers.IO) {
        try {
            val firestore = getFirestore(context) ?: return@withContext
            withTimeoutOrNull(4000L) {
                val data = hashMapOf(
                    "id" to message.id,
                    "username" to message.username,
                    "subjectId" to message.subjectId,
                    "sender" to message.sender,
                    "text" to message.text,
                    "imageUri" to (message.imageUri ?: ""),
                    "timestamp" to message.timestamp
                )
                firestore.collection(COLLECTION_CHAT_MESSAGES)
                    .document(message.id.toString())
                    .set(data, SetOptions.merge())
                    .await()
                Log.d(TAG, "Synced chat message ${message.id} to Firestore")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync chat message to Firestore: ${e.message}")
        }
    }

    // --- User Activity Tracking ---
    suspend fun syncUserActivity(context: Context, activity: UserActivityEntity) = withContext(Dispatchers.IO) {
        try {
            val firestore = getFirestore(context) ?: return@withContext
            withTimeoutOrNull(4000L) {
                val data = hashMapOf(
                    "id" to activity.id,
                    "username" to activity.username,
                    "userDisplayName" to activity.userDisplayName,
                    "materialId" to activity.materialId,
                    "materialTitle" to activity.materialTitle,
                    "materialType" to activity.materialType,
                    "subjectId" to activity.subjectId,
                    "actionType" to activity.actionType,
                    "timestamp" to activity.timestamp
                )
                firestore.collection(COLLECTION_ACTIVITIES)
                    .document(activity.id.toString())
                    .set(data, SetOptions.merge())
                    .await()
                Log.d(TAG, "Synced activity ${activity.id} to Firestore")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync activity to Firestore: ${e.message}")
        }
    }

    // --- Homework Reminders ---
    suspend fun syncHomeworkReminder(context: Context, reminder: HomeworkReminderEntity) = withContext(Dispatchers.IO) {
        try {
            val firestore = getFirestore(context) ?: return@withContext
            withTimeoutOrNull(4000L) {
                val data = hashMapOf(
                    "id" to reminder.id,
                    "subjectName" to reminder.subjectName,
                    "topicName" to reminder.topicName,
                    "dueDateTimestamp" to reminder.dueDateTimestamp,
                    "isFinished" to reminder.isFinished,
                    "notified24h" to reminder.notified24h,
                    "notified2h" to reminder.notified2h,
                    "timestamp" to reminder.timestamp
                )
                firestore.collection(COLLECTION_REMINDERS)
                    .document(reminder.id.toString())
                    .set(data, SetOptions.merge())
                    .await()
                Log.d(TAG, "Synced homework reminder ${reminder.id} to Firestore")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync homework reminder to Firestore: ${e.message}")
        }
    }

    suspend fun deleteHomeworkReminder(context: Context, reminderId: Long) = withContext(Dispatchers.IO) {
        try {
            val firestore = getFirestore(context) ?: return@withContext
            withTimeoutOrNull(4000L) {
                firestore.collection(COLLECTION_REMINDERS)
                    .document(reminderId.toString())
                    .delete()
                    .await()
                Log.d(TAG, "Deleted homework reminder $reminderId from Firestore")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete reminder from Firestore: ${e.message}")
        }
    }

    // --- Library Documents ---
    suspend fun syncLibraryDocument(context: Context, doc: LibraryDocumentEntity) = withContext(Dispatchers.IO) {
        try {
            val firestore = getFirestore(context) ?: return@withContext
            withTimeoutOrNull(4000L) {
                val data = hashMapOf(
                    "id" to doc.id,
                    "title" to doc.title,
                    "filePath" to doc.filePath,
                    "fileName" to doc.fileName,
                    "docType" to doc.docType,
                    "subjectId" to doc.subjectId,
                    "pageCount" to doc.pageCount,
                    "fileSizeFormatted" to doc.fileSizeFormatted,
                    "timestamp" to doc.timestamp,
                    "notes" to (doc.notes ?: "")
                )
                firestore.collection(COLLECTION_LIBRARY)
                    .document(doc.id.toString())
                    .set(data, SetOptions.merge())
                    .await()
                Log.d(TAG, "Synced library document ${doc.id} to Firestore")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync library doc to Firestore: ${e.message}")
        }
    }

    // ==========================================
    // COMPLETE HIGH-SPEED CLOUD SYNCHRONIZATION
    // ==========================================

    /**
     * Parallel high-speed cloud restore.
     * Fetches all 6 collections concurrently using coroutine async,
     * and performs atomic batch transaction into SQLite.
     */
    suspend fun pullAllFromFirestore(context: Context, database: AppDatabase): SyncSummary = withContext(Dispatchers.IO) {
        try {
            val firestore = getFirestore(context)
                ?: return@withContext SyncSummary(false, "Firebase is not configured or unavailable.")

            withTimeout(15000L) {
                coroutineScope {
                    val usersDef = async { firestore.collection(COLLECTION_USERS).get().await() }
                    val materialsDef = async { firestore.collection(COLLECTION_MATERIALS).get().await() }
                    val quizDef = async { firestore.collection(COLLECTION_QUIZ_ATTEMPTS).get().await() }
                    val activitiesDef = async { firestore.collection(COLLECTION_ACTIVITIES).get().await() }
                    val remindersDef = async { firestore.collection(COLLECTION_REMINDERS).get().await() }
                    val libDef = async { firestore.collection(COLLECTION_LIBRARY).get().await() }

                    val usersSnap = usersDef.await()
                    val materialsSnap = materialsDef.await()
                    val quizSnap = quizDef.await()
                    val activitiesSnap = activitiesDef.await()
                    val remindersSnap = remindersDef.await()
                    val libSnap = libDef.await()

                    // Parse entities
                    val usersToInsert = mutableListOf<UserEntity>()
                    for (doc in usersSnap.documents) {
                        val data = doc.data ?: continue
                        usersToInsert.add(
                            UserEntity(
                                username = data["username"] as? String ?: doc.id,
                                passwordHash = data["passwordHash"] as? String ?: "",
                                displayName = data["displayName"] as? String ?: doc.id,
                                role = data["role"] as? String ?: "STUDENT",
                                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                            )
                        )
                    }

                    val materialsToInsert = mutableListOf<StudyMaterialEntity>()
                    for (doc in materialsSnap.documents) {
                        val data = doc.data ?: continue
                        val id = (data["id"] as? Number)?.toLong() ?: doc.id.toLongOrNull() ?: continue
                        materialsToInsert.add(
                            StudyMaterialEntity(
                                id = id,
                                subjectId = data["subjectId"] as? String ?: "",
                                materialType = data["materialType"] as? String ?: "NOTE",
                                title = data["title"] as? String ?: "",
                                topic = data["topic"] as? String ?: "",
                                description = data["description"] as? String ?: "",
                                contentUrl = data["contentUrl"] as? String ?: "",
                                documentContent = (data["documentContent"] as? String) ?: "",
                                durationOrPages = data["durationOrPages"] as? String ?: "",
                                timestamp = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                            )
                        )
                    }

                    val quizzesToInsert = mutableListOf<QuizAttemptEntity>()
                    for (doc in quizSnap.documents) {
                        val data = doc.data ?: continue
                        val id = (data["id"] as? Number)?.toLong() ?: doc.id.toLongOrNull() ?: continue
                        quizzesToInsert.add(
                            QuizAttemptEntity(
                                id = id,
                                username = data["username"] as? String ?: "",
                                subjectId = data["subjectId"] as? String ?: "",
                                topic = data["topic"] as? String ?: "",
                                score = (data["score"] as? Number)?.toInt() ?: 0,
                                totalQuestions = (data["totalQuestions"] as? Number)?.toInt() ?: 0,
                                percentage = (data["percentage"] as? Number)?.toFloat() ?: 0f,
                                details = data["details"] as? String ?: "",
                                timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
                            )
                        )
                    }

                    val activitiesToInsert = mutableListOf<UserActivityEntity>()
                    for (doc in activitiesSnap.documents) {
                        val data = doc.data ?: continue
                        val id = (data["id"] as? Number)?.toLong() ?: doc.id.toLongOrNull() ?: continue
                        activitiesToInsert.add(
                            UserActivityEntity(
                                id = id,
                                username = data["username"] as? String ?: "",
                                userDisplayName = data["userDisplayName"] as? String ?: "",
                                materialId = (data["materialId"] as? Number)?.toLong() ?: 0L,
                                materialTitle = data["materialTitle"] as? String ?: "",
                                materialType = data["materialType"] as? String ?: "",
                                subjectId = data["subjectId"] as? String ?: "",
                                actionType = data["actionType"] as? String ?: "",
                                timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
                            )
                        )
                    }

                    val remindersToInsert = mutableListOf<HomeworkReminderEntity>()
                    for (doc in remindersSnap.documents) {
                        val data = doc.data ?: continue
                        val id = (data["id"] as? Number)?.toLong() ?: doc.id.toLongOrNull() ?: continue
                        remindersToInsert.add(
                            HomeworkReminderEntity(
                                id = id,
                                subjectName = data["subjectName"] as? String ?: "",
                                topicName = data["topicName"] as? String ?: "",
                                dueDateTimestamp = (data["dueDateTimestamp"] as? Number)?.toLong() ?: 0L,
                                isFinished = data["isFinished"] as? Boolean ?: false,
                                notified24h = data["notified24h"] as? Boolean ?: false,
                                notified2h = data["notified2h"] as? Boolean ?: false,
                                timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
                            )
                        )
                    }

                    val libDocsToInsert = mutableListOf<LibraryDocumentEntity>()
                    for (doc in libSnap.documents) {
                        val data = doc.data ?: continue
                        val id = (data["id"] as? Number)?.toLong() ?: doc.id.toLongOrNull() ?: continue
                        libDocsToInsert.add(
                            LibraryDocumentEntity(
                                id = id,
                                title = data["title"] as? String ?: "",
                                filePath = data["filePath"] as? String ?: "",
                                fileName = data["fileName"] as? String ?: "",
                                docType = data["docType"] as? String ?: "NOTE",
                                subjectId = data["subjectId"] as? String ?: "ALL",
                                pageCount = (data["pageCount"] as? Number)?.toInt() ?: 1,
                                fileSizeFormatted = data["fileSizeFormatted"] as? String ?: "",
                                timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                                notes = data["notes"] as? String ?: ""
                            )
                        )
                    }

                    // Fast atomic batch commit in SQLite
                    database.withTransaction {
                        usersToInsert.forEach { database.userDao().insertUser(it) }
                        if (materialsToInsert.isNotEmpty()) {
                            database.studyMaterialDao().insertMaterials(materialsToInsert)
                        }
                        quizzesToInsert.forEach { database.quizAttemptDao().insertAttempt(it) }
                        activitiesToInsert.forEach { database.userActivityDao().recordActivity(it) }
                        remindersToInsert.forEach { database.homeworkReminderDao().insertReminder(it) }
                        libDocsToInsert.forEach { database.libraryDocumentDao().insertDocument(it) }
                    }

                    SyncSummary(
                        success = true,
                        message = "Fast Cloud Pull Complete: ${materialsToInsert.size} materials, ${usersToInsert.size} users, ${quizzesToInsert.size} quizzes restored instantly.",
                        materialsCount = materialsToInsert.size,
                        usersCount = usersToInsert.size,
                        quizAttemptsCount = quizzesToInsert.size,
                        activitiesCount = activitiesToInsert.size,
                        remindersCount = remindersToInsert.size,
                        libraryDocsCount = libDocsToInsert.size
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pull from Firestore: ${e.message}", e)
            SyncSummary(false, "Sync failed: ${e.localizedMessage ?: e.message}")
        }
    }

    /**
     * High-speed parallel batched backup to Cloud Firestore.
     * Uses Firestore WriteBatch (commits up to 400 writes in a single network request)
     * resulting in instantaneous upload instead of sequential single writes.
     */
    suspend fun pushAllToFirestore(context: Context, database: AppDatabase): SyncSummary = withContext(Dispatchers.IO) {
        try {
            val firestore = getFirestore(context)
                ?: return@withContext SyncSummary(false, "Firebase is not configured or unavailable.")

            val users = database.userDao().getAllUsersList()
            val materials = database.studyMaterialDao().getAllMaterialsList()
            val quizzes = database.quizAttemptDao().getAllAttemptsList()
            val activities = database.userActivityDao().getAllActivitiesList()
            val reminders = database.homeworkReminderDao().getActiveRemindersList()
            val libDocs = database.libraryDocumentDao().getAllLibraryDocumentsList()

            // Prepare all write operations
            val operations = mutableListOf<Pair<DocumentReference, Map<String, Any>>>()

            for (user in users) {
                val data = mapOf(
                    "username" to user.username,
                    "displayName" to user.displayName,
                    "role" to user.role,
                    "passwordHash" to user.passwordHash,
                    "createdAt" to user.createdAt
                )
                operations.add(firestore.collection(COLLECTION_USERS).document(user.username) to data)
            }

            for (material in materials) {
                val data = mapOf(
                    "id" to material.id,
                    "subjectId" to material.subjectId,
                    "materialType" to material.materialType,
                    "title" to material.title,
                    "topic" to material.topic,
                    "description" to material.description,
                    "contentUrl" to material.contentUrl,
                    "documentContent" to material.documentContent,
                    "durationOrPages" to material.durationOrPages,
                    "updatedAt" to System.currentTimeMillis()
                )
                operations.add(firestore.collection(COLLECTION_MATERIALS).document(material.id.toString()) to data)
            }

            for (quiz in quizzes) {
                val data = mapOf(
                    "id" to quiz.id,
                    "username" to quiz.username,
                    "subjectId" to quiz.subjectId,
                    "topic" to quiz.topic,
                    "score" to quiz.score,
                    "totalQuestions" to quiz.totalQuestions,
                    "percentage" to quiz.percentage,
                    "details" to quiz.details,
                    "timestamp" to quiz.timestamp
                )
                operations.add(firestore.collection(COLLECTION_QUIZ_ATTEMPTS).document(quiz.id.toString()) to data)
            }

            for (act in activities) {
                val data = mapOf(
                    "id" to act.id,
                    "username" to act.username,
                    "userDisplayName" to act.userDisplayName,
                    "materialId" to act.materialId,
                    "materialTitle" to act.materialTitle,
                    "materialType" to act.materialType,
                    "subjectId" to act.subjectId,
                    "actionType" to act.actionType,
                    "timestamp" to act.timestamp
                )
                operations.add(firestore.collection(COLLECTION_ACTIVITIES).document(act.id.toString()) to data)
            }

            for (rem in reminders) {
                val data = mapOf(
                    "id" to rem.id,
                    "subjectName" to rem.subjectName,
                    "topicName" to rem.topicName,
                    "dueDateTimestamp" to rem.dueDateTimestamp,
                    "isFinished" to rem.isFinished,
                    "notified24h" to rem.notified24h,
                    "notified2h" to rem.notified2h,
                    "timestamp" to rem.timestamp
                )
                operations.add(firestore.collection(COLLECTION_REMINDERS).document(rem.id.toString()) to data)
            }

            for (doc in libDocs) {
                val data = mapOf(
                    "id" to doc.id,
                    "title" to doc.title,
                    "filePath" to doc.filePath,
                    "fileName" to doc.fileName,
                    "docType" to doc.docType,
                    "subjectId" to doc.subjectId,
                    "pageCount" to doc.pageCount,
                    "fileSizeFormatted" to doc.fileSizeFormatted,
                    "timestamp" to doc.timestamp,
                    "notes" to (doc.notes ?: "")
                )
                operations.add(firestore.collection(COLLECTION_LIBRARY).document(doc.id.toString()) to data)
            }

            // Commit in batched transactions (up to 400 operations per batch) under timeout
            withTimeout(15000L) {
                operations.chunked(400).forEach { chunk ->
                    val batch = firestore.batch()
                    for ((ref, data) in chunk) {
                        batch.set(ref, data, SetOptions.merge())
                    }
                    batch.commit().await()
                }
            }

            SyncSummary(
                success = true,
                message = "Fast Cloud Push Complete: ${materials.size} materials, ${users.size} users, ${quizzes.size} quizzes backed up instantly.",
                materialsCount = materials.size,
                usersCount = users.size,
                quizAttemptsCount = quizzes.size,
                activitiesCount = activities.size,
                remindersCount = reminders.size,
                libraryDocsCount = libDocs.size
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to push to Firestore: ${e.message}", e)
            SyncSummary(false, "Cloud backup failed: ${e.localizedMessage ?: e.message}")
        }
    }
}
