package com.example.ui.screens

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.FlashcardItem
import com.example.data.model.MistakeItem
import com.example.data.model.QuestionType
import com.example.data.model.QuizDifficulty
import com.example.data.model.QuizMode
import com.example.data.model.QuizQuestion
import com.example.ui.components.MediaCaptureRow
import com.example.ui.strings.AppStrings
import com.example.ui.theme.BorderNavy
import com.example.ui.theme.CoralContainer
import com.example.ui.theme.CoralError
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.ElectricSky
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmAmber
import com.example.viewmodel.ExamSubTab
import com.example.viewmodel.ExamUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExamScreen(
    strings: AppStrings,
    examState: ExamUiState,
    mistakes: List<MistakeItem>,
    flashcards: List<FlashcardItem>,
    isAnswerCorrect: (QuizQuestion, String) -> Boolean,
    onSelectSubTab: (ExamSubTab) -> Unit,
    onSourceTextChange: (String) -> Unit,
    onImageSelected: (Uri?) -> Unit,
    onImportLesson: () -> Unit,
    onSetMode: (QuizMode) -> Unit,
    onSetDifficulty: (QuizDifficulty) -> Unit,
    onToggleType: (QuestionType) -> Unit,
    onSetCount: (Int) -> Unit,
    onSetScore: (Int) -> Unit,
    onSetTimer: (Int) -> Unit,
    onGenerateQuiz: () -> Unit,
    onSelectAnswer: (String, String) -> Unit,
    onCheckSingle: (QuizQuestion) -> Unit,
    onNextQuestion: () -> Unit,
    onSubmitFullExam: () -> Unit,
    onResetQuiz: () -> Unit,
    onRemoveMistake: (String) -> Unit,
    onClearMistakes: () -> Unit,
    onPracticeMistakes: () -> Unit,
    onGenerateFlashcards: () -> Unit,
    onFlipFlashcard: () -> Unit,
    onMoveFlashcard: (Int) -> Unit,
    onToggleFlashcardMastered: (String, Boolean) -> Unit,
    onTapSound: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Sub-tab selector: Quiz | Mistake Bank | Flashcards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SubTabPill(
                    label = strings.subTabQuiz,
                    icon = Icons.Default.Quiz,
                    selected = examState.subTab == ExamSubTab.QUIZ,
                    tag = "subtab_quiz",
                    modifier = Modifier.weight(1f)
                ) { onSelectSubTab(ExamSubTab.QUIZ) }

                SubTabPill(
                    label = "${strings.subTabMistakes} (${mistakes.size})",
                    icon = Icons.Default.ErrorOutline,
                    selected = examState.subTab == ExamSubTab.MISTAKES,
                    tag = "subtab_mistakes",
                    modifier = Modifier.weight(1f)
                ) { onSelectSubTab(ExamSubTab.MISTAKES) }

                SubTabPill(
                    label = strings.subTabFlashcards,
                    icon = Icons.Default.Style,
                    selected = examState.subTab == ExamSubTab.FLASHCARDS,
                    tag = "subtab_flashcards",
                    modifier = Modifier.weight(1f)
                ) { onSelectSubTab(ExamSubTab.FLASHCARDS) }
            }

            when (examState.subTab) {
                ExamSubTab.QUIZ -> {
                    if (examState.activeQuestions.isEmpty()) {
                        QuizBuilderContent(
                            strings = strings,
                            examState = examState,
                            onSourceTextChange = onSourceTextChange,
                            onImageSelected = onImageSelected,
                            onImportLesson = onImportLesson,
                            onSetMode = onSetMode,
                            onSetDifficulty = onSetDifficulty,
                            onToggleType = onToggleType,
                            onSetCount = onSetCount,
                            onSetScore = onSetScore,
                            onSetTimer = onSetTimer,
                            onGenerateQuiz = onGenerateQuiz,
                            onTapSound = onTapSound
                        )
                    } else {
                        ActiveQuizContent(
                            strings = strings,
                            examState = examState,
                            isAnswerCorrect = isAnswerCorrect,
                            onSelectAnswer = onSelectAnswer,
                            onCheckSingle = onCheckSingle,
                            onNextQuestion = onNextQuestion,
                            onSubmitFullExam = onSubmitFullExam,
                            onResetQuiz = onResetQuiz
                        )
                    }
                }

                ExamSubTab.MISTAKES -> {
                    MistakeBankContent(
                        strings = strings,
                        mistakes = mistakes,
                        onRemoveMistake = onRemoveMistake,
                        onClearMistakes = onClearMistakes,
                        onPracticeMistakes = onPracticeMistakes
                    )
                }

                ExamSubTab.FLASHCARDS -> {
                    FlashcardsContent(
                        strings = strings,
                        flashcards = flashcards,
                        currentIndex = examState.currentFlashcardIndex,
                        isFlipped = examState.isFlashcardFlipped,
                        isGenerating = examState.isGeneratingFlashcards,
                        onGenerate = onGenerateFlashcards,
                        onFlip = onFlipFlashcard,
                        onMove = onMoveFlashcard,
                        onMastered = onToggleFlashcardMastered
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SubTabPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    tag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (selected) RoyalBlue else NavyCard,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (selected) ElectricSky else BorderNavy),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) Color.White else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) Color.White else TextSecondary,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuizBuilderContent(
    strings: AppStrings,
    examState: ExamUiState,
    onSourceTextChange: (String) -> Unit,
    onImageSelected: (Uri?) -> Unit,
    onImportLesson: () -> Unit,
    onSetMode: (QuizMode) -> Unit,
    onSetDifficulty: (QuizDifficulty) -> Unit,
    onToggleType: (QuestionType) -> Unit,
    onSetCount: (Int) -> Unit,
    onSetScore: (Int) -> Unit,
    onSetTimer: (Int) -> Unit,
    onGenerateQuiz: () -> Unit,
    onTapSound: () -> Unit
) {
    Surface(
        color = NavyCard,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, BorderNavy),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MediaCaptureRow(
                strings = strings,
                selectedImageUri = examState.selectedImageUri,
                onImagePicked = onImageSelected,
                onButtonTap = onTapSound,
                cameraTag = "exam_camera_button",
                galleryTag = "exam_gallery_button"
            )

            OutlinedButton(
                onClick = onImportLesson,
                border = BorderStroke(1.dp, ElectricSky.copy(alpha = 0.45f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("import_lesson_button")
            ) {
                Icon(Icons.Default.Download, contentDescription = strings.importFromLesson, tint = ElectricSky)
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.importFromLesson, color = ElectricSky)
            }

            OutlinedTextField(
                value = examState.sourceText,
                onValueChange = onSourceTextChange,
                placeholder = { Text(strings.examInputHint, color = TextSecondary) },
                minLines = 3,
                maxLines = 5,
                colors = darkFieldColors(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("exam_source_input")
            )

            // Mode: Full Exam vs Question-by-Question
            Text(strings.quizModeLabel, style = MaterialTheme.typography.labelLarge, color = ElectricSky)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = examState.quizMode == QuizMode.ONE_BY_ONE,
                    onClick = { onSetMode(QuizMode.ONE_BY_ONE) },
                    label = { Text(strings.oneByOne) },
                    colors = chipColors(),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = examState.quizMode == QuizMode.FULL_EXAM,
                    onClick = { onSetMode(QuizMode.FULL_EXAM) },
                    label = { Text(strings.fullExam) },
                    colors = chipColors(),
                    modifier = Modifier.weight(1f)
                )
            }

            // Difficulty
            Text(strings.difficultyLabel, style = MaterialTheme.typography.labelLarge, color = ElectricSky)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuizDifficulty.entries.forEach { diff ->
                    FilterChip(
                        selected = examState.difficulty == diff,
                        onClick = { onSetDifficulty(diff) },
                        label = { Text(strings.difficultyName(diff)) },
                        colors = chipColors()
                    )
                }
            }

            // Question types
            Text(strings.questionTypesLabel, style = MaterialTheme.typography.labelLarge, color = ElectricSky)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuestionType.entries.forEach { type ->
                    FilterChip(
                        selected = type in examState.selectedTypes,
                        onClick = { onToggleType(type) },
                        label = { Text(strings.questionTypeName(type)) },
                        colors = chipColors()
                    )
                }
            }

            // Count
            Text(strings.questionCountLabel, style = MaterialTheme.typography.labelLarge, color = ElectricSky)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(3, 5, 10, 15).forEach { cnt ->
                    FilterChip(
                        selected = examState.questionCount == cnt,
                        onClick = { onSetCount(cnt) },
                        label = { Text("$cnt") },
                        colors = chipColors()
                    )
                }
            }

            // Total Score
            Text(strings.totalScoreLabel, style = MaterialTheme.typography.labelLarge, color = ElectricSky)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(10, 20, 50, 100).forEach { pts ->
                    FilterChip(
                        selected = examState.totalScore == pts,
                        onClick = { onSetScore(pts) },
                        label = { Text("$pts") },
                        colors = chipColors()
                    )
                }
            }

            // Optional Timer
            Text(strings.timerLabel, style = MaterialTheme.typography.labelLarge, color = ElectricSky)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(0, 3, 5, 10, 15).forEach { mins ->
                    FilterChip(
                        selected = examState.timerMinutes == mins,
                        onClick = { onSetTimer(mins) },
                        label = {
                            Text(if (mins == 0) strings.timerOff else "$mins ${strings.minutesShort}")
                        },
                        colors = chipColors()
                    )
                }
            }

            Button(
                onClick = onGenerateQuiz,
                enabled = !examState.isGenerating,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue, contentColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("generate_quiz_button")
            ) {
                if (examState.isGenerating) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.generatingQuiz)
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = strings.generateQuiz)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.generateQuiz, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ActiveQuizContent(
    strings: AppStrings,
    examState: ExamUiState,
    isAnswerCorrect: (QuizQuestion, String) -> Boolean,
    onSelectAnswer: (String, String) -> Unit,
    onCheckSingle: (QuizQuestion) -> Unit,
    onNextQuestion: () -> Unit,
    onSubmitFullExam: () -> Unit,
    onResetQuiz: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Timer & Reset bar
        if (examState.timerMinutes > 0 && !examState.isSubmitted) {
            val mins = examState.remainingSeconds / 60
            val secs = examState.remainingSeconds % 60
            Surface(
                color = NavyCardElevated,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, WarmAmber.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = strings.timerLabel, tint = WarmAmber)
                        Text(
                            text = "%02d:%02d".format(mins, secs),
                            style = MaterialTheme.typography.titleMedium,
                            color = WarmAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    OutlinedButton(onClick = onResetQuiz) {
                        Text(strings.retakeOrNew, color = TextSecondary)
                    }
                }
            }
        }

        if (examState.isSubmitted) {
            val totalEarned = examState.activeQuestions.sumOf { q ->
                val ans = examState.userAnswers[q.id].orEmpty()
                if (isAnswerCorrect(q, ans)) q.points else 0
            }
            val maxPossible = examState.activeQuestions.sumOf { it.points }.coerceAtLeast(1)

            Surface(
                color = NavyCardElevated,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, ElectricSky)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(strings.finalScoreTitle, style = MaterialTheme.typography.titleLarge, color = ElectricSky)
                    Text(
                        text = "$totalEarned / $maxPossible",
                        style = MaterialTheme.typography.displaySmall,
                        color = WarmAmber,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = onResetQuiz,
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = strings.retakeOrNew)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.retakeOrNew, color = Color.White)
                    }
                }
            }

            examState.activeQuestions.forEachIndexed { idx, q ->
                QuestionCard(
                    index = idx + 1,
                    question = q,
                    selectedAnswer = examState.userAnswers[q.id].orEmpty(),
                    showFeedback = true,
                    isCorrect = isAnswerCorrect(q, examState.userAnswers[q.id].orEmpty()),
                    strings = strings,
                    onAnswerChange = {}
                )
            }
        } else if (examState.quizMode == QuizMode.ONE_BY_ONE) {
            val q = examState.activeQuestions.getOrNull(examState.currentQuestionIndex)
            if (q != null) {
                val userAns = examState.userAnswers[q.id].orEmpty()
                val checked = q.id in examState.checkedQuestionIds
                LinearProgressIndicator(
                    progress = { (examState.currentQuestionIndex + 1f) / examState.activeQuestions.size.toFloat() },
                    color = ElectricSky,
                    trackColor = NavyCard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
                QuestionCard(
                    index = examState.currentQuestionIndex + 1,
                    question = q,
                    selectedAnswer = userAns,
                    showFeedback = checked,
                    isCorrect = isAnswerCorrect(q, userAns),
                    strings = strings,
                    onAnswerChange = { if (!checked) onSelectAnswer(q.id, it) }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!checked) {
                        Button(
                            onClick = { onCheckSingle(q) },
                            enabled = userAns.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("check_answer_button")
                        ) {
                            Text(strings.checkAnswer, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onNextQuestion,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("next_question_button")
                        ) {
                            Text(strings.nextQuestion, color = DeepNavyBg, fontWeight = FontWeight.Bold)
                        }
                    }
                    OutlinedButton(
                        onClick = onResetQuiz,
                        border = BorderStroke(1.dp, BorderNavy),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text(strings.retakeOrNew, color = TextSecondary)
                    }
                }
            }
        } else {
            // FULL_EXAM
            examState.activeQuestions.forEachIndexed { idx, q ->
                QuestionCard(
                    index = idx + 1,
                    question = q,
                    selectedAnswer = examState.userAnswers[q.id].orEmpty(),
                    showFeedback = false,
                    isCorrect = false,
                    strings = strings,
                    onAnswerChange = { onSelectAnswer(q.id, it) }
                )
            }
            Button(
                onClick = onSubmitFullExam,
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_full_exam_button")
            ) {
                Text(strings.submitExam, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun QuestionCard(
    index: Int,
    question: QuizQuestion,
    selectedAnswer: String,
    showFeedback: Boolean,
    isCorrect: Boolean,
    strings: AppStrings,
    onAnswerChange: (String) -> Unit
) {
    Surface(
        color = NavyCard,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            1.dp,
            when {
                !showFeedback -> BorderNavy
                isCorrect -> EmeraldSuccess
                else -> CoralError
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "$index. ${question.prompt}",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )

            if (question.type == QuestionType.SHORT_ANSWER || question.options.isEmpty()) {
                OutlinedTextField(
                    value = selectedAnswer,
                    onValueChange = onAnswerChange,
                    enabled = !showFeedback,
                    placeholder = { Text(strings.yourAnswerHint, color = TextSecondary) },
                    colors = darkFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                question.options.forEach { option ->
                    val chosen = selectedAnswer == option
                    val bg = when {
                        showFeedback && option == question.correctAnswer -> EmeraldContainer
                        showFeedback && chosen && !isCorrect -> CoralContainer
                        chosen -> RoyalBlue.copy(alpha = 0.28f)
                        else -> DeepNavyBg.copy(alpha = 0.65f)
                    }
                    val borderColor = when {
                        showFeedback && option == question.correctAnswer -> EmeraldSuccess
                        showFeedback && chosen && !isCorrect -> CoralError
                        chosen -> ElectricSky
                        else -> BorderNavy
                    }
                    Surface(
                        onClick = { if (!showFeedback) onAnswerChange(option) },
                        color = bg,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                        )
                    }
                }
            }

            if (showFeedback) {
                Surface(
                    color = if (isCorrect) EmeraldContainer.copy(alpha = 0.6f) else CoralContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isCorrect) strings.correctBadge else strings.wrongBadge,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isCorrect) EmeraldSuccess else CoralError
                        )
                        if (!isCorrect) {
                            Text(
                                text = "${strings.correctAnswerLabel} ${question.correctAnswer}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = EmeraldSuccess,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (question.explanation.isNotBlank()) {
                            Text(
                                text = question.explanation,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MistakeBankContent(
    strings: AppStrings,
    mistakes: List<MistakeItem>,
    onRemoveMistake: (String) -> Unit,
    onClearMistakes: () -> Unit,
    onPracticeMistakes: () -> Unit
) {
    if (mistakes.isEmpty()) {
        Surface(
            color = NavyCard,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, BorderNavy),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(40.dp))
                Text(strings.mistakesEmpty, style = MaterialTheme.typography.bodyLarge, color = TextSecondary, textAlign = TextAlign.Center)
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onPracticeMistakes,
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("practice_mistakes_button")
            ) {
                Text(strings.practiceMistakes, color = Color.White)
            }
            OutlinedButton(
                onClick = onClearMistakes,
                border = BorderStroke(1.dp, CoralError.copy(alpha = 0.6f)),
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Text(strings.clearMistakes, color = CoralError)
            }
        }

        mistakes.forEach { m ->
            Surface(
                color = NavyCard,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CoralError.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = m.subject,
                            style = MaterialTheme.typography.labelMedium,
                            color = WarmAmber
                        )
                        IconButton(
                            onClick = { onRemoveMistake(m.id) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = TextSecondary)
                        }
                    }
                    Text(m.questionPrompt, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Text("${strings.yourWrongAnswer} ${m.userAnswer}", style = MaterialTheme.typography.bodySmall, color = CoralError)
                    Text("${strings.correctAnswerLabel} ${m.correctAnswer}", style = MaterialTheme.typography.bodyMedium, color = EmeraldSuccess)
                    if (m.explanation.isNotBlank()) {
                        Text(m.explanation, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun FlashcardsContent(
    strings: AppStrings,
    flashcards: List<FlashcardItem>,
    currentIndex: Int,
    isFlipped: Boolean,
    isGenerating: Boolean,
    onGenerate: () -> Unit,
    onFlip: () -> Unit,
    onMove: (Int) -> Unit,
    onMastered: (String, Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onGenerate,
            enabled = !isGenerating,
            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("generate_flashcards_button")
        ) {
            if (isGenerating) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = strings.generateFlashcards, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.generateFlashcards, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        if (flashcards.isEmpty()) {
            Surface(
                color = NavyCard,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BorderNavy),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Style, contentDescription = null, tint = ElectricSky, modifier = Modifier.size(40.dp))
                    Text(strings.flashcardsEmpty, style = MaterialTheme.typography.bodyLarge, color = TextSecondary, textAlign = TextAlign.Center)
                }
            }
        } else {
            val safeIdx = currentIndex.coerceIn(0, flashcards.lastIndex)
            val card = flashcards[safeIdx]
            val rotation by animateFloatAsState(
                targetValue = if (isFlipped) 180f else 0f,
                animationSpec = tween(320),
                label = "cardFlip"
            )

            Text(
                text = "${safeIdx + 1} / ${flashcards.size} • ${card.subject}",
                style = MaterialTheme.typography.labelLarge,
                color = ElectricSky
            )

            Surface(
                color = if (isFlipped) NavyCardElevated else NavyCard,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.5.dp, if (card.mastered) EmeraldSuccess else ElectricSky.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
                    .clickable(onClick = onFlip)
                    .testTag("flashcard_surface")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(22.dp)
                        .graphicsLayer {
                            if (rotation > 90f) rotationY = 180f
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flip,
                            contentDescription = strings.flipCardHint,
                            tint = if (isFlipped) WarmAmber else ElectricSky
                        )
                        Text(
                            text = if (rotation <= 90f) card.front else card.back,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = strings.flipCardHint,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onMove(-1) },
                    border = BorderStroke(1.dp, BorderNavy),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                ) {
                    Text(strings.prevCard, color = TextPrimary)
                }
                Button(
                    onClick = { onMastered(card.id, true) },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                ) {
                    Text(strings.masteredCard, color = DeepNavyBg, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { onMastered(card.id, false) },
                    border = BorderStroke(1.dp, WarmAmber),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                ) {
                    Text(strings.reviewAgain, color = WarmAmber)
                }
                OutlinedButton(
                    onClick = { onMove(1) },
                    border = BorderStroke(1.dp, BorderNavy),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                ) {
                    Text(strings.nextCard, color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun chipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = RoyalBlue,
    selectedLabelColor = Color.White,
    containerColor = DeepNavyBg.copy(alpha = 0.7f),
    labelColor = TextSecondary
)
