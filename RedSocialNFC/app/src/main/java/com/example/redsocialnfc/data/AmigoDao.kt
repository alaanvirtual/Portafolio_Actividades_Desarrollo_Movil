package com.example.redsocialnfc.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AmigoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(amigo: Amigo): Long

    @Query("SELECT * FROM amigos ORDER BY timestamp DESC")
    fun obtenerTodos(): Flow<List<Amigo>>

    @Query("SELECT COUNT(*) FROM amigos")
    fun observarConteo(): Flow<Int>

    @Query("SELECT COUNT(*) FROM amigos")
    suspend fun contarTotal(): Int

    @Query("SELECT * FROM amigos WHERE handle = :handle LIMIT 1")
    suspend fun obtenerPorHandle(handle: String): Amigo?

    @Delete
    suspend fun eliminar(amigo: Amigo)

    @Query("DELETE FROM amigos")
    suspend fun eliminarTodos()
}
