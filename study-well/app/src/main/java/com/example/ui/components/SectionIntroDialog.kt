package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.SubjectEnum
import com.example.ui.theme.SleekBlue400
import com.example.ui.theme.SleekBlue600
import com.example.ui.theme.SleekGold400
import com.example.ui.theme.SleekGold500
import com.example.ui.theme.SleekNavy800
import com.example.ui.theme.SleekNavy900
import com.example.ui.theme.SleekNavy950

data class SectionIntroData(
    val sectionKey: String,
    val badge: String,
    val title: String,
    val subtitle: String,
    val headerIcon: ImageVector,
    val accentColor: Color,
    val tips: List<Triple<ImageVector, String, String>>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionIntroDialog(
    sectionKey: String,
    subject: SubjectEnum,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val introData = when (sectionKey) {
        "VIDEOS" -> SectionIntroData(
            sectionKey = "VIDEOS",
            badge = "🎬 VIDEO MASTERCLASS INTRO",
            title = "${subject.title} Video Lectures & Masterclasses",
            subtitle = "Expert Explanations, Problem Breakdowns & Step-by-Step Solutions",
            headerIcon = Icons.Default.PlayCircleFilled,
            accentColor = SleekBlue400,
            tips = listOf(
                Triple(
                    Icons.Default.School,
                    "Topic-by-Topic Visual Mastery",
                    "Comprehensive video tutorials organized chapter by chapter to build deep conceptual clarity on tough IGCSE concepts."
                ),
                Triple(
                    Icons.Default.Videocam,
                    "Worked Past Paper Walkthroughs",
                    "Watch expert instructors demonstrate how to structure answers and secure maximum method marks on exam questions."
                ),
                Triple(
                    Icons.Default.Speed,
                    "Flexible Playback & Speed Controls",
                    "Adjust playback from 0.75x to 2.0x, skip ±30s, and follow along with embedded transcripts and revision takeaways."
                ),
                Triple(
                    Icons.Default.AutoAwesome,
                    "Pre-Lesson Roadmap & Tips",
                    "Every lecture opens with a curated overview, formula focus, and examiner advice before playback starts."
                )
            )
        )
        "BOOKS" -> SectionIntroData(
            sectionKey = "BOOKS",
            badge = "📚 OFFICIAL COURSEBOOKS INTRO",
            title = "${subject.title} Coursebooks & Texts",
            subtitle = "Comprehensive Cambridge & Edexcel Syllabus Guides",
            headerIcon = Icons.AutoMirrored.Filled.MenuBook,
            accentColor = SleekBlue400,
            tips = listOf(
                Triple(
                    Icons.Default.School,
                    "Complete Syllabus Alignment",
                    "Official endorsed textbooks covering every learning objective for Cambridge 0625/0580/0417 & Edexcel specifications."
                ),
                Triple(
                    Icons.AutoMirrored.Filled.MenuBook,
                    "In-App Digital PDF Reader",
                    "Browse chapters smoothly, jump directly to page numbers, and switch between Light, Dark, and Sepia reader modes."
                ),
                Triple(
                    Icons.Default.Edit,
                    "Stylus & Apple Pencil Markup",
                    "Annotate book margins, highlight key definitions, and write handwritten worked examples directly over the pages."
                ),
                Triple(
                    Icons.Default.Download,
                    "Offline Reading Mode",
                    "Download books to your local storage to study anytime, anywhere without an internet connection."
                )
            )
        )
        "NOTES" -> SectionIntroData(
            sectionKey = "NOTES",
            badge = "📝 REVISION NOTES INTRO",
            title = "${subject.title} Revision Notes & Cheatsheets",
            subtitle = "High-Yield Summaries & Digital Blank Notebooks",
            headerIcon = Icons.Default.Description,
            accentColor = SleekGold400,
            tips = listOf(
                Triple(
                    Icons.Default.AutoAwesome,
                    "Distilled Core Summaries",
                    "Concise, bulletproof revision sheets crafted for rapid recall, formula memorization, and night-before-exam prep."
                ),
                Triple(
                    Icons.Default.Edit,
                    "Blank Digital Notebooks",
                    "Create your own lined, grid, or dotted PDF revision pads with custom page counts to write your own notes."
                ),
                Triple(
                    Icons.Default.Bookmark,
                    "Formulas & Key Laws",
                    "Quick-access tables for physics equations, math theorems, and chemical equations organized by topic."
                ),
                Triple(
                    Icons.Default.CheckCircle,
                    "Export & Share",
                    "Save your annotated revision notes or share annotated copies with classmates and study groups."
                )
            )
        )
        "MARKSCHEMES" -> SectionIntroData(
            sectionKey = "MARKSCHEMES",
            badge = "🎯 EXAMINER MARK SCHEMES INTRO",
            title = "${subject.title} Official Mark Schemes",
            subtitle = "Master Examiner Scoring Rubrics & Method Marks",
            headerIcon = Icons.AutoMirrored.Filled.FactCheck,
            accentColor = Color(0xFF10B981),
            tips = listOf(
                Triple(
                    Icons.Default.Grade,
                    "M, A & B Mark Breakdown",
                    "Learn exactly how Method marks (M), Accuracy marks (A), and Independent marks (B) are allocated by examiners."
                ),
                Triple(
                    Icons.Default.Rule,
                    "Avoid Common Penalties",
                    "Identify frequently penalized errors such as premature rounding, missing units, and incorrect significant figures."
                ),
                Triple(
                    Icons.Default.CheckCircle,
                    "Official Step-by-Step Solutions",
                    "Verify each step of your working against the official Cambridge/Edexcel mark guidelines."
                ),
                Triple(
                    Icons.AutoMirrored.Filled.FactCheck,
                    "Self-Assessment & Grading",
                    "Grade your practice exams with the same rigor as IGCSE chief examiners to track true readiness."
                )
            )
        )
        else -> return
    }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = modifier
            .fillMaxWidth(0.94f)
            .testTag("section_intro_dialog"),
        content = {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = SleekNavy950,
                border = BorderStroke(1.dp, introData.accentColor.copy(alpha = 0.35f)),
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Row with Subject Icon & Close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = introData.accentColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, introData.accentColor.copy(alpha = 0.4f)),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = introData.headerIcon,
                                    contentDescription = null,
                                    tint = introData.accentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = subject.color.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, subject.color.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "${subject.title} • ${subject.syllabusCode}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = subject.color,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_section_intro_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = introData.accentColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, introData.accentColor.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = introData.badge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                            color = introData.accentColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Title & Subtitle
                    Text(
                        text = introData.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = introData.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Tip Cards
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        introData.tips.forEach { (icon, title, desc) ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = SleekNavy900),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
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
                                        color = introData.accentColor.copy(alpha = 0.15f),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = introData.accentColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
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
                                            color = Color.White.copy(alpha = 0.68f),
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Explore Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("explore_section_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = introData.accentColor,
                            contentColor = if (introData.accentColor == SleekGold400) SleekNavy950 else Color.White
                        )
                    ) {
                        Text(
                            text = "Explore ${introData.sectionKey.lowercase().replaceFirstChar { it.uppercase() }}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    )
}
