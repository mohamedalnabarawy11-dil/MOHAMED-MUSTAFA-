package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppLanguage
import com.example.data.model.AppStage
import com.example.data.model.MainTab
import com.example.ui.components.StudioBackdrop
import com.example.ui.components.StudioTopBar
import com.example.ui.components.openWhatsAppSupport
import com.example.ui.screens.DarkSplashScreen
import com.example.ui.screens.EntryGateScreen
import com.example.ui.screens.ExamScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.TeacherScreen
import com.example.ui.strings.stringsFor
import com.example.ui.theme.BorderNavy
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.ElectricSky
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavySurface
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.NabraawyStudyTheme
import com.example.viewmodel.StudyViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: StudyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val isArabic = state.persisted.language == AppLanguage.AR
            val strings = stringsFor(state.persisted.language)

            NabraawyStudyTheme(isArabic = isArabic) {
                CompositionLocalProvider(
                    LocalLayoutDirection provides if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
                ) {
                    NabraawyAppRoot(
                        viewModel = viewModel,
                        strings = strings,
                        onExitApp = { finishAffinity() }
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.audioEngine.onPause()
    }

    override fun onResume() {
        super.onResume()
        viewModel.audioEngine.onResume()
    }
}

@Composable
private fun NabraawyAppRoot(
    viewModel: StudyViewModel,
    strings: com.example.ui.strings.AppStrings,
    onExitApp: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    when (state.appStage) {
        AppStage.ENTRY_GATE -> {
            EntryGateScreen(
                strings = strings,
                onInstallAndEnter = { viewModel.onInstallAndEnterApp() },
                onExitApp = {
                    viewModel.playClick()
                    onExitApp()
                }
            )
        }

        AppStage.SPLASH_FADE -> {
            DarkSplashScreen(
                strings = strings,
                onFinished = { viewModel.onSplashFinished() }
            )
        }

        AppStage.MAIN_STUDIO -> {
            if (state.currentTab != MainTab.TEACHER) {
                BackHandler {
                    viewModel.selectTab(MainTab.TEACHER)
                }
            }

            Scaffold(
                containerColor = DeepNavyBg,
                contentWindowInsets = WindowInsets.safeDrawing,
                topBar = {
                    StudioTopBar(
                        strings = strings,
                        language = state.persisted.language,
                        musicEnabled = state.persisted.studyMusicEnabled,
                        soundEffectsEnabled = state.persisted.soundEffectsEnabled,
                        isLiveAiMode = state.isLiveAiMode,
                        onToggleMusic = { viewModel.toggleStudyMusic() },
                        onToggleSoundEffects = { viewModel.toggleSoundEffects() },
                        onToggleLanguage = { viewModel.toggleLanguage() },
                        onOpenWhatsapp = {
                            viewModel.playClick()
                            openWhatsAppSupport(context, strings.whatsappNotFound)
                        }
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = NavySurface,
                        contentColor = TextPrimary
                    ) {
                        NavigationBarItem(
                            selected = state.currentTab == MainTab.TEACHER,
                            onClick = { viewModel.selectTab(MainTab.TEACHER) },
                            icon = {
                                Icon(
                                    imageVector = if (state.currentTab == MainTab.TEACHER) Icons.Filled.School else Icons.Outlined.School,
                                    contentDescription = strings.tabTeacher
                                )
                            },
                            label = { Text(strings.tabTeacher, maxLines = 1) },
                            colors = navBarItemColors(),
                            modifier = Modifier.testTag("nav_teacher")
                        )
                        NavigationBarItem(
                            selected = state.currentTab == MainTab.EXAM,
                            onClick = { viewModel.selectTab(MainTab.EXAM) },
                            icon = {
                                Icon(
                                    imageVector = if (state.currentTab == MainTab.EXAM) Icons.Filled.Quiz else Icons.Outlined.Quiz,
                                    contentDescription = strings.tabExam
                                )
                            },
                            label = { Text(strings.tabExam, maxLines = 1) },
                            colors = navBarItemColors(),
                            modifier = Modifier.testTag("nav_exam")
                        )
                        NavigationBarItem(
                            selected = state.currentTab == MainTab.SCHEDULE,
                            onClick = { viewModel.selectTab(MainTab.SCHEDULE) },
                            icon = {
                                Icon(
                                    imageVector = if (state.currentTab == MainTab.SCHEDULE) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                                    contentDescription = strings.tabSchedule
                                )
                            },
                            label = { Text(strings.tabSchedule, maxLines = 1) },
                            colors = navBarItemColors(),
                            modifier = Modifier.testTag("nav_schedule")
                        )
                    }
                }
            ) { innerPadding ->
                StudioBackdrop(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        AnimatedVisibility(visible = state.statusBanner != null) {
                            state.statusBanner?.let { banner ->
                                Surface(
                                    onClick = { viewModel.clearStatusBanner() },
                                    color = NavyCardElevated,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, WarmAmber.copy(alpha = 0.65f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = banner,
                                        color = WarmAmber,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            when (state.currentTab) {
                                MainTab.TEACHER -> TeacherScreen(
                                    strings = strings,
                                    teacherState = state.teacher,
                                    latestLesson = state.persisted.latestLesson,
                                    onSubjectChange = viewModel::updateTeacherSubject,
                                    onTextChange = viewModel::updateTeacherText,
                                    onImageSelected = viewModel::setTeacherImage,
                                    onToggleCramMode = viewModel::toggleCramMode,
                                    onExplainClick = {
                                        viewModel.explainLesson(
                                            strings.needInputError,
                                            strings.statusRetrying
                                        )
                                    },
                                    onClarifyInputChange = viewModel::updateClarifyInput,
                                    onAskClarify = viewModel::askClarification,
                                    onTeachBackInputChange = viewModel::updateTeachBackInput,
                                    onSubmitTeachBack = viewModel::submitTeachBack,
                                    onWantMorePractice = viewModel::onWantMorePracticeFromLesson,
                                    onDonePractice = {
                                        viewModel.onDoneWithLessonPractice(strings.masterySavedMsg)
                                    },
                                    onTapSound = viewModel::playClick
                                )

                                MainTab.EXAM -> ExamScreen(
                                    strings = strings,
                                    examState = state.exam,
                                    mistakes = state.persisted.mistakeBank,
                                    flashcards = state.persisted.flashcards,
                                    isAnswerCorrect = viewModel::isAnswerCorrect,
                                    onSelectSubTab = viewModel::selectExamSubTab,
                                    onSourceTextChange = viewModel::updateExamSourceText,
                                    onImageSelected = viewModel::setExamImage,
                                    onImportLesson = viewModel::importCurrentLessonToExam,
                                    onSetMode = viewModel::setQuizMode,
                                    onSetDifficulty = viewModel::setQuizDifficulty,
                                    onToggleType = viewModel::toggleQuestionType,
                                    onSetCount = viewModel::setQuestionCount,
                                    onSetScore = viewModel::setTotalScore,
                                    onSetTimer = viewModel::setTimerMinutes,
                                    onGenerateQuiz = {
                                        viewModel.generateQuiz(
                                            strings.needInputError,
                                            strings.statusRetrying
                                        )
                                    },
                                    onSelectAnswer = viewModel::selectAnswer,
                                    onCheckSingle = viewModel::checkSingleQuestion,
                                    onNextQuestion = viewModel::nextRecitationQuestion,
                                    onSubmitFullExam = viewModel::submitFullExam,
                                    onResetQuiz = viewModel::resetActiveQuiz,
                                    onRemoveMistake = viewModel::removeMistake,
                                    onClearMistakes = viewModel::clearAllMistakes,
                                    onPracticeMistakes = viewModel::practiceMistakesAsQuiz,
                                    onGenerateFlashcards = {
                                        viewModel.generateFlashcards(strings.needInputError)
                                    },
                                    onFlipFlashcard = viewModel::flipFlashcard,
                                    onMoveFlashcard = viewModel::moveFlashcard,
                                    onToggleFlashcardMastered = viewModel::toggleFlashcardMastered,
                                    onTapSound = viewModel::playClick
                                )

                                MainTab.SCHEDULE -> ScheduleScreen(
                                    strings = strings,
                                    language = state.persisted.language,
                                    scheduleState = state.schedule,
                                    busySlots = state.persisted.busySlots,
                                    subjects = state.persisted.subjects,
                                    scheduleBlocks = state.persisted.scheduleBlocks,
                                    onBusyLabelChange = viewModel::updateNewBusyLabel,
                                    onAdjustBusyStart = viewModel::adjustBusyStartHour,
                                    onAdjustBusyEnd = viewModel::adjustBusyEndHour,
                                    onAddBusySlot = viewModel::addBusySlot,
                                    onRemoveBusySlot = viewModel::removeBusySlot,
                                    onToggleAllSubjects = viewModel::toggleAllSubjects,
                                    onToggleSubjectSelected = viewModel::toggleSubjectSelected,
                                    onCycleSubjectDifficulty = viewModel::cycleSubjectDifficulty,
                                    onNewSubjectNameChange = viewModel::updateNewSubjectName,
                                    onSetNewSubjectDifficulty = viewModel::setNewSubjectDifficulty,
                                    onAddSubject = viewModel::addSubject,
                                    onRemoveSubject = viewModel::removeSubject,
                                    onGenerateSchedule = viewModel::generateSmartSchedule,
                                    onToggleBlockCompleted = viewModel::toggleScheduleBlockCompleted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun navBarItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = ElectricSky,
    selectedTextColor = ElectricSky,
    indicatorColor = RoyalBlue.copy(alpha = 0.25f),
    unselectedIconColor = TextSecondary,
    unselectedTextColor = TextSecondary
)
