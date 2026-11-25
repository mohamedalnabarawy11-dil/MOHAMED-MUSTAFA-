package com.example.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.LessonBreakdown
import com.example.ui.components.MediaCaptureRow
import com.example.ui.strings.AppStrings
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.BorderNavy
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.ElectricSky
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletContainer
import com.example.ui.theme.VioletInsight
import com.example.ui.theme.WarmAmber
import com.example.viewmodel.TeacherUiState

@Composable
fun TeacherScreen(
    strings: AppStrings,
    teacherState: TeacherUiState,
    latestLesson: LessonBreakdown?,
    onSubjectChange: (String) -> Unit,
    onTextChange: (String) -> Unit,
    onImageSelected: (Uri?) -> Unit,
    onToggleCramMode: (Boolean) -> Unit,
    onExplainClick: () -> Unit,
    onClarifyInputChange: (String) -> Unit,
    onAskClarify: () -> Unit,
    onTeachBackInputChange: (String) -> Unit,
    onSubmitTeachBack: () -> Unit,
    onWantMorePractice: () -> Unit,
    onDonePractice: () -> Unit,
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
            // Hero Banner Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(144.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, BorderNavy, RoundedCornerShape(22.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_study_hero_1795635766120),
                    contentDescription = strings.teacherHeroTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    DeepNavyBg.copy(alpha = 0.28f),
                                    DeepNavyBg.copy(alpha = 0.88f)
                                )
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = ElectricSky.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = strings.teacherHeroTitle,
                            tint = ElectricSky,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.teacherHeroTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = strings.teacherHeroSub,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 2
                        )
                    }
                }
            }

            // Input Card
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
                        selectedImageUri = teacherState.selectedImageUri,
                        onImagePicked = onImageSelected,
                        onButtonTap = onTapSound,
                        cameraTag = "teacher_camera_button",
                        galleryTag = "teacher_gallery_button"
                    )

                    OutlinedTextField(
                        value = teacherState.subjectTag,
                        onValueChange = onSubjectChange,
                        placeholder = { Text(strings.subjectTagHint, color = TextSecondary) },
                        singleLine = true,
                        colors = darkFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("teacher_subject_input")
                    )

                    OutlinedTextField(
                        value = teacherState.lessonText,
                        onValueChange = onTextChange,
                        placeholder = { Text(strings.lessonInputHint, color = TextSecondary) },
                        minLines = 3,
                        maxLines = 6,
                        colors = darkFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("teacher_lesson_input")
                    )

                    // Cram Mode ("وضع الزنقة")
                    Surface(
                        color = if (teacherState.isCramMode) AmberContainer else NavyCardElevated,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            1.dp,
                            if (teacherState.isCramMode) WarmAmber else BorderNavy
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = strings.cramModeTitle,
                                    tint = WarmAmber
                                )
                                Column {
                                    Text(
                                        text = strings.cramModeTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = strings.cramModeSub,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Switch(
                                checked = teacherState.isCramMode,
                                onCheckedChange = onToggleCramMode,
                                colors = SwitchDefaults.colors(checkedTrackColor = WarmAmber),
                                modifier = Modifier.testTag("cram_mode_switch")
                            )
                        }
                    }

                    Button(
                        onClick = onExplainClick,
                        enabled = !teacherState.isLoading,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoyalBlue,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("teacher_explain_button")
                    ) {
                        if (teacherState.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.explainingLoading)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = strings.explainNow)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.explainNow, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 4 Separate Lesson Cards
            AnimatedVisibility(visible = latestLesson != null) {
                latestLesson?.let { lesson ->
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // 1. Simplified Explanation
                        LessonSectionCard(
                            title = strings.cardSimplified,
                            icon = Icons.Default.Lightbulb,
                            accentColor = ElectricSky,
                            containerColor = NavyCard,
                            tag = "card_simplified"
                        ) {
                            Text(
                                text = lesson.simplifiedExplanation,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary
                            )
                        }

                        // 2. Outside-the-Lesson Connection
                        LessonSectionCard(
                            title = strings.cardOutside,
                            icon = Icons.Default.Hub,
                            accentColor = VioletInsight,
                            containerColor = VioletContainer.copy(alpha = 0.65f),
                            tag = "card_outside_connection"
                        ) {
                            Text(
                                text = lesson.outsideConnection,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary
                            )
                        }

                        // 3. Important Questions Radar
                        LessonSectionCard(
                            title = strings.cardRadar,
                            icon = Icons.Default.Radar,
                            accentColor = WarmAmber,
                            containerColor = AmberContainer.copy(alpha = 0.45f),
                            tag = "card_questions_radar"
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                lesson.importantQuestionsRadar.forEach { q ->
                                    Text(
                                        text = "• $q",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }

                        // 4. Must-Memorize Essentials
                        LessonSectionCard(
                            title = strings.cardMemorize,
                            icon = Icons.Default.Bookmark,
                            accentColor = EmeraldSuccess,
                            containerColor = EmeraldContainer.copy(alpha = 0.45f),
                            tag = "card_must_memorize"
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                lesson.mustMemorize.forEach { item ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = EmeraldSuccess,
                                            modifier = Modifier
                                                .padding(top = 3.dp)
                                                .size(16.dp)
                                        )
                                        Text(
                                            text = item,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }

                        // Clarify a specific point
                        LessonSectionCard(
                            title = strings.clarifyTitle,
                            icon = Icons.Default.HelpOutline,
                            accentColor = ElectricSky,
                            containerColor = NavyCard,
                            tag = "card_clarify"
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                lesson.clarifications.forEach { c ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(DeepNavyBg.copy(alpha = 0.7f))
                                            .padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = c.question,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = ElectricSky
                                        )
                                        Text(
                                            text = c.answer,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = teacherState.clarifyInput,
                                        onValueChange = onClarifyInputChange,
                                        placeholder = { Text(strings.clarifyHint, color = TextSecondary) },
                                        singleLine = true,
                                        colors = darkFieldColors(),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("clarify_input")
                                    )
                                    Button(
                                        onClick = onAskClarify,
                                        enabled = !teacherState.isClarifying && teacherState.clarifyInput.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("clarify_button")
                                    ) {
                                        if (teacherState.isClarifying) {
                                            CircularProgressIndicator(
                                                color = Color.White,
                                                strokeWidth = 2.dp,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        } else {
                                            Text(strings.clarifyBtn, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }

                        // Teach-Back ("اشرحلي إنت فهمت إيه")
                        LessonSectionCard(
                            title = strings.teachBackTitle,
                            icon = Icons.Default.Psychology,
                            accentColor = WarmAmber,
                            containerColor = NavyCard,
                            tag = "card_teach_back"
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = strings.teachBackSub,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )

                                OutlinedTextField(
                                    value = teacherState.teachBackInput,
                                    onValueChange = onTeachBackInputChange,
                                    placeholder = { Text(strings.teachBackHint, color = TextSecondary) },
                                    minLines = 2,
                                    maxLines = 4,
                                    colors = darkFieldColors(),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("teach_back_input")
                                )

                                Button(
                                    onClick = onSubmitTeachBack,
                                    enabled = !teacherState.isEvaluatingTeachBack && teacherState.teachBackInput.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = WarmAmber,
                                        contentColor = DeepNavyBg
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .minimumInteractiveComponentSize()
                                        .testTag("teach_back_submit_button")
                                ) {
                                    if (teacherState.isEvaluatingTeachBack) {
                                        CircularProgressIndicator(
                                            color = DeepNavyBg,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Text(strings.teachBackBtn, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (lesson.lastTeachBackFeedback.isNotBlank()) {
                                    Surface(
                                        color = DeepNavyBg.copy(alpha = 0.8f),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, ElectricSky.copy(alpha = 0.45f))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text(
                                                text = lesson.lastTeachBackFeedback,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextPrimary
                                            )

                                            if (lesson.needsMorePracticePrompt) {
                                                Text(
                                                    text = strings.teachBackClosingQuestion,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = WarmAmber
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Button(
                                                        onClick = onWantMorePractice,
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = RoyalBlue,
                                                            contentColor = Color.White
                                                        ),
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .minimumInteractiveComponentSize()
                                                            .testTag("want_more_practice_button")
                                                    ) {
                                                        Text(strings.wantMorePractice)
                                                    }
                                                    OutlinedButton(
                                                        onClick = onDonePractice,
                                                        border = BorderStroke(1.dp, EmeraldSuccess),
                                                        colors = ButtonDefaults.outlinedButtonColors(
                                                            contentColor = EmeraldSuccess
                                                        ),
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .minimumInteractiveComponentSize()
                                                            .testTag("all_good_done_button")
                                                    ) {
                                                        Text(strings.allGoodDone)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                teacherState.masteryNotice?.let { notice ->
                                    Text(
                                        text = notice,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = EmeraldSuccess,
                                        fontWeight = FontWeight.SemiBold
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
private fun LessonSectionCard(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    containerColor: Color,
    tag: String,
    content: @Composable () -> Unit
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

@Composable
fun darkFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = ElectricSky,
    unfocusedBorderColor = BorderNavy,
    focusedContainerColor = DeepNavyBg.copy(alpha = 0.72f),
    unfocusedContainerColor = DeepNavyBg.copy(alpha = 0.72f),
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = ElectricSky
)
