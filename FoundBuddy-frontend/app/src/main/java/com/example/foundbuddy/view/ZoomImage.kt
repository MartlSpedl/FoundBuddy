package com.example.foundbuddy.view

import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.foundbuddy.R
import com.example.foundbuddy.util.ImageUtils
import kotlin.Unit

@Composable
fun ZoomImage(url: String?, modifier: Modifier = Modifier) {
    println("LOGCAT: ZoomImage aufgerufen mit URL: $url")
    
    if (url.isNullOrBlank()) {
        println("LOGCAT: URL ist null oder leer - zeige 'Kein Bild'")
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
    println("LOGCAT: ZoomImage - Dekodierte URL: $decodedUrl")

    // ImageRequest mit Hilfsfunktion
    val imageModel = ImageUtils.createImageRequest(LocalContext.current, decodedUrl)
        .newBuilder()
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .networkCachePolicy(CachePolicy.ENABLED)
        .build()

    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Lade Bild...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("URL-Typ: Web URL", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    Text("URL: ${decodedUrl?.take(50) ?: "null"}...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 8.sp)
                }
            }
        } else if (imageLoadFailed) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Bild konnte nicht geladen werden", color = MaterialTheme.colorScheme.error)
                    Text("URL-Typ: Web URL", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    Text("URL: ${decodedUrl?.take(50) ?: "null"}...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 8.sp)
                }
            }
        } else {
            AsyncImage(
                model = imageModel,
                contentDescription = "Zoombares Bild",
                contentScale = ContentScale.Fit,
                onError = { error ->
                    println("LOGCAT: Bild-Lade-Fehler: ${error.result.throwable?.message}")
                    error.result.throwable?.printStackTrace()
                    imageLoadFailed = true
                    isLoading = false
                },
                onLoading = {
                    println("LOGCAT: Bild wird geladen...")
                    imageLoadFailed = false
                    isLoading = true
                },
                onSuccess = { 
                    println("LOGCAT: Bild erfolgreich geladen!")
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
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            if (scale > 1f) {
                                offsetX += pan.x
                                offsetY += pan.y
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    }
            )
        }
    }
}
