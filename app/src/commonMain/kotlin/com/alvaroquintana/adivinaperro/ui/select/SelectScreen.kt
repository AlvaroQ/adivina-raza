package com.alvaroquintana.adivinaperro.ui.select

import adivinaraza.app.generated.resources.Res
import adivinaraza.app.generated.resources.ic_paw
import adivinaraza.app.generated.resources.landing_dogs_group
import adivinaraza.app.generated.resources.app_name
import adivinaraza.app.generated.resources.cancel
import adivinaraza.app.generated.resources.content_desc_dog
import adivinaraza.app.generated.resources.learn
import adivinaraza.app.generated.resources.mode_bigger_smaller
import adivinaraza.app.generated.resources.mode_bigger_smaller_subtitle
import adivinaraza.app.generated.resources.mode_classic_subtitle
import adivinaraza.app.generated.resources.mode_classic_title
import adivinaraza.app.generated.resources.mode_description
import adivinaraza.app.generated.resources.mode_description_subtitle
import adivinaraza.app.generated.resources.mode_fci_trivia
import adivinaraza.app.generated.resources.mode_fci_trivia_subtitle
import adivinaraza.app.generated.resources.select_mode_description
import adivinaraza.app.generated.resources.select_mode_title
import adivinaraza.app.generated.resources.select_subtitle
import adivinaraza.app.generated.resources.identify_breed
import adivinaraza.app.generated.resources.settings
import adivinaraza.app.generated.resources.start_game

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.alvaroquintana.adivinaperro.ui.theme.dynaPuffFamily
import com.alvaroquintana.adivinaperro.ui.theme.PillShape
import com.alvaroquintana.adivinaperro.ui.theme.getHeroGradient

private data class ModeItem(
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit
)

@Composable
fun SelectScreen(
    showRecognition: Boolean = false,
    onNavigateToGame: () -> Unit,
    onNavigateToBiggerSmaller: () -> Unit,
    onNavigateToDescription: () -> Unit,
    onNavigateToFciTrivia: () -> Unit,
    onNavigateToLearn: () -> Unit,
    onNavigateToRecognition: () -> Unit = {},
    onNavigateToSettings: () -> Unit
) {
    var showModeDialog by remember { mutableStateOf(false) }

    val modeItems = listOf(
        ModeItem(
            stringResource(Res.string.mode_classic_title),
            stringResource(Res.string.mode_classic_subtitle),
            onNavigateToGame
        ),
        ModeItem(
            stringResource(Res.string.mode_bigger_smaller),
            stringResource(Res.string.mode_bigger_smaller_subtitle),
            onNavigateToBiggerSmaller
        ),
        ModeItem(
            stringResource(Res.string.mode_description),
            stringResource(Res.string.mode_description_subtitle),
            onNavigateToDescription
        ),
        ModeItem(
            stringResource(Res.string.mode_fci_trivia),
            stringResource(Res.string.mode_fci_trivia_subtitle),
            onNavigateToFciTrivia
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(getHeroGradient())
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(62.dp))

        // Header: paw icon + title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_paw),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = stringResource(Res.string.app_name),
                fontFamily = dynaPuffFamily(),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dog image
        Image(
            painter = painterResource(Res.drawable.landing_dogs_group),
            contentDescription = stringResource(Res.string.content_desc_dog),
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(MaterialTheme.shapes.large),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Subtitle
        Text(
            text = stringResource(Res.string.select_subtitle),
            fontFamily = dynaPuffFamily(),
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Buttons section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Play button - filled orange pill
            Surface(
                onClick = { showModeDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = PillShape,
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 2.dp,
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(Res.string.start_game),
                        fontFamily = dynaPuffFamily(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // Learn button - outlined orange pill
            Surface(
                onClick = onNavigateToLearn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = PillShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(Res.string.learn),
                        fontFamily = dynaPuffFamily(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Identify Breed button - outlined orange pill with camera icon
            if (showRecognition) {
                Surface(
                    onClick = onNavigateToRecognition,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = PillShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CameraAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = stringResource(Res.string.identify_breed),
                            fontFamily = dynaPuffFamily(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Settings button - outlined orange pill
            Surface(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = PillShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(Res.string.settings),
                        fontFamily = dynaPuffFamily(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    if (showModeDialog) {
        GameModeDialog(
            items = modeItems,
            onDismiss = { showModeDialog = false }
        )
    }
}

@Composable
private fun GameModeDialog(
    items: List<ModeItem>,
    onDismiss: () -> Unit
) {
    val openProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "dialogOpen"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = 0.92f + (0.08f * openProgress)
                    scaleY = 0.92f + (0.08f * openProgress)
                    alpha = openProgress
                },
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 10.dp,
            shadowElevation = 14.dp
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            )
                        )
                    )
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_paw),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(Res.string.select_mode_title),
                        fontFamily = dynaPuffFamily(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = stringResource(Res.string.select_mode_description),
                    fontFamily = dynaPuffFamily(),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                items.forEach { item ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismiss()
                                item.onClick()
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                            Text(
                                text = item.title,
                                fontFamily = dynaPuffFamily(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.subtitle,
                                fontFamily = dynaPuffFamily(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(Res.string.cancel),
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable(onClick = onDismiss)
                        .padding(top = 4.dp, end = 4.dp),
                    fontFamily = dynaPuffFamily(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
