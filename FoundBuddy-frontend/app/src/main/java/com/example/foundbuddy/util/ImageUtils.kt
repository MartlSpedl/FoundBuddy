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
        if (url.isNullOrBlank()) return null
        // If it's already a full HF URL or similar, keep it.
        if (url.startsWith("http")) return url
        
        // Potential fallback: absolute path prefix if the backend returns relative ones
        val baseUrl = "https://martlspedl-foundbuddy-backend.hf.space"
        return if (url.startsWith("/")) "$baseUrl$url" else "$baseUrl/$url"
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
