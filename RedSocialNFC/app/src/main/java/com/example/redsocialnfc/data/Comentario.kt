package com.example.redsocialnfc.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "comentarios",
    indices = [Index(value = ["publicacionId"])]
)
data class Comentario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val publicacionId: Int,
    val autorId: Int,
    val autorNombre: String,
    val autorHandle: String,
    val autorAnonimo: String,
    val esAnonimo: Boolean = false,
    val contenido: String,
    val timestamp: Long = System.currentTimeMillis()
)
