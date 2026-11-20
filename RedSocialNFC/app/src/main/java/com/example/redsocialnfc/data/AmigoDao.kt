package com.example.redsocialnfc.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AmigoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(amigo: Amigo): Long

    @Query("SELECT * FROM amigos ORDER BY timestamp DESC")
    fun obtenerTodos(): Flow<List<Amigo>>

    @Query("SELECT COUNT(*) FROM amigos")
    suspend fun contarTotal(): Int

    @Query("SELECT * FROM amigos WHERE handle = :handle LIMIT 1")
    suspend fun obtenerPorHandle(handle: String): Amigo?
}
