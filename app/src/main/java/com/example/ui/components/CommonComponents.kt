package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.model.AppLanguage
import com.example.data.remote.ImageEncodingUtil
import com.example.ui.strings.AppStrings
import com.example.ui.theme.BorderNavy
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.ElectricSky
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavySurface
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmAmber

@Composable
fun StudioBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavyBg)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            RoyalBlue.copy(alpha = 0.22f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.85f, size.height * 0.08f),
                        radius = size.width * 0.85f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ElectricSky.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.12f, size.height * 0.72f),
                        radius = size.width * 0.75f
                    )
                )
            },
        content = content
    )
}

@Composable
fun StudioTopBar(
    strings: AppStrings,
    language: AppLanguage,
    musicEnabled: Boolean,
    soundEffectsEnabled: Boolean,
    isLiveAiMode: Boolean,
    onToggleMusic: () -> Unit,
    onToggleSoundEffects: () -> Unit,
    onToggleLanguage: () -> Unit,
    onOpenWhatsapp: () -> Unit
) {
    Surface(
        color = NavySurface.copy(alpha = 0.94f),
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.appTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isLiveAiMode) EmeraldSuccess else WarmAmber)
                        )
                        Text(
                            text = if (isLiveAiMode) strings.liveAiBadge else strings.demoBadge,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isLiveAiMode) EmeraldSuccess else WarmAmber
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Prominent Music Mute/Play toggle
                    Surface(
                        onClick = onToggleMusic,
                        shape = RoundedCornerShape(20.dp),
                        color = if (musicEnabled) ElectricSky.copy(alpha = 0.18f) else NavyCard,
                        border = BorderStroke(
                            1.dp,
                            if (musicEnabled) ElectricSky else BorderNavy
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("music_toggle_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (musicEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                                contentDescription = if (musicEnabled) strings.musicMute else strings.musicPlay,
                                tint = if (musicEnabled) ElectricSky else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (musicEnabled) strings.musicMute else strings.musicPlay,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (musicEnabled) ElectricSky else TextSecondary,
                                maxLines = 1
                            )
                        }
                    }

                    // AR / EN Toggle
                    Surface(
                        onClick = onToggleLanguage,
                        shape = RoundedCornerShape(14.dp),
                        color = NavyCard,
                        border = BorderStroke(1.dp, BorderNavy),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("language_toggle_button")
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (language == AppLanguage.AR) "EN" else "عربي",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Direct WhatsApp Support button (01034910183)
                    IconButton(
                        onClick = onOpenWhatsapp,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("whatsapp_support_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = strings.supportWhatsapp,
                            tint = EmeraldSuccess
                        )
                    }

                    // Soft Click Sound Toggle
                    IconButton(
                        onClick = onToggleSoundEffects,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("sound_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (soundEffectsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = strings.soundEffectsLabel,
                            tint = if (soundEffectsEnabled) ElectricSky else TextSecondary
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(BorderNavy.copy(alpha = 0.6f))
            )
        }
    }
}

@Composable
fun MediaCaptureRow(
    strings: AppStrings,
    selectedImageUri: Uri?,
    onImagePicked: (Uri?) -> Unit,
    onButtonTap: () -> Unit,
    cameraTag: String,
    galleryTag: String
) {
    val context = LocalContext.current
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingCameraUri != null) {
            onImagePicked(pendingCameraUri)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = ImageEncodingUtil.createTempCameraUri(context)
            pendingCameraUri = uri
            takePictureLauncher.launch(uri)
        } else {
            Toast.makeText(context, strings.cameraPermissionDenied, Toast.LENGTH_SHORT).show()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onImagePicked(uri)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    onButtonTap()
                    val hasPerm = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPerm) {
                        val uri = ImageEncodingUtil.createTempCameraUri(context)
                        pendingCameraUri = uri
                        takePictureLauncher.launch(uri)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, ElectricSky.copy(alpha = 0.55f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = NavyCard,
                    contentColor = ElectricSky
                ),
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag(cameraTag)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = strings.cameraCapture,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = strings.cameraCapture,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            OutlinedButton(
                onClick = {
                    onButtonTap()
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, BorderNavy),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = NavyCard,
                    contentColor = TextPrimary
                ),
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag(galleryTag)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = strings.uploadImage,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = strings.uploadImage,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (selectedImageUri != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NavyCard)
                    .border(1.dp, EmeraldSuccess.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = strings.imageAttached,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Text(
                    text = strings.imageAttached,
                    style = MaterialTheme.typography.bodyMedium,
                    color = EmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { onImagePicked(null) },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = strings.clearImage,
                        tint = TextSecondary
                    )
                }
            }
        }
    }
}

fun openWhatsAppSupport(context: Context, fallbackMessage: String) {
    val url = "https://wa.me/201034910183?text=" + Uri.encode("السلام عليكم، محتاج دعم بخصوص تطبيق نبراوي ستوديو للمذاكرة")
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    runCatching {
        context.startActivity(intent)
    }.onFailure {
        Toast.makeText(context, fallbackMessage, Toast.LENGTH_LONG).show()
    }
}
