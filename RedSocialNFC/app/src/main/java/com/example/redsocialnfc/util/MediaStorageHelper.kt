package com.example.redsocialnfc.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class MediaFileResult(
    val localPath: String,
    val fileName: String,
    val sizeBytes: Long,
    val durationMs: Long,
    val mimeType: String
)

object MediaStorageHelper {

    private fun getMediaDir(context: Context): File {
        val dir = File(context.filesDir, "media")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    suspend fun copiarUriAAlmacenamientoLocal(
        context: Context,
        uri: Uri,
        prefijo: String = "archivo",
        extraerDuracion: Boolean = false
    ): MediaFileResult? = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val mimeType = resolver.getType(uri) ?: "application/octet-stream"
            var nombreOriginal = "archivo"
            var sizeBytes = 0L

            resolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIdx != -1) nombreOriginal = cursor.getString(nameIdx) ?: nombreOriginal
                    if (sizeIdx != -1) sizeBytes = cursor.getLong(sizeIdx)
                }
            }

            val extension = nombreOriginal.substringAfterLast('.', "").ifBlank {
                MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "bin"
            }
            val safeFileName = "${prefijo}_${UUID.randomUUID().toString().take(8)}.$extension"
            val destFile = File(getMediaDir(context), safeFileName)

            resolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext null

            if (sizeBytes <= 0L) {
                sizeBytes = destFile.length()
            }

            val duracionMs = if (extraerDuracion) {
                obtenerDuracionArchivo(destFile.absolutePath)
            } else {
                0L
            }

            MediaFileResult(
                localPath = destFile.absolutePath,
                fileName = nombreOriginal,
                sizeBytes = sizeBytes,
                durationMs = duracionMs,
                mimeType = mimeType
            )
        } catch (_: Exception) {
            null
        }
    }

    fun crearArchivoParaAudio(context: Context): File {
        return File(getMediaDir(context), "voz_${System.currentTimeMillis()}.m4a")
    }

    fun crearArchivoFotoCamara(context: Context, prefijo: String = "cam"): Pair<File, Uri> {
        val file = File(getMediaDir(context), "${prefijo}_${UUID.randomUUID().toString().take(8)}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return file to uri
    }

    fun obtenerDuracionArchivo(path: String): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(path)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        } catch (_: Exception) {
            0L
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {
            }
        }
    }

    suspend fun extraerMiniaturaVideo(path: String): Bitmap? = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(path)
            retriever.getFrameAtTime(500_000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.frameAtTime
        } catch (_: Exception) {
            null
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {
            }
        }
    }

    fun abrirArchivoExterno(context: Context, localPath: String, nombreArchivo: String?) {
        val file = File(localPath)
        if (!file.exists()) {
            Toast.makeText(context, "El archivo ya no está disponible localmente", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val extension = (nombreArchivo ?: file.name).substringAfterLast('.', "").lowercase(Locale.getDefault())
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Abrir $nombreArchivo"))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "No hay una app instalada para abrir este tipo de archivo", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun formatearTamano(bytes: Long): String {
        if (bytes <= 0L) return "0 KB"
        val kb = bytes / 1024.0
        return if (kb < 1024.0) {
            String.format(Locale.getDefault(), "%.0f KB", kb.coerceAtLeast(1.0))
        } else {
            String.format(Locale.getDefault(), "%.1f MB", kb / 1024.0)
        }
    }

    fun formatearDuracion(ms: Long): String {
        if (ms <= 0L) return "0:00"
        val totalSegundos = (ms / 1000).toInt()
        val minutos = totalSegundos / 60
        val segundos = totalSegundos % 60
        return String.format(Locale.getDefault(), "%d:%02d", minutos, segundos)
    }

    fun formatearTiempoRelativo(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val mins = diff / 60_000L
        val horas = diff / 3_600_000L
        return when {
            mins < 1 -> "Ahora"
            mins < 60 -> "${mins}m"
            horas < 24 -> "${horas}h"
            else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
        }
    }

    fun calcularTamanoMediaBytes(context: Context): Long {
        val dir = getMediaDir(context)
        return dir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    fun limpiarMediaNoReferenciada(context: Context, rutasEnUso: Set<String>): Int {
        val dir = getMediaDir(context)
        var eliminados = 0
        dir.listFiles()?.forEach { file ->
            if (file.absolutePath !in rutasEnUso) {
                if (file.delete()) eliminados++
            }
        }
        return eliminados
    }
}
