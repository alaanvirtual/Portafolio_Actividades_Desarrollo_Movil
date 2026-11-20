package com.example.redsocialnfc.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val dao = remember { db.mensajeDao() }
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    val mensajes by dao.obtenerTodos().collectAsState(initial = emptyList())
    val listState = rememberLazyListState()

    var textoInput by remember { mutableStateOf("") }
    var enviarComoAnonimo by remember { mutableStateOf(false) }
    var mostrarDialogoNfc by remember { mutableStateOf(false) }
    var mensajeSincronizado by remember { mutableStateOf<String?>(null) }

    // Precargar mensajes iniciales si el canal está vacío
    LaunchedEffect(Unit) {
        if (dao.contarTotal() == 0) {
            dao.insertar(
                Mensaje(
                    remitenteId = 101,
                    remitenteHandle = "@carlos_dev",
                    remitenteAnonimo = "@Anonimo_12A",
                    texto = "¡Hola a todos! Este es el canal de chat de proximidad local.",
                    esAmigoDirecto = true,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 15
                )
            )
            dao.insertar(
                Mensaje(
                    remitenteId = 102,
                    remitenteHandle = "@sofia_tec",
                    remitenteAnonimo = "@Anonimo_89B",
                    texto = "Enviando mensaje anónimo desde el círculo cercano. ¿Alguien cerca para sincronizar por NFC?",
                    esAmigoDirecto = false,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 5
                )
            )
        }
    }

    // Auto-scroll al recibir un nuevo mensaje
    LaunchedEffect(mensajes.size) {
        if (mensajes.isNotEmpty()) {
            listState.animateScrollToItem(mensajes.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MutedPurple)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Canal de Proximidad",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }
                        Text(
                            "Dispositivos cercanos · Radio activo",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                actions = {
                    // Botón para simular / activar Handshake NFC
                    IconButton(onClick = { mostrarDialogoNfc = true }) {
                        Icon(
                            imageVector = Icons.Default.Nfc,
                            contentDescription = "Sincronizar por Proximidad",
                            tint = MutedPurpleLight
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
        ) {
            // Banner de estado de revelación o sincronización
            AnimatedVisibility(visible = mensajeSincronizado != null) {
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
                            text = mensajeSincronizado ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { mensajeSincronizado = null }) {
                            Text("OK", color = MutedPurple, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Lista de mensajes (Burbujas de Chat)
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(mensajes, key = { it.id }) { mensaje ->
                    val esMio = mensaje.remitenteId == sessionManager.getUserId()
                    BurbujaMensaje(
                        mensaje = mensaje,
                        esMio = esMio
                    )
                }
            }

            // Barra inferior para redactar mensajes
            Surface(
                color = DarkSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    // Selector de modo de identidad (Real vs Anónimo)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Enviar como:",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Spacer(Modifier.width(8.dp))

                            FilterChip(
                                selected = !enviarComoAnonimo,
                                onClick = { enviarComoAnonimo = false },
                                label = { Text(sessionManager.getRealHandle().ifEmpty { "@usuario" }, fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(12.dp))
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MutedPurple,
                                    selectedLabelColor = Color.White,
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = TextSecondary
                                )
                            )

                            Spacer(Modifier.width(6.dp))

                            FilterChip(
                                selected = enviarComoAnonimo,
                                onClick = { enviarComoAnonimo = true },
                                label = { Text(sessionManager.getAnonHandle().ifEmpty { "@Anonimo" }, fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.VisibilityOff, contentDescription = null, modifier = Modifier.size(12.dp))
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MutedGray,
                                    selectedLabelColor = Color.White,
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    // Campo de entrada y botón de envío
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = textoInput,
                            onValueChange = { textoInput = it },
                            placeholder = {
                                Text(
                                    if (enviarComoAnonimo) "Mensaje en modo anónimo..." else "Mensaje visible...",
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (enviarComoAnonimo) MutedGray else MutedPurple,
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
                                if (textoInput.isNotBlank()) {
                                    val textoAEnviar = textoInput.trim()
                                    textoInput = ""
                                    scope.launch {
                                        dao.insertar(
                                            Mensaje(
                                                remitenteId = sessionManager.getUserId(),
                                                remitenteHandle = sessionManager.getRealHandle(),
                                                remitenteAnonimo = sessionManager.getAnonHandle(),
                                                texto = textoAEnviar,
                                                esAmigoDirecto = !enviarComoAnonimo,
                                                timestamp = System.currentTimeMillis()
                                            )
                                        )
                                    }
                                }
                            },
                            enabled = textoInput.isNotBlank(),
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (textoInput.isNotBlank()) MutedPurple else DarkSurfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Enviar",
                                tint = if (textoInput.isNotBlank()) Color.White else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Diálogo para simular el Handshake NFC y la Revelación
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
                        "Sincronización por Proximidad",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                },
                text = {
                    Column {
                        Text(
                            "Acerca tu teléfono a otro dispositivo para realizar el handshake por la antena NFC.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "Dispositivo detectado en rango:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MutedPurple
                        )
                        Spacer(Modifier.height(4.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SubtleGrayPurple),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MutedPurple, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text("@Anonimo_89B", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace), color = TextPrimary)
                                    Text("Identidad real oculta hasta chocar teléfonos", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                dao.revelarUsuario("@Anonimo_89B")
                                mensajeSincronizado = "¡Handshake completado! @Anonimo_89B fue verificado como @sofia_tec."
                                mostrarDialogoNfc = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MutedPurple)
                    ) {
                        Text("Simular Choque NFC", color = Color.White)
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
}

@Composable
private fun BurbujaMensaje(
    mensaje: Mensaje,
    esMio: Boolean
) {
    val formatoHora = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val horaLegible = remember(mensaje.timestamp) { formatoHora.format(Date(mensaje.timestamp)) }

    val colorBurbuja = when {
        esMio -> MutedPurple.copy(alpha = 0.45f)
        mensaje.esAmigoDirecto -> DarkSurfaceVariant
        else -> SubtleGrayPurple
    }

    val colorBorde = when {
        esMio -> MutedPurple
        mensaje.esAmigoDirecto -> MutedPurple.copy(alpha = 0.5f)
        else -> MutedGray.copy(alpha = 0.4f)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (esMio) Alignment.End else Alignment.Start
    ) {
        // Encabezado del remitente
        if (!esMio) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
            ) {
                Text(
                    text = if (mensaje.esAmigoDirecto) mensaje.remitenteHandle else mensaje.remitenteAnonimo,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = if (!mensaje.esAmigoDirecto) FontFamily.Monospace else FontFamily.Default
                    ),
                    color = if (mensaje.esAmigoDirecto) MutedPurpleLight else MutedAccent
                )
                Spacer(Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (mensaje.esAmigoDirecto) MutedPurple.copy(alpha = 0.2f) else DarkSurfaceVariant
                ) {
                    Text(
                        text = if (mensaje.esAmigoDirecto) "Contacto" else "Anónimo",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (mensaje.esAmigoDirecto) MutedPurpleLight else TextSecondary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }

        // Cuerpo de la burbuja
        Box(
            modifier = Modifier
                .widthIn(max = 290.dp)
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
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = horaLegible,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
