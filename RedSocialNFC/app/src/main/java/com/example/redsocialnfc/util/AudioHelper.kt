package com.example.redsocialnfc.util

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class VoiceNoteRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startTimeMs: Long = 0L

    fun iniciarGrabacion(): Boolean {
        return try {
            detenerYDescartar()
            val file = MediaStorageHelper.crearArchivoParaAudio(context)
            outputFile = file
            val newRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            newRecorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(96_000)
                setAudioSamplingRate(44_100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            recorder = newRecorder
            startTimeMs = System.currentTimeMillis()
            true
        } catch (_: Exception) {
            detenerYDescartar()
            false
        }
    }

    fun detenerYGuardar(): MediaFileResult? {
        val file = outputFile ?: return null
        val elapsed = (System.currentTimeMillis() - startTimeMs).coerceAtLeast(0L)
        return try {
            recorder?.stop()
            recorder?.release()
            recorder = null
            outputFile = null

            if (!file.exists() || file.length() == 0L || elapsed < 350L) {
                file.delete()
                return null
            }

            val realDuration = MediaStorageHelper.obtenerDuracionArchivo(file.absolutePath)
                .let { if (it > 0L) it else elapsed }

            MediaFileResult(
                localPath = file.absolutePath,
                fileName = file.name,
                sizeBytes = file.length(),
                durationMs = realDuration,
                mimeType = "audio/mp4"
            )
        } catch (_: Exception) {
            try {
                recorder?.release()
            } catch (_: Exception) {
            }
            recorder = null
            outputFile = null
            file.delete()
            null
        }
    }

    fun detenerYDescartar() {
        try {
            recorder?.stop()
        } catch (_: Exception) {
        }
        try {
            recorder?.release()
        } catch (_: Exception) {
        }
        recorder = null
        outputFile?.delete()
        outputFile = null
    }
}

class SimpleAudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    var currentPath: String? = null
        private set

    fun playOrPause(
        path: String,
        onStateChange: (isPlaying: Boolean, positionMs: Int, durationMs: Int) -> Unit,
        onCompleted: () -> Unit
    ) {
        if (currentPath == path && mediaPlayer != null) {
            val player = mediaPlayer!!
            if (player.isPlaying) {
                player.pause()
                onStateChange(false, player.currentPosition, player.duration.coerceAtLeast(1))
            } else {
                player.start()
                onStateChange(true, player.currentPosition, player.duration.coerceAtLeast(1))
            }
            return
        }

        stop()
        try {
            val player = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                setOnCompletionListener {
                    onCompleted()
                }
                start()
            }
            mediaPlayer = player
            currentPath = path
            onStateChange(true, 0, player.duration.coerceAtLeast(1))
        } catch (_: Exception) {
            stop()
            onCompleted()
        }
    }

    fun getPosition(): Int = try {
        mediaPlayer?.currentPosition ?: 0
    } catch (_: Exception) {
        0
    }

    fun getDuration(): Int = try {
        mediaPlayer?.duration ?: 0
    } catch (_: Exception) {
        0
    }

    fun isPlayingPath(path: String): Boolean = try {
        currentPath == path && (mediaPlayer?.isPlaying == true)
    } catch (_: Exception) {
        false
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {
        }
        try {
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
        currentPath = null
    }
}
