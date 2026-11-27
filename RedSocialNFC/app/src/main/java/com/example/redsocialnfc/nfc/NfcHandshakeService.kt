package com.example.redsocialnfc.nfc

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import com.example.redsocialnfc.util.SessionManager

class NfcHandshakeService : HostApduService() {

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) return NfcProtocol.SW_UNKNOWN

        Log.d(TAG, "processCommandApdu recibido: ${NfcProtocol.bytesToHex(commandApdu)}")

        if (NfcProtocol.isSelectAidCommand(commandApdu)) {
            val sessionManager = SessionManager(applicationContext)
            val handle = sessionManager.getRealHandle()
            val nombre = sessionManager.getDisplayName()
            val nfcUuid = sessionManager.obtenerOGenerarDeviceNfcUuid()
            val avatarUri = sessionManager.getAvatarUri()

            val json = NfcProtocol.crearPayloadJson(
                handle = handle,
                nombre = nombre,
                nfcUuid = nfcUuid,
                avatarUri = avatarUri
            )

            val payloadBytes = json.toByteArray(Charsets.UTF_8)
            val response = ByteArray(payloadBytes.size + NfcProtocol.SW_SUCCESS.size)
            System.arraycopy(payloadBytes, 0, response, 0, payloadBytes.size)
            System.arraycopy(NfcProtocol.SW_SUCCESS, 0, response, payloadBytes.size, NfcProtocol.SW_SUCCESS.size)

            Log.d(TAG, "Enviando respuesta HCE exitosa (${payloadBytes.size} bytes): $json")
            return response
        }

        return NfcProtocol.SW_UNKNOWN
    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "NfcHandshakeService desactivado, motivo: $reason")
    }

    companion object {
        private const val TAG = "NfcHandshakeService"
    }
}
