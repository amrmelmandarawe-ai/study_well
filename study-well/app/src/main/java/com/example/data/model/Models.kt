package com.example.data.model

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ui.theme.ArabicRose
import com.example.ui.theme.BiologyGreen
import com.example.ui.theme.ChemistryCyan
import com.example.ui.theme.EnglishPurple
import com.example.ui.theme.IctEmerald
import com.example.ui.theme.MathAmber
import com.example.ui.theme.PhysicsBlue
import com.example.ui.theme.SubjectOrange
import com.example.ui.theme.SubjectPink
import com.example.ui.theme.SubjectTeal
import com.example.ui.theme.SubjectViolet
import java.util.UUID

enum class UserRole {
    STUDENT,
    ADMIN
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val username: String,
    val passwordHash: String,
    val displayName: String,
    val role: String, // "STUDENT" or "ADMIN"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val syllabusCode: String,
    val description: String,
    val defaultTopicsJson: String, // Comma or newline separated list of topics
    val colorHex: Long = 0xFF0284C7,
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toSubjectItem(): SubjectItem {
        val topics = defaultTopicsJson.split("\n", ",").map { it.trim() }.filter { it.isNotBlank() }
        return SubjectItem(
            id = id,
            title = title,
            syllabusCode = syllabusCode,
            description = description,
            defaultTopics = if (topics.isNotEmpty()) topics else listOf("General Topics", "Past Papers"),
            color = Color(colorHex),
            isCustom = isCustom
        )
    }
}

data class SubjectItem(
    val id: String,
    val title: String,
    val syllabusCode: String,
    val description: String,
    val defaultTopics: List<String>,
    val color: Color,
    val isCustom: Boolean = false
) {
    fun toSubjectEnum(): SubjectEnum {
        return SubjectEnum.fromId(id)
    }

    fun toSubjectEntity(): SubjectEntity {
        return SubjectEntity(
            id = id,
            title = title,
            syllabusCode = syllabusCode,
            description = description,
            defaultTopicsJson = defaultTopics.joinToString("\n"),
            colorHex = color.value.toLong(),
            isCustom = isCustom
        )
    }
}

enum class SubjectEnum(
    val id: String,
    val title: String,
    val syllabusCode: String,
    val description: String,
    val defaultTopics: List<String>,
    val color: Color
) {
    PHYSICS(
        id = "PHYSICS",
        title = "Physics",
        syllabusCode = "IGCSE 0625",
        description = "Mechanics, Thermal Physics, Waves, Electricity & Magnetism, Nuclear & Space Physics",
        defaultTopics = listOf("Motion, Forces & Energy", "Thermal Physics", "Waves & Light", "Electricity & Magnetism", "Nuclear Physics", "Space Physics"),
        color = PhysicsBlue
    ),
    ENGLISH(
        id = "ENGLISH",
        title = "English",
        syllabusCode = "IGCSE 0500",
        description = "Reading Comprehension, Directed Writing, Summary, Composition & Literary Analysis",
        defaultTopics = listOf("Reading & Comprehension", "Summary & Synthesis", "Descriptive & Narrative Writing", "Directed Writing & Argument", "Vocabulary & Connotations"),
        color = EnglishPurple
    ),
    MATH(
        id = "MATH",
        title = "Math",
        syllabusCode = "IGCSE 0580",
        description = "Number Theory, Algebra & Graphs, Geometry, Trigonometry, Vectors & Statistics",
        defaultTopics = listOf("Numbers & Percentages", "Algebra & Quadratics", "Coordinate Geometry & Trigonometry", "Probability & Statistics", "Vectors & Transformations"),
        color = MathAmber
    ),
    BIOLOGY(
        id = "BIOLOGY",
        title = "Biology",
        syllabusCode = "IGCSE 0610",
        description = "Classification of Organisms, Cells & Enzymes, Plant Nutrition, Human Physiology, Genetics & Ecology",
        defaultTopics = listOf("Classification of Living Organisms", "Cell Structure & Enzymes", "Plant Nutrition & Transport", "Human Transport, Gas Exchange & Respiration", "Reproduction & Inheritance", "Ecology & Human Impact"),
        color = BiologyGreen
    ),
    CHEMISTRY(
        id = "CHEMISTRY",
        title = "Chemistry",
        syllabusCode = "IGCSE 0620",
        description = "Particulate Nature of Matter, Atomic Structure, Stoichiometry, Chemical Energetics, Acids & Bases, Organic Chemistry",
        defaultTopics = listOf("States of Matter & Experimental Techniques", "Atoms, Elements & Compounds", "Stoichiometry & Mole Calculations", "Chemical Energetics & Reaction Rates", "Acids, Bases & Salts", "Organic Chemistry & Polymers"),
        color = ChemistryCyan
    ),
    ARABIC_OL(
        id = "ARABIC_OL",
        title = "Arabic OL",
        syllabusCode = "IGCSE 0508 / 3180",
        description = "Reading & Comprehension (الفهم والاستيعاب), Grammar & Syntax (النحو والصرف), Directed Writing (التعبير والانشاء), Literary Rhetoric (البلاغة)",
        defaultTopics = listOf("القراءة والفهم والاستيعاب (Comprehension)", "النحو والصرف وقواعد اللغة (Grammar & Syntax)", "التعبير الكتابي والانشاء (Directed Composition)", "البلاغة والتذوق الأدبي (Rhetoric & Literary Devices)", "امتحانات وتدريبات السنوات السابقة (Past Papers & Mocks)"),
        color = ArabicRose
    ),
    ICT(
        id = "ICT",
        title = "ICT",
        syllabusCode = "IGCSE 0417",
        description = "Hardware & Software, Network Communication, Database Systems, Web Design & Logic Gates",
        defaultTopics = listOf("Types & Components of Computer Systems", "Input & Output Devices", "Networks & Internet Security", "Databases & Web Authoring", "Logic Gates & Automation"),
        color = IctEmerald
    );

    fun toSubjectItem(): SubjectItem {
        return SubjectItem(
            id = id,
            title = title,
            syllabusCode = syllabusCode,
            description = description,
            defaultTopics = defaultTopics,
            color = color,
            isCustom = false
        )
    }

    companion object {
        fun fromId(id: String, customSubjects: List<SubjectItem> = emptyList()): SubjectEnum {
            return values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: PHYSICS
        }

        fun toSubjectItemFromId(id: String, customSubjects: List<SubjectItem> = emptyList()): SubjectItem {
            val custom = customSubjects.firstOrNull { it.id.equals(id, ignoreCase = true) }
            if (custom != null) return custom
            val enumMatch = values().firstOrNull { it.id.equals(id, ignoreCase = true) }
            return enumMatch?.toSubjectItem() ?: SubjectItem(
                id = id,
                title = id.replace("_", " ").capitalizeWords(),
                syllabusCode = "General",
                description = "Curriculum studies for $id",
                defaultTopics = listOf("Topic 1", "Topic 2", "Past Papers"),
                color = PhysicsBlue,
                isCustom = true
            )
        }
    }
}

private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { word ->
    word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
}

enum class ExamBoard(
    val id: String,
    val displayName: String,
    val shortName: String,
    val subtitle: String,
    val description: String,
    val logoEmoji: String,
    val badgeColorHex: Long,
    val standardPapers: List<String>
) {
    CAMBRIDGE(
        id = "CAMBRIDGE",
        displayName = "International (CAIE / CIE)",
        shortName = "International",
        subtitle = "International Assessment & Curriculum",
        description = "IGCSE & O-Level (0580, 0625, 0620, 0610, 0500, 0417, 0508)",
        logoEmoji = "🏛️",
        badgeColorHex = 0xFF0284C7,
        standardPapers = listOf(
            "Paper 1 (Core MCQ)",
            "Paper 2 (Extended MCQ / Theory)",
            "Paper 4 (Extended Theory / Structured)",
            "Paper 6 (Alternative to Practical)"
        )
    ),
    EDEXCEL(
        id = "EDEXCEL",
        displayName = "Pearson Edexcel",
        shortName = "Edexcel",
        subtitle = "Pearson Edexcel International GCSE (9-1)",
        description = "IGCSE (9-1) & O-Level (4MA1, 4PH1, 4CH1, 4BI1, 4EA1, 4IT1, 4AA1)",
        logoEmoji = "⚡",
        badgeColorHex = 0xFFD97706,
        standardPapers = listOf(
            "Paper 1 (Foundation 1F)",
            "Paper 1H (Higher Tier 1H)",
            "Paper 2 (Core 2F)",
            "Paper 2H (Higher Extended 2H)"
        )
    ),
    OXFORD_AQA(
        id = "OXFORD_AQA",
        displayName = "Oxford AQA",
        shortName = "Oxford AQA",
        subtitle = "Oxford International AQA Examinations",
        description = "International GCSE (9260, 9203, 9202, 9201, 9280, 9210)",
        logoEmoji = "🎓",
        badgeColorHex = 0xFF7C3AED,
        standardPapers = listOf(
            "Paper 1 (Core Tier)",
            "Paper 2 (Extended Tier)",
            "Paper 3 (Experimental & Practical Skills)"
        )
    );

    companion object {
        fun fromId(id: String): ExamBoard {
            return values().firstOrNull { 
                it.id.equals(id, ignoreCase = true) || 
                it.shortName.equals(id, ignoreCase = true) ||
                it.displayName.contains(id, ignoreCase = true)
            } ?: CAMBRIDGE
        }
    }
}

enum class MaterialType(val displayName: String) {
    VIDEO("Video"),
    BOOK("Book"),
    NOTE("Notes"),
    SHEET("Practice Sheet"),
    EXAM("Exam / Past Paper")
}

@Entity(tableName = "study_materials")
data class StudyMaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: String, // PHYSICS, ENGLISH, MATH, ICT, BIOLOGY, CHEMISTRY, ARABIC_OL, etc.
    val materialType: String, // VIDEO, BOOK, NOTE, SHEET, EXAM
    val title: String,
    val topic: String,
    val description: String,
    val contentUrl: String, // Video stream URL or file reference
    val documentContent: String, // Full text / rich markdown content for books, notes, sheets, exams
    val durationOrPages: String, // e.g. "15 mins" or "34 pages"
    val uploadedBy: String = "selimamr",
    val timestamp: Long = System.currentTimeMillis(),
    val isFeatured: Boolean = false,
    val isDownloaded: Boolean = false,
    val localFilePath: String? = null
)

data class QuizQuestion(
    val id: String,
    val subjectId: String,
    val topic: String,
    val questionText: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String,
    val marks: Int = 1,
    val igcseTip: String = "",
    val examBoard: String = "CAMBRIDGE", // "CAMBRIDGE", "EDEXCEL", "OXFORD_AQA"
    val pastYearSession: String = "", // e.g. "May/June 2023 - Paper 22"
    val paperCode: String = "", // e.g. "0625/22" or "4MA1/1H" or "9203/2"
    val syllabusTier: String = "Extended",
    val pastPaperCitation: String = ""
)

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val subjectId: String,
    val topic: String,
    val score: Int,
    val totalQuestions: Int,
    val percentage: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val details: String = ""
)

@Entity(tableName = "user_activity")
data class UserActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val userDisplayName: String = "",
    val materialId: Long,
    val materialTitle: String,
    val materialType: String, // VIDEO, BOOK, NOTE
    val subjectId: String,
    val actionType: String, // "VIEWED_VIDEO", "READ_DOCUMENT", "COMPLETED_QUIZ"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val subjectId: String,
    val sender: String, // "user" or "gemini"
    val text: String,
    val imageUri: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "user" or "gemini"
    val text: String,
    val imageUri: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
)

data class SharedIncomingFile(
    val id: String = UUID.randomUUID().toString(),
    val uriString: String,
    val localFilePath: String?,
    val fileName: String,
    val mimeType: String,
    val sizeFormatted: String,
    val isPdf: Boolean,
    val isImage: Boolean,
    val isVideo: Boolean,
    val isText: Boolean,
    val textContent: String = "",
    val pageCount: Int = 0
)

data class SharedIncomingBatch(
    val id: String = UUID.randomUUID().toString(),
    val items: List<SharedIncomingFile>,
    val sharedText: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class AdminSelectedFile(
    val uri: Uri,
    val originalName: String,
    val sizeFormatted: String,
    val isPdf: Boolean,
    val customTitle: String = "",
    val contentUrl: String = "",
    val documentContent: String = "",
    val durationOrPages: String = "",
    val customTopic: String = ""
)

@Entity(tableName = "student_library_documents")
data class LibraryDocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val filePath: String,
    val fileName: String,
    val docType: String, // "EDITED_PDF", "EXAM_PDF", "DOWNLOADED_PDF"
    val subjectId: String = "ALL",
    val pageCount: Int = 1,
    val fileSizeFormatted: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "homework_reminders")
data class HomeworkReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectName: String,
    val topicName: String = "",
    val dueDateTimestamp: Long, // timestamp when homework is due
    val isFinished: Boolean = false,
    val notified24h: Boolean = false,
    val notified2h: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)


