package com.alvaroquintana.adivinaperro.ui.recognition

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun CameraPreviewContent(
    captureRequested: Boolean,
    onImageCaptured: (ByteArray) -> Unit,
    onCaptureConsumed: () -> Unit,
    modifier: Modifier
)
