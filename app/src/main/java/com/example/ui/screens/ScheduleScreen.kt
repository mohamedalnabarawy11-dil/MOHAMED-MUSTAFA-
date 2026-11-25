package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.model.AppLanguage
import com.example.data.model.BusySlot
import com.example.data.model.ScheduleBlock
import com.example.data.model.StudySubject
import com.example.data.model.SubjectDifficulty
import com.example.domain.SmartScheduleGenerator
import com.example.ui.strings.AppStrings
import com.example.ui.theme.BorderNavy
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
import com.example.viewmodel.ScheduleUiState

@Composable
fun ScheduleScreen(
    strings: AppStrings,
    language: AppLanguage,
    scheduleState: ScheduleUiState,
    busySlots: List<BusySlot>,
    subjects: List<StudySubject>,
    scheduleBlocks: List<ScheduleBlock>,
    onBusyLabelChange: (String) -> Unit,
    onAdjustBusyStart: (Int) -> Unit,
    onAdjustBusyEnd: (Int) -> Unit,
    onAddBusySlot: () -> Unit,
    onRemoveBusySlot: (String) -> Unit,
    onToggleAllSubjects: (Boolean) -> Unit,
    onToggleSubjectSelected: (String) -> Unit,
    onCycleSubjectDifficulty: (String) -> Unit,
    onNewSubjectNameChange: (String) -> Unit,
    onSetNewSubjectDifficulty: (SubjectDifficulty) -> Unit,
    onAddSubject: () -> Unit,
    onRemoveSubject: (String) -> Unit,
    onGenerateSchedule: () -> Unit,
    onToggleBlockCompleted: (String) -> Unit
) {
    val isArabic = language == AppLanguage.AR

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
            // Hero Header
            Surface(
                color = NavyCard,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BorderNavy)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = ElectricSky.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = strings.scheduleHeroTitle,
                            tint = ElectricSky,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(strings.scheduleHeroTitle, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                        Text(strings.scheduleHeroSub, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }

            // Busy Times Card
            Surface(
                color = NavyCard,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BorderNavy)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(strings.busyTimesTitle, style = MaterialTheme.typography.titleMedium, color = WarmAmber)

                    busySlots.forEach { slot ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DeepNavyBg.copy(alpha = 0.65f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(slot.label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                Text(
                                    text = "${SmartScheduleGenerator.formatMinutes(slot.startMinutes, isArabic)} - ${SmartScheduleGenerator.formatMinutes(slot.endMinutes, isArabic)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            IconButton(
                                onClick = { onRemoveBusySlot(slot.id) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = CoralError)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = scheduleState.newBusyLabel,
                        onValueChange = onBusyLabelChange,
                        placeholder = { Text(strings.busyLabelHint, color = TextSecondary) },
                        singleLine = true,
                        colors = darkFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HourStepper(
                            label = strings.startHour,
                            hour = scheduleState.newBusyStartHour,
                            isArabic = isArabic,
                            onDec = { onAdjustBusyStart(-1) },
                            onInc = { onAdjustBusyStart(1) },
                            modifier = Modifier.weight(1f)
                        )
                        HourStepper(
                            label = strings.endHour,
                            hour = scheduleState.newBusyEndHour,
                            isArabic = isArabic,
                            onDec = { onAdjustBusyEnd(-1) },
                            onInc = { onAdjustBusyEnd(1) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedButton(
                        onClick = onAddBusySlot,
                        border = BorderStroke(1.dp, WarmAmber.copy(alpha = 0.65f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("add_busy_slot_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = strings.addBusySlot, tint = WarmAmber)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.addBusySlot, color = WarmAmber)
                    }
                }
            }

            // Subjects Card
            Surface(
                color = NavyCard,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BorderNavy)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(strings.subjectsTitle, style = MaterialTheme.typography.titleMedium, color = ElectricSky)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(strings.allSubjectsToggle, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = scheduleState.useAllSubjects,
                                onCheckedChange = onToggleAllSubjects,
                                colors = SwitchDefaults.colors(checkedTrackColor = RoyalBlue)
                            )
                        }
                    }

                    subjects.forEach { subj ->
                        val diffColor = when (subj.difficulty) {
                            SubjectDifficulty.HARD -> CoralError
                            SubjectDifficulty.MEDIUM -> WarmAmber
                            SubjectDifficulty.EASY -> EmeraldSuccess
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DeepNavyBg.copy(alpha = 0.65f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!scheduleState.useAllSubjects) {
                                Checkbox(
                                    checked = subj.isSelected,
                                    onCheckedChange = { onToggleSubjectSelected(subj.id) },
                                    colors = CheckboxDefaults.colors(checkedColor = RoyalBlue)
                                )
                            } else {
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = subj.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                onClick = { onCycleSubjectDifficulty(subj.id) },
                                color = diffColor.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, diffColor.copy(alpha = 0.6f)),
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = strings.subjectDifficultyName(subj.difficulty),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = diffColor
                                    )
                                }
                            }
                            IconButton(
                                onClick = { onRemoveSubject(subj.id) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextSecondary)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = scheduleState.newSubjectName,
                            onValueChange = onNewSubjectNameChange,
                            placeholder = { Text(strings.addSubjectHint, color = TextSecondary) },
                            singleLine = true,
                            colors = darkFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = onAddSubject,
                            enabled = scheduleState.newSubjectName.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Text(strings.addSubjectBtn, color = Color.White)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SubjectDifficulty.entries.forEach { diff ->
                            FilterChip(
                                selected = scheduleState.newSubjectDifficulty == diff,
                                onClick = { onSetNewSubjectDifficulty(diff) },
                                label = { Text(strings.subjectDifficultyName(diff)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoyalBlue,
                                    selectedLabelColor = Color.White,
                                    containerColor = DeepNavyBg,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    Button(
                        onClick = onGenerateSchedule,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("generate_schedule_button")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = strings.generateScheduleBtn, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.generateScheduleBtn, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Generated Plan Timeline
            Text(strings.todayPlanTitle, style = MaterialTheme.typography.titleLarge, color = TextPrimary)

            if (scheduleBlocks.isEmpty()) {
                Surface(
                    color = NavyCard,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, BorderNavy),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = strings.noScheduleYet,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            } else {
                val studyOnly = scheduleBlocks.filter { !it.isBreak }
                val completedCount = studyOnly.count { it.isCompleted }
                val progress = if (studyOnly.isEmpty()) 0f else completedCount.toFloat() / studyOnly.size.toFloat()

                Surface(
                    color = NavyCardElevated,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ElectricSky.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(strings.completedProgress, style = MaterialTheme.typography.labelLarge, color = ElectricSky)
                            Text("$completedCount / ${studyOnly.size}", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            color = EmeraldSuccess,
                            trackColor = DeepNavyBg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                }

                scheduleBlocks.forEach { block ->
                    val timeRange = "${SmartScheduleGenerator.formatMinutes(block.startMinutes, isArabic)} - ${SmartScheduleGenerator.formatMinutes(block.endMinutes, isArabic)}"
                    val durationMins = block.endMinutes - block.startMinutes

                    if (block.isBreak) {
                        Surface(
                            color = EmeraldContainer.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Coffee, contentDescription = strings.restBreakLabel, tint = EmeraldSuccess)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(block.title, style = MaterialTheme.typography.titleMedium, color = EmeraldSuccess)
                                    Text("$timeRange (${durationMins} ${strings.minutesShort})", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                        }
                    } else {
                        val accent = when (block.difficulty) {
                            SubjectDifficulty.HARD -> CoralError
                            SubjectDifficulty.MEDIUM -> WarmAmber
                            else -> ElectricSky
                        }
                        Surface(
                            onClick = { onToggleBlockCompleted(block.id) },
                            color = if (block.isCompleted) NavyCard.copy(alpha = 0.55f) else NavyCard,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, if (block.isCompleted) EmeraldSuccess else accent.copy(alpha = 0.55f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (block.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = block.title,
                                    tint = if (block.isCompleted) EmeraldSuccess else accent
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = block.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary,
                                        textDecoration = if (block.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Schedule, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                        Text(
                                            text = "$timeRange • ${durationMins}${strings.minutesShort}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                block.difficulty?.let { diff ->
                                    Text(
                                        text = strings.subjectDifficultyName(diff),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = accent
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HourStepper(
    label: String,
    hour: Int,
    isArabic: Boolean,
    onDec: () -> Unit,
    onInc: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DeepNavyBg.copy(alpha = 0.7f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderNavy),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onDec, modifier = Modifier.minimumInteractiveComponentSize()) {
                Text("-", color = ElectricSky, style = MaterialTheme.typography.titleLarge)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Text(
                    text = SmartScheduleGenerator.formatMinutes(hour * 60, isArabic),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }
            IconButton(onClick = onInc, modifier = Modifier.minimumInteractiveComponentSize()) {
                Text("+", color = ElectricSky, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}
