package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ChatMessageEntity
import com.example.data.model.HomeworkReminderEntity
import com.example.data.model.LibraryDocumentEntity
import com.example.data.model.QuizAttemptEntity
import com.example.data.model.StudyMaterialEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.SubjectEnum
import com.example.data.model.UserActivityEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        SubjectEntity::class,
        StudyMaterialEntity::class,
        QuizAttemptEntity::class,
        UserActivityEntity::class,
        ChatMessageEntity::class,
        LibraryDocumentEntity::class,
        HomeworkReminderEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun subjectDao(): SubjectDao
    abstract fun studyMaterialDao(): StudyMaterialDao
    abstract fun quizAttemptDao(): QuizAttemptDao
    abstract fun userActivityDao(): UserActivityDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun libraryDocumentDao(): LibraryDocumentDao
    abstract fun homeworkReminderDao(): HomeworkReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val appContext = context.applicationContext
                val instance = Room.databaseBuilder(
                    appContext,
                    AppDatabase::class.java,
                    "study_well_database"
                )
                    .addCallback(DatabaseCallback(appContext))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val appContext: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            seedDatabase(database)
                        } catch (_: Exception) {}
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            ensureSubjectsSeeded(database)
                            ensureLibrarySeeded(database)
                        } catch (_: Exception) {}
                    }
                }
            }
        }

        suspend fun ensureSubjectsSeeded(database: AppDatabase) {
            val subjectDao = database.subjectDao()
            val defaultSubjects = SubjectEnum.values().map { it.toSubjectItem().toSubjectEntity() }
            subjectDao.insertSubjects(defaultSubjects)
        }

        suspend fun ensureRealPastPapersSeeded(database: AppDatabase) {
            // Re-seed past papers on startup to force immediate enforcement of the 2-year limitation
            com.example.data.repository.RealPastPaperRepository.seedRealPastPapersToDatabase(database)
        }

        suspend fun seedDatabase(database: AppDatabase) {
            val userDao = database.userDao()
            val subjectDao = database.subjectDao()

            // 1. Mandatory Default Admin Account
            userDao.insertUser(
                UserEntity(
                    username = "selimamr",
                    passwordHash = "sosololo9102011",
                    displayName = "Selim Amr (Admin)",
                    role = UserRole.ADMIN.name
                )
            )

            // 2. Default Student Account for demo & testing
            userDao.insertUser(
                UserEntity(
                    username = "student10",
                    passwordHash = "igcse2026",
                    displayName = "IGCSE Grade 10 Student",
                    role = UserRole.STUDENT.name
                )
            )

            // 3. Seed Default Subjects (Physics, English, Math, Biology, Chemistry, Arabic OL, ICT)
            val defaultSubjects = SubjectEnum.values().map { it.toSubjectItem().toSubjectEntity() }
            subjectDao.insertSubjects(defaultSubjects)

            // 4. Seed Essential Study Materials & Past Papers in Student Library
            val libraryDao = database.libraryDocumentDao()
            val sampleDocs = listOf(
                LibraryDocumentEntity(
                    title = "IGCSE Physics Paper 4 Extended (May/June 2024)",
                    filePath = "",
                    fileName = "Physics_0625_42_MayJune_2024.pdf",
                    docType = "EXAM_PDF",
                    subjectId = "Physics",
                    fileSizeFormatted = "1.8 MB",
                    timestamp = System.currentTimeMillis() - 86400000L * 2
                ),
                LibraryDocumentEntity(
                    title = "IGCSE Mathematics 0580 Core & Extended Revision Guide",
                    filePath = "",
                    fileName = "Math_0580_Revision_Guide.pdf",
                    docType = "DOWNLOADED_PDF",
                    subjectId = "Math",
                    fileSizeFormatted = "3.4 MB",
                    timestamp = System.currentTimeMillis() - 86400000L * 5
                ),
                LibraryDocumentEntity(
                    title = "Biology Photosynthesis & Respiration Summary Notes",
                    filePath = "",
                    fileName = "Biology_Photosynthesis_Notes_Annotated.pdf",
                    docType = "EDITED_PDF",
                    subjectId = "Biology",
                    fileSizeFormatted = "950 KB",
                    timestamp = System.currentTimeMillis() - 86400000L * 10
                ),
                LibraryDocumentEntity(
                    title = "IGCSE ICT Theory Masterclass Workbook",
                    filePath = "",
                    fileName = "ICT_0417_Theory_Masterclass.pdf",
                    docType = "DOWNLOADED_PDF",
                    subjectId = "ICT",
                    fileSizeFormatted = "2.1 MB",
                    timestamp = System.currentTimeMillis() - 86400000L * 15
                )
            )
            sampleDocs.forEach { libraryDao.insertDocument(it) }
        }

        suspend fun ensureLibrarySeeded(database: AppDatabase) {
            val libraryDao = database.libraryDocumentDao()
            val existing = libraryDao.getAllLibraryDocumentsList()
            if (existing.isEmpty()) {
                val sampleDocs = listOf(
                    LibraryDocumentEntity(
                        title = "IGCSE Physics Paper 4 Extended (May/June 2024)",
                        filePath = "",
                        fileName = "Physics_0625_42_MayJune_2024.pdf",
                        docType = "EXAM_PDF",
                        subjectId = "Physics",
                        fileSizeFormatted = "1.8 MB",
                        timestamp = System.currentTimeMillis() - 86400000L * 2
                    ),
                    LibraryDocumentEntity(
                        title = "IGCSE Mathematics 0580 Core & Extended Revision Guide",
                        filePath = "",
                        fileName = "Math_0580_Revision_Guide.pdf",
                        docType = "DOWNLOADED_PDF",
                        subjectId = "Math",
                        fileSizeFormatted = "3.4 MB",
                        timestamp = System.currentTimeMillis() - 86400000L * 5
                    ),
                    LibraryDocumentEntity(
                        title = "Biology Photosynthesis & Respiration Summary Notes",
                        filePath = "",
                        fileName = "Biology_Photosynthesis_Notes_Annotated.pdf",
                        docType = "EDITED_PDF",
                        subjectId = "Biology",
                        fileSizeFormatted = "950 KB",
                        timestamp = System.currentTimeMillis() - 86400000L * 10
                    ),
                    LibraryDocumentEntity(
                        title = "IGCSE ICT Theory Masterclass Workbook",
                        filePath = "",
                        fileName = "ICT_0417_Theory_Masterclass.pdf",
                        docType = "DOWNLOADED_PDF",
                        subjectId = "ICT",
                        fileSizeFormatted = "2.1 MB",
                        timestamp = System.currentTimeMillis() - 86400000L * 15
                    )
                )
                sampleDocs.forEach { libraryDao.insertDocument(it) }
            }
        }
    }
}
