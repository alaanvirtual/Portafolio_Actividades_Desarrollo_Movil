package com.example.redsocialnfc.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

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
}
