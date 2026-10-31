package com.example.redsocial.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PublicacionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(publicacion: Publicacion)

    @Update
    suspend fun actualizar(publicacion: Publicacion)

    @Delete
    suspend fun eliminar(publicacion: Publicacion)

    @Query("SELECT * FROM publicaciones ORDER BY id DESC")
    fun obtenerTodas(): Flow<List<Publicacion>>
}
