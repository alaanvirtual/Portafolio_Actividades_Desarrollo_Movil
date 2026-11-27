package com.example.redsocialnfc.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

object MediaTipoChat {
    const val TEXTO = "TEXTO"
    const val IMAGEN = "IMAGEN"
    const val VIDEO = "VIDEO"
    const val AUDIO = "AUDIO"
    const val DOCUMENTO = "DOCUMENTO"
}

@Entity(tableName = "mensajes")
data class Mensaje(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val remitenteHandle: String,
    val destinatarioHandle: String,
    val texto: String = "",
    val mediaTipo: String = MediaTipoChat.TEXTO,
    val mediaUri: String? = null,
    val mediaNombre: String? = null,
    val mediaTamanoBytes: Long = 0L,
    val duracionMs: Long = 0L,
    @ColumnInfo(defaultValue = "1")
    val leido: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
