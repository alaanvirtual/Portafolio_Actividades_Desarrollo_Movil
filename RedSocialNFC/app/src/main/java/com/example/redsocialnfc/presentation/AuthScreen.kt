package com.example.redsocialnfc.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.redsocialnfc.data.Usuario
import com.example.redsocialnfc.domain.AppDatabase
import com.example.redsocialnfc.ui.theme.DarkBackground
import com.example.redsocialnfc.ui.theme.DarkOutline
import com.example.redsocialnfc.ui.theme.DarkSurface
import com.example.redsocialnfc.ui.theme.DarkSurfaceVariant
import com.example.redsocialnfc.ui.theme.ElectricViolet
import com.example.redsocialnfc.ui.theme.NeonCyan
import com.example.redsocialnfc.ui.theme.NeonMagenta
import com.example.redsocialnfc.ui.theme.SubtleViolet
import com.example.redsocialnfc.ui.theme.TextPrimary
import com.example.redsocialnfc.ui.theme.TextSecondary
import com.example.redsocialnfc.util.SessionManager
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    onLoginExitoso: () -> Unit
) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).usuarioDao() }
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    var esModoRegistro by remember { mutableStateOf(false) }

    // RF-01: Estado de formulario (Email y Contraseña)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) } // RF-01: Conmutador de visibilidad

    // RF-02: Handle Real (@handle)
    var realHandleInput by remember { mutableStateOf("") }

    // RF-03: Identidad anónima autogenerada inalterable
    var anonHandleGenerado by remember { mutableStateOf(SessionManager.generarIdentidadAnonima()) }

    var mensajeError by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    // Regenerar identidad anónima si cambia a modo registro
    LaunchedEffect(esModoRegistro) {
        if (esModoRegistro) {
            anonHandleGenerado = SessionManager.generarIdentidadAnonima()
        }
        mensajeError = null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkBackground,
                        Color(0xFF1C1A24),
                        DarkBackground
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logotipo / Cabecera de la aplicación Local
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Logo",
                tint = NeonMagenta,
                modifier = Modifier.size(54.dp)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "LOCAL",
                style = MaterialTheme.typography.titleLarge.copy(
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Black
                ),
                color = TextPrimary
            )

            Text(
                text = "Red Social de Proximidad Criptográfica",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(Modifier.height(28.dp))

            // Tarjeta principal del formulario
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (esModoRegistro) "Crear Cuenta" else "Iniciar Sesión",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )

                    Spacer(Modifier.height(16.dp))

                    // RF-01: Campo de Email con OutlinedTextField
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it.trim()
                            mensajeError = null
                        },
                        label = { Text("Correo Electrónico") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = NeonMagenta)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonMagenta,
                            unfocusedBorderColor = DarkOutline,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    // RF-01: Campo de Contraseña con TrailingIcon (ojo conmutador)
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            mensajeError = null
                        },
                        label = { Text("Contraseña") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = NeonMagenta)
                        },
                        trailingIcon = {
                            // RF-01: El ícono conmutador de visibilidad previene errores de escritura
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                    tint = TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonMagenta,
                            unfocusedBorderColor = DarkOutline,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Campos adicionales solo en REGISTRO (RF-02 y RF-03)
                    AnimatedVisibility(visible = esModoRegistro) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Spacer(Modifier.height(12.dp))

                            // Handle de usuario (@handle) con prefixText
                            OutlinedTextField(
                                value = realHandleInput,
                                onValueChange = { input ->
                                    val filtrado = input.filter { it.isLetterOrDigit() || it == '_' }
                                    realHandleInput = filtrado
                                    mensajeError = null
                                },
                                label = { Text("Usuario") },
                                prefix = {
                                    Text("@", color = NeonMagenta, fontWeight = FontWeight.Bold)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = NeonMagenta)
                                },
                                singleLine = true,
                                supportingText = {
                                    Text("Nombre visible para tus contactos", color = TextSecondary)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonMagenta,
                                    unfocusedBorderColor = DarkOutline,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(14.dp))

                            // Pseudónimo Anónimo (Solo Lectura)
                            Text(
                                text = "Pseudónimo anónimo asignado:",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Spacer(Modifier.height(6.dp))

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SubtleViolet,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, ElectricViolet, RoundedCornerShape(10.dp))
                                    .padding(vertical = 12.dp, horizontal = 14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.background(Color.Transparent)
                                ) {
                                    Column {
                                        Text(
                                            text = anonHandleGenerado,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            ),
                                            color = NeonCyan
                                        )
                                        Text(
                                            text = "Identidad protegida autogenerada",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = DarkSurfaceVariant
                                    ) {
                                        Text(
                                            text = "FIJO",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = TextSecondary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Nfc,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Dispositivo vinculado de forma segura",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // Mensaje de Error
                    mensajeError?.let { err ->
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // Botón de Acción (Login / Registro)
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                mensajeError = "Por favor completa todos los campos"
                                return@Button
                            }

                            cargando = true
                            scope.launch {
                                try {
                                    if (esModoRegistro) {
                                        if (realHandleInput.isBlank()) {
                                            mensajeError = "Debes ingresar tu nombre de usuario (@handle)"
                                            cargando = false
                                            return@launch
                                        }

                                        // Validar que no exista el correo o handle
                                        val existenteEmail = dao.obtenerPorEmail(email)
                                        if (existenteEmail != null) {
                                            mensajeError = "El correo ya está registrado"
                                            cargando = false
                                            return@launch
                                        }

                                        val handleCompleto = "@$realHandleInput"
                                        val existenteHandle = dao.obtenerPorHandle(handleCompleto)
                                        if (existenteHandle != null) {
                                            mensajeError = "El handle $handleCompleto ya está ocupado"
                                            cargando = false
                                            return@launch
                                        }

                                        // RF-04: Obtener o generar UUID criptográfico de hardware
                                        val nfcUuid = sessionManager.obtenerOGenerarDeviceNfcUuid()

                                        val nuevoUsuario = Usuario(
                                            email = email,
                                            passwordHash = password, // Demo local
                                            realHandle = handleCompleto,
                                            anonHandle = anonHandleGenerado,
                                            nfcUuid = nfcUuid
                                        )

                                        val idGenerado = dao.registrarUsuario(nuevoUsuario)
                                        sessionManager.guardarSesion(
                                            userId = idGenerado.toInt(),
                                            email = email,
                                            realHandle = handleCompleto,
                                            anonHandle = anonHandleGenerado
                                        )
                                        cargando = false
                                        onLoginExitoso()
                                    } else {
                                        // Modo Login (RF-01)
                                        val user = dao.obtenerPorEmail(email)
                                        if (user == null || user.passwordHash != password) {
                                            mensajeError = "Correo o contraseña incorrectos"
                                            cargando = false
                                            return@launch
                                        }

                                        sessionManager.guardarSesion(
                                            userId = user.id,
                                            email = user.email,
                                            realHandle = user.realHandle,
                                            anonHandle = user.anonHandle
                                        )
                                        cargando = false
                                        onLoginExitoso()
                                    }
                                } catch (e: Exception) {
                                    mensajeError = "Error: ${e.message}"
                                    cargando = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonMagenta,
                            contentColor = Color.White
                        ),
                        enabled = !cargando
                    ) {
                        if (cargando) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (esModoRegistro) "REGISTRARSE" else "INICIAR SESIÓN",
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Conmutar entre Login y Registro
                    TextButton(onClick = { esModoRegistro = !esModoRegistro }) {
                        Text(
                            text = if (esModoRegistro) "¿Ya tienes cuenta? Inicia sesión" else "¿No tienes cuenta? Regístrate aquí",
                            color = NeonCyan
                        )
                    }
                }
            }
        }
    }
}
