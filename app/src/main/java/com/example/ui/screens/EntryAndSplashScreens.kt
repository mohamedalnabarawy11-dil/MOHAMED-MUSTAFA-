package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.StudioBackdrop
import com.example.ui.strings.AppStrings
import com.example.ui.theme.BorderNavy
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.DeepestSplashBg
import com.example.ui.theme.ElectricSky
import com.example.ui.theme.NavyCard
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmAmber
import kotlinx.coroutines.delay

@Composable
fun EntryGateScreen(
    strings: AppStrings,
    onInstallAndEnter: () -> Unit,
    onExitApp: () -> Unit
) {
    StudioBackdrop {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 460.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(NavyCard.copy(alpha = 0.72f))
                    .border(1.dp, BorderNavy, RoundedCornerShape(28.dp))
                    .padding(horizontal = 24.dp, vertical = 34.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(CircleShape)
                        .border(2.dp, ElectricSky.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_nabraawy_fg_1795635767286),
                        contentDescription = strings.appTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Text(
                    text = strings.appTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = strings.appTagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Button 1: Install App ("تثبيت التطبيق")
                Button(
                    onClick = onInstallAndEnter,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalBlue,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("install_app_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = strings.installApp
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = strings.installApp,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Button 2: Exit ("الخروج من الموقع")
                OutlinedButton(
                    onClick = onExitApp,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BorderNavy),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = DeepNavyBg.copy(alpha = 0.65f),
                        contentColor = TextSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("exit_app_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = strings.exitSite
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = strings.exitSite,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun DarkSplashScreen(
    strings: AppStrings,
    onFinished: () -> Unit
) {
    var phase by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        phase = 1
        delay(1150)
        phase = 2
        delay(550)
        onFinished()
    }

    val targetAlpha = when (phase) {
        1 -> 1f
        2 -> 0f
        else -> 0f
    }
    val targetScale = when (phase) {
        1 -> 1f
        2 -> 1.08f
        else -> 0.88f
    }

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "splashAlpha"
    )
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "splashScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepestSplashBg, DeepNavyBg, DeepestSplashBg)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .alpha(alpha)
                .scale(scale)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_nabraawy_fg_1795635767286),
                contentDescription = strings.nabraawySplash,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
            )
            Text(
                text = strings.nabraawySplash,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = ElectricSky
            )
            Text(
                text = strings.appTagline,
                style = MaterialTheme.typography.bodyMedium,
                color = WarmAmber
            )
        }
    }
}
