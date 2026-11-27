package com.example.redsocialnfc.presentation

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.redsocialnfc.data.Amigo
import com.example.redsocialnfc.data.MediaTipoChat
import com.example.redsocialnfc.data.Mensaje
import com.example.redsocialnfc.domain.AppDatabase
import com.example.redsocialnfc.network.LocalP2pSyncManager
import com.example.redsocialnfc.presentation.components.AudioBubblePlayer
import com.example.redsocialnfc.presentation.components.DialogNfcHandshake
import com.example.redsocialnfc.presentation.components.DocumentoAttachmentCard
import com.example.redsocialnfc.presentation.components.FotoPreviewCard
import com.example.redsocialnfc.presentation.components.ReproductorVideoDialog
import com.example.redsocialnfc.presentation.components.VideoPreviewCard
import com.example.redsocialnfc.presentation.components.VisorImagenDialog
import com.example.redsocialnfc.presentation.components.rememberCameraPhotoLauncher
import com.example.redsocialnfc.ui.theme.DarkBackground
import com.example.redsocialnfc.ui.theme.DarkOutline
import com.example.redsocialnfc.ui.theme.DarkSurface
import com.example.redsocialnfc.ui.theme.DarkSurfaceVariant
import com.example.redsocialnfc.ui.theme.ErrorColor
import com.example.redsocialnfc.ui.theme.MutedGray
import com.example.redsocialnfc.ui.theme.MutedPurple
import com.example.redsocialnfc.ui.theme.MutedPurpleLight
import com.example.redsocialnfc.ui.theme.SubtleGrayPurple
import com.example.redsocialnfc.ui.theme.TextPrimary
import com.example.redsocialnfc.ui.theme.TextSecondary
import com.example.redsocialnfc.util.MediaStorageHelper
import com.example.redsocialnfc.util.SessionManager
import com.example.redsocialnfc.util.SimpleAudioPlayer
import com.example.redsocialnfc.util.VoiceNoteRecorder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onEnConversacionChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val amigoDao = remember { db.amigoDao() }
    val mensajeDao = remember { db.mensajeDao() }
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    val miHandle = sessionManager.getRealHandle().ifEmpty { "@mi_usuario" }
    val ocultarEstadoEnLinea = sessionManager.getSetting(SessionManager.PREF_OCULTAR_EN_LINEA, false)

    val amigos by amigoDao.obtenerTodos().collectAsState(initial = emptyList())
    val todosMisMensajes by mensajeDao.observarTodosParaUsuario(miHandle).collectAsState(initial = emptyList())

    val ultimoMensajePorAmigo = remember(todosMisMensajes, miHandle) {
        val map = mutableMapOf<String, Mensaje>()
        for (msg in todosMisMensajes) {
            val amigoHandle = if (msg.remitenteHandle == miHandle) msg.destinatarioHandle else msg.remitenteHandle
            if (!map.containsKey(amigoHandle)) {
                map[amigoHandle] = msg
            }
        }
        map
    }

    val noLeidosPorAmigo = remember(todosMisMensajes, miHandle) {
        todosMisMensajes
            .filter { it.destinatarioHandle == miHandle && !it.leido }
            .groupingBy { it.remitenteHandle }
            .eachCount()
    }

    var amigoSeleccionado by remember { mutableStateOf<Amigo?>(null) }
    var amigoParaEliminar by remember { mutableStateOf<Amigo?>(null) }
    var mostrarBusquedaChat by remember { mutableStateOf(false) }
    var queryBusquedaChat by remember { mutableStateOf("") }
    var mostrarDialogoAgregarContacto by remember { mutableStateOf(false) }
    var avisoNuevoAmigo by remember { mutableStateOf<String?>(null) }

    val amigosFiltrados = remember(amigos, queryBusquedaChat) {
        val q = queryBusquedaChat.trim().lowercase()
        if (q.isEmpty()) amigos else amigos.filter {
            it.nombre.lowercase().contains(q) || it.handle.lowercase().contains(q)
        }
    }

    val audioPlayer = remember { SimpleAudioPlayer() }
    var activeAudioPath by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(amigoSeleccionado) {
        onEnConversacionChange(amigoSeleccionado != null)
        if (amigoSeleccionado == null) {
            audioPlayer.stop()
            activeAudioPath = null
        }
    }

    BackHandler(enabled = amigoSeleccionado != null) {
        amigoSeleccionado = null
    }

    DisposableEffect(Unit) {
        onDispose {
            onEnConversacionChange(false)
            audioPlayer.stop()
        }
    }

    if (amigoSeleccionado == null) {
        // VISTA 1: Lista de Chats / Amigos (Vacía por defecto)
        Scaffold(
            topBar = {
                Column(modifier = Modifier.background(DarkSurface)) {
                    TopAppBar(
                        title = {
                            val peersP2p by LocalP2pSyncManager.peersConectados.collectAsState()
                            Column {
                                Text(
                                    "Chats",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = buildString {
                                        append(if (amigos.isEmpty()) "Sin contactos vinculados" else "${amigos.size} contacto(s)")
                                        if (peersP2p.isNotEmpty()) append(" · ${peersP2p.size} cercano(s) Wi-Fi")
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        },
                        actions = {
                            if (amigos.isNotEmpty()) {
                                IconButton(onClick = {
                                    mostrarBusquedaChat = !mostrarBusquedaChat
                                    if (!mostrarBusquedaChat) queryBusquedaChat = ""
                                }) {
                                    Icon(
                                        imageVector = if (mostrarBusquedaChat) Icons.Default.Close else Icons.Default.Search,
                                        contentDescription = "Buscar chat",
                                        tint = MutedPurpleLight
                                    )
                                }
                            }
                            IconButton(onClick = { mostrarDialogoAgregarContacto = true }) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = "Agregar contacto",
                                    tint = MutedPurpleLight
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
                    )

                    AnimatedVisibility(visible = mostrarBusquedaChat && amigos.isNotEmpty()) {
                        OutlinedTextField(
                            value = queryBusquedaChat,
                            onValueChange = { queryBusquedaChat = it },
                            placeholder = { Text("Buscar contacto o @usuario...", color = TextSecondary) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = MutedPurpleLight)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MutedPurple,
                                unfocusedBorderColor = DarkOutline,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = DarkBackground,
                                unfocusedContainerColor = DarkBackground
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { mostrarDialogoAgregarContacto = true },
                    containerColor = MutedPurple,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Nuevo chat")
                }
            },
            containerColor = DarkBackground
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                AnimatedVisibility(visible = avisoNuevoAmigo != null) {
                    Surface(
                        color = SubtleGrayPurple,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MutedPurpleLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = avisoNuevoAmigo ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { avisoNuevoAmigo = null }) {
                                Text("Cerrar", color = MutedPurpleLight, fontSize = 11.sp)
                            }
                        }
                    }
                }

                if (amigos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                tint = DarkOutline,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = "Tus chats están vacíos",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Aún no tienes amigos agregados. Vincula un contacto para iniciar un chat privado con fotos, videos, audios y documentos.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(18.dp))
                            Button(
                                onClick = { mostrarDialogoAgregarContacto = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MutedPurple),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Icon(Icons.Default.Nfc, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Vincular amigo por NFC", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else if (amigosFiltrados.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontró ningún contacto con \"$queryBusquedaChat\".",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(amigosFiltrados, key = { it.id }) { amigo ->
                            TarjetaAmigoChat(
                                amigo = amigo,
                                ultimoMensaje = ultimoMensajePorAmigo[amigo.handle],
                                noLeidosCount = noLeidosPorAmigo[amigo.handle] ?: 0,
                                onClick = { amigoSeleccionado = amigo },
                                onEliminar = { amigoParaEliminar = amigo }
                            )
                        }
                    }
                }
            }
        }
    } else {
        // VISTA 2: Conversación privada 1-a-1 con adjuntos multimedia
        val amigo = amigoSeleccionado!!
        ConversacionPrivadaView(
            amigo = amigo,
            miHandle = miHandle,
            ocultarEstadoEnLinea = ocultarEstadoEnLinea,
            mensajeDao = mensajeDao,
            audioPlayer = audioPlayer,
            activeAudioPath = activeAudioPath,
            onActiveAudioChange = { activeAudioPath = it },
            onBack = { amigoSeleccionado = null }
        )
    }

    amigoParaEliminar?.let { amigo ->
        AlertDialog(
            onDismissRequest = { amigoParaEliminar = null },
            containerColor = DarkSurface,
            title = { Text("¿Eliminar contacto?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Se eliminará a ${amigo.nombre} (${amigo.handle}) junto con toda la conversación.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            mensajeDao.eliminarConversacion(miHandle, amigo.handle)
                            amigoDao.eliminar(amigo)
                            amigoParaEliminar = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { amigoParaEliminar = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    if (mostrarDialogoAgregarContacto) {
        DialogNfcHandshake(
            sessionManager = sessionManager,
            onDismiss = { mostrarDialogoAgregarContacto = false },
            onAmigoVinculado = { nuevoAmigo ->
                avisoNuevoAmigo = "¡Amigo vinculado por NFC: ${nuevoAmigo.handle}!"
                amigoSeleccionado = nuevoAmigo
                mostrarDialogoAgregarContacto = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversacionPrivadaView(
    amigo: Amigo,
    miHandle: String,
    ocultarEstadoEnLinea: Boolean,
    mensajeDao: com.example.redsocialnfc.data.MensajeDao,
    audioPlayer: SimpleAudioPlayer,
    activeAudioPath: String?,
    onActiveAudioChange: (String?) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mensajesConversacion by mensajeDao.obtenerConversacion(miHandle, amigo.handle)
        .collectAsState(initial = emptyList())
    val chatListState = rememberLazyListState()

    LaunchedEffect(amigo.handle, mensajesConversacion.size) {
        mensajeDao.marcarComoLeidos(miHandle, amigo.handle)
    }

    var textoMensaje by remember { mutableStateOf("") }
    var mostrarPanelAdjuntos by remember { mutableStateOf(false) }
    var confirmarVaciarChat by remember { mutableStateOf(false) }
    var imagenAmpliada by remember { mutableStateOf<String?>(null) }
    var videoReproducir by remember { mutableStateOf<String?>(null) }

    // Grabación de nota de voz
    val voiceRecorder = remember { VoiceNoteRecorder(context) }
    var grabandoVoz by remember { mutableStateOf(false) }
    var segundosGrabacion by remember { mutableIntStateOf(0) }

    val iniciarGrabacion = {
        audioPlayer.stop()
        onActiveAudioChange(null)
        if (voiceRecorder.iniciarGrabacion()) {
            grabandoVoz = true
            segundosGrabacion = 0
        } else {
            Toast.makeText(context, "No se pudo acceder al micrófono", Toast.LENGTH_SHORT).show()
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            iniciarGrabacion()
        } else {
            Toast.makeText(context, "Permiso de micrófono denegado", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(grabandoVoz) {
        while (grabandoVoz) {
            delay(1000)
            segundosGrabacion++
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (grabandoVoz) {
                voiceRecorder.detenerYDescartar()
            }
        }
    }

    // Launchers de archivos multimedia para Chat
    val enviarImagenLocal: (com.example.redsocialnfc.util.MediaFileResult) -> Unit = { res ->
        scope.launch {
            val caption = textoMensaje.trim()
            textoMensaje = ""
            mensajeDao.insertar(
                Mensaje(
                    remitenteHandle = miHandle,
                    destinatarioHandle = amigo.handle,
                    texto = caption,
                    mediaTipo = MediaTipoChat.IMAGEN,
                    mediaUri = res.localPath,
                    mediaNombre = res.fileName,
                    mediaTamanoBytes = res.sizeBytes
                )
            )
            mostrarPanelAdjuntos = false
        }
    }

    val lanzarCamaraChat = rememberCameraPhotoLauncher(prefijo = "chat_cam") { res ->
        enviarImagenLocal(res)
    }

    val pickFotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val res = MediaStorageHelper.copiarUriAAlmacenamientoLocal(context, uri, prefijo = "chat_img")
                if (res != null) {
                    enviarImagenLocal(res)
                }
            }
        }
    }

    val pickVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val res = MediaStorageHelper.copiarUriAAlmacenamientoLocal(
                    context, uri, prefijo = "chat_vid", extraerDuracion = true
                )
                if (res != null) {
                    val caption = textoMensaje.trim()
                    textoMensaje = ""
                    mensajeDao.insertar(
                        Mensaje(
                            remitenteHandle = miHandle,
                            destinatarioHandle = amigo.handle,
                            texto = caption,
                            mediaTipo = MediaTipoChat.VIDEO,
                            mediaUri = res.localPath,
                            mediaNombre = res.fileName,
                            mediaTamanoBytes = res.sizeBytes,
                            duracionMs = res.durationMs
                        )
                    )
                    mostrarPanelAdjuntos = false
                }
            }
        }
    }

    val pickAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val res = MediaStorageHelper.copiarUriAAlmacenamientoLocal(
                    context, uri, prefijo = "chat_aud", extraerDuracion = true
                )
                if (res != null) {
                    mensajeDao.insertar(
                        Mensaje(
                            remitenteHandle = miHandle,
                            destinatarioHandle = amigo.handle,
                            mediaTipo = MediaTipoChat.AUDIO,
                            mediaUri = res.localPath,
                            mediaNombre = res.fileName,
                            mediaTamanoBytes = res.sizeBytes,
                            duracionMs = res.durationMs
                        )
                    )
                    mostrarPanelAdjuntos = false
                }
            }
        }
    }

    val pickDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val res = MediaStorageHelper.copiarUriAAlmacenamientoLocal(context, uri, prefijo = "chat_doc")
                if (res != null) {
                    mensajeDao.insertar(
                        Mensaje(
                            remitenteHandle = miHandle,
                            destinatarioHandle = amigo.handle,
                            mediaTipo = MediaTipoChat.DOCUMENTO,
                            mediaUri = res.localPath,
                            mediaNombre = res.fileName,
                            mediaTamanoBytes = res.sizeBytes
                        )
                    )
                    mostrarPanelAdjuntos = false
                }
            }
        }
    }

    LaunchedEffect(mensajesConversacion.size) {
        if (mensajesConversacion.isNotEmpty()) {
            chatListState.animateScrollToItem(mensajesConversacion.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar a chats",
                            tint = TextPrimary
                        )
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MutedPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = amigo.nombre.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = amigo.nombre,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = if (ocultarEstadoEnLinea) amigo.handle else "${amigo.handle} · Chat directo",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedPurpleLight
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { confirmarVaciarChat = true }) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Vaciar chat",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            if (mensajesConversacion.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sin mensajes todavía.\nEnvía texto, fotos, videos, audios o documentos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    state = chatListState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(mensajesConversacion, key = { it.id }) { mensaje ->
                        BurbujaChatMultimedia(
                            mensaje = mensaje,
                            esMio = mensaje.remitenteHandle == miHandle,
                            audioPlayer = audioPlayer,
                            activeAudioPath = activeAudioPath,
                            onActiveAudioChange = onActiveAudioChange,
                            onAbrirImagen = { imagenAmpliada = it },
                            onAbrirVideo = { videoReproducir = it },
                            onEliminarMensaje = {
                                scope.launch {
                                    mensajeDao.eliminarPorId(mensaje.id)
                                }
                            }
                        )
                    }
                }
            }

            // Panel desplegable de adjuntos multimedia (Foto, Video, Audio, Documento)
            AnimatedVisibility(visible = mostrarPanelAdjuntos) {
                Surface(
                    color = DarkSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        OpcionAdjuntoChat(
                            icono = Icons.Default.PhotoCamera,
                            etiqueta = "Cámara",
                            color = Color(0xFFF48FB1),
                            onClick = lanzarCamaraChat
                        )
                        OpcionAdjuntoChat(
                            icono = Icons.Default.Image,
                            etiqueta = "Foto",
                            color = MutedPurpleLight,
                            onClick = {
                                pickFotoLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                        OpcionAdjuntoChat(
                            icono = Icons.Default.Videocam,
                            etiqueta = "Video",
                            color = Color(0xFF9FA8DA),
                            onClick = {
                                pickVideoLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            }
                        )
                        OpcionAdjuntoChat(
                            icono = Icons.Default.AudioFile,
                            etiqueta = "Audio",
                            color = Color(0xFF80CBC4),
                            onClick = {
                                pickAudioLauncher.launch(arrayOf("audio/*"))
                            }
                        )
                        OpcionAdjuntoChat(
                            icono = Icons.Default.Description,
                            etiqueta = "Documento",
                            color = Color(0xFFFFCC80),
                            onClick = {
                                pickDocLauncher.launch(
                                    arrayOf(
                                        "application/pdf",
                                        "application/msword",
                                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                        "text/plain",
                                        "*/*"
                                    )
                                )
                            }
                        )
                    }
                }
            }

            // Barra inferior de envío (Texto / Adjunto / Nota de Voz)
            Surface(
                color = DarkSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (grabandoVoz) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(ErrorColor)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Grabando audio... ${MediaStorageHelper.formatearDuracion(segundosGrabacion * 1000L)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                        }
                        Row {
                            IconButton(
                                onClick = {
                                    voiceRecorder.detenerYDescartar()
                                    grabandoVoz = false
                                }
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cancelar audio", tint = ErrorColor)
                            }
                            IconButton(
                                onClick = {
                                    grabandoVoz = false
                                    val res = voiceRecorder.detenerYGuardar()
                                    if (res != null) {
                                        scope.launch {
                                            mensajeDao.insertar(
                                                Mensaje(
                                                    remitenteHandle = miHandle,
                                                    destinatarioHandle = amigo.handle,
                                                    mediaTipo = MediaTipoChat.AUDIO,
                                                    mediaUri = res.localPath,
                                                    mediaNombre = "Nota de voz",
                                                    mediaTamanoBytes = res.sizeBytes,
                                                    duracionMs = res.durationMs
                                                )
                                            )
                                        }
                                    } else {
                                        Toast.makeText(context, "Nota de voz demasiado corta", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MutedPurple)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "Enviar nota de voz", tint = Color.White)
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { mostrarPanelAdjuntos = !mostrarPanelAdjuntos }) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "Adjuntar archivo",
                                tint = if (mostrarPanelAdjuntos) MutedPurpleLight else TextSecondary
                            )
                        }

                        OutlinedTextField(
                            value = textoMensaje,
                            onValueChange = { textoMensaje = it },
                            placeholder = {
                                Text(
                                    "Mensaje a ${amigo.handle}...",
                                    color = TextSecondary,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            maxLines = 4,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MutedPurple,
                                unfocusedBorderColor = DarkOutline,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = DarkBackground,
                                unfocusedContainerColor = DarkBackground
                            )
                        )

                        Spacer(Modifier.width(8.dp))

                        if (textoMensaje.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    val textoAEnviar = textoMensaje.trim()
                                    textoMensaje = ""
                                    mostrarPanelAdjuntos = false
                                    scope.launch {
                                        val msg = Mensaje(
                                            remitenteHandle = miHandle,
                                            destinatarioHandle = amigo.handle,
                                            texto = textoAEnviar,
                                            mediaTipo = MediaTipoChat.TEXTO
                                        )
                                        mensajeDao.insertar(msg)
                                        LocalP2pSyncManager.enviarMensaje(msg)
                                    }
                                },
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(MutedPurple)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Enviar mensaje",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    val permiso = ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.RECORD_AUDIO
                                    )
                                    if (permiso == PackageManager.PERMISSION_GRANTED) {
                                        iniciarGrabacion()
                                    } else {
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Grabar nota de voz",
                                    tint = MutedPurpleLight,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmarVaciarChat) {
        AlertDialog(
            onDismissRequest = { confirmarVaciarChat = false },
            containerColor = DarkSurface,
            title = { Text("¿Vaciar chat?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Se eliminarán todos los mensajes con ${amigo.nombre} (${amigo.handle}).",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            mensajeDao.eliminarConversacion(miHandle, amigo.handle)
                            confirmarVaciarChat = false
                            Toast.makeText(context, "Mensajes vaciados", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                ) {
                    Text("Vaciar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarVaciarChat = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    imagenAmpliada?.let { path ->
        VisorImagenDialog(path = path, onDismiss = { imagenAmpliada = null })
    }

    videoReproducir?.let { path ->
        ReproductorVideoDialog(path = path, onDismiss = { videoReproducir = null })
    }
}

@Composable
private fun OpcionAdjuntoChat(
    icono: ImageVector,
    etiqueta: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.16f))
                .border(1.dp, color.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icono, contentDescription = etiqueta, tint = color, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.height(5.dp))
        Text(text = etiqueta, style = MaterialTheme.typography.labelSmall, color = TextPrimary)
    }
}

@Composable
private fun TarjetaAmigoChat(
    amigo: Amigo,
    ultimoMensaje: Mensaje?,
    noLeidosCount: Int = 0,
    onClick: () -> Unit,
    onEliminar: () -> Unit
) {
    val resumenUltimo = when (ultimoMensaje?.mediaTipo) {
        MediaTipoChat.IMAGEN -> "Foto adjunta"
        MediaTipoChat.VIDEO -> "Video adjunto"
        MediaTipoChat.AUDIO -> "Audio / Nota de voz"
        MediaTipoChat.DOCUMENTO -> ultimoMensaje.mediaNombre ?: "Documento adjunto"
        MediaTipoChat.TEXTO -> ultimoMensaje.texto
        else -> amigo.handle
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, DarkOutline, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MutedPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = amigo.nombre.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = amigo.nombre,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = resumenUltimo,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (noLeidosCount > 0) {
                    Box(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clip(CircleShape)
                            .background(MutedPurple)
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$noLeidosCount",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                if (ultimoMensaje != null) {
                    Text(
                        text = MediaStorageHelper.formatearTiempoRelativo(ultimoMensaje.timestamp),
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
                IconButton(onClick = onEliminar, modifier = Modifier.size(30.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar contacto",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BurbujaChatMultimedia(
    mensaje: Mensaje,
    esMio: Boolean,
    audioPlayer: SimpleAudioPlayer,
    activeAudioPath: String?,
    onActiveAudioChange: (String?) -> Unit,
    onAbrirImagen: (String) -> Unit,
    onAbrirVideo: (String) -> Unit,
    onEliminarMensaje: () -> Unit
) {
    val context = LocalContext.current
    val formatoHora = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val horaLegible = remember(mensaje.timestamp) { formatoHora.format(Date(mensaje.timestamp)) }
    var menuMensajeAbierto by remember { mutableStateOf(false) }

    val colorBurbuja = if (esMio) MutedPurple.copy(alpha = 0.48f) else DarkSurfaceVariant
    val colorBorde = if (esMio) MutedPurple else DarkOutline

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (esMio) Alignment.End else Alignment.Start
    ) {
        Box {
            Box(
                modifier = Modifier
                    .widthIn(min = 120.dp, max = 290.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (esMio) 16.dp else 4.dp,
                            bottomEnd = if (esMio) 4.dp else 16.dp
                        )
                    )
                    .background(colorBurbuja)
                    .border(
                        width = 1.dp,
                        color = colorBorde,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (esMio) 16.dp else 4.dp,
                            bottomEnd = if (esMio) 4.dp else 16.dp
                        )
                    )
                    .combinedClickable(
                        onClick = {},
                        onLongClick = { menuMensajeAbierto = true }
                    )
                    .padding(horizontal = 12.dp, vertical = 9.dp)
            ) {
                Column {
                    val uri = mensaje.mediaUri
                    if (!uri.isNullOrBlank()) {
                        when (mensaje.mediaTipo) {
                            MediaTipoChat.IMAGEN -> {
                                FotoPreviewCard(path = uri, maxHeight = 200.dp, onClick = { onAbrirImagen(uri) })
                                Spacer(Modifier.height(6.dp))
                            }
                            MediaTipoChat.VIDEO -> {
                                VideoPreviewCard(
                                    path = uri,
                                    duracionMs = mensaje.duracionMs,
                                    maxHeight = 190.dp,
                                    onClick = { onAbrirVideo(uri) }
                                )
                                Spacer(Modifier.height(6.dp))
                            }
                            MediaTipoChat.AUDIO -> {
                                AudioBubblePlayer(
                                    path = uri,
                                    nombre = mensaje.mediaNombre,
                                    duracionMs = mensaje.duracionMs,
                                    audioPlayer = audioPlayer,
                                    activeAudioPath = activeAudioPath,
                                    onActiveChange = onActiveAudioChange
                                )
                                Spacer(Modifier.height(4.dp))
                            }
                            MediaTipoChat.DOCUMENTO -> {
                                DocumentoAttachmentCard(
                                    path = uri,
                                    nombre = mensaje.mediaNombre,
                                    tamanoBytes = mensaje.mediaTamanoBytes
                                )
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                    }

                    if (mensaje.texto.isNotBlank()) {
                        Text(
                            text = mensaje.texto,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(4.dp))
                    }

                    Text(
                        text = horaLegible,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = TextSecondary,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            DropdownMenu(
                expanded = menuMensajeAbierto,
                onDismissRequest = { menuMensajeAbierto = false },
                containerColor = DarkSurface
            ) {
                if (mensaje.texto.isNotBlank()) {
                    DropdownMenuItem(
                        text = { Text("Copiar texto", color = TextPrimary) },
                        leadingIcon = {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = MutedPurpleLight)
                        },
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Mensaje", mensaje.texto))
                            Toast.makeText(context, "Texto copiado", Toast.LENGTH_SHORT).show()
                            menuMensajeAbierto = false
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Eliminar mensaje", color = ErrorColor) },
                    leadingIcon = {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = ErrorColor)
                    },
                    onClick = {
                        menuMensajeAbierto = false
                        onEliminarMensaje()
                    }
                )
            }
        }
    }
}

@Composable
private fun AgregarContactoDialog(
    onDismiss: () -> Unit,
    onAgregar: (nombre: String, handle: String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var handle by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        icon = {
            Icon(
                imageVector = Icons.Default.Nfc,
                contentDescription = null,
                tint = MutedPurpleLight,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                "Agregar Contacto a Chats",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Los chats inician vacíos. Agrega un contacto manualmente para probar mensajes, audios, fotos, videos y documentos (en el siguiente paso lo conectaremos con NFC real).",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { if (it.length <= 30) nombre = it },
                    label = { Text("Nombre del contacto") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MutedPurple,
                        unfocusedBorderColor = DarkOutline,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = handle,
                    onValueChange = { input ->
                        handle = input.filter { it.isLetterOrDigit() || it == '_' }
                    },
                    label = { Text("Usuario") },
                    prefix = { Text("@", color = MutedPurpleLight, fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MutedPurple,
                        unfocusedBorderColor = DarkOutline,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            val valido = nombre.isNotBlank() && handle.isNotBlank()
            Button(
                onClick = { onAgregar(nombre, handle) },
                enabled = valido,
                colors = ButtonDefaults.buttonColors(containerColor = MutedPurple)
            ) {
                Text("Agregar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}
