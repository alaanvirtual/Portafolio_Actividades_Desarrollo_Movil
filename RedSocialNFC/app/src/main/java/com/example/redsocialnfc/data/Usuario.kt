package com.example.redsocialnfc.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val email: String,
    val passwordHash: String,
    val realHandle: String,        // @handle real único
    val displayName: String = "",  // Nombre visible editable estilo Twitter
    val bio: String = "",          // Biografía del perfil
    val avatarUri: String? = null, // Ruta local de foto de perfil
    val anonHandle: String,        // Pseudónimo anónimo inalterable (ej: @Anonimo_89B)
    val nfcUuid: String,           // UUID privado para handshake NFC
    val fechaRegistro: Long = System.currentTimeMillis()
)
