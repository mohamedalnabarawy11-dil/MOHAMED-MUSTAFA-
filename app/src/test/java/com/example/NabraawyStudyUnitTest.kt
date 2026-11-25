package com.example

import com.example.data.model.AppLanguage
import com.example.data.model.BusySlot
import com.example.data.model.QuestionType
import com.example.data.model.QuizDifficulty
import com.example.data.model.StudySubject
import com.example.data.model.SubjectDifficulty
import com.example.data.remote.DemoStudyEngine
import com.example.domain.SmartScheduleGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NabraawyStudyUnitTest {

    @Test
    fun `smart schedule excludes busy slots and gives longer duration to hard subjects`() {
        val busy = listOf(BusySlot("b1", "School", 8 * 60, 14 * 60))
        val subjects = listOf(
            StudySubject("s1", "Physics", SubjectDifficulty.HARD, true),
            StudySubject("s2", "English", SubjectDifficulty.EASY, true)
        )
        val plan = SmartScheduleGenerator.generateDailyPlan(
            subjects = subjects,
            useAllSubjects = true,
            busySlots = busy,
            language = AppLanguage.AR
        )
        assertTrue(plan.isNotEmpty())
        val studyBlocks = plan.filter { !it.isBreak }
        val breakBlocks = plan.filter { it.isBreak }
        assertTrue(breakBlocks.isNotEmpty())

        // No block overlaps 08:00-14:00
        for (block in plan) {
            val overlapsBusy = block.startMinutes < 14 * 60 && block.endMinutes > 8 * 60
            assertFalse("Block overlapped busy interval: $block", overlapsBusy)
        }

        val hardDuration = studyBlocks.first { it.subjectName == "Physics" }.let { it.endMinutes - it.startMinutes }
        val easyDuration = studyBlocks.first { it.subjectName == "English" }.let { it.endMinutes - it.startMinutes }
        assertTrue("Hard subject should have longer block than easy", hardDuration > easyDuration)
    }

    @Test
    fun `demo teacher teach-back ends with exact follow-up question`() {
        val lesson = DemoStudyEngine.buildLessonBreakdown(
            sourceText = "قانون نيوتن الأول ينص على أن الجسم الساكن يبقى ساكناً ما لم تؤثر عليه قوة خارجية.",
            subjectTag = "فيزياء",
            hasImage = false,
            isCramMode = false,
            language = AppLanguage.AR
        )
        val feedback = DemoStudyEngine.evaluateTeachBack(
            studentExplanation = "يعني الجسم بيفضل على حالته إلا لو جت قوة تغيره",
            lesson = lesson,
            language = AppLanguage.AR
        )
        assertTrue(feedback.contains("عايز تدريب كمان ولا تمام كده؟"))
    }

    @Test
    fun `demo quiz generates requested count and types grounded in content`() {
        val questions = DemoStudyEngine.buildQuizFromContent(
            content = "التمثيل الضوئي عملية تحول الطاقة الضوئية إلى طاقة كيميائية. يحدث التمثيل الضوئي في البلاستيدات الخضراء.",
            hasImage = false,
            types = setOf(QuestionType.MCQ, QuestionType.TRUE_FALSE),
            difficulty = QuizDifficulty.MEDIUM,
            count = 4,
            totalScore = 20,
            language = AppLanguage.AR
        )
        assertEquals(4, questions.size)
        assertTrue(questions.any { it.type == QuestionType.MCQ })
        assertTrue(questions.any { it.type == QuestionType.TRUE_FALSE })
    }
}
