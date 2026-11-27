package com.example.redsocialnfc.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registrarUsuario(usuario: Usuario): Long

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun obtenerPorEmail(email: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE realHandle = :realHandle LIMIT 1")
    suspend fun obtenerPorHandle(realHandle: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): Usuario?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    fun observarPorId(id: Int): Flow<Usuario?>

    @Query("UPDATE usuarios SET displayName = :displayName, bio = :bio, avatarUri = :avatarUri WHERE id = :userId")
    suspend fun actualizarPerfil(userId: Int, displayName: String, bio: String, avatarUri: String?)

    @Query("UPDATE usuarios SET passwordHash = :newPasswordHash WHERE id = :userId")
    suspend fun actualizarPassword(userId: Int, newPasswordHash: String)

    @Query("DELETE FROM usuarios WHERE id = :userId")
    suspend fun eliminarPorId(userId: Int)
}
