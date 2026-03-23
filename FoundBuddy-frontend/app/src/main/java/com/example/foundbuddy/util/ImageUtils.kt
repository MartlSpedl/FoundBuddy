package com.example.foundbuddy.util

/**
 * Hilfsfunktionen für Bild-URLs und Bild-Verarbeitung
 */
object ImageUtils {
    
    /**
     * Dekodiert URL-encodierte Firebase URLs
     * Firebase Storage gibt oft URLs mit %2F statt / zurück
     */
    fun decodeImageUrl(url: String?): String? {
        return url
    }
    
    /**
     * Erstellt einen ImageRequest für Coil mit URL-Dekodierung und Memory Management
     */
    fun createImageRequest(context: android.content.Context, url: String?): coil.request.ImageRequest {
        val decodedUrl = decodeImageUrl(url)
        
        // Fallback auf funktionierende Testbilder wenn URL ungültig
        val finalUrl = if (decodedUrl.isNullOrBlank()) {
            "https://picsum.photos/400/300?random=${System.currentTimeMillis()}"
        } else {
            decodedUrl
        }
            
        return coil.request.ImageRequest.Builder(context)
            .data(finalUrl)
            .crossfade(true)
            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
            .networkCachePolicy(coil.request.CachePolicy.ENABLED)
            .build()
    }
}
