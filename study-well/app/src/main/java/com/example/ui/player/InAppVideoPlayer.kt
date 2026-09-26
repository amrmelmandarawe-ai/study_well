package com.example.ui.player

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.StudyMaterialEntity
import com.example.data.model.SubjectEnum
import com.example.ui.components.VideoLectureIntroOverlay
import com.example.utils.IntroPreferencesManager
import com.example.ui.theme.SleekBlue400
import com.example.ui.theme.SleekGold400
import com.example.ui.theme.SleekNavy900
import com.example.ui.theme.SleekNavy950

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InAppVideoPlayer(
    material: StudyMaterialEntity,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isFullScreen by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Overview, 1: Lesson Notes, 2: Key Takeaways
    var isBuffering by remember { mutableStateOf(true) }
    var playbackSpeed by remember { mutableStateOf("1.0x") }
    val speeds = listOf("0.75x", "1.0x", "1.25x", "1.5x", "2.0x")

    val subject = SubjectEnum.fromId(material.subjectId)
    var showVideoIntro by remember {
        mutableStateOf(!IntroPreferencesManager.hasSeenVideoIntro(context))
    }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var isVideoPlaying by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("in_app_video_player_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.testTag("close_video_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = subject.title + " Lecture",
                                style = MaterialTheme.typography.labelMedium,
                                color = subject.color,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = material.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                videoViewRef?.pause()
                                isVideoPlaying = false
                                showVideoIntro = true
                            },
                            modifier = Modifier.testTag("show_video_intro_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Lesson Intro & Tips",
                                tint = SleekBlue400
                            )
                        }

                        if (material.contentUrl.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    com.example.utils.FileUtils.openExternalUriOrUrl(context, material.contentUrl)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = "Open in Drive / External Player",
                                    tint = SleekGold400
                                )
                            }
                        }

                        IconButton(onClick = { isFullScreen = !isFullScreen }) {
                            Icon(
                                imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                contentDescription = "Toggle Fullscreen",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Video Player View Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isFullScreen) Modifier.weight(1f)
                            else Modifier.aspectRatio(16f / 9f)
                        )
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    var hasError by remember { mutableStateOf(false) }

                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                val mediaController = MediaController(ctx)
                                mediaController.setAnchorView(this)
                                setMediaController(mediaController)

                                try {
                                    val videoUri = Uri.parse(material.contentUrl)
                                    setVideoURI(videoUri)

                                    setOnPreparedListener { mp ->
                                        isBuffering = false
                                        hasError = false
                                        mp.isLooping = true
                                        if (!showVideoIntro) {
                                            isVideoPlaying = true
                                            start()
                                        } else {
                                            isVideoPlaying = false
                                        }
                                    }

                                    setOnErrorListener { _, _, _ ->
                                        isBuffering = false
                                        hasError = true
                                        true
                                    }
                                } catch (_: Exception) {
                                    isBuffering = false
                                    hasError = true
                                }
                                videoViewRef = this
                            }
                        },
                        update = { view ->
                            videoViewRef = view
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("video_view_component")
                    )

                if (isBuffering && !hasError) {
                    CircularProgressIndicator(
                        color = SleekGold400,
                        modifier = Modifier.size(48.dp)
                    )
                }

                if (hasError && material.contentUrl.isNotBlank()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Stream loading / External format",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.material3.Button(
                            onClick = {
                                com.example.utils.FileUtils.openExternalUriOrUrl(context, material.contentUrl)
                            },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = SleekGold400,
                                contentColor = SleekNavy950
                            )
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open in Google Drive / Video Player", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Dedicated Video Controls: -30s Seek Backward, Play/Pause, +30s Seek Forward
            Surface(
                color = SleekNavy950,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // -30 Seconds Seek Backward
                    IconButton(
                        onClick = {
                            videoViewRef?.let { vv ->
                                val cur = vv.currentPosition
                                val target = (cur - 30_000).coerceAtLeast(0)
                                vv.seekTo(target)
                            }
                        },
                        modifier = Modifier.testTag("seek_backward_30s_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FastRewind,
                                contentDescription = "Rewind 30s",
                                tint = SleekGold400,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("30s", color = SleekGold400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Play / Pause Toggle
                    IconButton(
                        onClick = {
                            videoViewRef?.let { vv ->
                                if (vv.isPlaying) {
                                    vv.pause()
                                    isVideoPlaying = false
                                } else {
                                    vv.start()
                                    isVideoPlaying = true
                                }
                            }
                        },
                        modifier = Modifier
                            .background(SleekGold400, CircleShape)
                            .size(40.dp)
                            .testTag("video_play_pause_toggle")
                    ) {
                        Icon(
                            imageVector = if (isVideoPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = SleekNavy950,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // +30 Seconds Seek Forward
                    IconButton(
                        onClick = {
                            videoViewRef?.let { vv ->
                                val cur = vv.currentPosition
                                val dur = vv.duration
                                val target = if (dur > 0) (cur + 30_000).coerceAtMost(dur) else (cur + 30_000)
                                vv.seekTo(target)
                            }
                        },
                        modifier = Modifier.testTag("seek_forward_30s_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("30s", color = SleekGold400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.FastForward,
                                contentDescription = "Fast Forward 30s",
                                tint = SleekGold400,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            if (!isFullScreen) {
                // Playback speed selector row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Speed",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Speed:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        speeds.forEach { speed ->
                            FilterChip(
                                selected = playbackSpeed == speed,
                                onClick = { playbackSpeed = speed },
                                label = { Text(speed, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Tabs for Video Info
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Overview") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Lesson Notes") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("IGCSE Exam Tips") }
                    )
                }

                // Tab Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTab) {
                        0 -> {
                            Text(
                                text = material.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = subject.color.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = subject.syllabusCode,
                                        color = subject.color,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "• Duration: ${material.durationOrPages}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "• Topic: ${material.topic}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Description",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = material.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )
                        }
                        1 -> {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.MenuBook,
                                            contentDescription = null,
                                            tint = subject.color,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Transcript & Formula Summary",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = material.documentContent.ifBlank {
                                            "Follow along with the video lecture. Make sure to note down step-by-step solutions and pause at practice problems."
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }
                        2 -> {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = SleekGold400.copy(alpha = 0.12f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = null,
                                            tint = SleekGold400,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Cambridge IGCSE Examiner Advice",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "1. Show working clearly for method marks in structured questions.\n2. Always double check standard units before substituting into equations.\n3. In Paper 2 Multiple Choice, eliminate obviously incorrect answers first.\n4. Take the topic quiz after watching this video to test your mastery!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

            // Video Lecture Intro Overlay (Presented before video playback starts)
            if (showVideoIntro) {
                VideoLectureIntroOverlay(
                    material = material,
                    subject = subject,
                    onStartLesson = {
                        IntroPreferencesManager.markVideoIntroSeen(context)
                        showVideoIntro = false
                        isVideoPlaying = true
                        videoViewRef?.start()
                    },
                    onClose = onClose
                )
            }
        }
    }
}
