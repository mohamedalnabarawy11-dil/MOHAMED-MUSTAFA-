package com.example.domain

import com.example.data.model.AppLanguage
import com.example.data.model.BusySlot
import com.example.data.model.ScheduleBlock
import com.example.data.model.StudySubject
import com.example.data.model.SubjectDifficulty
import java.util.UUID

object SmartScheduleGenerator {

    fun generateDailyPlan(
        subjects: List<StudySubject>,
        useAllSubjects: Boolean,
        busySlots: List<BusySlot>,
        language: AppLanguage
    ): List<ScheduleBlock> {
        val chosenSubjects = (if (useAllSubjects) subjects else subjects.filter { it.isSelected })
            .ifEmpty { subjects.take(1) }
        if (chosenSubjects.isEmpty()) return emptyList()

        // Priority: Hard first (longer duration), then Medium, then Easy
        val sortedSubjects = chosenSubjects.sortedBy {
            when (it.difficulty) {
                SubjectDifficulty.HARD -> 0
                SubjectDifficulty.MEDIUM -> 1
                SubjectDifficulty.EASY -> 2
            }
        }

        // Day study bounds: 06:00 (360) to 23:30 (1410)
        val dayStart = 6 * 60
        val dayEnd = 23 * 60 + 30

        val mergedBusy = mergeBusyIntervals(busySlots, dayStart, dayEnd)
        val freeWindows = computeFreeWindows(mergedBusy, dayStart, dayEnd)
            .sortedByDescending { windowQuietScore(it.first, it.second) }
            .sortedBy { it.first } // Keep chronological readability while picking clean gaps

        val result = mutableListOf<ScheduleBlock>()
        val breakTitle = if (language == AppLanguage.AR) "استراحة وتجديد نشاط" else "Rest & Recharge Break"

        var subjectIdx = 0
        val rounds = if (sortedSubjects.size <= 2) 2 else 1
        val queue = buildList {
            repeat(rounds) { addAll(sortedSubjects) }
        }

        for (window in freeWindows) {
            var cursor = window.first
            val windowEnd = window.second

            while (subjectIdx < queue.size && cursor + 25 <= windowEnd) {
                val subj = queue[subjectIdx]
                val desiredDuration = when (subj.difficulty) {
                    SubjectDifficulty.HARD -> 75
                    SubjectDifficulty.MEDIUM -> 50
                    SubjectDifficulty.EASY -> 35
                }
                val available = windowEnd - cursor
                val sessionDuration = desiredDuration.coerceAtMost(available)
                if (sessionDuration < 25) break

                val sessionEnd = cursor + sessionDuration
                result.add(
                    ScheduleBlock(
                        id = UUID.randomUUID().toString(),
                        title = subj.name,
                        subjectName = subj.name,
                        difficulty = subj.difficulty,
                        startMinutes = cursor,
                        endMinutes = sessionEnd,
                        isBreak = false,
                        isCompleted = false
                    )
                )
                cursor = sessionEnd
                subjectIdx++

                // Add explicit break after study block if room remains in this window
                val breakDuration = if (subj.difficulty == SubjectDifficulty.HARD) 15 else 10
                if (subjectIdx < queue.size && cursor + breakDuration + 25 <= windowEnd) {
                    result.add(
                        ScheduleBlock(
                            id = UUID.randomUUID().toString(),
                            title = breakTitle,
                            subjectName = breakTitle,
                            difficulty = null,
                            startMinutes = cursor,
                            endMinutes = cursor + breakDuration,
                            isBreak = true,
                            isCompleted = false
                        )
                    )
                    cursor += breakDuration
                }
            }
        }

        return result.sortedBy { it.startMinutes }
    }

    private fun mergeBusyIntervals(
        slots: List<BusySlot>,
        dayStart: Int,
        dayEnd: Int
    ): List<Pair<Int, Int>> {
        val clamped = slots.mapNotNull { slot ->
            val s = slot.startMinutes.coerceIn(dayStart, dayEnd)
            val e = slot.endMinutes.coerceIn(dayStart, dayEnd)
            if (e > s) s to e else null
        }.sortedBy { it.first }

        if (clamped.isEmpty()) return emptyList()
        val merged = mutableListOf<Pair<Int, Int>>()
        var current = clamped.first()
        for (i in 1 until clamped.size) {
            val next = clamped[i]
            if (next.first <= current.second) {
                current = current.first to maxOf(current.second, next.second)
            } else {
                merged.add(current)
                current = next
            }
        }
        merged.add(current)
        return merged
    }

    private fun computeFreeWindows(
        busy: List<Pair<Int, Int>>,
        dayStart: Int,
        dayEnd: Int
    ): List<Pair<Int, Int>> {
        val windows = mutableListOf<Pair<Int, Int>>()
        var cursor = dayStart
        for (b in busy) {
            if (b.first > cursor) {
                windows.add(cursor to b.first)
            }
            cursor = maxOf(cursor, b.second)
        }
        if (cursor < dayEnd) {
            windows.add(cursor to dayEnd)
        }
        return windows.filter { it.second - it.first >= 25 }
    }

    private fun windowQuietScore(start: Int, end: Int): Int {
        val mid = (start + end) / 2
        return when (mid) {
            in (6 * 60)..(10 * 60) -> 3 // Quiet early morning
            in (16 * 60)..(21 * 60) -> 2 // Calm evening focus
            else -> 1
        }
    }

    fun formatMinutes(totalMinutes: Int, isArabic: Boolean): String {
        val clamped = totalMinutes.coerceIn(0, 24 * 60)
        val hour24 = (clamped / 60) % 24
        val min = clamped % 60
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        val suffix = if (isArabic) {
            if (hour24 < 12) "ص" else "م"
        } else {
            if (hour24 < 12) "AM" else "PM"
        }
        return "%d:%02d %s".format(hour12, min, suffix)
    }
}
