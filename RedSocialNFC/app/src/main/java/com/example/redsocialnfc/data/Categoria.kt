package com.example.redsocialnfc.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

object VisibilidadMuro {
    const val TODOS = "TODOS"
    const val AMIGOS = "AMIGOS"
    const val SELECCIONADOS = "SELECCIONADOS"
    const val PRIVADO = "PRIVADO"

    fun parsearHandles(csv: String): Set<String> {
        if (csv.isBlank()) return emptySet()
        return csv.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    fun serializarHandles(handles: Collection<String>): String {
        return handles.map { it.trim() }.filter { it.isNotEmpty() }.distinct().joinToString(",")
    }

    fun formatearResumen(visibilidad: String, permitidosCsv: String): String {
        return when (visibilidad) {
            AMIGOS -> "Solo amigos NFC"
            SELECCIONADOS -> {
                val lista = parsearHandles(permitidosCsv)
                when {
                    lista.isEmpty() -> "Elegir amigos (ninguno)"
                    lista.size <= 2 -> "Solo: ${lista.joinToString(", ")}"
                    else -> "Solo ${lista.size} amigos elegidos"
                }
            }
            PRIVADO -> "Solo tú (Privado)"
            else -> "Todos (Público local)"
        }
    }
}

@Entity(
    tableName = "categorias",
    indices = [Index(value = ["nombre"], unique = true)]
)
data class Categoria(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val autorId: Int,
    @ColumnInfo(defaultValue = "TODOS")
    val visibilidad: String = VisibilidadMuro.TODOS,
    @ColumnInfo(defaultValue = "")
    val permitidosHandles: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
