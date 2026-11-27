package com.example.redsocialnfc

import com.example.redsocialnfc.data.VisibilidadMuro
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
}
