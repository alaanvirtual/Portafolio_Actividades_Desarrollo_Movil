package com.example.redsocialnfc.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PublicacionDao {

    @Insert
    suspend fun insertar(publicacion: Publicacion): Long

    @Query("SELECT * FROM publicaciones ORDER BY timestamp DESC")
    fun obtenerTodas(): Flow<List<Publicacion>>

    @Delete
    suspend fun eliminar(publicacion: Publicacion)

    @Query("SELECT COUNT(*) FROM publicaciones WHERE autorId = :autorId")
    suspend fun contarPorAutor(autorId: Int): Int

    @Query("SELECT COUNT(*) FROM publicaciones")
    suspend fun contarTotal(): Int
}
