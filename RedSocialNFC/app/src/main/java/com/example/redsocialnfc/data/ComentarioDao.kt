package com.example.redsocialnfc.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ComentarioDao {

    @Insert
    suspend fun insertar(comentario: Comentario): Long

    @Query("SELECT * FROM comentarios WHERE publicacionId = :publicacionId ORDER BY timestamp ASC")
    fun observarPorPublicacion(publicacionId: Int): Flow<List<Comentario>>

    @Query("SELECT * FROM comentarios ORDER BY timestamp ASC")
    fun observarTodos(): Flow<List<Comentario>>

    @Delete
    suspend fun eliminar(comentario: Comentario)

    @Query("DELETE FROM comentarios WHERE publicacionId = :publicacionId")
    suspend fun eliminarPorPublicacion(publicacionId: Int)

    @Query("DELETE FROM comentarios WHERE autorId = :autorId")
    suspend fun eliminarPorAutor(autorId: Int)

    @Query("DELETE FROM comentarios")
    suspend fun eliminarTodos()
}
