package com.example.practica14

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.practica14.ui.theme.Practica14Theme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val ctx = applicationContext
        Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", 0))
        // Asignamos una firma única, eliminando cualquier rastro de "com.example"
        Configuration.getInstance().userAgentValue = "Practica14_Alan_UAS/1.0"

        setContent {
            Practica14Theme {
                MapaOSMScreen()
            }
        }
    }
}
