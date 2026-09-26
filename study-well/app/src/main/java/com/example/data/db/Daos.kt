package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChatMessageEntity
import com.example.data.model.HomeworkReminderEntity
import com.example.data.model.LibraryDocumentEntity
import com.example.data.model.QuizAttemptEntity
import com.example.data.model.StudyMaterialEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.UserActivityEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY createdAt ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects ORDER BY createdAt ASC")
    suspend fun getAllSubjectsList(): List<SubjectEntity>

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    suspend fun getSubjectById(id: String): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubjectById(id: String)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    suspend fun getAllUsersList(): List<UserEntity>
}

@Dao
interface StudyMaterialDao {
    @Query("SELECT * FROM study_materials WHERE subjectId = :subjectId ORDER BY timestamp DESC")
    fun getMaterialsBySubject(subjectId: String): Flow<List<StudyMaterialEntity>>

    @Query("SELECT * FROM study_materials WHERE subjectId = :subjectId AND materialType = :type ORDER BY timestamp DESC")
    fun getMaterialsBySubjectAndType(subjectId: String, type: String): Flow<List<StudyMaterialEntity>>

    @Query("SELECT * FROM study_materials WHERE id = :id LIMIT 1")
    suspend fun getMaterialById(id: Long): StudyMaterialEntity?

    @Query("SELECT * FROM study_materials ORDER BY timestamp DESC")
    fun getAllMaterials(): Flow<List<StudyMaterialEntity>>

    @Query("SELECT * FROM study_materials ORDER BY timestamp DESC")
    suspend fun getAllMaterialsList(): List<StudyMaterialEntity>

    @Query("SELECT COUNT(*) FROM study_materials WHERE subjectId = :subjectId")
    suspend fun countMaterialsForSubject(subjectId: String): Int

    @Query("SELECT COUNT(*) FROM study_materials WHERE materialType = :materialType")
    suspend fun countMaterialsByType(materialType: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterials(materials: List<StudyMaterialEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: StudyMaterialEntity): Long

    @Update
    suspend fun updateMaterial(material: StudyMaterialEntity)

    @Delete
    suspend fun deleteMaterial(material: StudyMaterialEntity)

    @Query("DELETE FROM study_materials WHERE id = :id")
    suspend fun deleteMaterialById(id: Long)

    @Query("DELETE FROM study_materials WHERE subjectId = :subjectId")
    suspend fun deleteMaterialsBySubject(subjectId: String)

    @Query("DELETE FROM study_materials WHERE materialType = :materialType")
    suspend fun deleteMaterialsByType(materialType: String)

    @Query("DELETE FROM study_materials")
    suspend fun deleteAllMaterials()
}

@Dao
interface QuizAttemptDao {
    @Query("SELECT * FROM quiz_attempts WHERE username = :username ORDER BY timestamp DESC")
    fun getAttemptsForUser(username: String): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE username = :username AND subjectId = :subjectId ORDER BY timestamp DESC")
    fun getAttemptsForUserAndSubject(username: String, subjectId: String): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts ORDER BY timestamp DESC")
    fun getAllAttempts(): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts ORDER BY timestamp DESC")
    suspend fun getAllAttemptsList(): List<QuizAttemptEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: QuizAttemptEntity): Long

    @Query("SELECT AVG(percentage) FROM quiz_attempts WHERE username = :username")
    suspend fun getAverageScore(username: String): Float?

    @Query("SELECT COUNT(*) FROM quiz_attempts WHERE username = :username")
    suspend fun getTotalQuizzesTaken(username: String): Int
}

@Dao
interface UserActivityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordActivity(activity: UserActivityEntity): Long

    @Query("SELECT * FROM user_activity ORDER BY timestamp DESC")
    fun getAllActivities(): Flow<List<UserActivityEntity>>

    @Query("SELECT * FROM user_activity ORDER BY timestamp DESC")
    suspend fun getAllActivitiesList(): List<UserActivityEntity>

    @Query("SELECT * FROM user_activity WHERE username = :username ORDER BY timestamp DESC")
    fun getActivitiesForUser(username: String): Flow<List<UserActivityEntity>>

    @Query("SELECT COUNT(DISTINCT materialId) FROM user_activity WHERE username = :username")
    suspend fun getCountViewedMaterials(username: String): Int

    @Query("DELETE FROM user_activity")
    suspend fun clearAllActivity()
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE username = :username AND subjectId = :subjectId ORDER BY timestamp ASC")
    fun getChatHistoryForSubject(username: String, subjectId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE username = :username ORDER BY timestamp ASC")
    fun getAllChatHistory(username: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    suspend fun getAllMessagesList(): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages WHERE username = :username AND subjectId = :subjectId")
    suspend fun clearChatForSubject(username: String, subjectId: String)

    @Query("DELETE FROM chat_messages WHERE username = :username")
    suspend fun clearAllChatsForUser(username: String)
}

@Dao
interface LibraryDocumentDao {
    @Query("SELECT * FROM student_library_documents ORDER BY timestamp DESC")
    fun getAllLibraryDocuments(): Flow<List<LibraryDocumentEntity>>

    @Query("SELECT * FROM student_library_documents ORDER BY timestamp DESC")
    suspend fun getAllLibraryDocumentsList(): List<LibraryDocumentEntity>

    @Query("SELECT * FROM student_library_documents WHERE docType = :docType ORDER BY timestamp DESC")
    fun getDocumentsByType(docType: String): Flow<List<LibraryDocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: LibraryDocumentEntity): Long

    @Delete
    suspend fun deleteDocument(doc: LibraryDocumentEntity)

    @Query("DELETE FROM student_library_documents WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM student_library_documents WHERE filePath = :path")
    suspend fun deleteByPath(path: String)
}

@Dao
interface HomeworkReminderDao {
    @Query("SELECT * FROM homework_reminders ORDER BY dueDateTimestamp ASC")
    fun getAllReminders(): Flow<List<HomeworkReminderEntity>>

    @Query("SELECT * FROM homework_reminders WHERE isFinished = 0 ORDER BY dueDateTimestamp ASC")
    suspend fun getActiveRemindersList(): List<HomeworkReminderEntity>

    @Query("SELECT * FROM homework_reminders WHERE id = :id LIMIT 1")
    suspend fun getReminderById(id: Long): HomeworkReminderEntity?

    @Query("UPDATE homework_reminders SET notified24h = 1 WHERE id = :id")
    suspend fun markNotified24h(id: Long)

    @Query("UPDATE homework_reminders SET notified2h = 1 WHERE id = :id")
    suspend fun markNotified2h(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: HomeworkReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: HomeworkReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: HomeworkReminderEntity)
}

