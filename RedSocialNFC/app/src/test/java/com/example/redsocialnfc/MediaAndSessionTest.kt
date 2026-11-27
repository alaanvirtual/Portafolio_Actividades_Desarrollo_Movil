package com.example.redsocialnfc

import com.example.redsocialnfc.data.VisibilidadMuro
import com.example.redsocialnfc.nfc.NfcProtocol
import com.example.redsocialnfc.util.MediaStorageHelper
import com.example.redsocialnfc.util.PollHelper
import com.example.redsocialnfc.util.SessionManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaAndSessionTest {

    @Test
    fun passwordHashYVerificacionFuncionanCorrectamente() {
        val hash = SessionManager.hashPassword("secreto123")
        assertNotEquals("secreto123", hash)
        assertTrue(SessionManager.verificarPassword("secreto123", hash))
        assertTrue(SessionManager.verificarPassword("legacy", "legacy"))
        assertFalse(SessionManager.verificarPassword("incorrecta", hash))
    }

    @Test
    fun identidadAnonimaCumpleFormato() {
        val anon = SessionManager.generarIdentidadAnonima()
        assertTrue(anon.startsWith("@Anonimo_"))
        assertEquals(12, anon.length)
    }

    @Test
    fun formateoTamanoYDuracionEsLegible() {
        assertEquals("0 KB", MediaStorageHelper.formatearTamano(0L))
        assertEquals("1 KB", MediaStorageHelper.formatearTamano(512L))
        assertEquals("0:00", MediaStorageHelper.formatearDuracion(0L))
        assertEquals("1:05", MediaStorageHelper.formatearDuracion(65_000L))
    }

    @Test
    fun encuestaSerializacionYVotosFuncionanCorrectamente() {
        val (opcJson, votosJson) = PollHelper.serializarEncuesta(listOf("Sí", "No", "  ", "Tal vez"))
        val parsed = PollHelper.parsearEncuesta(opcJson, votosJson)
        assertNotNull(parsed)
        assertEquals(listOf("Sí", "No", "Tal vez"), parsed!!.opciones)
        assertEquals(0, parsed.totalVotos)

        // Primer voto en índice 0
        val (votos1, voto1) = PollHelper.registrarVoto(votosJson, 3, -1, 0)
        assertEquals(0, voto1)
        assertEquals(1, PollHelper.parsearEncuesta(opcJson, votos1)!!.totalVotos)

        // Cambiar voto al índice 2
        val (votos2, voto2) = PollHelper.registrarVoto(votos1, 3, voto1, 2)
        assertEquals(2, voto2)
        val parsed2 = PollHelper.parsearEncuesta(opcJson, votos2)!!
        assertEquals(listOf(0, 0, 1), parsed2.votos)

        // Quitar voto tocando el mismo índice
        val (votos3, voto3) = PollHelper.registrarVoto(votos2, 3, voto2, 2)
        assertEquals(-1, voto3)
        assertEquals(0, PollHelper.parsearEncuesta(opcJson, votos3)!!.totalVotos)

        assertNull(PollHelper.parsearEncuesta("", ""))
    }

    @Test
    fun visibilidadMuroSerializacionYResumenFuncionan() {
        val csv = VisibilidadMuro.serializarHandles(listOf("@ana", " @beto ", "@ana", ""))
        val parsed = VisibilidadMuro.parsearHandles(csv).toSet()
        assertEquals(setOf("@ana", "@beto"), parsed)
        assertTrue(VisibilidadMuro.parsearHandles("").isEmpty())

        val resumenTodos = VisibilidadMuro.formatearResumen(VisibilidadMuro.TODOS, "")
        val resumenAmigos = VisibilidadMuro.formatearResumen(VisibilidadMuro.AMIGOS, "")
        val resumenSeleccion = VisibilidadMuro.formatearResumen(VisibilidadMuro.SELECCIONADOS, csv)
        val resumenPrivado = VisibilidadMuro.formatearResumen(VisibilidadMuro.PRIVADO, "")

        assertTrue(resumenTodos.isNotBlank())
        assertTrue(resumenAmigos.isNotBlank())
        assertTrue(resumenSeleccion.isNotBlank())
        assertTrue(resumenPrivado.isNotBlank())
        assertEquals(4, setOf(resumenTodos, resumenAmigos, resumenSeleccion, resumenPrivado).size)
    }

    @Test
    fun nfcProtocoloHexYApduFuncionanCorrectamente() {
        val hex = "F0010203040506"
        val bytes = NfcProtocol.hexToBytes(hex)
        assertEquals(7, bytes.size)
        assertEquals(hex.uppercase(), NfcProtocol.bytesToHex(bytes))

        val selectApdu = NfcProtocol.buildSelectApdu(NfcProtocol.AID_HEX)
        assertTrue(NfcProtocol.isSelectAidCommand(selectApdu, NfcProtocol.AID_HEX))

        // Comando con AID incorrecto
        val apduInvalido = NfcProtocol.buildSelectApdu("F0999999999999")
        assertFalse(NfcProtocol.isSelectAidCommand(apduInvalido, NfcProtocol.AID_HEX))
        assertFalse(NfcProtocol.isSelectAidCommand(byteArrayOf(0x00, 0xA4.toByte())))
        assertFalse(NfcProtocol.isSelectAidCommand(null))
    }

    @Test
    fun nfcPayloadJsonSerializacionYParseoFuncionan() {
        val json = NfcProtocol.crearPayloadJson(
            handle = "@Carlos_Dev",
            nombre = "Carlos Mendoza",
            nfcUuid = "uuid-test-nfc-12345",
            avatarUri = null
        )
        assertNotNull(json)
        assertTrue(json.contains("@Carlos_Dev"))
        assertTrue(json.contains("RedSocialNFC"))

        val contacto = NfcProtocol.parsearPayloadJson(json)
        assertNotNull(contacto)
        assertEquals("@Carlos_Dev", contacto!!.handle)
        assertEquals("Carlos Mendoza", contacto.nombre)
        assertEquals("uuid-test-nfc-12345", contacto.nfcUuid)
        assertNull(contacto.avatarUri)
        assertTrue(contacto.timestamp > 0)

        // Parseo con json inválido
        assertNull(NfcProtocol.parsearPayloadJson("invalido"))
        assertNull(NfcProtocol.parsearPayloadJson("{}"))
    }
}
