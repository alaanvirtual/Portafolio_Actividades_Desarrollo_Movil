package com.example.redsocialnfc.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MensajeDao {

    @Insert
    suspend fun insertar(mensaje: Mensaje): Long

    @Query("SELECT * FROM mensajes ORDER BY timestamp ASC")
    fun obtenerTodos(): Flow<List<Mensaje>>

    @Query("SELECT COUNT(*) FROM mensajes")
    suspend fun contarTotal(): Int

    @Query("UPDATE mensajes SET esAmigoDirecto = 1 WHERE remitenteAnonimo = :anonHandle")
    suspend fun revelarUsuario(anonHandle: String)
}
