package com.alvaroquintana.adivinaperro.ui.recognition

import adivinaraza.app.generated.resources.Res
import adivinaraza.app.generated.resources.recognition_analyzing
import adivinaraza.app.generated.resources.recognition_best_match
import adivinaraza.app.generated.resources.recognition_capture
import adivinaraza.app.generated.resources.recognition_close
import adivinaraza.app.generated.resources.recognition_error
import adivinaraza.app.generated.resources.recognition_not_recognized
import adivinaraza.app.generated.resources.recognition_not_recognized_subtitle
import adivinaraza.app.generated.resources.recognition_other_matches
import adivinaraza.app.generated.resources.recognition_try_again
import adivinaraza.app.generated.resources.recognition_view_detail
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvaroquintana.adivinaperro.ui.composables.BreedImage
import com.alvaroquintana.adivinaperro.ui.theme.GameGold
import com.alvaroquintana.adivinaperro.ui.theme.GameOrange
import com.alvaroquintana.adivinaperro.ui.theme.dynaPuffFamily
import com.alvaroquintana.adivinaperro.ui.theme.getBackgroundGradient
import com.alvaroquintana.domain.BreedPrediction
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
fun RecognitionScreen(
    viewModel: RecognitionViewModel,
    uiState: RecognitionUiState,
    onBreedClick: (Long) -> Unit
) {
    var captureRequested by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreviewContent(
            captureRequested = captureRequested,
            onImageCaptured = { bytes -> viewModel.classify(bytes) },
            onCaptureConsumed = { captureRequested = false },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (uiState) {
                is RecognitionUiState.Idle -> CaptureButton(
                    onClick = { captureRequested = true }
                )
                is RecognitionUiState.Loading -> LoadingIndicator()
                is RecognitionUiState.Results,
                is RecognitionUiState.NotRecognized,
                is RecognitionUiState.Error -> RetryButton(
                    onClick = { viewModel.resetToIdle() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        AnimatedVisibility(
            visible = uiState is RecognitionUiState.Results ||
                    uiState is RecognitionUiState.NotRecognized ||
                    uiState is RecognitionUiState.Error,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                    scaleIn(
                        initialScale = 0.9f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ),
            exit = fadeOut()
        ) {
            ResultsDialog(
                uiState = uiState,
                onBreedClick = onBreedClick,
                onDismiss = { viewModel.resetToIdle() }
            )
        }
    }
}

@Composable
private fun CaptureButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(72.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 8.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Rounded.CameraAlt,
                contentDescription = stringResource(Res.string.recognition_capture),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.recognition_analyzing),
            fontFamily = dynaPuffFamily(),
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun RetryButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(72.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 8.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = stringResource(Res.string.recognition_try_again),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

// ── Full-screen results dialog ──────────────────────────────────────────────

@Composable
private fun ResultsDialog(
    uiState: RecognitionUiState,
    onBreedClick: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(enabled = false, onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 48.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 16.dp
        ) {
            Box {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 24.dp)
                ) {
                    when (uiState) {
                        is RecognitionUiState.Results -> ResultsContent(
                            predictions = uiState.predictions,
                            onBreedClick = onBreedClick
                        )

                        is RecognitionUiState.NotRecognized -> NotRecognizedContent()

                        is RecognitionUiState.Error -> ErrorContent(
                            message = uiState.message
                        )

                        else -> {}
                    }
                }

                // Close button top-right
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(40.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(Res.string.recognition_close),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ── Results content with hero + runner-ups ───────────────────────────────────

@Composable
private fun ResultsContent(
    predictions: List<BreedPrediction>,
    onBreedClick: (Long) -> Unit
) {
    val top = predictions.firstOrNull() ?: return
    val runnerUps = predictions.drop(1)

    // Hero section
    HeroPrediction(
        prediction = top,
        onClick = { top.breedId?.let(onBreedClick) }
    )

    // Runner-ups section
    if (runnerUps.isNotEmpty()) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        Text(
            text = stringResource(Res.string.recognition_other_matches),
            fontFamily = dynaPuffFamily(),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 12.dp)
        )

        runnerUps.forEachIndexed { index, prediction ->
            RunnerUpRow(
                prediction = prediction,
                onClick = { prediction.breedId?.let(onBreedClick) }
            )
            if (index < runnerUps.lastIndex) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun HeroPrediction(
    prediction: BreedPrediction,
    onClick: () -> Unit
) {
    val confidencePercent = (prediction.confidence * 100).roundToInt()
    val hasDetail = prediction.breedId != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (hasDetail) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(top = 20.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // "Best match" label
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = GameOrange.copy(alpha = 0.12f)
        ) {
            Text(
                text = stringResource(Res.string.recognition_best_match),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                fontFamily = dynaPuffFamily(),
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = GameOrange
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Breed image
        val imageUrl = prediction.imageUrl
        if (imageUrl != null) {
            BreedImage(
                imageData = imageUrl,
                contentDescription = prediction.breedName,
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Breed name
        Text(
            text = prediction.breedName ?: prediction.label,
            fontFamily = dynaPuffFamily(),
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Confidence bar + percentage
        ConfidenceBar(percent = confidencePercent)

        if (hasDetail) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.recognition_view_detail),
                fontFamily = dynaPuffFamily(),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ConfidenceBar(percent: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 48.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$percent%",
                fontFamily = dynaPuffFamily(),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = confidenceColor(percent)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = confidenceColor(percent),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
private fun confidenceColor(percent: Int): Color = when {
    percent >= 70 -> MaterialTheme.colorScheme.primary
    percent >= 50 -> GameGold
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

// ── Runner-up row ────────────────────────────────────────────────────────────

@Composable
private fun RunnerUpRow(
    prediction: BreedPrediction,
    onClick: () -> Unit
) {
    val confidencePercent = (prediction.confidence * 100).roundToInt()
    val hasDetail = prediction.breedId != null

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .then(if (hasDetail) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imageUrl = prediction.imageUrl
            if (imageUrl != null) {
                BreedImage(
                    imageData = imageUrl,
                    contentDescription = prediction.breedName,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = prediction.breedName ?: prediction.label,
                    fontFamily = dynaPuffFamily(),
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (hasDetail) {
                    Text(
                        text = stringResource(Res.string.recognition_view_detail),
                        fontFamily = dynaPuffFamily(),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            ConfidenceBadge(percent = confidencePercent)
        }
    }
}

@Composable
private fun ConfidenceBadge(percent: Int) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = confidenceColor(percent).copy(alpha = 0.12f)
    ) {
        Text(
            text = "$percent%",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            fontFamily = dynaPuffFamily(),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = confidenceColor(percent),
            textAlign = TextAlign.Center
        )
    }
}

// ── Not recognized & error states ────────────────────────────────────────────

@Composable
private fun NotRecognizedContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.recognition_not_recognized),
            fontFamily = dynaPuffFamily(),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.recognition_not_recognized_subtitle),
            fontFamily = dynaPuffFamily(),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorContent(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.recognition_error),
            fontFamily = dynaPuffFamily(),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            fontFamily = dynaPuffFamily(),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
