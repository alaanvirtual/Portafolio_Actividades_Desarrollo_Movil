package com.example.redsocialnfc.util

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.util.UUID
import kotlin.random.Random

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("nfc_social_session", Context.MODE_PRIVATE)

    private val settingsPrefs: SharedPreferences =
        context.getSharedPreferences("nfc_social_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_REAL_HANDLE = "real_handle"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val KEY_BIO = "user_bio"
        private const val KEY_AVATAR_URI = "user_avatar_uri"
        private const val KEY_ANON_HANDLE = "anon_handle"
        private const val KEY_DEVICE_NFC_UUID = "device_nfc_uuid"

        // Claves de preferencias de usuario
        const val PREF_DEFAULT_ANON_POST = "default_anon_post"
        const val PREF_SOLO_AMIGOS_CHAT = "solo_amigos_chat"
        const val PREF_OCULTAR_EN_LINEA = "ocultar_en_linea"
        const val PREF_COMPACT_FEED = "compact_feed"
        const val PREF_NOTIF_CONEXIONES = "notif_conexiones"
        const val PREF_NOTIF_MENSAJES = "notif_mensajes"
        const val PREF_NOTIF_INTERACCIONES = "notif_interacciones"
        const val PREF_NFC_BACKGROUND = "nfc_background"
        const val PREF_PRINCIPAL_VISIBILIDAD = "principal_visibilidad"
        const val PREF_PRINCIPAL_PERMITIDOS = "principal_permitidos"

        fun generarIdentidadAnonima(): String {
            val numero = Random.nextInt(10, 99)
            val letras = ('A'..'Z').toList()
            val letra = letras[Random.nextInt(letras.size)]
            return "@Anonimo_${numero}${letra}"
        }

        fun hashPassword(password: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }

        fun verificarPassword(rawPassword: String, storedHash: String): Boolean {
            return hashPassword(rawPassword) == storedHash || rawPassword == storedHash
        }
    }

    fun obtenerOGenerarDeviceNfcUuid(): String {
        var uuid = prefs.getString(KEY_DEVICE_NFC_UUID, null)
        if (uuid.isNullOrBlank()) {
            uuid = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_NFC_UUID, uuid).apply()
        }
        return uuid
    }

    fun guardarSesion(
        userId: Int,
        email: String,
        realHandle: String,
        displayName: String,
        anonHandle: String,
        bio: String = "",
        avatarUri: String? = null,
        nfcUuid: String? = null
    ) {
        val editor = prefs.edit()
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_EMAIL, email)
            .putString(KEY_REAL_HANDLE, realHandle)
            .putString(KEY_DISPLAY_NAME, displayName.ifBlank { realHandle.removePrefix("@") })
            .putString(KEY_ANON_HANDLE, anonHandle)
            .putString(KEY_BIO, bio)
            .putString(KEY_AVATAR_URI, avatarUri)

        if (!nfcUuid.isNullOrBlank()) {
            editor.putString(KEY_DEVICE_NFC_UUID, nfcUuid)
        }
        editor.apply()
    }

    fun actualizarDatosPerfil(displayName: String, bio: String, avatarUri: String?) {
        prefs.edit()
            .putString(KEY_DISPLAY_NAME, displayName)
            .putString(KEY_BIO, bio)
            .putString(KEY_AVATAR_URI, avatarUri)
            .apply()
    }

    fun cerrarSesion() {
        prefs.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_EMAIL)
            .remove(KEY_REAL_HANDLE)
            .remove(KEY_DISPLAY_NAME)
            .remove(KEY_BIO)
            .remove(KEY_AVATAR_URI)
            .remove(KEY_ANON_HANDLE)
            .apply()
    }

    fun haySesionActiva(): Boolean = prefs.getInt(KEY_USER_ID, -1) != -1

    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)
    fun getEmail(): String = prefs.getString(KEY_EMAIL, "") ?: ""
    fun getRealHandle(): String = prefs.getString(KEY_REAL_HANDLE, "") ?: ""
    fun getDisplayName(): String {
        val saved = prefs.getString(KEY_DISPLAY_NAME, "") ?: ""
        return saved.ifBlank { getRealHandle().removePrefix("@").ifBlank { "Usuario" } }
    }
    fun getBio(): String = prefs.getString(KEY_BIO, "") ?: ""
    fun getAvatarUri(): String? = prefs.getString(KEY_AVATAR_URI, null)
    fun getAnonHandle(): String = prefs.getString(KEY_ANON_HANDLE, "") ?: ""

    fun getSetting(key: String, default: Boolean): Boolean = settingsPrefs.getBoolean(key, default)
    fun setSetting(key: String, value: Boolean) {
        settingsPrefs.edit().putBoolean(key, value).apply()
    }

    fun getStringSetting(key: String, default: String): String = settingsPrefs.getString(key, default) ?: default
    fun setStringSetting(key: String, value: String) {
        settingsPrefs.edit().putString(key, value).apply()
    }
}
