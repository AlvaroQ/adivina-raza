package com.alvaroquintana.adivinaperro.ui.recognition

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun CameraPreviewContent(
    captureRequested: Boolean,
    onImageCaptured: (ByteArray) -> Unit,
    onCaptureConsumed: () -> Unit,
    modifier: Modifier
) {
    Box(modifier = modifier)
}
