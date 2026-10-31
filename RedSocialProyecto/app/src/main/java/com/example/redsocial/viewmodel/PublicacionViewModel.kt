package com.example.redsocial.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.redsocial.data.AppDatabase
import com.example.redsocial.data.Publicacion
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PublicacionViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).publicacionDao()

    val publicaciones: StateFlow<List<Publicacion>> = dao.obtenerTodas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun agregarPublicacion(titulo: String, descripcion: String, fotoUri: String?, fecha: String) {
        viewModelScope.launch {
            val nueva = Publicacion(
                titulo = titulo,
                descripcion = descripcion,
                fotoUri = fotoUri,
                fecha = fecha
            )
            dao.insertar(nueva)
        }
    }

    fun actualizarPublicacion(publicacion: Publicacion) {
        viewModelScope.launch {
            dao.actualizar(publicacion)
        }
    }

    fun eliminarPublicacion(publicacion: Publicacion) {
        viewModelScope.launch {
            dao.eliminar(publicacion)
        }
    }
}
