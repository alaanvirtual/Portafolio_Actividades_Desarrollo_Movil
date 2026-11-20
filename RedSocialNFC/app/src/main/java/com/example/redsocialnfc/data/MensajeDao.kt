package com.example.redsocialnfc.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MensajeDao {

    @Insert
    suspend fun insertar(mensaje: Mensaje): Long

    @Query("""
        SELECT * FROM mensajes 
        WHERE (remitenteHandle = :miHandle AND destinatarioHandle = :amigoHandle)
           OR (remitenteHandle = :amigoHandle AND destinatarioHandle = :miHandle)
        ORDER BY timestamp ASC
    """)
    fun obtenerConversacion(miHandle: String, amigoHandle: String): Flow<List<Mensaje>>

    @Query("SELECT COUNT(*) FROM mensajes")
    suspend fun contarTotal(): Int
}
