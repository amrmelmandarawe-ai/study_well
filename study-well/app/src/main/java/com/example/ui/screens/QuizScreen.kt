package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuizQuestion
import com.example.data.model.SubjectEnum
import com.example.ui.theme.SleekBlue600
import com.example.ui.theme.SleekGold400
import com.example.ui.theme.SleekGold500
import com.example.ui.theme.SleekNavy900
import com.example.ui.theme.SleekNavy950
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    subject: SubjectEnum,
    topic: String,
    questions: List<QuizQuestion>,
    onSubmitResult: (score: Int, total: Int, details: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (questions.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No questions available for this topic.",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBack) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    val userAnswers = remember { mutableStateMapOf<Int, Int>() } // questionIndex -> selectedOptionIndex
    var isSubmitted by remember { mutableStateOf(false) }
    var secondsRemaining by remember { mutableIntStateOf(300) } // 5 minutes timer

    // Timer loop
    LaunchedEffect(isSubmitted) {
        if (!isSubmitted) {
            while (secondsRemaining > 0) {
                delay(1000)
                secondsRemaining--
            }
            if (secondsRemaining == 0 && !isSubmitted) {
                isSubmitted = true
            }
        }
    }

    val currentQuestion = questions[currentIndex]

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("quiz_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "${subject.title} AI Quiz",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = topic,
                            style = MaterialTheme.typography.labelSmall,
                            color = subject.color,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("quiz_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit Quiz")
                    }
                },
                actions = {
                    if (!isSubmitted) {
                        // Timer Indicator
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (secondsRemaining < 60) MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                if (secondsRemaining < 60) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Timer",
                                    tint = if (secondsRemaining < 60) MaterialTheme.colorScheme.error else subject.color,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                val mins = secondsRemaining / 60
                                val secs = secondsRemaining % 60
                                Text(
                                    text = String.format("%02d:%02d", mins, secs),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (secondsRemaining < 60) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (isSubmitted) {
            // Results & Review Screen
            var calculatedScore = 0
            questions.forEachIndexed { idx, q ->
                if (userAnswers[idx] == q.correctOptionIndex) {
                    calculatedScore++
                }
            }
            val percentage = (calculatedScore.toFloat() / questions.size) * 100f
            val igcseGrade = when {
                percentage >= 90 -> "A* (Outstanding)"
                percentage >= 80 -> "A (Distinction)"
                percentage >= 70 -> "B (Merit)"
                percentage >= 60 -> "C (Pass)"
                percentage >= 50 -> "D"
                else -> "Ungraded (Review Required)"
            }

            LaunchedEffect(Unit) {
                onSubmitResult(calculatedScore, questions.size, "Scored $calculatedScore/${questions.size} ($igcseGrade)")
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Score Summary Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = SleekNavy950
                        ),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = SleekGold400,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(
                                    Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = SleekNavy950,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Quiz Completed!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Score: $calculatedScore / ${questions.size} (${percentage.toInt()}%)",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = SleekGold400
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "Estimated IGCSE Grade: $igcseGrade",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Detailed Answers & Explanations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Question by Question Review
                itemsIndexed(questions) { index, q ->
                    val userAns = userAnswers[index]
                    val isCorrect = userAns == q.correctOptionIndex

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isCorrect) SuccessGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Question ${index + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isCorrect) SuccessGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                                            contentDescription = null,
                                            tint = if (isCorrect) SuccessGreen else MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isCorrect) "Correct (+1)" else "Incorrect (0)",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCorrect) SuccessGreen else MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }

                            if (q.pastPaperCitation.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Paper Reference: ${q.pastPaperCitation}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = subject.color
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = q.questionText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Correct Answer: ${('A' + q.correctOptionIndex)}. ${q.options[q.correctOptionIndex]}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold
                            )

                            if (userAns != null && !isCorrect) {
                                Text(
                                    text = "Your Answer: ${('A' + userAns)}. ${q.options[userAns]}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Cambridge Examiner Mark Scheme Explanation:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = subject.color
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = q.explanation,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Back to Subject")
                        }

                        Button(
                            onClick = {
                                userAnswers.clear()
                                currentIndex = 0
                                isSubmitted = false
                                secondsRemaining = 300
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SleekBlue600, contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retake Quiz", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Active Quiz In-Progress Session
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Question Progress Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Question ${currentIndex + 1} of ${questions.size}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = subject.color
                        )
                        Text(
                            text = "${currentQuestion.marks} Mark",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { (currentIndex + 1).toFloat() / questions.size },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = subject.color,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Question Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            if (currentQuestion.pastPaperCitation.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = subject.color.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, subject.color.copy(alpha = 0.25f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.HelpOutline,
                                            contentDescription = null,
                                            tint = subject.color,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = currentQuestion.pastPaperCitation,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = subject.color
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            Text(
                                text = currentQuestion.questionText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 24.sp
                            )

                            if (currentQuestion.igcseTip.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SleekGold400.copy(alpha = 0.12f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = null,
                                            tint = SleekGold500,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Tip: ${currentQuestion.igcseTip}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Options List
                    currentQuestion.options.forEachIndexed { optIndex, optionText ->
                        val isSelected = userAnswers[currentIndex] == optIndex
                        val optionLetter = ('A' + optIndex).toString()

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clickable {
                                    userAnswers[currentIndex] = optIndex
                                }
                                .testTag("quiz_option_${optIndex}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) subject.color.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) subject.color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) subject.color else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = optionLetter,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = optionText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Bottom Navigation Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentIndex > 0) {
                        OutlinedButton(
                            onClick = { currentIndex-- },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Previous")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (currentIndex < questions.size - 1) {
                        Button(
                            onClick = { currentIndex++ },
                            colors = ButtonDefaults.buttonColors(containerColor = subject.color, contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = { isSubmitted = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SleekGold400, contentColor = SleekNavy950),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("submit_quiz_button")
                        ) {
                            Text("Submit Exam", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

