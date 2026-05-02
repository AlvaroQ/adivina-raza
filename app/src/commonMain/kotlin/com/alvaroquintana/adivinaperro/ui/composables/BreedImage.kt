package com.alvaroquintana.adivinaperro.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable
expect fun rememberBase64ImageBitmap(data: String): ImageBitmap?

/**
 * Unified image component for the app.
 * Handles both HTTP URLs (via Coil) and base64-encoded image data (manual decode).
 */
@Composable
fun BreedImage(
    imageData: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val normalized = imageData.trim()
    if (normalized.isBlank()) return

    if (normalized.startsWith("http", ignoreCase = true)) {
        AsyncImage(
            model = normalized,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            onError = { state ->
                println("[BreedImage] Error loading url='$normalized': ${state.result.throwable.message}")
            }
        )
    } else {
        val bitmap = rememberBase64ImageBitmap(normalized)
        bitmap?.let {
            Image(
                bitmap = it,
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
    }
}
