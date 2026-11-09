package com.example.redsocialnfc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.redsocialnfc.presentation.AuthScreen
import com.example.redsocialnfc.presentation.PerfilScreen
import com.example.redsocialnfc.ui.theme.RedSocialNFCTheme
import com.example.redsocialnfc.util.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
    var sesionActiva by remember { mutableStateOf(sessionManager.haySesionActiva()) }

    if (sesionActiva) {
        PerfilScreen(
            onCerrarSesion = {
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
