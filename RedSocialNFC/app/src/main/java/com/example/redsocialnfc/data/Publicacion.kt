package com.example.redsocialnfc.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "publicaciones")
data class Publicacion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val autorId: Int,
    val autorHandle: String,
    val autorAnonimo: String,
    val contenido: String,
    val esAmigoDirecto: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
