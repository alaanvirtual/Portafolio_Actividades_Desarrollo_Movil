package com.example.redsocialnfc.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.redsocialnfc.domain.AppDatabase
import com.example.redsocialnfc.ui.theme.DarkBackground
import com.example.redsocialnfc.ui.theme.DarkSurface
import com.example.redsocialnfc.ui.theme.MutedPurple
import com.example.redsocialnfc.ui.theme.TextPrimary
import com.example.redsocialnfc.ui.theme.TextSecondary
import com.example.redsocialnfc.util.SessionManager

@Composable
fun MainDashboard(
    onCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val sessionManager = remember { SessionManager(context) }
    val miHandle = sessionManager.getRealHandle()
    val mensajes by db.mensajeDao().observarTodosParaUsuario(miHandle).collectAsState(initial = emptyList())
    val totalNoLeidos = remember(mensajes, miHandle) {
        mensajes.count { it.destinatarioHandle == miHandle && !it.leido }
    }

    var pestanaSeleccionada by remember { mutableIntStateOf(0) }
    var enConversacionChat by remember { mutableStateOf(false) }

    val mostrarBottomBar = !(pestanaSeleccionada == 1 && enConversacionChat)

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (mostrarBottomBar) {
                NavigationBar(
                    containerColor = DarkSurface
                ) {
                    NavigationBarItem(
                        selected = pestanaSeleccionada == 0,
                        onClick = { pestanaSeleccionada = 0 },
                        icon = {
                            Icon(Icons.Default.DynamicFeed, contentDescription = "Muro")
                        },
                        label = { Text("Muro") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = TextPrimary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = MutedPurple
                        )
                    )

                    NavigationBarItem(
                        selected = pestanaSeleccionada == 1,
                        onClick = { pestanaSeleccionada = 1 },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (totalNoLeidos > 0) {
                                        Badge(containerColor = MutedPurple, contentColor = Color.White) {
                                            Text("$totalNoLeidos")
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chats")
                            }
                        },
                        label = { Text("Chats") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = TextPrimary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = MutedPurple
                        )
                    )

                    NavigationBarItem(
                        selected = pestanaSeleccionada == 2,
                        onClick = { pestanaSeleccionada = 2 },
                        icon = {
                            Icon(Icons.Default.Person, contentDescription = "Perfil")
                        },
                        label = { Text("Perfil") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = TextPrimary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = MutedPurple
                        )
                    )

                    NavigationBarItem(
                        selected = pestanaSeleccionada == 3,
                        onClick = { pestanaSeleccionada = 3 },
                        icon = {
                            Icon(Icons.Default.Settings, contentDescription = "Ajustes")
                        },
                        label = { Text("Ajustes") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = TextPrimary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = MutedPurple
                        )
                    )
                }
            }
        },
        containerColor = DarkBackground
    ) { padding ->
        val bottomPad = PaddingValues(bottom = padding.calculateBottomPadding())
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottomPad)
                .consumeWindowInsets(bottomPad)
        ) {
            when (pestanaSeleccionada) {
                0 -> FeedScreen()
                1 -> ChatScreen(
                    onEnConversacionChange = { enConversacionChat = it }
                )
                2 -> PerfilScreen(onCerrarSesion = onCerrarSesion)
                3 -> AjustesScreen(onCuentaEliminada = onCerrarSesion)
            }
        }
    }
}
