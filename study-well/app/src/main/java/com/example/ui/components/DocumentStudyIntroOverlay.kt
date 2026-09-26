package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudyMaterialEntity
import com.example.data.model.SubjectEnum
import com.example.ui.theme.SleekBlue400
import com.example.ui.theme.SleekBlue600
import com.example.ui.theme.SleekGold400
import com.example.ui.theme.SleekGold500
import com.example.ui.theme.SleekNavy800
import com.example.ui.theme.SleekNavy900
import com.example.ui.theme.SleekNavy950

@Composable
fun DocumentStudyIntroOverlay(
    material: StudyMaterialEntity,
    subject: SubjectEnum,
    onStartReading: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Determine type branding
    val isBook = material.materialType.equals("BOOK", ignoreCase = true)
    val isNote = material.materialType.equals("NOTE", ignoreCase = true)
    val isMarkScheme = material.materialType.equals("MARKSCHEME", ignoreCase = true) || material.title.contains("Mark Scheme", ignoreCase = true)
    val isSheet = material.materialType.equals("SHEET", ignoreCase = true) || material.materialType.equals("EXAM", ignoreCase = true)

    val typeBadge = when {
        isBook -> "📚 OFFICIAL COURSEBOOK INTRO"
        isNote -> "📝 REVISION NOTE & SUMMARY"
        isMarkScheme -> "🎯 EXAMINER MARK SCHEME"
        else -> "📄 STUDY DOCUMENT & EXAM SHEET"
    }

    val typeIcon: ImageVector = when {
        isBook -> Icons.AutoMirrored.Filled.MenuBook
        isNote -> Icons.Default.Description
        isMarkScheme -> Icons.AutoMirrored.Filled.FactCheck
        else -> Icons.Default.PictureAsPdf
    }

    val accentColor: Color = when {
        isBook -> SleekBlue400
        isNote -> SleekGold400
        isMarkScheme -> Color(0xFF10B981)
        else -> subject.color
    }

    val guideTips = when {
        isBook -> listOf(
            Triple(
                Icons.Default.School,
                "Structured Syllabus Reading",
                "Work through textbook theory step-by-step, paying close attention to bold terminology and boxed worked examples."
            ),
            Triple(
                Icons.Default.Edit,
                "Handwrite & Annotate Margins",
                "Use the PDF markup pen or highlighter tool to write step-by-step working directly on textbook problem sets."
            ),
            Triple(
                Icons.Default.Visibility,
                "Custom Themes & Focus Mode",
                "Switch between Light, Dark, and Sepia modes or tap Focus to eliminate distracting UI bars."
            ),
            Triple(
                Icons.Default.Download,
                "Offline & Share Ready",
                "Save your handwritten margin annotations or export copies to review anywhere without internet."
            )
        )
        isNote -> listOf(
            Triple(
                Icons.Default.AutoAwesome,
                "High-Yield Rapid Recall",
                "Formulas, definitions, and core memory cues distilled for fast revision before tests and mock exams."
            ),
            Triple(
                Icons.Default.Edit,
                "Personal Digital Notebook",
                "Add your own memory mnemonics, color code key points, and write practice answers alongside definitions."
            ),
            Triple(
                Icons.Default.Headphones,
                "Text-to-Speech Audio Mode",
                "Listen to your notes read aloud with British English accent pronunciation while resting your eyes."
            ),
            Triple(
                Icons.Default.Bookmark,
                "Quick Topic Bookmarks",
                "Tag critical formulas and definitions to jump straight to them during active recall sessions."
            )
        )
        isMarkScheme -> listOf(
            Triple(
                Icons.Default.Grade,
                "Method (M) vs Accuracy (A) Marks",
                "Look for 'M' marks for valid formulas/method, 'A' marks for correct numerical answers with units, and 'B' for independent facts."
            ),
            Triple(
                Icons.Default.Rule,
                "Examiner Avoid List",
                "Observe penalties for premature rounding, missing standard units, and incorrect significant figures."
            ),
            Triple(
                Icons.Default.CheckCircle,
                "Official Model Answers",
                "Compare each line of your working against the examiner's step-by-step expected rubric."
            ),
            Triple(
                Icons.AutoMirrored.Filled.FactCheck,
                "Realistic Self-Grading",
                "Award yourself marks conservatively to identify exact weak areas before the real exam."
            )
        )
        else -> listOf(
            Triple(
                Icons.Default.School,
                "Exam Practice Sheet",
                "Solve past paper questions under timed conditions before reviewing solutions."
            ),
            Triple(
                Icons.Default.Edit,
                "Annotate & Grade Directly",
                "Write your working with the digital stylus pen and cross-check against official mark schemes."
            ),
            Triple(
                Icons.Default.Download,
                "Download for Print / Offline",
                "Save a local copy of this worksheet to study or print whenever you need."
            )
        )
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("document_study_intro_overlay"),
        color = SleekNavy950.copy(alpha = 0.96f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation & Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = subject.color.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, subject.color.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = null,
                            tint = subject.color,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${subject.title} • ${subject.syllabusCode}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = subject.color
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("doc_intro_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Glowing Animated Document Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(90.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.15f),
                    modifier = Modifier
                        .size(80.dp)
                        .scale(pulseScale)
                ) {}

                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.25f),
                    border = BorderStroke(2.dp, accentColor.copy(alpha = 0.7f)),
                    shadowElevation = 12.dp,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section / Type Badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accentColor.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
            ) {
                Text(
                    text = typeBadge,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.1.sp,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Document Title
            Text(
                text = material.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Topic & Details
            Text(
                text = if (material.topic.isNotBlank()) "Topic: ${material.topic}" else "Cambridge & Edexcel Resource",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Material Metadata Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DocMetaChip(
                    icon = Icons.Default.Description,
                    text = material.durationOrPages.ifBlank { "Full Document" },
                    color = SleekGold400
                )
                Spacer(modifier = Modifier.width(8.dp))
                DocMetaChip(
                    icon = Icons.Default.Edit,
                    text = "Stylus Markup Ready",
                    color = SleekBlue400
                )
                Spacer(modifier = Modifier.width(8.dp))
                DocMetaChip(
                    icon = Icons.Default.Download,
                    text = "Offline Mode",
                    color = Color(0xFF10B981)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Study Guidance Cards
            Text(
                text = "STUDY GUIDELINES & TIPS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = Color.White.copy(alpha = 0.55f),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                guideTips.forEach { (icon, title, desc) ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SleekNavy900),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.07f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = accentColor.copy(alpha = 0.15f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Start Reading Button
            Button(
                onClick = onStartReading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("start_reading_document_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = if (accentColor == SleekGold400) SleekNavy950 else Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        isBook -> "Start Reading Coursebook"
                        isNote -> "Open Revision Note"
                        isMarkScheme -> "Review Examiner Mark Scheme"
                        else -> "Open Document"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun DocMetaChip(
    icon: ImageVector,
    text: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SleekNavy900,
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                fontSize = 11.sp
            )
        }
    }
}
