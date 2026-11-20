package com.example.redsocialnfc.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "amigos")
data class Amigo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val handle: String,
    val nombre: String,
    val nfcUuid: String,
    val timestamp: Long = System.currentTimeMillis()
)
