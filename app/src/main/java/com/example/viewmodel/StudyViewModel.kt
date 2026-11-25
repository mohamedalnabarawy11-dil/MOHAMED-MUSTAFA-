package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.StudyAudioEngine
import com.example.data.local.PersistedStudyState
import com.example.data.local.StudyStorageRepository
import com.example.data.model.AppLanguage
import com.example.data.model.AppStage
import com.example.data.model.BusySlot
import com.example.data.model.ClarificationEntry
import com.example.data.model.FlashcardItem
import com.example.data.model.LessonBreakdown
import com.example.data.model.MainTab
import com.example.data.model.MistakeItem
import com.example.data.model.QuestionType
import com.example.data.model.QuizDifficulty
import com.example.data.model.QuizMode
import com.example.data.model.QuizQuestion
import com.example.data.model.ScheduleBlock
import com.example.data.model.StudySubject
import com.example.data.model.SubjectDifficulty
import com.example.data.remote.AiServiceOutcome
import com.example.data.remote.ImageEncodingUtil
import com.example.data.remote.OpenRouterService
import com.example.domain.SmartScheduleGenerator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class ExamSubTab {
    QUIZ,
    MISTAKES,
    FLASHCARDS
}

data class TeacherUiState(
    val subjectTag: String = "",
    val lessonText: String = "",
    val selectedImageUri: Uri? = null,
    val isCramMode: Boolean = false,
    val isLoading: Boolean = false,
    val clarifyInput: String = "",
    val isClarifying: Boolean = false,
    val teachBackInput: String = "",
    val isEvaluatingTeachBack: Boolean = false,
    val masteryNotice: String? = null
)

data class ExamUiState(
    val subTab: ExamSubTab = ExamSubTab.QUIZ,
    val sourceText: String = "",
    val selectedImageUri: Uri? = null,
    val quizMode: QuizMode = QuizMode.ONE_BY_ONE,
    val difficulty: QuizDifficulty = QuizDifficulty.MEDIUM,
    val selectedTypes: Set<QuestionType> = setOf(QuestionType.MCQ, QuestionType.TRUE_FALSE),
    val questionCount: Int = 5,
    val totalScore: Int = 20,
    val timerMinutes: Int = 0, // 0 = off
    val remainingSeconds: Int = 0,
    val isGenerating: Boolean = false,
    val activeQuestions: List<QuizQuestion> = emptyList(),
    val userAnswers: Map<String, String> = emptyMap(),
    val checkedQuestionIds: Set<String> = emptySet(),
    val currentQuestionIndex: Int = 0,
    val isSubmitted: Boolean = false,
    val currentFlashcardIndex: Int = 0,
    val isFlashcardFlipped: Boolean = false,
    val isGeneratingFlashcards: Boolean = false
)

data class ScheduleUiState(
    val useAllSubjects: Boolean = true,
    val newBusyLabel: String = "",
    val newBusyStartHour: Int = 16,
    val newBusyEndHour: Int = 18,
    val newSubjectName: String = "",
    val newSubjectDifficulty: SubjectDifficulty = SubjectDifficulty.MEDIUM
)

data class StudioUiState(
    val persisted: PersistedStudyState = PersistedStudyState(),
    val appStage: AppStage = AppStage.ENTRY_GATE,
    val currentTab: MainTab = MainTab.TEACHER,
    val isLiveAiMode: Boolean = false,
    val statusBanner: String? = null,
    val teacher: TeacherUiState = TeacherUiState(),
    val exam: ExamUiState = ExamUiState(),
    val schedule: ScheduleUiState = ScheduleUiState()
)

class StudyViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = StudyStorageRepository(application.applicationContext)
    private val openRouter = OpenRouterService()
    val audioEngine = StudyAudioEngine()

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            storage.stateFlow.collect { saved ->
                audioEngine.setClickEnabled(saved.soundEffectsEnabled)
                // Only play music once inside splash or main studio
                val stage = _uiState.value.appStage
                audioEngine.setMusicEnabled(
                    saved.studyMusicEnabled && stage != AppStage.ENTRY_GATE
                )
                _uiState.update { it.copy(persisted = saved) }
            }
        }
    }

    fun playClick() {
        audioEngine.playClick()
    }

    fun onInstallAndEnterApp() {
        playClick()
        viewModelScope.launch {
            storage.setInstalledLocally(true)
        }
        _uiState.update { it.copy(appStage = AppStage.SPLASH_FADE) }
        audioEngine.setMusicEnabled(_uiState.value.persisted.studyMusicEnabled)
    }

    fun onSplashFinished() {
        _uiState.update { it.copy(appStage = AppStage.MAIN_STUDIO) }
        audioEngine.setMusicEnabled(_uiState.value.persisted.studyMusicEnabled)
    }

    fun selectTab(tab: MainTab) {
        playClick()
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun toggleLanguage() {
        playClick()
        val next = if (_uiState.value.persisted.language == AppLanguage.AR) {
            AppLanguage.EN
        } else {
            AppLanguage.AR
        }
        viewModelScope.launch { storage.setLanguage(next) }
    }

    fun toggleStudyMusic() {
        playClick()
        val next = !_uiState.value.persisted.studyMusicEnabled
        audioEngine.setMusicEnabled(next && _uiState.value.appStage != AppStage.ENTRY_GATE)
        viewModelScope.launch { storage.setStudyMusicEnabled(next) }
    }

    fun toggleSoundEffects() {
        val next = !_uiState.value.persisted.soundEffectsEnabled
        audioEngine.setClickEnabled(next)
        if (next) playClick()
        viewModelScope.launch { storage.setSoundEffectsEnabled(next) }
    }

    fun clearStatusBanner() {
        _uiState.update { it.copy(statusBanner = null) }
    }

    // --- Teacher Section ---

    fun updateTeacherSubject(value: String) {
        _uiState.update { it.copy(teacher = it.teacher.copy(subjectTag = value)) }
    }

    fun updateTeacherText(value: String) {
        _uiState.update { it.copy(teacher = it.teacher.copy(lessonText = value)) }
    }

    fun setTeacherImage(uri: Uri?) {
        playClick()
        _uiState.update { it.copy(teacher = it.teacher.copy(selectedImageUri = uri)) }
    }

    fun toggleCramMode(enabled: Boolean) {
        playClick()
        _uiState.update { it.copy(teacher = it.teacher.copy(isCramMode = enabled)) }
    }

    fun explainLesson(needInputErrorText: String, retryLabel: String) {
        playClick()
        val tState = _uiState.value.teacher
        if (tState.lessonText.isBlank() && tState.selectedImageUri == null) {
            _uiState.update { it.copy(statusBanner = needInputErrorText) }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    teacher = it.teacher.copy(isLoading = true, masteryNotice = null),
                    statusBanner = null
                )
            }
            val imageData = tState.selectedImageUri?.let {
                ImageEncodingUtil.encodeUriToBase64DataUrl(getApplication(), it)
            }
            val outcome = openRouter.explainLesson(
                customKey = _uiState.value.persisted.customApiKey,
                sourceText = tState.lessonText,
                subjectTag = tState.subjectTag,
                imageDataUrl = imageData,
                isCramMode = tState.isCramMode,
                language = _uiState.value.persisted.language,
                onRetryStatus = {
                    _uiState.update { s -> s.copy(statusBanner = retryLabel) }
                }
            )
            val (lesson, isLive) = when (outcome) {
                is AiServiceOutcome.LiveSuccess -> outcome.data to true
                is AiServiceOutcome.DemoFallback -> outcome.data to false
            }
            storage.saveLatestLesson(lesson)
            _uiState.update {
                it.copy(
                    isLiveAiMode = isLive,
                    statusBanner = null,
                    teacher = it.teacher.copy(isLoading = false)
                )
            }
        }
    }

    fun updateClarifyInput(value: String) {
        _uiState.update { it.copy(teacher = it.teacher.copy(clarifyInput = value)) }
    }

    fun askClarification() {
        playClick()
        val lesson = _uiState.value.persisted.latestLesson ?: return
        val question = _uiState.value.teacher.clarifyInput.trim()
        if (question.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(teacher = it.teacher.copy(isClarifying = true)) }
            val outcome = openRouter.clarifyPoint(
                customKey = _uiState.value.persisted.customApiKey,
                point = question,
                lesson = lesson,
                language = _uiState.value.persisted.language
            )
            val (answer, isLive) = when (outcome) {
                is AiServiceOutcome.LiveSuccess -> outcome.data to true
                is AiServiceOutcome.DemoFallback -> outcome.data to false
            }
            val updated = lesson.copy(
                clarifications = lesson.clarifications + ClarificationEntry(
                    id = UUID.randomUUID().toString(),
                    question = question,
                    answer = answer
                )
            )
            storage.saveLatestLesson(updated)
            _uiState.update {
                it.copy(
                    isLiveAiMode = isLive,
                    teacher = it.teacher.copy(clarifyInput = "", isClarifying = false)
                )
            }
        }
    }

    fun updateTeachBackInput(value: String) {
        _uiState.update { it.copy(teacher = it.teacher.copy(teachBackInput = value)) }
    }

    fun submitTeachBack() {
        playClick()
        val lesson = _uiState.value.persisted.latestLesson ?: return
        val studentText = _uiState.value.teacher.teachBackInput.trim()
        if (studentText.isEmpty()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(teacher = it.teacher.copy(isEvaluatingTeachBack = true, masteryNotice = null))
            }
            val outcome = openRouter.evaluateTeachBack(
                customKey = _uiState.value.persisted.customApiKey,
                studentExplanation = studentText,
                lesson = lesson,
                language = _uiState.value.persisted.language
            )
            val (feedback, isLive) = when (outcome) {
                is AiServiceOutcome.LiveSuccess -> outcome.data to true
                is AiServiceOutcome.DemoFallback -> outcome.data to false
            }
            val updated = lesson.copy(
                lastTeachBackStudentText = studentText,
                lastTeachBackFeedback = feedback,
                needsMorePracticePrompt = true
            )
            storage.saveLatestLesson(updated)
            _uiState.update {
                it.copy(
                    isLiveAiMode = isLive,
                    teacher = it.teacher.copy(
                        teachBackInput = "",
                        isEvaluatingTeachBack = false
                    )
                )
            }
        }
    }

    fun onWantMorePracticeFromLesson() {
        playClick()
        val lesson = _uiState.value.persisted.latestLesson
        val seedText = lesson?.let {
            buildString {
                if (it.sourceText.isNotBlank()) appendLine(it.sourceText)
                appendLine(it.simplifiedExplanation)
                appendLine(it.mustMemorize.joinToString("\n"))
            }
        } ?: _uiState.value.teacher.lessonText

        _uiState.update {
            it.copy(
                currentTab = MainTab.EXAM,
                exam = it.exam.copy(
                    subTab = ExamSubTab.QUIZ,
                    sourceText = seedText,
                    selectedImageUri = it.teacher.selectedImageUri
                )
            )
        }
    }

    fun onDoneWithLessonPractice(savedMsg: String) {
        playClick()
        val lesson = _uiState.value.persisted.latestLesson
        if (lesson != null) {
            viewModelScope.launch {
                storage.saveLatestLesson(lesson.copy(needsMorePracticePrompt = false))
            }
        }
        _uiState.update {
            it.copy(teacher = it.teacher.copy(masteryNotice = savedMsg))
        }
    }

    // --- Exam & Recitation Section ---

    fun selectExamSubTab(subTab: ExamSubTab) {
        playClick()
        _uiState.update { it.copy(exam = it.exam.copy(subTab = subTab)) }
    }

    fun updateExamSourceText(text: String) {
        _uiState.update { it.copy(exam = it.exam.copy(sourceText = text)) }
    }

    fun setExamImage(uri: Uri?) {
        playClick()
        _uiState.update { it.copy(exam = it.exam.copy(selectedImageUri = uri)) }
    }

    fun importCurrentLessonToExam() {
        playClick()
        val lesson = _uiState.value.persisted.latestLesson
        val text = lesson?.let {
            buildString {
                if (it.sourceText.isNotBlank()) appendLine(it.sourceText)
                appendLine(it.simplifiedExplanation)
                if (it.mustMemorize.isNotEmpty()) {
                    appendLine(it.mustMemorize.joinToString("\n"))
                }
            }.trim()
        } ?: _uiState.value.teacher.lessonText

        _uiState.update {
            it.copy(
                exam = it.exam.copy(
                    sourceText = text,
                    selectedImageUri = it.teacher.selectedImageUri
                )
            )
        }
    }

    fun setQuizMode(mode: QuizMode) {
        playClick()
        _uiState.update { it.copy(exam = it.exam.copy(quizMode = mode)) }
    }

    fun setQuizDifficulty(difficulty: QuizDifficulty) {
        playClick()
        _uiState.update { it.copy(exam = it.exam.copy(difficulty = difficulty)) }
    }

    fun toggleQuestionType(type: QuestionType) {
        playClick()
        val current = _uiState.value.exam.selectedTypes
        val updated = if (type in current) {
            if (current.size > 1) current - type else current
        } else {
            current + type
        }
        _uiState.update { it.copy(exam = it.exam.copy(selectedTypes = updated)) }
    }

    fun setQuestionCount(count: Int) {
        playClick()
        _uiState.update { it.copy(exam = it.exam.copy(questionCount = count)) }
    }

    fun setTotalScore(score: Int) {
        playClick()
        _uiState.update { it.copy(exam = it.exam.copy(totalScore = score)) }
    }

    fun setTimerMinutes(minutes: Int) {
        playClick()
        _uiState.update { it.copy(exam = it.exam.copy(timerMinutes = minutes)) }
    }

    fun generateQuiz(needInputErrorText: String, retryLabel: String) {
        playClick()
        val eState = _uiState.value.exam
        if (eState.sourceText.isBlank() && eState.selectedImageUri == null) {
            _uiState.update { it.copy(statusBanner = needInputErrorText) }
            return
        }

        timerJob?.cancel()
        viewModelScope.launch {
            _uiState.update { it.copy(exam = it.exam.copy(isGenerating = true), statusBanner = null) }
            val imageData = eState.selectedImageUri?.let {
                ImageEncodingUtil.encodeUriToBase64DataUrl(getApplication(), it)
            }
            val outcome = openRouter.generateQuiz(
                customKey = _uiState.value.persisted.customApiKey,
                content = eState.sourceText,
                imageDataUrl = imageData,
                types = eState.selectedTypes,
                difficulty = eState.difficulty,
                count = eState.questionCount,
                totalScore = eState.totalScore,
                language = _uiState.value.persisted.language,
                onRetryStatus = { _uiState.update { s -> s.copy(statusBanner = retryLabel) } }
            )
            val (questions, isLive) = when (outcome) {
                is AiServiceOutcome.LiveSuccess -> outcome.data to true
                is AiServiceOutcome.DemoFallback -> outcome.data to false
            }

            val totalSecs = eState.timerMinutes * 60
            _uiState.update {
                it.copy(
                    isLiveAiMode = isLive,
                    statusBanner = null,
                    exam = it.exam.copy(
                        isGenerating = false,
                        activeQuestions = questions,
                        userAnswers = emptyMap(),
                        checkedQuestionIds = emptySet(),
                        currentQuestionIndex = 0,
                        isSubmitted = false,
                        remainingSeconds = totalSecs
                    )
                )
            }
            if (totalSecs > 0 && questions.isNotEmpty()) {
                startQuizCountdown()
            }
        }
    }

    private fun startQuizCountdown() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.exam.remainingSeconds > 0 && !_uiState.value.exam.isSubmitted) {
                delay(1000)
                val next = (_uiState.value.exam.remainingSeconds - 1).coerceAtLeast(0)
                _uiState.update { it.copy(exam = it.exam.copy(remainingSeconds = next)) }
                if (next == 0) {
                    submitFullExam()
                }
            }
        }
    }

    fun selectAnswer(questionId: String, answer: String) {
        playClick()
        _uiState.update {
            it.copy(
                exam = it.exam.copy(
                    userAnswers = it.exam.userAnswers + (questionId to answer)
                )
            )
        }
    }

    fun checkSingleQuestion(question: QuizQuestion) {
        playClick()
        val userAns = _uiState.value.exam.userAnswers[question.id]?.trim().orEmpty()
        if (userAns.isEmpty()) return

        if (!isAnswerCorrect(question, userAns)) {
            recordMistake(question, userAns)
        }
        _uiState.update {
            it.copy(
                exam = it.exam.copy(
                    checkedQuestionIds = it.exam.checkedQuestionIds + question.id
                )
            )
        }
    }

    fun nextRecitationQuestion() {
        playClick()
        val exam = _uiState.value.exam
        if (exam.currentQuestionIndex + 1 < exam.activeQuestions.size) {
            _uiState.update {
                it.copy(exam = it.exam.copy(currentQuestionIndex = exam.currentQuestionIndex + 1))
            }
        } else {
            timerJob?.cancel()
            _uiState.update { it.copy(exam = it.exam.copy(isSubmitted = true)) }
        }
    }

    fun submitFullExam() {
        playClick()
        timerJob?.cancel()
        val exam = _uiState.value.exam
        val wrongQuestions = exam.activeQuestions.filter { q ->
            val ans = exam.userAnswers[q.id]?.trim().orEmpty()
            !isAnswerCorrect(q, ans)
        }
        wrongQuestions.forEach { q ->
            val ans = exam.userAnswers[q.id]?.trim().orEmpty().ifEmpty { "-" }
            recordMistake(q, ans)
        }
        _uiState.update {
            it.copy(
                exam = it.exam.copy(
                    isSubmitted = true,
                    checkedQuestionIds = exam.activeQuestions.map { q -> q.id }.toSet()
                )
            )
        }
    }

    fun resetActiveQuiz() {
        playClick()
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                exam = it.exam.copy(
                    activeQuestions = emptyList(),
                    userAnswers = emptyMap(),
                    checkedQuestionIds = emptySet(),
                    currentQuestionIndex = 0,
                    isSubmitted = false,
                    remainingSeconds = 0
                )
            )
        }
    }

    fun isAnswerCorrect(question: QuizQuestion, userAnswer: String): Boolean {
        val cleanUser = userAnswer.trim().lowercase()
        val cleanCorrect = question.correctAnswer.trim().lowercase()
        if (cleanUser.isEmpty()) return false
        return if (question.type == QuestionType.SHORT_ANSWER) {
            cleanUser == cleanCorrect ||
                (cleanUser.length >= 4 && cleanCorrect.contains(cleanUser)) ||
                (cleanCorrect.length >= 4 && cleanUser.contains(cleanCorrect.take(12)))
        } else {
            cleanUser == cleanCorrect
        }
    }

    private fun recordMistake(question: QuizQuestion, userAns: String) {
        val subj = _uiState.value.persisted.latestLesson?.subjectTag
            ?: if (_uiState.value.persisted.language == AppLanguage.AR) "مادة الدرس" else "Study"
        val item = MistakeItem(
            id = UUID.randomUUID().toString(),
            questionPrompt = question.prompt,
            userAnswer = userAns,
            correctAnswer = question.correctAnswer,
            explanation = question.explanation,
            subject = subj,
            timestampMillis = System.currentTimeMillis()
        )
        val updated = (listOf(item) + _uiState.value.persisted.mistakeBank)
            .distinctBy { it.questionPrompt }
            .take(50)
        viewModelScope.launch { storage.saveMistakes(updated) }
    }

    fun removeMistake(id: String) {
        playClick()
        val updated = _uiState.value.persisted.mistakeBank.filterNot { it.id == id }
        viewModelScope.launch { storage.saveMistakes(updated) }
    }

    fun clearAllMistakes() {
        playClick()
        viewModelScope.launch { storage.saveMistakes(emptyList()) }
    }

    fun practiceMistakesAsQuiz() {
        playClick()
        val mistakes = _uiState.value.persisted.mistakeBank
        if (mistakes.isEmpty()) return
        val questions = mistakes.take(10).mapIndexed { idx, m ->
            QuizQuestion(
                id = "mistake_drill_${idx}_${m.id}",
                type = QuestionType.SHORT_ANSWER,
                prompt = m.questionPrompt,
                options = emptyList(),
                correctAnswer = m.correctAnswer,
                explanation = m.explanation,
                points = 2
            )
        }
        _uiState.update {
            it.copy(
                exam = it.exam.copy(
                    subTab = ExamSubTab.QUIZ,
                    quizMode = QuizMode.ONE_BY_ONE,
                    activeQuestions = questions,
                    userAnswers = emptyMap(),
                    checkedQuestionIds = emptySet(),
                    currentQuestionIndex = 0,
                    isSubmitted = false,
                    remainingSeconds = 0
                )
            )
        }
    }

    // --- Flashcards ---

    fun generateFlashcards(needInputErrorText: String) {
        playClick()
        val content = _uiState.value.exam.sourceText.ifBlank {
            _uiState.value.persisted.latestLesson?.let {
                "${it.sourceText}\n${it.simplifiedExplanation}\n${it.mustMemorize.joinToString("\n")}"
            }.orEmpty()
        }
        if (content.isBlank()) {
            _uiState.update { it.copy(statusBanner = needInputErrorText) }
            return
        }

        val subj = _uiState.value.persisted.latestLesson?.subjectTag.orEmpty()
        viewModelScope.launch {
            _uiState.update { it.copy(exam = it.exam.copy(isGeneratingFlashcards = true)) }
            val outcome = openRouter.generateFlashcards(
                customKey = _uiState.value.persisted.customApiKey,
                content = content,
                subject = subj,
                language = _uiState.value.persisted.language
            )
            val (cards, isLive) = when (outcome) {
                is AiServiceOutcome.LiveSuccess -> outcome.data to true
                is AiServiceOutcome.DemoFallback -> outcome.data to false
            }
            storage.saveFlashcards(cards)
            _uiState.update {
                it.copy(
                    isLiveAiMode = isLive,
                    exam = it.exam.copy(
                        isGeneratingFlashcards = false,
                        currentFlashcardIndex = 0,
                        isFlashcardFlipped = false
                    )
                )
            }
        }
    }

    fun flipFlashcard() {
        playClick()
        _uiState.update {
            it.copy(exam = it.exam.copy(isFlashcardFlipped = !it.exam.isFlashcardFlipped))
        }
    }

    fun moveFlashcard(delta: Int) {
        playClick()
        val count = _uiState.value.persisted.flashcards.size
        if (count == 0) return
        val next = (_uiState.value.exam.currentFlashcardIndex + delta + count) % count
        _uiState.update {
            it.copy(exam = it.exam.copy(currentFlashcardIndex = next, isFlashcardFlipped = false))
        }
    }

    fun toggleFlashcardMastered(id: String, mastered: Boolean) {
        playClick()
        val updated = _uiState.value.persisted.flashcards.map {
            if (it.id == id) it.copy(mastered = mastered) else it
        }
        viewModelScope.launch { storage.saveFlashcards(updated) }
        moveFlashcard(1)
    }

    // --- Smart Schedule Section ---

    fun updateNewBusyLabel(label: String) {
        _uiState.update { it.copy(schedule = it.schedule.copy(newBusyLabel = label)) }
    }

    fun adjustBusyStartHour(delta: Int) {
        playClick()
        val next = (_uiState.value.schedule.newBusyStartHour + delta).coerceIn(0, 22)
        val end = _uiState.value.schedule.newBusyEndHour.coerceAtLeast(next + 1)
        _uiState.update {
            it.copy(schedule = it.schedule.copy(newBusyStartHour = next, newBusyEndHour = end))
        }
    }

    fun adjustBusyEndHour(delta: Int) {
        playClick()
        val start = _uiState.value.schedule.newBusyStartHour
        val next = (_uiState.value.schedule.newBusyEndHour + delta).coerceIn(start + 1, 24)
        _uiState.update {
            it.copy(schedule = it.schedule.copy(newBusyEndHour = next))
        }
    }

    fun addBusySlot() {
        playClick()
        val s = _uiState.value.schedule
        val defaultLabel = if (_uiState.value.persisted.language == AppLanguage.AR) "موعد مشغول" else "Busy"
        val slot = BusySlot(
            id = UUID.randomUUID().toString(),
            label = s.newBusyLabel.trim().ifEmpty { defaultLabel },
            startMinutes = s.newBusyStartHour * 60,
            endMinutes = s.newBusyEndHour * 60
        )
        val updated = _uiState.value.persisted.busySlots + slot
        viewModelScope.launch { storage.saveBusySlots(updated) }
        _uiState.update { it.copy(schedule = it.schedule.copy(newBusyLabel = "")) }
    }

    fun removeBusySlot(id: String) {
        playClick()
        val updated = _uiState.value.persisted.busySlots.filterNot { it.id == id }
        viewModelScope.launch { storage.saveBusySlots(updated) }
    }

    fun toggleAllSubjects(all: Boolean) {
        playClick()
        _uiState.update { it.copy(schedule = it.schedule.copy(useAllSubjects = all)) }
    }

    fun toggleSubjectSelected(id: String) {
        playClick()
        val updated = _uiState.value.persisted.subjects.map {
            if (it.id == id) it.copy(isSelected = !it.isSelected) else it
        }
        viewModelScope.launch { storage.saveSubjects(updated) }
    }

    fun cycleSubjectDifficulty(id: String) {
        playClick()
        val updated = _uiState.value.persisted.subjects.map {
            if (it.id == id) {
                val nextDiff = when (it.difficulty) {
                    SubjectDifficulty.HARD -> SubjectDifficulty.MEDIUM
                    SubjectDifficulty.MEDIUM -> SubjectDifficulty.EASY
                    SubjectDifficulty.EASY -> SubjectDifficulty.HARD
                }
                it.copy(difficulty = nextDiff)
            } else it
        }
        viewModelScope.launch { storage.saveSubjects(updated) }
    }

    fun updateNewSubjectName(name: String) {
        _uiState.update { it.copy(schedule = it.schedule.copy(newSubjectName = name)) }
    }

    fun setNewSubjectDifficulty(diff: SubjectDifficulty) {
        playClick()
        _uiState.update { it.copy(schedule = it.schedule.copy(newSubjectDifficulty = diff)) }
    }

    fun addSubject() {
        playClick()
        val s = _uiState.value.schedule
        val name = s.newSubjectName.trim()
        if (name.isEmpty()) return
        val newSubj = StudySubject(
            id = UUID.randomUUID().toString(),
            name = name,
            difficulty = s.newSubjectDifficulty,
            isSelected = true
        )
        val updated = _uiState.value.persisted.subjects + newSubj
        viewModelScope.launch { storage.saveSubjects(updated) }
        _uiState.update { it.copy(schedule = it.schedule.copy(newSubjectName = "")) }
    }

    fun removeSubject(id: String) {
        playClick()
        val updated = _uiState.value.persisted.subjects.filterNot { it.id == id }
        viewModelScope.launch { storage.saveSubjects(updated) }
    }

    fun generateSmartSchedule() {
        playClick()
        val persisted = _uiState.value.persisted
        val blocks = SmartScheduleGenerator.generateDailyPlan(
            subjects = persisted.subjects,
            useAllSubjects = _uiState.value.schedule.useAllSubjects,
            busySlots = persisted.busySlots,
            language = persisted.language
        )
        viewModelScope.launch { storage.saveScheduleBlocks(blocks) }
    }

    fun toggleScheduleBlockCompleted(id: String) {
        playClick()
        val updated = _uiState.value.persisted.scheduleBlocks.map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
        }
        viewModelScope.launch { storage.saveScheduleBlocks(updated) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        audioEngine.release()
    }
}
