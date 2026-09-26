package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.theme.SleekBlue400
import com.example.ui.theme.SleekBlue600
import com.example.ui.theme.SleekGold400
import com.example.ui.theme.SleekNavy800
import com.example.ui.theme.SleekNavy900
import com.example.ui.theme.SleekNavy950

data class IntroSlideData(
    val title: String,
    val subtitle: String,
    val description: String,
    val badgeText: String,
    val icon: ImageVector,
    val accentColor: Color,
    val highlights: List<Pair<ImageVector, String>>
)

@Composable
fun OnboardingIntroScreen(
    onFinishIntro: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentSlide by remember { mutableIntStateOf(0) }

    val slides = listOf(
        IntroSlideData(
            title = "🎬 Video Masterclasses & Lectures",
            subtitle = "HD Topic Tutorials & Worked Exam Solutions",
            description = "Watch chapter-by-chapter video lectures with interactive speed controls (0.75x to 2.0x), quick ±30s seeks, pre-lesson roadmap tips, and synced formula summaries.",
            badgeText = "Video Lessons",
            icon = Icons.Default.PlayCircleFilled,
            accentColor = SleekBlue400,
            highlights = listOf(
                Pair(Icons.Default.School, "Full Cambridge & Edexcel syllabus video lessons"),
                Pair(Icons.Default.Speed, "Custom speed controls (0.75x – 2.0x) & quick seek"),
                Pair(Icons.Default.PlayCircle, "Step-by-step past exam problem walkthroughs"),
                Pair(Icons.Default.CheckCircle, "Pre-lesson roadmap briefing & examiner advice")
            )
        ),
        IntroSlideData(
            title = "📚 Official Coursebooks & Reader",
            subtitle = "Endorsed Textbooks & Built-In Digital PDF Reader",
            description = "Access endorsed Cambridge 0625/0580/0417 & Edexcel coursebooks. Enjoy comfortable reading with Sepia/Dark modes, full-screen focus, and offline storage.",
            badgeText = "Coursebooks",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            accentColor = SleekBlue400,
            highlights = listOf(
                Pair(Icons.Default.School, "Official Cambridge & Edexcel syllabus textbooks"),
                Pair(Icons.AutoMirrored.Filled.MenuBook, "Digital reader with Light, Dark & Sepia themes"),
                Pair(Icons.Default.Edit, "Digital stylus markup & margin annotations"),
                Pair(Icons.Default.Download, "Download books for offline study anytime")
            )
        ),
        IntroSlideData(
            title = "📝 High-Yield Revision Notes",
            subtitle = "Concise Cheatsheets & Personal Blank Notebooks",
            description = "Master key definitions and formulas with high-yield summary sheets. Listen to British voice audio narration, or create custom blank ruled/grid digital notebooks to write your own notes.",
            badgeText = "Revision Notes",
            icon = Icons.Default.Description,
            accentColor = SleekGold400,
            highlights = listOf(
                Pair(Icons.Default.AutoAwesome, "High-yield summary cheat sheets for rapid recall"),
                Pair(Icons.Default.Edit, "Create custom Ruled, Grid & Cornell blank notebooks"),
                Pair(Icons.AutoMirrored.Filled.VolumeUp, "British English text-to-speech audio reader"),
                Pair(Icons.Default.Bookmark, "Quick-access topic bookmarks & equation tables")
            )
        ),
        IntroSlideData(
            title = "🎯 Examiner Mark Schemes",
            subtitle = "Official Marking Rubrics & Method Mark Scoring",
            description = "Learn how chief examiners grade your exams. Understand Method (M), Accuracy (A), and Independent (B) marks, eliminate common penalties, and master model answers.",
            badgeText = "Mark Schemes",
            icon = Icons.AutoMirrored.Filled.FactCheck,
            accentColor = Color(0xFF10B981),
            highlights = listOf(
                Pair(Icons.Default.Grade, "M, A, and B mark allocation breakdowns"),
                Pair(Icons.Default.Rule, "Avoid penalties for rounding, units, and sig figs"),
                Pair(Icons.Default.CheckCircle, "Official step-by-step model solutions"),
                Pair(Icons.AutoMirrored.Filled.FactCheck, "Self-assessment scoring to track true readiness")
            )
        ),
        IntroSlideData(
            title = "🤖 AI Gemini Tutor & Quizzes",
            subtitle = "24/7 Intelligent Subject Help & Timed Assessments",
            description = "Ask your AI tutor powered by Gemini for instant step-by-step explanations, and test your mastery with custom timed topic quizzes and diagnostic analytics.",
            badgeText = "AI & Assessments",
            icon = Icons.Default.AutoAwesome,
            accentColor = Color(0xFF38BDF8),
            highlights = listOf(
                Pair(Icons.Default.AutoAwesome, "24/7 Intelligent AI Subject Tutor"),
                Pair(Icons.Default.Quiz, "Custom topic quizzes with instant grading"),
                Pair(Icons.Default.Psychology, "Detailed step-by-step answer analysis"),
                Pair(Icons.Default.CheckCircle, "Real-time performance analytics & streak tracking")
            )
        )
    )

    val slide = slides[currentSlide]

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(SleekNavy950, SleekNavy900, SleekNavy800)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar with Skip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Brand Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = SleekGold400,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            tint = SleekNavy950,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Study Well",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                TextButton(
                    onClick = onFinishIntro,
                    modifier = Modifier.testTag("intro_skip_button")
                ) {
                    Text(
                        text = "Skip",
                        color = Color(0xFF94A3B8),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Content Card
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Feature Icon Badge
                Surface(
                    shape = CircleShape,
                    color = slide.accentColor.copy(alpha = 0.15f),
                    border = BorderStroke(2.dp, slide.accentColor.copy(alpha = 0.4f)),
                    modifier = Modifier.size(100.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = slide.icon,
                            contentDescription = null,
                            tint = slide.accentColor,
                            modifier = Modifier.size(52.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Tag Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = slide.accentColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, slide.accentColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = slide.badgeText,
                        color = slide.accentColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = slide.title,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = slide.subtitle,
                    color = SleekGold400,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = slide.description,
                    color = Color(0xFFCBD5E1),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Feature Highlights Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SleekNavy900.copy(alpha = 0.8f)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        slide.highlights.forEach { (hIcon, hText) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = slide.accentColor.copy(alpha = 0.15f),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = hIcon,
                                        contentDescription = null,
                                        tint = slide.accentColor,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = hText,
                                    color = Color(0xFFF1F5F9),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom Navigation & Action
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    slides.indices.forEach { index ->
                        val isSelected = index == currentSlide
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (isSelected) 24.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) SleekBlue400 else Color(0xFF475569)
                                )
                        )
                    }
                }

                // Next / Get Started Button
                Button(
                    onClick = {
                        if (currentSlide < slides.size - 1) {
                            currentSlide++
                        } else {
                            onFinishIntro()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag(if (currentSlide == slides.size - 1) "intro_get_started_button" else "intro_next_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentSlide == slides.size - 1) SleekBlue600 else SleekNavy800,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = if (currentSlide != slides.size - 1) BorderStroke(1.dp, SleekBlue400.copy(alpha = 0.4f)) else null
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (currentSlide == slides.size - 1) "Get Started" else "Next",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
