package com.example.practica14.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MarcadorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(marcador: MarcadorEntity): Long

    @Delete
    suspend fun eliminar(marcador: MarcadorEntity)

    @Query("SELECT * FROM marcadores ORDER BY id DESC")
    fun obtenerTodos(): Flow<List<MarcadorEntity>>

    @Query("DELETE FROM marcadores")
    suspend fun eliminarTodos()
}
