package com.example.practica17.data

import kotlinx.coroutines.delay

class TareaRepository {

    // Simula la obtención de tareas
    suspend fun obtenerTareasIniciales(simularError: Boolean = false): List<Tarea> {
        delay(1500L)

        if (simularError) {
            throw Exception("No se pudo conectar con el servidor de tareas.")
        }

        return listOf(
            Tarea(id = 1, titulo = "Estudiar arquitectura MVVM", completada = true),
            Tarea(id = 2, titulo = "Implementar StateFlow en Compose", completada = false),
            Tarea(id = 3, titulo = "Probar rotación de pantalla", completada = false)
        )
    }
}
