package com.example.redsocialnfc.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val email: String,
    val passwordHash: String,
    val realHandle: String,        // RF-02: @handle real único
    val anonHandle: String,        // RF-03: Pseudónimo anónimo inalterable (ej: @Anonimo_89B)
    val nfcUuid: String,           // RF-04: UUID privado para handshake NFC
    val fechaRegistro: Long = System.currentTimeMillis()
)
