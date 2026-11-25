package com.example.data.model

import com.squareup.moshi.JsonClass

enum class AppLanguage {
    AR, EN
}

enum class AppStage {
    ENTRY_GATE,
    SPLASH_FADE,
    MAIN_STUDIO
}

enum class MainTab {
    TEACHER,
    EXAM,
    SCHEDULE
}

@JsonClass(generateAdapter = true)
data class ClarificationEntry(
    val id: String,
    val question: String,
    val answer: String
)

@JsonClass(generateAdapter = true)
data class LessonBreakdown(
    val id: String,
    val subjectTag: String,
    val sourceText: String,
    val simplifiedExplanation: String,
    val outsideConnection: String,
    val importantQuestionsRadar: List<String>,
    val mustMemorize: List<String>,
    val isCramMode: Boolean,
    val clarifications: List<ClarificationEntry> = emptyList(),
    val lastTeachBackStudentText: String = "",
    val lastTeachBackFeedback: String = "",
    val needsMorePracticePrompt: Boolean = false
)

enum class QuestionType {
    MCQ,
    TRUE_FALSE,
    SHORT_ANSWER
}

enum class QuizDifficulty {
    EASY,
    MEDIUM,
    HARD,
    MIXED
}

enum class QuizMode {
    FULL_EXAM,
    ONE_BY_ONE
}

@JsonClass(generateAdapter = true)
data class QuizQuestion(
    val id: String,
    val type: QuestionType,
    val prompt: String,
    val options: List<String> = emptyList(),
    val correctAnswer: String,
    val explanation: String,
    val points: Int = 2
)

@JsonClass(generateAdapter = true)
data class MistakeItem(
    val id: String,
    val questionPrompt: String,
    val userAnswer: String,
    val correctAnswer: String,
    val explanation: String,
    val subject: String,
    val timestampMillis: Long
)

@JsonClass(generateAdapter = true)
data class FlashcardItem(
    val id: String,
    val front: String,
    val back: String,
    val subject: String,
    val mastered: Boolean = false
)

enum class SubjectDifficulty {
    HARD,
    MEDIUM,
    EASY
}

@JsonClass(generateAdapter = true)
data class StudySubject(
    val id: String,
    val name: String,
    val difficulty: SubjectDifficulty,
    val isSelected: Boolean = true
)

@JsonClass(generateAdapter = true)
data class BusySlot(
    val id: String,
    val label: String,
    val startMinutes: Int,
    val endMinutes: Int
)

@JsonClass(generateAdapter = true)
data class ScheduleBlock(
    val id: String,
    val title: String,
    val subjectName: String,
    val difficulty: SubjectDifficulty?,
    val startMinutes: Int,
    val endMinutes: Int,
    val isBreak: Boolean = false,
    val isCompleted: Boolean = false
)
