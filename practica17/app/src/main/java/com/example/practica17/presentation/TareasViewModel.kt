package com.example.practica17.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.practica17.data.Tarea
import com.example.practica17.data.TareaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TareasViewModel(
    private val repository: TareaRepository = TareaRepository()
) : ViewModel() {

    // Estado interno mutable
    private val _uiState = MutableStateFlow(TareasUiState())

    // Estado público
    val uiState: StateFlow<TareasUiState> = _uiState.asStateFlow()

    private var siguienteId = 4

    init {
        cargarTareas()
    }

    fun cargarTareas(simularError: Boolean = false) {
        viewModelScope.launch {
            // Cambiamos el estado a Cargando
            _uiState.update { it.copy(estadoCarga = EstadoCarga.Cargando) }

            try {
                val listaObtenida = repository.obtenerTareasIniciales(simularError)
                siguienteId = (listaObtenida.maxOfOrNull { it.id } ?: 0) + 1

                // Si tuvo éxito, entregamos los datos y actualizamos el estado
                _uiState.update {
                    it.copy(
                        estadoCarga = EstadoCarga.Exito,
                        tareas = listaObtenida
                    )
                }
            } catch (e: Exception) {
                // Si ocurrió un fallo, pasamos al estado de Error
                _uiState.update {
                    it.copy(
                        estadoCarga = EstadoCarga.Error(
                            e.message ?: "Error desconocido al cargar los datos."
                        )
                    )
                }
            }
        }
    }

    fun onTextoCambio(nuevoTexto: String) {
        _uiState.update { it.copy(textoNuevaTarea = nuevoTexto) }
    }

    fun agregarTarea() {
        val tituloLimpio = _uiState.value.textoNuevaTarea.trim()
        if (tituloLimpio.isEmpty()) return

        val nuevaTarea = Tarea(id = siguienteId++, titulo = tituloLimpio)

        _uiState.update { estadoActual ->
            estadoActual.copy(
                tareas = listOf(nuevaTarea) + estadoActual.tareas,
                textoNuevaTarea = ""
            )
        }
    }

    fun alternarCompletada(idTarea: Int) {
        _uiState.update { estadoActual ->
            val listaActualizada = estadoActual.tareas.map { tarea ->
                if (tarea.id == idTarea) {
                    tarea.copy(completada = !tarea.completada)
                } else {
                    tarea
                }
            }
            estadoActual.copy(tareas = listaActualizada)
        }
    }

    fun eliminarTarea(idTarea: Int) {
        _uiState.update { estadoActual ->
            estadoActual.copy(
                tareas = estadoActual.tareas.filterNot { it.id == idTarea }
            )
        }
    }

    fun cambiarFiltro(nuevoFiltro: FiltroTarea) {
        _uiState.update { it.copy(filtroActivo = nuevoFiltro) }
    }

    // Reto (Entregable): Botón "Limpiar completadas"
    fun limpiarCompletadas() {
        _uiState.update { estadoActual ->
            estadoActual.copy(
                tareas = estadoActual.tareas.filterNot { it.completada }
            )
        }
    }
}
