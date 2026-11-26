package com.example.practica17.presentation

import com.example.practica17.data.Tarea

enum class FiltroTarea(val etiqueta: String) {
    TODAS("Todas"),
    PENDIENTES("Pendientes"),
    COMPLETADAS("Completadas")
}

sealed interface EstadoCarga {
    data object Cargando : EstadoCarga
    data object Exito : EstadoCarga
    data class Error(val mensaje: String) : EstadoCarga
}

data class TareasUiState(
    val estadoCarga: EstadoCarga = EstadoCarga.Cargando,
    val tareas: List<Tarea> = emptyList(),
    val filtroActivo: FiltroTarea = FiltroTarea.TODAS,
    val textoNuevaTarea: String = ""
) {
    // Estados
    val tareasFiltradas: List<Tarea>
        get() = when (filtroActivo) {
            FiltroTarea.TODAS -> tareas
            FiltroTarea.PENDIENTES -> tareas.filter { !it.completada }
            FiltroTarea.COMPLETADAS -> tareas.filter { it.completada }
        }

    // Reto (Entregable): Resumen de Estadísticas Derivado
    val totalTareas: Int get() = tareas.size
    val totalPendientes: Int get() = tareas.count { !it.completada }
    val totalCompletadas: Int get() = tareas.count { it.completada }
}
