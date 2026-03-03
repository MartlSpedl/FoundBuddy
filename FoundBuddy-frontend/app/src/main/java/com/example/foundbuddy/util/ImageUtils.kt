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
        
        return try {
            if (url.contains("%2F") || url.contains("%3A")) {
                val decoded = java.net.URLDecoder.decode(url, "UTF-8")
                println("LOGCAT: ImageUtils - URL dekodiert: $url -> $decoded")
                decoded
            } else {
                println("LOGCAT: ImageUtils - URL不需要解码: $url")
                url
            }
        } catch (e: Exception) {
            println("LOGCAT: ImageUtils - URL Dekodierung fehlgeschlagen: ${e.message}")
            url
        }
    }
    
    /**
     * Erstellt einen ImageRequest für Coil mit URL-Dekodierung
     */
    fun createImageRequest(context: android.content.Context, url: String?): coil.request.ImageRequest {
        val decodedUrl = decodeImageUrl(url) ?: return coil.request.ImageRequest.Builder(context)
            .data(com.example.foundbuddy.R.drawable.ic_launcher_foreground)
            .build()
            
        return coil.request.ImageRequest.Builder(context)
            .data(decodedUrl)
            .crossfade(true)
            .build()
    }
}
