package com.example.redsocialnfc

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.redsocialnfc.domain.AppDatabase
import com.example.redsocialnfc.network.LocalP2pSyncManager
import com.example.redsocialnfc.presentation.AuthScreen
import com.example.redsocialnfc.presentation.MainDashboard
import com.example.redsocialnfc.ui.theme.DarkBackground
import com.example.redsocialnfc.ui.theme.MutedPurple
import com.example.redsocialnfc.ui.theme.RedSocialNFCTheme
import com.example.redsocialnfc.util.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        setContent {
            RedSocialNFCTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val db = remember { AppDatabase.getDatabase(context) }

    var validandoSesion by remember { mutableStateOf(true) }
    var sesionActiva by remember { mutableStateOf(false) }

    // Sincroniza SessionManager con Room (evita sesiones huérfanas tras migración destructiva)
    LaunchedEffect(Unit) {
        if (sessionManager.haySesionActiva()) {
            val user = db.usuarioDao().obtenerPorId(sessionManager.getUserId())
            if (user != null) {
                sessionManager.guardarSesion(
                    userId = user.id,
                    email = user.email,
                    realHandle = user.realHandle,
                    displayName = user.displayName,
                    anonHandle = user.anonHandle,
                    bio = user.bio,
                    avatarUri = user.avatarUri,
                    nfcUuid = user.nfcUuid
                )
                sesionActiva = true
            } else {
                sessionManager.cerrarSesion()
                sesionActiva = false
            }
        } else {
            sesionActiva = false
        }
        validandoSesion = false
    }

    if (validandoSesion) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MutedPurple)
        }
    } else if (sesionActiva) {
        DisposableEffect(Unit) {
            LocalP2pSyncManager.iniciar(context)
            onDispose {
                LocalP2pSyncManager.detener()
            }
        }
        MainDashboard(
            onCerrarSesion = {
                LocalP2pSyncManager.detener()
                sesionActiva = false
            }
        )
    } else {
        AuthScreen(
            onLoginExitoso = {
                sesionActiva = true
            }
        )
    }
}
