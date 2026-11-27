package com.example.redsocialnfc.presentation.components

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.redsocialnfc.data.Amigo
import com.example.redsocialnfc.domain.AppDatabase
import com.example.redsocialnfc.network.LocalP2pSyncManager
import com.example.redsocialnfc.nfc.NfcEvent
import com.example.redsocialnfc.nfc.NfcHelper
import com.example.redsocialnfc.nfc.NfcProtocol
import com.example.redsocialnfc.ui.theme.DarkBackground
import com.example.redsocialnfc.ui.theme.DarkOutline
import com.example.redsocialnfc.ui.theme.DarkSurface
import com.example.redsocialnfc.ui.theme.DarkSurfaceVariant
import com.example.redsocialnfc.ui.theme.MutedAccent
import com.example.redsocialnfc.ui.theme.MutedPurple
import com.example.redsocialnfc.ui.theme.MutedPurpleLight
import com.example.redsocialnfc.ui.theme.SubtleGrayPurple
import com.example.redsocialnfc.ui.theme.TextPrimary
import com.example.redsocialnfc.ui.theme.TextSecondary
import com.example.redsocialnfc.util.SessionManager
import kotlinx.coroutines.launch

@Composable
fun DialogNfcHandshake(
    sessionManager: SessionManager,
    onDismiss: () -> Unit,
    onAmigoVinculado: (Amigo) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }

    var tabSeleccionada by remember { mutableIntStateOf(0) } // 0: Radar Hardware NFC, 1: Simulación / Manual
    var amigoDetectado by remember { mutableStateOf<Amigo?>(null) }
    var mensajeEstado by remember { mutableStateOf("Esperando proximidad...") }

    val nfcSoportado = remember { NfcHelper.isNfcSupported(context) }
    var nfcHabilitado by remember { mutableStateOf(NfcHelper.isNfcEnabled(context)) }

    // Animación de radar pulsante
    val infiniteTransition = rememberInfiniteTransition(label = "RadarTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha"
    )

    // Activar y desactivar Reader Mode en el ciclo de vida del Diálogo
    DisposableEffect(nfcHabilitado) {
        if (activity != null && nfcHabilitado) {
            NfcHelper.enableReaderMode(activity, scope) { nuevoAmigo ->
                scope.launch {
                    db.amigoDao().insertar(nuevoAmigo)
                    LocalP2pSyncManager.enviarHandshakeAmigo(nuevoAmigo.handle)
                    amigoDetectado = nuevoAmigo
                    mensajeEstado = "¡Conexión establecida con @${nuevoAmigo.handle.removePrefix("@")}!"
                    onAmigoVinculado(nuevoAmigo)
                }
            }
        }
        onDispose {
            if (activity != null) {
                NfcHelper.disableReaderMode(activity)
            }
        }
    }

    // Escuchar eventos globales del bus NFC
    LaunchedEffect(Unit) {
        NfcHelper.eventosNfc.collect { evento ->
            when (evento) {
                is NfcEvent.ContactoDetectado -> {
                    amigoDetectado = evento.amigo
                    mensajeEstado = "¡Contacto vinculado con éxito!"
                }
                is NfcEvent.Error -> {
                    mensajeEstado = evento.mensaje
                }
                NfcEvent.LecturaIniciada -> {
                    mensajeEstado = "Leyendo etiqueta/dispositivo NFC..."
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Nfc,
                        contentDescription = null,
                        tint = MutedPurpleLight,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Vincular por NFC",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TabRow(
                    selectedTabIndex = tabSeleccionada,
                    containerColor = DarkBackground,
                    contentColor = MutedPurpleLight,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[tabSeleccionada]),
                            color = MutedPurple
                        )
                    }
                ) {
                    Tab(
                        selected = tabSeleccionada == 0,
                        onClick = { tabSeleccionada = 0 },
                        text = { Text("Hardware NFC", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = tabSeleccionada == 1,
                        onClick = { tabSeleccionada = 1 },
                        text = { Text("Simular / Manual", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }

                Spacer(Modifier.height(16.dp))

                if (amigoDetectado != null) {
                    // Estado de Éxito
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SubtleGrayPurple,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MutedPurpleLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "¡Amigo Vinculado!",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = amigoDetectado!!.nombre,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                            Text(
                                text = amigoDetectado!!.handle,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = MutedPurpleLight
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "UUID: ${amigoDetectado!!.nfcUuid.take(16)}...",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                } else if (tabSeleccionada == 0) {
                    // Pestaña 0: Hardware Real
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Radar animado
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(110.dp)
                                .padding(vertical = 4.dp)
                        ) {
                            if (nfcHabilitado) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .scale(pulseScale)
                                        .background(MutedPurple.copy(alpha = pulseAlpha), shape = CircleShape)
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = DarkSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(2.dp, MutedPurpleLight),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (nfcHabilitado) Icons.Default.Sensors else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (nfcHabilitado) MutedPurpleLight else MutedAccent,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        if (!nfcSoportado) {
                            Text(
                                text = "Sensor NFC físico no disponible en este dispositivo (Modo Emulador).",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Puedes usar la pestaña 'Simular / Manual' para probar el intercambio completo de paquetes APDU y guardado en SQLite.",
                                fontSize = 11.sp,
                                color = MutedAccent,
                                textAlign = TextAlign.Center
                            )
                        } else if (!nfcHabilitado) {
                            Text(
                                text = "El sensor NFC está apagado en el sistema.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MutedAccent,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(6.dp))
                            Button(
                                onClick = {
                                    NfcHelper.abrirAjustesNfc(context)
                                    nfcHabilitado = NfcHelper.isNfcEnabled(context)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MutedPurple)
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Activar NFC en Ajustes")
                            }
                        } else {
                            Text(
                                text = "Acerca la parte trasera de otro teléfono con Local abierto",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Servicio HCE transmitiendo AID: ${NfcProtocol.AID_HEX}",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = mensajeEstado,
                                fontSize = 11.sp,
                                color = MutedPurpleLight,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Tarjeta resumen de mi identidad que se transmitirá
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DarkBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(MutedPurple, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = sessionManager.getDisplayName().take(1).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Transmitiendo mi perfil:",
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "${sessionManager.getDisplayName()} (${sessionManager.getRealHandle()})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Pestaña 1: Simulación / Manual para Emuladores y Pruebas
                    SimuladorNfcContent(
                        sessionManager = sessionManager,
                        onSimularContacto = { mockAmigo ->
                            scope.launch {
                                db.amigoDao().insertar(mockAmigo)
                                LocalP2pSyncManager.enviarHandshakeAmigo(mockAmigo.handle)
                                NfcHelper.vibrarConfirmacion(context)
                                amigoDetectado = mockAmigo
                                onAmigoVinculado(mockAmigo)
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            if (amigoDetectado != null) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = MutedPurple)
                ) {
                    Text("Listo", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun SimuladorNfcContent(
    sessionManager: SessionManager,
    onSimularContacto: (Amigo) -> Unit
) {
    var nombreInput by remember { mutableStateOf("") }
    var handleInput by remember { mutableStateOf("") }

    val presetAmigos = remember {
        listOf(
            Amigo(handle = "@Carlos_Dev", nombre = "Carlos Mendoza", nfcUuid = "nfc-sim-carlos-101"),
            Amigo(handle = "@Valeria_UX", nombre = "Valeria Ríos", nfcUuid = "nfc-sim-valeria-202"),
            Amigo(handle = "@Profe_JC", nombre = "Profesor Juan Carlos", nfcUuid = "nfc-sim-profjc-303")
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Pruebas rápidas (Presets NFC):",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MutedPurpleLight
        )

        presetAmigos.forEach { preset ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = DarkBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Empaquetar y desempaquetar con el protocolo JSON real
                        val json = NfcProtocol.crearPayloadJson(
                            handle = preset.handle,
                            nombre = preset.nombre,
                            nfcUuid = preset.nfcUuid
                        )
                        val parseado = NfcProtocol.parsearPayloadJson(json)
                        if (parseado != null) {
                            onSimularContacto(
                                Amigo(
                                    handle = parseado.handle,
                                    nombre = parseado.nombre,
                                    nfcUuid = parseado.nfcUuid,
                                    timestamp = parseado.timestamp
                                )
                            )
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Nfc,
                        contentDescription = null,
                        tint = MutedPurpleLight,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(preset.nombre, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(preset.handle, fontSize = 10.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                    }
                    Text(
                        text = "Tocar",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedPurpleLight
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            text = "O simular con datos personalizados:",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
        )

        OutlinedTextField(
            value = nombreInput,
            onValueChange = { if (it.length <= 30) nombreInput = it },
            label = { Text("Nombre") },
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
            value = handleInput,
            onValueChange = { input -> handleInput = input.filter { it.isLetterOrDigit() || it == '_' } },
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

        val esValido = nombreInput.isNotBlank() && handleInput.isNotBlank()
        Button(
            onClick = {
                val handleFinal = if (handleInput.startsWith("@")) handleInput else "@$handleInput"
                val json = NfcProtocol.crearPayloadJson(
                    handle = handleFinal,
                    nombre = nombreInput.trim(),
                    nfcUuid = "nfc-custom-${System.currentTimeMillis()}"
                )
                val parseado = NfcProtocol.parsearPayloadJson(json)
                if (parseado != null) {
                    onSimularContacto(
                        Amigo(
                            handle = parseado.handle,
                            nombre = parseado.nombre,
                            nfcUuid = parseado.nfcUuid,
                            timestamp = parseado.timestamp
                        )
                    )
                }
            },
            enabled = esValido,
            colors = ButtonDefaults.buttonColors(containerColor = MutedPurple),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Simular Handshake NFC", color = Color.White)
        }
    }
}
