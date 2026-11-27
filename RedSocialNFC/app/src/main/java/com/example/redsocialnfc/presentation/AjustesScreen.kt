package com.example.redsocialnfc.presentation

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.redsocialnfc.domain.AppDatabase
import com.example.redsocialnfc.ui.theme.DarkBackground
import com.example.redsocialnfc.ui.theme.DarkOutline
import com.example.redsocialnfc.ui.theme.DarkSurface
import com.example.redsocialnfc.ui.theme.DarkSurfaceVariant
import com.example.redsocialnfc.ui.theme.ErrorColor
import com.example.redsocialnfc.ui.theme.MutedPurple
import com.example.redsocialnfc.ui.theme.MutedPurpleLight
import com.example.redsocialnfc.ui.theme.TextPrimary
import com.example.redsocialnfc.ui.theme.TextSecondary
import com.example.redsocialnfc.util.MediaStorageHelper
import com.example.redsocialnfc.util.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    onCuentaEliminada: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    // Preferencias de Privacidad y Muro
    var defaultAnonPost by remember {
        mutableStateOf(sessionManager.getSetting(SessionManager.PREF_DEFAULT_ANON_POST, false))
    }
    var soloAmigosChat by remember {
        mutableStateOf(sessionManager.getSetting(SessionManager.PREF_SOLO_AMIGOS_CHAT, true))
    }
    var ocultarEnLinea by remember {
        mutableStateOf(sessionManager.getSetting(SessionManager.PREF_OCULTAR_EN_LINEA, false))
    }
    var compactFeed by remember {
        mutableStateOf(sessionManager.getSetting(SessionManager.PREF_COMPACT_FEED, false))
    }

    // Preferencias de Notificaciones y NFC
    var notifConexiones by remember {
        mutableStateOf(sessionManager.getSetting(SessionManager.PREF_NOTIF_CONEXIONES, true))
    }
    var notifMensajes by remember {
        mutableStateOf(sessionManager.getSetting(SessionManager.PREF_NOTIF_MENSAJES, true))
    }
    var notifInteracciones by remember {
        mutableStateOf(sessionManager.getSetting(SessionManager.PREF_NOTIF_INTERACCIONES, true))
    }
    var escaneoContinuo by remember {
        mutableStateOf(sessionManager.getSetting(SessionManager.PREF_NFC_BACKGROUND, false))
    }

    var tamanoMediaBytes by remember { mutableLongStateOf(0L) }
    var mostrarDialogoPassword by remember { mutableStateOf(false) }
    var confirmarVaciarChats by remember { mutableStateOf(false) }
    var confirmarResetAmigos by remember { mutableStateOf(false) }
    var confirmarEliminarCuenta by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        tamanoMediaBytes = MediaStorageHelper.calcularTamanoMediaBytes(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Configuración",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            "${sessionManager.getDisplayName()} (${sessionManager.getRealHandle()})",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            SeccionTitulo("Privacidad y Apariencia del Muro")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AjusteSwitchItem(
                        icono = Icons.Default.VisibilityOff,
                        titulo = "Publicar como anónimo por defecto",
                        descripcion = "El compositor iniciará usando ${sessionManager.getAnonHandle()}",
                        activo = defaultAnonPost,
                        onCambio = {
                            defaultAnonPost = it
                            sessionManager.setSetting(SessionManager.PREF_DEFAULT_ANON_POST, it)
                        }
                    )
                    SeparadorSutil()
                    AjusteSwitchItem(
                        icono = Icons.Default.ViewAgenda,
                        titulo = "Modo compacto tipo timeline",
                        descripcion = "Reduce la altura entre publicaciones del muro",
                        activo = compactFeed,
                        onCambio = {
                            compactFeed = it
                            sessionManager.setSetting(SessionManager.PREF_COMPACT_FEED, it)
                        }
                    )
                    SeparadorSutil()
                    AjusteSwitchItem(
                        icono = Icons.Default.Lock,
                        titulo = "Chats exclusivos con amigos NFC",
                        descripcion = "Solo permitir mensajes de dispositivos vinculados",
                        activo = soloAmigosChat,
                        onCambio = {
                            soloAmigosChat = it
                            sessionManager.setSetting(SessionManager.PREF_SOLO_AMIGOS_CHAT, it)
                        }
                    )
                    SeparadorSutil()
                    AjusteSwitchItem(
                        icono = Icons.Default.PersonOff,
                        titulo = "Ocultar estado en línea",
                        descripcion = "No mostrar indicador de conexión activa en chats",
                        activo = ocultarEnLinea,
                        onCambio = {
                            ocultarEnLinea = it
                            sessionManager.setSetting(SessionManager.PREF_OCULTAR_EN_LINEA, it)
                        }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            SeccionTitulo("Notificaciones y Proximidad NFC")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AjusteSwitchItem(
                        icono = Icons.Default.Nfc,
                        titulo = "Nuevas conexiones por proximidad",
                        descripcion = "Avisar cuando otro dispositivo sincronice contigo",
                        activo = notifConexiones,
                        onCambio = {
                            notifConexiones = it
                            sessionManager.setSetting(SessionManager.PREF_NOTIF_CONEXIONES, it)
                        }
                    )
                    SeparadorSutil()
                    AjusteSwitchItem(
                        icono = Icons.AutoMirrored.Filled.Chat,
                        titulo = "Mensajes directos en chats",
                        descripcion = "Alertas de texto, audio, fotos o documentos",
                        activo = notifMensajes,
                        onCambio = {
                            notifMensajes = it
                            sessionManager.setSetting(SessionManager.PREF_NOTIF_MENSAJES, it)
                        }
                    )
                    SeparadorSutil()
                    AjusteSwitchItem(
                        icono = Icons.Default.Notifications,
                        titulo = "Interacciones en publicaciones",
                        descripcion = "Likes, reposts y respuestas en tus posts",
                        activo = notifInteracciones,
                        onCambio = {
                            notifInteracciones = it
                            sessionManager.setSetting(SessionManager.PREF_NOTIF_INTERACCIONES, it)
                        }
                    )
                    SeparadorSutil()
                    AjusteSwitchItem(
                        icono = Icons.Default.Security,
                        titulo = "Detección NFC pasiva en segundo plano",
                        descripcion = "Mantener antena lista al acercar otro dispositivo",
                        activo = escaneoContinuo,
                        onCambio = {
                            escaneoContinuo = it
                            sessionManager.setSetting(SessionManager.PREF_NFC_BACKGROUND, it)
                        }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            SeccionTitulo("Seguridad y Almacenamiento Local")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AjusteAccionItem(
                        icono = Icons.Default.Key,
                        titulo = "Cambiar contraseña",
                        descripcion = "Actualiza tu clave de acceso de forma segura",
                        colorIcono = MutedPurpleLight,
                        onClick = { mostrarDialogoPassword = true }
                    )
                    SeparadorSutil()
                    AjusteAccionItem(
                        icono = Icons.Default.CleaningServices,
                        titulo = "Limpiar multimedia sin usar",
                        descripcion = "Espacio usado localmente: ${MediaStorageHelper.formatearTamano(tamanoMediaBytes)}",
                        colorIcono = MutedPurpleLight,
                        onClick = {
                            scope.launch {
                                val posts = db.publicacionDao().obtenerTodas().first()
                                val msgs = db.mensajeDao().observarTodosParaUsuario(sessionManager.getRealHandle()).first()
                                val user = db.usuarioDao().obtenerPorId(sessionManager.getUserId())
                                val rutasEnUso = buildSet {
                                    posts.mapNotNullTo(this) { it.mediaUri }
                                    posts.mapNotNullTo(this) { it.autorAvatarUri }
                                    msgs.mapNotNullTo(this) { it.mediaUri }
                                    user?.avatarUri?.let { add(it) }
                                }
                                val borrados = MediaStorageHelper.limpiarMediaNoReferenciada(context, rutasEnUso)
                                tamanoMediaBytes = MediaStorageHelper.calcularTamanoMediaBytes(context)
                                Toast.makeText(
                                    context,
                                    if (borrados > 0) "Se liberaron $borrados archivo(s)" else "No hay caché multimedia sin usar",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                    SeparadorSutil()
                    AjusteAccionItem(
                        icono = Icons.Default.DeleteSweep,
                        titulo = "Vaciar historial de mensajes",
                        descripcion = "Elimina los mensajes de todos tus chats locales",
                        colorIcono = ErrorColor,
                        onClick = { confirmarVaciarChats = true }
                    )
                    SeparadorSutil()
                    AjusteAccionItem(
                        icono = Icons.Default.DeleteSweep,
                        titulo = "Eliminar todos los amigos y chats",
                        descripcion = "Deja las listas de contactos y chats completamente vacías",
                        colorIcono = ErrorColor,
                        onClick = { confirmarResetAmigos = true }
                    )
                    SeparadorSutil()
                    AjusteAccionItem(
                        icono = Icons.Default.DeleteForever,
                        titulo = "Eliminar mi cuenta",
                        descripcion = "Borra tu usuario, publicaciones, categorías y datos locales",
                        colorIcono = ErrorColor,
                        onClick = { confirmarEliminarCuenta = true }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (mostrarDialogoPassword) {
        CambiarPasswordDialog(
            onDismiss = { mostrarDialogoPassword = false },
            onCambiar = { actual, nueva ->
                scope.launch {
                    val user = db.usuarioDao().obtenerPorId(sessionManager.getUserId())
                    if (user == null || !SessionManager.verificarPassword(actual, user.passwordHash)) {
                        Toast.makeText(context, "La contraseña actual es incorrecta", Toast.LENGTH_SHORT).show()
                    } else if (nueva.length < 4) {
                        Toast.makeText(context, "Usa al menos 4 caracteres", Toast.LENGTH_SHORT).show()
                    } else {
                        db.usuarioDao().actualizarPassword(user.id, SessionManager.hashPassword(nueva))
                        mostrarDialogoPassword = false
                        Toast.makeText(context, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    if (confirmarVaciarChats) {
        AlertDialog(
            onDismissRequest = { confirmarVaciarChats = false },
            containerColor = DarkSurface,
            title = { Text("¿Vaciar mensajes?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Se borrarán todos los mensajes locales. Tus contactos NFC vinculados permanecerán guardados.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            db.mensajeDao().eliminarTodos()
                            confirmarVaciarChats = false
                            Toast.makeText(context, "Historial de chats eliminado", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                ) {
                    Text("Vaciar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarVaciarChats = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    if (confirmarResetAmigos) {
        AlertDialog(
            onDismissRequest = { confirmarResetAmigos = false },
            containerColor = DarkSurface,
            title = { Text("¿Eliminar amigos y chats?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Se borrarán todos los contactos vinculados y sus conversaciones.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            db.mensajeDao().eliminarTodos()
                            db.amigoDao().eliminarTodos()
                            confirmarResetAmigos = false
                            Toast.makeText(context, "Chats y amigos vaciados", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                ) {
                    Text("Eliminar todo", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarResetAmigos = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    if (confirmarEliminarCuenta) {
        ConfirmarEliminarCuentaDialog(
            onDismiss = { confirmarEliminarCuenta = false },
            onConfirmar = { passwordConfirm ->
                scope.launch {
                    val userId = sessionManager.getUserId()
                    val user = db.usuarioDao().obtenerPorId(userId)
                    if (user == null || !SessionManager.verificarPassword(passwordConfirm, user.passwordHash)) {
                        Toast.makeText(context, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    db.comentarioDao().eliminarPorAutor(userId)
                    db.categoriaDao().eliminarPorAutor(userId)
                    db.publicacionDao().eliminarPorAutor(userId)
                    db.mensajeDao().eliminarTodos()
                    db.amigoDao().eliminarTodos()
                    db.usuarioDao().eliminarPorId(userId)
                    sessionManager.cerrarSesion()
                    confirmarEliminarCuenta = false
                    Toast.makeText(context, "Cuenta eliminada", Toast.LENGTH_SHORT).show()
                    onCuentaEliminada()
                }
            }
        )
    }
}

@Composable
private fun SeccionTitulo(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MutedPurpleLight
    )
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun SeparadorSutil() {
    HorizontalDivider(
        color = DarkOutline.copy(alpha = 0.45f),
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
private fun AjusteSwitchItem(
    icono: ImageVector,
    titulo: String,
    descripcion: String,
    activo: Boolean,
    onCambio: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = MutedPurpleLight,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = activo,
            onCheckedChange = onCambio,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextPrimary,
                checkedTrackColor = MutedPurple,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = DarkSurfaceVariant
            )
        )
    }
}

@Composable
private fun AjusteAccionItem(
    icono: ImageVector,
    titulo: String,
    descripcion: String,
    colorIcono: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = colorIcono,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun CambiarPasswordDialog(
    onDismiss: () -> Unit,
    onCambiar: (String, String) -> Unit
) {
    var actual by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                "Cambiar Contraseña",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = actual,
                    onValueChange = { actual = it },
                    label = { Text("Contraseña actual") },
                    visualTransformation = PasswordVisualTransformation(),
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
                    value = nueva,
                    onValueChange = { nueva = it },
                    label = { Text("Nueva contraseña (mín. 4)") },
                    visualTransformation = PasswordVisualTransformation(),
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
            Button(
                onClick = { onCambiar(actual, nueva) },
                colors = ButtonDefaults.buttonColors(containerColor = MutedPurple)
            ) {
                Text("Actualizar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun ConfirmarEliminarCuentaDialog(
    onDismiss: () -> Unit,
    onConfirmar: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                "Eliminar cuenta permanentemente",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = ErrorColor
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Se eliminarán tu perfil, publicaciones, categorías y chats locales. Ingresa tu contraseña para confirmar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Tu contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ErrorColor,
                        unfocusedBorderColor = DarkOutline,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmar(password) },
                enabled = password.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
            ) {
                Text("Eliminar cuenta", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}
