package com.example.redsocialnfc.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.redsocialnfc.data.Amigo
import com.example.redsocialnfc.data.Mensaje
import com.example.redsocialnfc.domain.AppDatabase
import com.example.redsocialnfc.ui.theme.DarkBackground
import com.example.redsocialnfc.ui.theme.DarkOutline
import com.example.redsocialnfc.ui.theme.DarkSurface
import com.example.redsocialnfc.ui.theme.DarkSurfaceVariant
import com.example.redsocialnfc.ui.theme.MutedAccent
import com.example.redsocialnfc.ui.theme.MutedGray
import com.example.redsocialnfc.ui.theme.MutedPurple
import com.example.redsocialnfc.ui.theme.MutedPurpleLight
import com.example.redsocialnfc.ui.theme.SubtleGrayPurple
import com.example.redsocialnfc.ui.theme.TextPrimary
import com.example.redsocialnfc.ui.theme.TextSecondary
import com.example.redsocialnfc.util.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val amigoDao = remember { db.amigoDao() }
    val mensajeDao = remember { db.mensajeDao() }
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    val miHandle = sessionManager.getRealHandle().ifEmpty { "@mi_usuario" }
    val amigos by amigoDao.obtenerTodos().collectAsState(initial = emptyList())
    var amigoSeleccionado by remember { mutableStateOf<Amigo?>(null) }
    var mostrarDialogoNfc by remember { mutableStateOf(false) }
    var avisoNuevoAmigo by remember { mutableStateOf<String?>(null) }

    // Precargar amigos iniciales si la lista está vacía
    LaunchedEffect(Unit) {
        if (amigoDao.contarTotal() == 0) {
            amigoDao.insertar(
                Amigo(
                    handle = "@carlos_dev",
                    nombre = "Carlos Mendoza",
                    nfcUuid = UUID.randomUUID().toString()
                )
            )
            amigoDao.insertar(
                Amigo(
                    handle = "@sofia_tec",
                    nombre = "Sofía Ramírez",
                    nfcUuid = UUID.randomUUID().toString()
                )
            )

            // Mensajes iniciales de prueba con @carlos_dev
            mensajeDao.insertar(
                Mensaje(
                    remitenteHandle = "@carlos_dev",
                    destinatarioHandle = miHandle,
                    texto = "¡Hola! Qué bien que pudimos sincronizar los teléfonos por NFC.",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 30
                )
            )
            mensajeDao.insertar(
                Mensaje(
                    remitenteHandle = miHandle,
                    destinatarioHandle = "@carlos_dev",
                    texto = "¡Excelente! Ya tenemos chat privado directo entre nosotros.",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 20
                )
            )
        }
    }

    if (amigoSeleccionado == null) {
        // VISTA 1: Lista de Amigos Sincronizados
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Chats con Amigos",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                "Conexiones verificadas por NFC",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { mostrarDialogoNfc = true }) {
                            Icon(
                                imageVector = Icons.Default.Nfc,
                                contentDescription = "Sincronizar nuevo amigo",
                                tint = MutedPurpleLight
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { mostrarDialogoNfc = true },
                    containerColor = MutedPurple,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Nfc, contentDescription = "Vincular nuevo amigo")
                }
            },
            containerColor = DarkBackground
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Banner de aviso tras sincronizar
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
                                tint = MutedPurple,
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
                                Text("Cerrar", color = MutedPurple, fontSize = 11.sp)
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
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = DarkOutline,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Aún no tienes amigos vinculados por NFC",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(amigos, key = { it.id }) { amigo ->
                            TarjetaAmigoChat(
                                amigo = amigo,
                                onClick = { amigoSeleccionado = amigo }
                            )
                        }
                    }
                }
            }
        }
    } else {
        // VISTA 2: Conversación Privada con el Amigo Seleccionado
        val amigo = amigoSeleccionado!!
        val mensajesConversacion by mensajeDao.obtenerConversacion(miHandle, amigo.handle)
            .collectAsState(initial = emptyList())
        val chatListState = rememberLazyListState()
        var textoMensaje by remember { mutableStateOf("") }

        LaunchedEffect(mensajesConversacion.size) {
            if (mensajesConversacion.isNotEmpty()) {
                chatListState.animateScrollToItem(mensajesConversacion.size - 1)
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { amigoSeleccionado = null }) {
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
                                    text = "${amigo.handle} · Conectado",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedPurpleLight
                                )
                            }
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
            ) {
                // Mensajes de la conversación privada
                LazyColumn(
                    state = chatListState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(mensajesConversacion, key = { it.id }) { mensaje ->
                        val esMio = mensaje.remitenteHandle == miHandle
                        BurbujaChatAmigo(
                            mensaje = mensaje,
                            esMio = esMio
                        )
                    }
                }

                // Barra de texto inferior
                Surface(
                    color = DarkSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = textoMensaje,
                            onValueChange = { textoMensaje = it },
                            placeholder = {
                                Text("Mensaje a ${amigo.handle}...", color = TextSecondary, fontSize = 14.sp)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
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

                        IconButton(
                            onClick = {
                                if (textoMensaje.isNotBlank()) {
                                    val textoAEnviar = textoMensaje.trim()
                                    textoMensaje = ""
                                    scope.launch {
                                        mensajeDao.insertar(
                                            Mensaje(
                                                remitenteHandle = miHandle,
                                                destinatarioHandle = amigo.handle,
                                                texto = textoAEnviar
                                            )
                                        )
                                    }
                                }
                            },
                            enabled = textoMensaje.isNotBlank(),
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (textoMensaje.isNotBlank()) MutedPurple else DarkSurfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Enviar",
                                tint = if (textoMensaje.isNotBlank()) Color.White else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal para simular Handshake NFC y agregar a un nuevo amigo
    if (mostrarDialogoNfc) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoNfc = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Nfc,
                    contentDescription = null,
                    tint = MutedPurpleLight,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    "Vincular Amigo por NFC",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        "Acerca tu teléfono al de tu amigo para sincronizar identificadores por hardware y habilitar el chat privado directo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(14.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SubtleGrayPurple),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MutedPurple, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Valeria Moreno", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                                Text("@valeria_m · Antena detectada", style = MaterialTheme.typography.labelSmall, color = MutedPurpleLight)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val nuevoAmigo = Amigo(
                                handle = "@valeria_m",
                                nombre = "Valeria Moreno",
                                nfcUuid = UUID.randomUUID().toString()
                            )
                            amigoDao.insertar(nuevoAmigo)
                            mensajeDao.insertar(
                                Mensaje(
                                    remitenteHandle = "@valeria_m",
                                    destinatarioHandle = miHandle,
                                    texto = "¡Hola! Ya estamos conectados por proximidad NFC."
                                )
                            )
                            avisoNuevoAmigo = "¡Conexión NFC exitosa! Se agregó @valeria_m a tus chats."
                            mostrarDialogoNfc = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MutedPurple)
                ) {
                    Text("Completar Choque NFC", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoNfc = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
private fun TarjetaAmigoChat(
    amigo: Amigo,
    onClick: () -> Unit
) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
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

                Column {
                    Text(
                        text = amigo.nombre,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = amigo.handle,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MutedPurple.copy(alpha = 0.15f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Nfc,
                        contentDescription = null,
                        tint = MutedPurpleLight,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "NFC Verificado",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MutedPurpleLight
                    )
                }
            }
        }
    }
}

@Composable
private fun BurbujaChatAmigo(
    mensaje: Mensaje,
    esMio: Boolean
) {
    val formatoHora = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val horaLegible = remember(mensaje.timestamp) { formatoHora.format(Date(mensaje.timestamp)) }

    val colorBurbuja = if (esMio) MutedPurple.copy(alpha = 0.5f) else DarkSurfaceVariant
    val colorBorde = if (esMio) MutedPurple else DarkOutline

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (esMio) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
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
                .padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            Column {
                Text(
                    text = mensaje.texto,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = horaLegible,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
