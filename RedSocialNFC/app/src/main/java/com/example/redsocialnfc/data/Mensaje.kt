package com.example.redsocialnfc.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mensajes")
data class Mensaje(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val remitenteId: Int,
    val remitenteHandle: String,
    val remitenteAnonimo: String,
    val texto: String,
    val esAmigoDirecto: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
