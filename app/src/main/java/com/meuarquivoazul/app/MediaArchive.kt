package com.meuarquivoazul.app

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import java.io.File

data class ArchivedMedia(val name: String, val file: File, val isVideo: Boolean)

object MediaArchive {
    private const val DIR = "midias_whatsapp"

    fun importAvailable(context: Context): List<ArchivedMedia> {
        val folder = File(context.filesDir, DIR).apply { mkdirs() }
        val resolver = context.contentResolver
        val result = mutableListOf<ArchivedMedia>()
        val collections = listOf(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI to false,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI to true
        )
        for ((collection, isVideo) in collections) {
            val projection = buildList {
                add(MediaStore.MediaColumns._ID)
                add(MediaStore.MediaColumns.DISPLAY_NAME)
                if (Build.VERSION.SDK_INT >= 29) add(MediaStore.MediaColumns.RELATIVE_PATH)
                else add(MediaStore.MediaColumns.DATA)
            }.toTypedArray()
            try {
                resolver.query(collection, projection, null, null, MediaStore.MediaColumns.DATE_ADDED + " DESC")?.use { cursor ->
                    val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                    val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                    val pathCol = cursor.getColumnIndex(projection[2])
                    while (cursor.moveToNext()) {
                        val relative = if (pathCol >= 0) cursor.getString(pathCol).orEmpty() else ""
                        val pathMatches = relative.contains("WhatsApp/Media/", ignoreCase = true) ||
                            relative.contains("WhatsApp Business/Media/", ignoreCase = true)
                        if (!pathMatches) continue
                        val id = cursor.getLong(idCol)
                        val originalName = cursor.getString(nameCol) ?: if (isVideo) "video_$id.mp4" else "imagem_$id"
                        val safeName = originalName.replace(Regex("[^A-Za-z0-9._-]"), "_")
                        val destination = File(folder, "${if (isVideo) "video" else "foto"}_${id}_$safeName")
                        if (!destination.exists()) {
                            val source = ContentUris.withAppendedId(collection, id)
                            try {
                                resolver.openInputStream(source)?.use { input ->
                                    destination.outputStream().use { output -> input.copyTo(output) }
                                }
                            } catch (_: Exception) {
                                destination.delete()
                            }
                        }
                        if (destination.exists() && destination.length() > 0L) {
                            result.add(ArchivedMedia(originalName, destination, isVideo))
                        }
                    }
                }
            } catch (_: SecurityException) {
                // User has not granted access to this media collection.
            } catch (_: Exception) {
                // Skip media that Android does not expose.
            }
        }
        return result.distinctBy { it.file.absolutePath }
    }

    fun all(context: Context): List<ArchivedMedia> =
        File(context.filesDir, DIR).listFiles()?.filter { it.isFile }?.map {
            ArchivedMedia(it.name.substringAfter('_').substringAfter('_'), it, it.name.startsWith("video_"))
        }?.sortedByDescending { it.file.lastModified() } ?: emptyList()
}
