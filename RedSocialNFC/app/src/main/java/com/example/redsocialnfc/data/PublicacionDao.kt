package com.example.redsocialnfc.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PublicacionDao {

    @Insert
    suspend fun insertar(publicacion: Publicacion): Long

    @Update
    suspend fun actualizar(publicacion: Publicacion)

    @Query("SELECT * FROM publicaciones ORDER BY fijado DESC, timestamp DESC")
    fun obtenerTodas(): Flow<List<Publicacion>>

    @Delete
    suspend fun eliminar(publicacion: Publicacion)

    @Query("SELECT COUNT(*) FROM publicaciones WHERE autorId = :autorId")
    fun observarConteoPorAutor(autorId: Int): Flow<Int>

    @Query("SELECT COALESCE(SUM(likesCount), 0) FROM publicaciones WHERE autorId = :autorId")
    fun observarLikesRecibidos(autorId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM publicaciones WHERE guardado = 1")
    fun observarConteoGuardados(): Flow<Int>

    @Query("SELECT COUNT(*) FROM publicaciones")
    suspend fun contarTotal(): Int

    @Query("UPDATE publicaciones SET autorNombre = :displayName, autorAvatarUri = :avatarUri WHERE autorId = :autorId")
    suspend fun actualizarAutorEnPublicaciones(autorId: Int, displayName: String, avatarUri: String?)

    @Query("UPDATE publicaciones SET categoria = 'Principal' WHERE categoria = :categoriaBorrada")
    suspend fun moverACategoriaPrincipal(categoriaBorrada: String)

    @Query("UPDATE publicaciones SET fijado = 0 WHERE autorId = :autorId AND id != :exceptoId")
    suspend fun desfijarOtrasDeAutor(autorId: Int, exceptoId: Int)

    @Query("DELETE FROM publicaciones WHERE autorId = :autorId")
    suspend fun eliminarPorAutor(autorId: Int)

    @Query("DELETE FROM publicaciones")
    suspend fun eliminarTodas()
}
