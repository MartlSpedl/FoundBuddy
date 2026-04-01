package com.example.foundbuddy.view

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.foundbuddy.util.ImageUtils
import kotlin.math.abs
import kotlin.Unit

@Composable
fun ZoomImage(url: String?, modifier: Modifier = Modifier) {
    if (url.isNullOrBlank()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Kein Bild", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var imageLoadFailed by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // Dekodierte URL mit Hilfsfunktion
    val decodedUrl = ImageUtils.decodeImageUrl(url)

    // ImageRequest mit Hilfsfunktion und verbessertem Memory Management
    val imageModel = ImageUtils.createImageRequest(LocalContext.current, decodedUrl)
        .newBuilder()
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .networkCachePolicy(CachePolicy.ENABLED)
        .crossfade(true)
        .build()

    Box(modifier = modifier.fillMaxSize()) {
        AsyncImage(
            model = imageModel,
            contentDescription = "Zoombares Bild",
            contentScale = ContentScale.Fit,
            onError = { error ->
                imageLoadFailed = true
                isLoading = false
            },
            onLoading = {
                imageLoadFailed = false
                isLoading = true
            },
            onSuccess = { 
                imageLoadFailed = false
                isLoading = false
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale.coerceAtLeast(1f),
                    scaleY = scale.coerceAtLeast(1f),
                    translationX = offsetX,
                    translationY = offsetY
                )
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val firstDown = awaitFirstDown(requireUnconsumed = false)
                        var zoomBy = 1f
                        var panBy = androidx.compose.ui.geometry.Offset.Zero
                        var pastTouchSlop = false
                        val touchSlop = viewConfiguration.touchSlop
                        var lockedToPanZoom = false
                        var zoomGestureStarted = false

                        do {
                            val event = awaitPointerEvent()
                            val canceled = event.changes.any { it.isConsumed }
                            if (canceled) break

                            if (event.changes.size > 1) {
                                zoomGestureStarted = true
                                lockedToPanZoom = true

                                val zoomChange = event.calculateZoom()
                                val panChange = event.calculatePan()

                                if (!pastTouchSlop) {
                                    zoomBy *= zoomChange
                                    panBy += panChange

                                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
                                    val zoomMotion = abs(1 - zoomBy) * centroidSize
                                    val panMotion = panBy.getDistance()

                                    if (zoomMotion > touchSlop || panMotion > touchSlop) {
                                        pastTouchSlop = true
                                    }
                                }

                                if (pastTouchSlop) {
                                    val newScale = (scale * zoomChange).coerceIn(1f, 5f)
                                    scale = newScale
                                    if (newScale > 1f) {
                                        offsetX += panChange.x
                                        offsetY += panChange.y
                                    } else {
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                }

                                event.changes.forEach {
                                    if (it.positionChanged()) {
                                        it.consume()
                                    }
                                }
                            } else if (lockedToPanZoom) {
                                val panChange = event.calculatePan()
                                val newScale = (scale * zoomBy).coerceIn(1f, 5f)
                                scale = newScale
                                if (newScale > 1f) {
                                    offsetX += panChange.x
                                    offsetY += panChange.y
                                }

                                event.changes.forEach {
                                    if (it.positionChanged()) {
                                        it.consume()
                                    }
                                }
                            }
                        } while (event.changes.any { it.pressed })

                        if (!lockedToPanZoom && !zoomGestureStarted && scale == 1f) {
                            firstDown.consume()
                        }
                    }
                }
        )

        // Loading/Error overlay
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Lade Bild...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else if (imageLoadFailed) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Bild konnte nicht geladen werden", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
