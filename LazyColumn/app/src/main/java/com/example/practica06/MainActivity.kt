package com.example.practica06

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.practica06.ui.theme.Practica06Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Practica06Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ListaScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
