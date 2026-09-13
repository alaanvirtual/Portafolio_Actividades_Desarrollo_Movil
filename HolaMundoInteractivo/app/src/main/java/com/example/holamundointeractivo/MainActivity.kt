package com.example.holamundointeractivo

import android.content.Intent // Importación necesaria para cambiar de pantalla
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    // Todo debe ir dentro de la función onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Aquí se buscan los elementos después de que la pantalla cargó
        val etInput = findViewById<EditText>(R.id.etInput)
        val btnAccion = findViewById<Button>(R.id.btnAccion)

        // Aquí programamos el botón para viajar a la segunda pantalla
        btnAccion.setOnClickListener {
            val nombre = etInput.text.toString()

            if (nombre.isNotBlank()) {
                // Creamos el viaje hacia la segunda pantalla
                val intento = Intent(this, SaludoActivity::class.java)
                // Empacamos el nombre ingresado para llevarlo
                intento.putExtra("NOMBRE_USUARIO", nombre)
                // Iniciamos el viaje
                startActivity(intento)
            } else {
                etInput.hint = "¡Por favor, ingresa tu nombre!"
            }
        }
    } // Fin función onCreate
} // Fin de la clase MainActivity