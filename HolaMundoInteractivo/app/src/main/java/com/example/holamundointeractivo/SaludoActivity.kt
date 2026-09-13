package com.example.holamundointeractivo

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SaludoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_saludo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. Buscamos la etiqueta de esta nueva pantalla.
        // Nota: Asegúrate de que en tu archivo activity_saludo.xml el ID sea "textView"
        val tvSaludoFinal = findViewById<TextView>(R.id.textView)

        // 2. Abrimos el "paquete" para sacar el texto que nos mandaron desde MainActivity
        val nombreRecibido = intent.getStringExtra("NOMBRE_USUARIO")

        // 3. Mostramos el mensaje final en pantalla
        tvSaludoFinal.text = "Texto recibido:\n¡Hola, $nombreRecibido!"
    }
}