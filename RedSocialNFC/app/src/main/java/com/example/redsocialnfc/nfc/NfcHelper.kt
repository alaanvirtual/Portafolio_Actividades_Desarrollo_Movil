package com.example.redsocialnfc.nfc

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.Ndef
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import com.example.redsocialnfc.data.Amigo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.IOException

sealed class NfcEvent {
    data class ContactoDetectado(val amigo: Amigo) : NfcEvent()
    data class Error(val mensaje: String) : NfcEvent()
    object LecturaIniciada : NfcEvent()
}

object NfcHelper {
    private const val TAG = "NfcHelper"
    private val _eventosNfc = MutableSharedFlow<NfcEvent>(extraBufferCapacity = 10)
    val eventosNfc: SharedFlow<NfcEvent> = _eventosNfc.asSharedFlow()

    fun isNfcSupported(context: Context): Boolean {
        val adapter = NfcAdapter.getDefaultAdapter(context)
        return adapter != null
    }

    fun isNfcEnabled(context: Context): Boolean {
        val adapter = NfcAdapter.getDefaultAdapter(context)
        return adapter != null && adapter.isEnabled
    }

    fun abrirAjustesNfc(context: Context) {
        val intent = Intent(Settings.ACTION_NFC_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallback)
        }
    }

    fun vibrarConfirmacion(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 80, 50, 120), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createWaveform(longArrayOf(0, 80, 50, 120), -1)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(150)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al vibrar: ${e.message}")
        }
    }

    fun enableReaderMode(
        activity: Activity,
        scope: CoroutineScope,
        onContactFound: (Amigo) -> Unit
    ) {
        val adapter = NfcAdapter.getDefaultAdapter(activity) ?: return
        if (!adapter.isEnabled) return

        val flags = NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK

        val options = Bundle().apply {
            putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
        }

        try {
            adapter.enableReaderMode(
                activity,
                { tag ->
                    scope.launch(Dispatchers.IO) {
                        procesarTag(tag, activity, onContactFound)
                    }
                },
                flags,
                options
            )
            Log.d(TAG, "ReaderMode NFC habilitado exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error habilitando ReaderMode: ${e.message}")
        }
    }

    fun disableReaderMode(activity: Activity) {
        val adapter = NfcAdapter.getDefaultAdapter(activity) ?: return
        try {
            adapter.disableReaderMode(activity)
            Log.d(TAG, "ReaderMode NFC deshabilitado")
        } catch (e: Exception) {
            Log.e(TAG, "Error deshabilitando ReaderMode: ${e.message}")
        }
    }

    private fun procesarTag(
        tag: Tag,
        context: Context,
        onContactFound: (Amigo) -> Unit
    ) {
        _eventosNfc.tryEmit(NfcEvent.LecturaIniciada)
        val isoDep = IsoDep.get(tag)

        if (isoDep != null) {
            try {
                isoDep.connect()
                isoDep.timeout = 5000

                val selectApdu = NfcProtocol.buildSelectApdu()
                Log.d(TAG, "Enviando SELECT APDU: ${NfcProtocol.bytesToHex(selectApdu)}")

                val response = isoDep.transceive(selectApdu)
                Log.d(TAG, "Respuesta recibida (${response.size} bytes): ${NfcProtocol.bytesToHex(response)}")

                if (response.size >= 2) {
                    val sw1 = response[response.size - 2]
                    val sw2 = response[response.size - 1]

                    if (sw1 == 0x90.toByte() && sw2 == 0x00.toByte()) {
                        val payloadBytes = response.copyOfRange(0, response.size - 2)
                        val jsonStr = String(payloadBytes, Charsets.UTF_8)
                        val contacto = NfcProtocol.parsearPayloadJson(jsonStr)

                        if (contacto != null) {
                            val amigo = Amigo(
                                handle = contacto.handle,
                                nombre = contacto.nombre,
                                nfcUuid = contacto.nfcUuid,
                                timestamp = System.currentTimeMillis()
                            )
                            vibrarConfirmacion(context)
                            _eventosNfc.tryEmit(NfcEvent.ContactoDetectado(amigo))
                            onContactFound(amigo)
                            return
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error transceive IsoDep: ${e.message}")
            } finally {
                try {
                    isoDep.close()
                } catch (_: Exception) {}
            }
        }

        // Fallback a NDEF si es una etiqueta física tradicional grabada
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            try {
                ndef.connect()
                val ndefMessage = ndef.ndefMessage
                if (ndefMessage != null && ndefMessage.records.isNotEmpty()) {
                    for (record in ndefMessage.records) {
                        val text = String(record.payload, Charsets.UTF_8)
                        val contacto = NfcProtocol.parsearPayloadJson(text)
                        if (contacto != null) {
                            val amigo = Amigo(
                                handle = contacto.handle,
                                nombre = contacto.nombre,
                                nfcUuid = contacto.nfcUuid,
                                timestamp = System.currentTimeMillis()
                            )
                            vibrarConfirmacion(context)
                            _eventosNfc.tryEmit(NfcEvent.ContactoDetectado(amigo))
                            onContactFound(amigo)
                            return
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error leyendo NDEF: ${e.message}")
            } finally {
                try {
                    ndef.close()
                } catch (_: Exception) {}
            }
        }

        _eventosNfc.tryEmit(NfcEvent.Error("Dispositivo o tag NFC no reconocido"))
    }
}
