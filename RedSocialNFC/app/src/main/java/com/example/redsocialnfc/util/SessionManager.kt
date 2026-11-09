package com.example.redsocialnfc.util

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID
import kotlin.random.Random

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("nfc_social_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_REAL_HANDLE = "real_handle"
        private const val KEY_ANON_HANDLE = "anon_handle"
        private const val KEY_DEVICE_NFC_UUID = "device_nfc_uuid"

        // RF-03: Generador de identidad anónima aleatoria inalterable (ej. @Anonimo_89B)
        fun generarIdentidadAnonima(): String {
            val numero = Random.nextInt(10, 99)
            val letras = ('A'..'Z').toList()
            val letra = letras[Random.nextInt(letras.size)]
            return "@Anonimo_${numero}${letra}"
        }
    }

    // RF-04: Identificador Criptográfico NFC por dispositivo (proceso en segundo plano)
    fun obtenerOGenerarDeviceNfcUuid(): String {
        var uuid = prefs.getString(KEY_DEVICE_NFC_UUID, null)
        if (uuid == null) {
            uuid = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_NFC_UUID, uuid).apply()
        }
        return uuid
    }

    fun guardarSesion(userId: Int, email: String, realHandle: String, anonHandle: String) {
        prefs.edit()
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_EMAIL, email)
            .putString(KEY_REAL_HANDLE, realHandle)
            .putString(KEY_ANON_HANDLE, anonHandle)
            .apply()
    }

    fun cerrarSesion() {
        prefs.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_EMAIL)
            .remove(KEY_REAL_HANDLE)
            .remove(KEY_ANON_HANDLE)
            .apply()
    }

    fun haySesionActiva(): Boolean = prefs.getInt(KEY_USER_ID, -1) != -1

    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)
    fun getEmail(): String = prefs.getString(KEY_EMAIL, "") ?: ""
    fun getRealHandle(): String = prefs.getString(KEY_REAL_HANDLE, "") ?: ""
    fun getAnonHandle(): String = prefs.getString(KEY_ANON_HANDLE, "") ?: ""
}
