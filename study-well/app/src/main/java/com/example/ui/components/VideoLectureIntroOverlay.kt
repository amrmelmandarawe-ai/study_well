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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VideoLibrary
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
import com.example.ui.theme.SleekNavy800
import com.example.ui.theme.SleekNavy900
import com.example.ui.theme.SleekNavy950

@Composable
fun VideoLectureIntroOverlay(
    material: StudyMaterialEntity,
    subject: SubjectEnum,
    onStartLesson: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Smooth pulse animation for the central Play button
    val infiniteTransition = rememberInfiniteTransition(label = "play_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SleekNavy950.copy(alpha = 0.98f),
                        SleekNavy900.copy(alpha = 0.99f),
                        Color(0xFF070D1E)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("video_lecture_intro_overlay")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("intro_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White.copy(alpha = 0.85f)
                    )
                }

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
                            imageVector = Icons.Default.VideoLibrary,
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
                    modifier = Modifier.testTag("intro_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Animated Glowing Play Icon Badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(110.dp)
            ) {
                // Outer glowing halo
                Box(
                    modifier = Modifier
                        .size(105.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    subject.color.copy(alpha = 0.35f),
                                    SleekBlue600.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Inner circle with gradient
                Surface(
                    shape = CircleShape,
                    color = SleekNavy800,
                    border = BorderStroke(2.dp, subject.color.copy(alpha = 0.8f)),
                    modifier = Modifier.size(76.dp),
                    shadowElevation = 8.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        SleekNavy900,
                                        subject.color.copy(alpha = 0.25f)
                                    )
                                )
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Video Intro",
                            tint = SleekGold400,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Badge: Lesson Preview
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SleekGold400.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, SleekGold400.copy(alpha = 0.35f))
            ) {
                Text(
                    text = "🎬 LECTURE VIDEO INTRO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.3.sp,
                    color = SleekGold400,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Video Title
            Text(
                text = material.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )

            if (material.topic.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Topic: ${material.topic}",
                    style = MaterialTheme.typography.titleSmall,
                    color = SleekBlue400,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Specs Pill Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SpecChip(
                    icon = Icons.Default.Schedule,
                    label = if (material.durationOrPages.isNotBlank()) material.durationOrPages else "Full Lesson"
                )
                Spacer(modifier = Modifier.width(8.dp))
                SpecChip(
                    icon = Icons.Default.HighQuality,
                    label = "1080p HD Stream"
                )
                Spacer(modifier = Modifier.width(8.dp))
                SpecChip(
                    icon = Icons.Default.Speed,
                    label = "0.75x - 2.0x Speed"
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Study Focus Preparation Checklist Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SleekNavy900),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "💡 Optimal Study Preparation",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SleekGold400
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    PreparationItem(
                        icon = Icons.Default.Headphones,
                        title = "Headphones Recommended",
                        description = "Use earphones or headphones for clear instructor explanations and focus."
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    PreparationItem(
                        icon = Icons.Default.MenuBook,
                        title = "Active Note-Taking",
                        description = "Keep your digital notebook or revision sheet ready for formulas and key definitions."
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    PreparationItem(
                        icon = Icons.Default.PauseCircle,
                        title = "Interactive Pauses",
                        description = "Pause when worked examples appear to attempt the problem independently first."
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Button(
                onClick = onStartLesson,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("start_video_lesson_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SleekBlue600,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start Video Lesson",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("cancel_video_intro_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White.copy(alpha = 0.75f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Text(
                    text = "Back to Materials",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SpecChip(
    icon: ImageVector,
    label: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = SleekNavy800,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SleekBlue400,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PreparationItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = SleekBlue600.copy(alpha = 0.18f),
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SleekBlue400,
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
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                lineHeight = 18.sp
            )
        }
    }
}
