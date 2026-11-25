package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppLanguage
import com.example.data.model.BusySlot
import com.example.data.model.FlashcardItem
import com.example.data.model.LessonBreakdown
import com.example.data.model.MistakeItem
import com.example.data.model.ScheduleBlock
import com.example.data.model.StudySubject
import com.example.data.model.SubjectDifficulty
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.studyDataStore by preferencesDataStore(name = "nabraawy_local_storage")

data class PersistedStudyState(
    val language: AppLanguage = AppLanguage.AR,
    val soundEffectsEnabled: Boolean = true,
    val studyMusicEnabled: Boolean = true,
    val installedLocally: Boolean = false,
    val customApiKey: String = "",
    val latestLesson: LessonBreakdown? = null,
    val mistakeBank: List<MistakeItem> = emptyList(),
    val flashcards: List<FlashcardItem> = emptyList(),
    val subjects: List<StudySubject> = defaultSubjects(),
    val busySlots: List<BusySlot> = defaultBusySlots(),
    val scheduleBlocks: List<ScheduleBlock> = emptyList()
)

fun defaultSubjects(): List<StudySubject> = listOf(
    StudySubject("sub_math", "الرياضيات / Math", SubjectDifficulty.HARD, true),
    StudySubject("sub_phys", "الفيزياء / Physics", SubjectDifficulty.HARD, true),
    StudySubject("sub_ar", "اللغة العربية / Arabic", SubjectDifficulty.MEDIUM, true),
    StudySubject("sub_en", "اللغة الإنجليزية / English", SubjectDifficulty.EASY, true)
)

fun defaultBusySlots(): List<BusySlot> = listOf(
    BusySlot("busy_school", "المدرسة / School", 8 * 60, 14 * 60),
    BusySlot("busy_sleep", "النوم / Sleep", 23 * 60, 24 * 60)
)

class StudyStorageRepository(private val context: Context) {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val lessonAdapter = moshi.adapter(LessonBreakdown::class.java)
    private val mistakeListAdapter = moshi.adapter<List<MistakeItem>>(
        Types.newParameterizedType(List::class.java, MistakeItem::class.java)
    )
    private val flashcardListAdapter = moshi.adapter<List<FlashcardItem>>(
        Types.newParameterizedType(List::class.java, FlashcardItem::class.java)
    )
    private val subjectListAdapter = moshi.adapter<List<StudySubject>>(
        Types.newParameterizedType(List::class.java, StudySubject::class.java)
    )
    private val busySlotListAdapter = moshi.adapter<List<BusySlot>>(
        Types.newParameterizedType(List::class.java, BusySlot::class.java)
    )
    private val scheduleListAdapter = moshi.adapter<List<ScheduleBlock>>(
        Types.newParameterizedType(List::class.java, ScheduleBlock::class.java)
    )

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val SOUND_EFFECTS = booleanPreferencesKey("sound_effects")
        val STUDY_MUSIC = booleanPreferencesKey("study_music")
        val INSTALLED_LOCALLY = booleanPreferencesKey("installed_locally")
        val CUSTOM_API_KEY = stringPreferencesKey("custom_api_key")
        val LATEST_LESSON = stringPreferencesKey("latest_lesson")
        val MISTAKE_BANK = stringPreferencesKey("mistake_bank")
        val FLASHCARDS = stringPreferencesKey("flashcards")
        val SUBJECTS = stringPreferencesKey("subjects")
        val BUSY_SLOTS = stringPreferencesKey("busy_slots")
        val SCHEDULE_BLOCKS = stringPreferencesKey("schedule_blocks")
    }

    val stateFlow: Flow<PersistedStudyState> = context.studyDataStore.data.map { prefs ->
        val lang = prefs[Keys.LANGUAGE]?.let { runCatching { AppLanguage.valueOf(it) }.getOrNull() } ?: AppLanguage.AR
        val latestLesson = prefs[Keys.LATEST_LESSON]?.let { runCatching { lessonAdapter.fromJson(it) }.getOrNull() }
        val mistakes = prefs[Keys.MISTAKE_BANK]?.let { runCatching { mistakeListAdapter.fromJson(it) }.getOrNull() } ?: emptyList()
        val flashcards = prefs[Keys.FLASHCARDS]?.let { runCatching { flashcardListAdapter.fromJson(it) }.getOrNull() } ?: emptyList()
        val subjects = prefs[Keys.SUBJECTS]?.let { runCatching { subjectListAdapter.fromJson(it) }.getOrNull() } ?: defaultSubjects()
        val busySlots = prefs[Keys.BUSY_SLOTS]?.let { runCatching { busySlotListAdapter.fromJson(it) }.getOrNull() } ?: defaultBusySlots()
        val schedule = prefs[Keys.SCHEDULE_BLOCKS]?.let { runCatching { scheduleListAdapter.fromJson(it) }.getOrNull() } ?: emptyList()

        PersistedStudyState(
            language = lang,
            soundEffectsEnabled = prefs[Keys.SOUND_EFFECTS] ?: true,
            studyMusicEnabled = prefs[Keys.STUDY_MUSIC] ?: true,
            installedLocally = prefs[Keys.INSTALLED_LOCALLY] ?: false,
            customApiKey = prefs[Keys.CUSTOM_API_KEY] ?: "",
            latestLesson = latestLesson,
            mistakeBank = mistakes,
            flashcards = flashcards,
            subjects = subjects,
            busySlots = busySlots,
            scheduleBlocks = schedule
        )
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.studyDataStore.edit { it[Keys.LANGUAGE] = language.name }
    }

    suspend fun setSoundEffectsEnabled(enabled: Boolean) {
        context.studyDataStore.edit { it[Keys.SOUND_EFFECTS] = enabled }
    }

    suspend fun setStudyMusicEnabled(enabled: Boolean) {
        context.studyDataStore.edit { it[Keys.STUDY_MUSIC] = enabled }
    }

    suspend fun setInstalledLocally(installed: Boolean) {
        context.studyDataStore.edit { it[Keys.INSTALLED_LOCALLY] = installed }
    }

    suspend fun setCustomApiKey(key: String) {
        context.studyDataStore.edit { it[Keys.CUSTOM_API_KEY] = key.trim() }
    }

    suspend fun saveLatestLesson(lesson: LessonBreakdown?) {
        context.studyDataStore.edit {
            if (lesson == null) it.remove(Keys.LATEST_LESSON)
            else it[Keys.LATEST_LESSON] = lessonAdapter.toJson(lesson)
        }
    }

    suspend fun saveMistakes(mistakes: List<MistakeItem>) {
        context.studyDataStore.edit { it[Keys.MISTAKE_BANK] = mistakeListAdapter.toJson(mistakes) }
    }

    suspend fun saveFlashcards(flashcards: List<FlashcardItem>) {
        context.studyDataStore.edit { it[Keys.FLASHCARDS] = flashcardListAdapter.toJson(flashcards) }
    }

    suspend fun saveSubjects(subjects: List<StudySubject>) {
        context.studyDataStore.edit { it[Keys.SUBJECTS] = subjectListAdapter.toJson(subjects) }
    }

    suspend fun saveBusySlots(slots: List<BusySlot>) {
        context.studyDataStore.edit { it[Keys.BUSY_SLOTS] = busySlotListAdapter.toJson(slots) }
    }

    suspend fun saveScheduleBlocks(blocks: List<ScheduleBlock>) {
        context.studyDataStore.edit { it[Keys.SCHEDULE_BLOCKS] = scheduleListAdapter.toJson(blocks) }
    }
}
