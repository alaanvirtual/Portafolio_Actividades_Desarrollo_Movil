package com.example.redsocialnfc.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(categoria: Categoria): Long

    @Query("SELECT * FROM categorias ORDER BY timestamp ASC")
    fun observarTodas(): Flow<List<Categoria>>

    @Query("SELECT * FROM categorias WHERE LOWER(nombre) = LOWER(:nombre) LIMIT 1")
    suspend fun obtenerPorNombre(nombre: String): Categoria?

    @Query("UPDATE categorias SET visibilidad = :visibilidad, permitidosHandles = :permitidosHandles WHERE id = :categoriaId")
    suspend fun actualizarVisibilidad(categoriaId: Int, visibilidad: String, permitidosHandles: String)

    @Delete
    suspend fun eliminar(categoria: Categoria)

    @Query("DELETE FROM categorias WHERE autorId = :autorId")
    suspend fun eliminarPorAutor(autorId: Int)

    @Query("DELETE FROM categorias")
    suspend fun eliminarTodas()
}
