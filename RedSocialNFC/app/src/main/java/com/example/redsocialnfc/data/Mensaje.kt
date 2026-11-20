package com.example.redsocialnfc.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mensajes")
data class Mensaje(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val remitenteHandle: String,
    val destinatarioHandle: String,
    val texto: String,
    val timestamp: Long = System.currentTimeMillis()
)
