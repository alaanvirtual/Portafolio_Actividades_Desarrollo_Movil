package com.example.redsocialnfc.presentation.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.redsocialnfc.ui.theme.DarkBackground
import com.example.redsocialnfc.ui.theme.DarkOutline
import com.example.redsocialnfc.ui.theme.DarkSurface
import com.example.redsocialnfc.ui.theme.DarkSurfaceVariant
import com.example.redsocialnfc.ui.theme.MutedAccent
import com.example.redsocialnfc.ui.theme.MutedGray
import com.example.redsocialnfc.ui.theme.MutedPurple
import com.example.redsocialnfc.ui.theme.MutedPurpleLight
import com.example.redsocialnfc.ui.theme.TextPrimary
import com.example.redsocialnfc.ui.theme.TextSecondary
import com.example.redsocialnfc.util.MediaFileResult
import com.example.redsocialnfc.util.MediaStorageHelper
import com.example.redsocialnfc.util.SimpleAudioPlayer
import kotlinx.coroutines.delay
import java.io.File

private fun coilModelFor(pathOrUri: String): Any {
    return if (pathOrUri.startsWith("/")) File(pathOrUri) else pathOrUri
}

@Composable
fun AvatarUsuario(
    nombreVisible: String,
    avatarUri: String?,
    esAnonimo: Boolean,
    size: Dp = 42.dp
) {
    val bgColor = if (esAnonimo) MutedGray else MutedPurple
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        if (!esAnonimo && !avatarUri.isNullOrBlank() && File(avatarUri).exists()) {
            AsyncImage(
                model = coilModelFor(avatarUri),
                contentDescription = "Avatar de $nombreVisible",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (esAnonimo) {
            Icon(
                imageVector = Icons.Default.VisibilityOff,
                contentDescription = "Anónimo",
                tint = Color.White,
                modifier = Modifier.size(size * 0.5f)
            )
        } else {
            val inicial = nombreVisible.removePrefix("@").take(1).uppercase().ifBlank { "U" }
            Text(
                text = inicial,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.42f).sp
            )
        }
    }
}

@Composable
fun FotoPreviewCard(
    path: String,
    maxHeight: Dp = 240.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp, max = maxHeight)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, DarkOutline, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = coilModelFor(path),
            contentDescription = "Fotografía adjunta",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 140.dp, max = maxHeight)
        )
    }
}

@Composable
fun VideoPreviewCard(
    path: String,
    duracionMs: Long = 0L,
    maxHeight: Dp = 230.dp,
    onClick: () -> Unit
) {
    var thumbnail by remember(path) { mutableStateOf<Bitmap?>(null) }
    var duracionReal by remember(path, duracionMs) { mutableStateOf(duracionMs) }

    LaunchedEffect(path) {
        thumbnail = MediaStorageHelper.extraerMiniaturaVideo(path)
        if (duracionReal <= 0L) {
            duracionReal = MediaStorageHelper.obtenerDuracionArchivo(path)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp.coerceAtMost(maxHeight))
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0E0D12))
            .border(1.dp, DarkOutline, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        val bmp = thumbnail
        if (bmp != null) {
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Vista previa de video",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = null,
                tint = MutedGray,
                modifier = Modifier.size(48.dp)
            )
        }

        // Overlay play circular
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.65f))
                .border(1.5.dp, MutedPurpleLight, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Reproducir video",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }

        // Badge de duración / VIDEO en esquina inferior izquierda
        Surface(
            color = Color.Black.copy(alpha = 0.72f),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = MutedPurpleLight,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (duracionReal > 0L) MediaStorageHelper.formatearDuracion(duracionReal) else "VIDEO",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun AudioBubblePlayer(
    path: String,
    nombre: String?,
    duracionMs: Long,
    audioPlayer: SimpleAudioPlayer,
    activeAudioPath: String?,
    onActiveChange: (String?) -> Unit
) {
    val estaActivo = activeAudioPath == path
    var reproduciendo by remember(activeAudioPath) {
        mutableStateOf(estaActivo && audioPlayer.isPlayingPath(path))
    }
    var posicionMs by remember(path) { mutableIntStateOf(0) }
    var totalMs by remember(path, duracionMs) { mutableIntStateOf(duracionMs.toInt().coerceAtLeast(1000)) }

    LaunchedEffect(estaActivo, reproduciendo) {
        while (estaActivo && reproduciendo) {
            val pos = audioPlayer.getPosition()
            val dur = audioPlayer.getDuration().coerceAtLeast(1)
            posicionMs = pos
            totalMs = dur
            if (!audioPlayer.isPlayingPath(path)) {
                reproduciendo = false
                posicionMs = 0
                onActiveChange(null)
                break
            }
            delay(200)
        }
        if (!estaActivo) {
            reproduciendo = false
            posicionMs = 0
        }
    }

    val progreso = (posicionMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface.copy(alpha = 0.65f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                onActiveChange(path)
                audioPlayer.playOrPause(
                    path = path,
                    onStateChange = { playing, pos, dur ->
                        reproduciendo = playing
                        posicionMs = pos
                        totalMs = dur
                        if (!playing && pos >= dur) onActiveChange(null)
                    },
                    onCompleted = {
                        reproduciendo = false
                        posicionMs = 0
                        onActiveChange(null)
                    }
                )
            },
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MutedPurple)
        ) {
            Icon(
                imageVector = if (reproduciendo) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (reproduciendo) "Pausar" else "Reproducir",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = MutedPurpleLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = nombre ?: "Nota de voz",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (reproduciendo) {
                        MediaStorageHelper.formatearDuracion(posicionMs.toLong())
                    } else {
                        MediaStorageHelper.formatearDuracion(totalMs.toLong())
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedPurpleLight
                )
            }

            Spacer(Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { if (estaActivo) progreso else 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = MutedPurpleLight,
                trackColor = DarkOutline
            )
        }
    }
}

@Composable
fun DocumentoAttachmentCard(
    path: String,
    nombre: String?,
    tamanoBytes: Long
) {
    val context = LocalContext.current
    val esPdf = (nombre ?: path).lowercase().endsWith(".pdf")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface.copy(alpha = 0.75f))
            .border(1.dp, DarkOutline, RoundedCornerShape(12.dp))
            .clickable { MediaStorageHelper.abrirArchivoExterno(context, path, nombre) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (esPdf) Color(0xFF7D3232).copy(alpha = 0.45f) else MutedPurple.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (esPdf) Icons.Default.PictureAsPdf else Icons.Default.Description,
                contentDescription = null,
                tint = if (esPdf) Color(0xFFEF9A9A) else MutedPurpleLight,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = nombre ?: "Documento adjunto",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${MediaStorageHelper.formatearTamano(tamanoBytes)} · Toca para abrir",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = "Abrir documento",
            tint = MutedAccent,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun VisorImagenDialog(
    path: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.94f))
                .clickable(onClick = onDismiss)
        ) {
            AsyncImage(
                model = coilModelFor(path),
                contentDescription = "Imagen ampliada",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 16.dp)
                    .clip(CircleShape)
                    .background(DarkSurface.copy(alpha = 0.8f))
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
            }
        }
    }
}

@Composable
fun ReproductorVideoDialog(
    path: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
        ) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        val uri = if (path.startsWith("/")) Uri.fromFile(File(path)) else Uri.parse(path)
                        setVideoURI(uri)
                        val controller = MediaController(ctx)
                        controller.setAnchorView(this)
                        setMediaController(controller)
                        setOnPreparedListener { mp ->
                            mp.isLooping = false
                            start()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 16.dp)
                    .clip(CircleShape)
                    .background(DarkSurface.copy(alpha = 0.85f))
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar video", tint = Color.White)
            }
        }
    }
}

@Composable
fun rememberCameraPhotoLauncher(
    prefijo: String,
    onFotoCapturada: (MediaFileResult) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val callbackActual by rememberUpdatedState(onFotoCapturada)
    var rutaPendiente by rememberSaveable { mutableStateOf<String?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val path = rutaPendiente
        rutaPendiente = null
        if (path != null) {
            val file = File(path)
            if (success && file.exists() && file.length() > 0L) {
                callbackActual(
                    MediaFileResult(
                        localPath = file.absolutePath,
                        fileName = file.name,
                        sizeBytes = file.length(),
                        durationMs = 0L,
                        mimeType = "image/jpeg"
                    )
                )
            } else {
                file.delete()
            }
        }
    }

    val abrirCamara: () -> Unit = {
        try {
            val (file, contentUri) = MediaStorageHelper.crearArchivoFotoCamara(context, prefijo)
            rutaPendiente = file.absolutePath
            takePictureLauncher.launch(contentUri)
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir la cámara: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            abrirCamara()
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    return {
        val permiso = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permiso == PackageManager.PERMISSION_GRANTED) {
            abrirCamara()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}
