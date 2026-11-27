package com.example.redsocialnfc.nfc

object NfcProtocol {
    // Application ID (AID) propietario para Local Social Network (Categoría F0)
    const val AID_HEX = "F0010203040506"

    val SELECT_APDU_HEADER = byteArrayOf(
        0x00.toByte(), // CLA: Clase estándar ISO 7816-4
        0xA4.toByte(), // INS: Instrucción SELECT FILE / APPLICATION
        0x04.toByte(), // P1: Selección directa por DF Name (AID)
        0x00.toByte()  // P2: Primera coincidencia
    )

    // Status Words (SW1 - SW2) ISO 7816-4
    val SW_SUCCESS = byteArrayOf(0x90.toByte(), 0x00.toByte()) // 9000: Operación exitosa
    val SW_UNKNOWN = byteArrayOf(0x6F.toByte(), 0x00.toByte()) // 6F00: Comando no reconocido o error

    fun hexToBytes(hex: String): ByteArray {
        val clean = hex.replace(" ", "").trim()
        val result = ByteArray(clean.length / 2)
        for (i in clean.indices step 2) {
            result[i / 2] = clean.substring(i, i + 2).toInt(16).toByte()
        }
        return result
    }

    fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02X", b))
        }
        return sb.toString()
    }

    fun buildSelectApdu(aid: String = AID_HEX): ByteArray {
        val aidBytes = hexToBytes(aid)
        val apdu = ByteArray(SELECT_APDU_HEADER.size + 1 + aidBytes.size + 1)
        System.arraycopy(SELECT_APDU_HEADER, 0, apdu, 0, SELECT_APDU_HEADER.size)
        apdu[SELECT_APDU_HEADER.size] = aidBytes.size.toByte() // Lc (longitud de datos entrantes)
        System.arraycopy(aidBytes, 0, apdu, SELECT_APDU_HEADER.size + 1, aidBytes.size)
        apdu[apdu.size - 1] = 0x00.toByte() // Le (longitud esperada)
        return apdu
    }

    fun isSelectAidCommand(apdu: ByteArray?, expectedAid: String = AID_HEX): Boolean {
        if (apdu == null || apdu.size < SELECT_APDU_HEADER.size + 1) return false
        for (i in SELECT_APDU_HEADER.indices) {
            if (apdu[i] != SELECT_APDU_HEADER[i]) return false
        }
        val lc = apdu[4].toInt() and 0xFF
        val aidBytes = hexToBytes(expectedAid)
        if (lc != aidBytes.size || apdu.size < 5 + lc) return false
        for (i in aidBytes.indices) {
            if (apdu[5 + i] != aidBytes[i]) return false
        }
        return true
    }

    private fun escapeJson(value: String): String {
        return value.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
    }

    private fun unescapeJson(value: String): String {
        return value.replace("\\\"", "\"")
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\\\", "\\")
    }

    fun crearPayloadJson(
        handle: String,
        nombre: String,
        nfcUuid: String,
        avatarUri: String? = null
    ): String {
        val avatarVal = if (avatarUri.isNullOrBlank()) "null" else "\"${escapeJson(avatarUri)}\""
        return "{\"app\":\"RedSocialNFC\",\"version\":1,\"handle\":\"${escapeJson(handle)}\",\"nombre\":\"${escapeJson(nombre)}\",\"nfcUuid\":\"${escapeJson(nfcUuid)}\",\"avatarUri\":$avatarVal,\"timestamp\":${System.currentTimeMillis()}}"
    }

    fun parsearPayloadJson(jsonStr: String): NfcContactPayload? {
        if (jsonStr.isBlank() || !jsonStr.contains("{") || !jsonStr.contains("}")) return null
        return try {
            val handleRegex = "\"handle\"\\s*:\\s*\"([^\"]*)\"".toRegex()
            val nombreRegex = "\"nombre\"\\s*:\\s*\"([^\"]*)\"".toRegex()
            val uuidRegex = "\"nfcUuid\"\\s*:\\s*\"([^\"]*)\"".toRegex()
            val avatarRegex = "\"avatarUri\"\\s*:\\s*\"([^\"]*)\"".toRegex()
            val timestampRegex = "\"timestamp\"\\s*:\\s*(\\d+)".toRegex()

            val handleMatch = handleRegex.find(jsonStr)?.groupValues?.get(1)?.let { unescapeJson(it).trim() } ?: ""
            val nombreMatch = nombreRegex.find(jsonStr)?.groupValues?.get(1)?.let { unescapeJson(it).trim() } ?: ""
            val uuidMatch = uuidRegex.find(jsonStr)?.groupValues?.get(1)?.let { unescapeJson(it).trim() } ?: ""
            val avatarMatch = avatarRegex.find(jsonStr)?.groupValues?.get(1)?.let { unescapeJson(it).trim() }?.ifBlank { null }
            val timestampMatch = timestampRegex.find(jsonStr)?.groupValues?.get(1)?.toLongOrNull() ?: System.currentTimeMillis()

            if (handleMatch.isNotBlank() && uuidMatch.isNotBlank()) {
                NfcContactPayload(
                    handle = handleMatch,
                    nombre = nombreMatch,
                    nfcUuid = uuidMatch,
                    avatarUri = avatarMatch,
                    timestamp = timestampMatch
                )
            } else null
        } catch (_: Exception) {
            null
        }
    }
}

data class NfcContactPayload(
    val handle: String,
    val nombre: String,
    val nfcUuid: String,
    val avatarUri: String?,
    val timestamp: Long
)
