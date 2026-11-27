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

    @Query("""
        SELECT * FROM mensajes
        WHERE remitenteHandle = :miHandle OR destinatarioHandle = :miHandle
        ORDER BY timestamp DESC
    """)
    fun observarTodosParaUsuario(miHandle: String): Flow<List<Mensaje>>

    @Query("""
        UPDATE mensajes SET leido = 1
        WHERE remitenteHandle = :amigoHandle AND destinatarioHandle = :miHandle AND leido = 0
    """)
    suspend fun marcarComoLeidos(miHandle: String, amigoHandle: String)

    @Query("""
        DELETE FROM mensajes
        WHERE (remitenteHandle = :miHandle AND destinatarioHandle = :amigoHandle)
           OR (remitenteHandle = :amigoHandle AND destinatarioHandle = :miHandle)
    """)
    suspend fun eliminarConversacion(miHandle: String, amigoHandle: String)

    @Query("DELETE FROM mensajes WHERE id = :mensajeId")
    suspend fun eliminarPorId(mensajeId: Int)

    @Query("DELETE FROM mensajes")
    suspend fun eliminarTodos()

    @Query("SELECT COUNT(*) FROM mensajes")
    suspend fun contarTotal(): Int
}
