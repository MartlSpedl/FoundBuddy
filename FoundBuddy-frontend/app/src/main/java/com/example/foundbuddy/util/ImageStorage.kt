package com.example.foundbuddy.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

object ImageStorage {

    fun persistToInternalFiles(context: Context, sourceUri: Uri): Uri? {
        return try {
            val imagesDir = File(context.filesDir, "images")
            if (!imagesDir.exists()) imagesDir.mkdirs()

            val targetFile = File(imagesDir, "${UUID.randomUUID()}.jpg")

            context.contentResolver.openInputStream(sourceUri).use { input ->
                if (input == null) return null
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            Uri.fromFile(targetFile) // => file://...
        } catch (_: Exception) {
            null
        }
    }
}
