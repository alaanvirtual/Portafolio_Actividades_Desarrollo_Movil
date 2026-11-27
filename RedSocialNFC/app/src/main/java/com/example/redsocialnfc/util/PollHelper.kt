package com.example.redsocialnfc.util

import java.net.URLDecoder
import java.net.URLEncoder

data class EncuestaData(
    val opciones: List<String>,
    val votos: List<Int>,
    val totalVotos: Int
)

object PollHelper {

    fun serializarEncuesta(opciones: List<String>): Pair<String, String> {
        val clean = opciones.map { it.trim() }.filter { it.isNotEmpty() }.take(4)
        val serializedOpc = clean.joinToString("|") { URLEncoder.encode(it, "UTF-8") }
        val serializedVotos = clean.joinToString(",") { "0" }
        return serializedOpc to serializedVotos
    }

    fun parsearEncuesta(opcionesJson: String, votosJson: String): EncuestaData? {
        if (opcionesJson.isBlank()) return null
        return try {
            val opciones = opcionesJson.split("|")
                .filter { it.isNotEmpty() }
                .map { URLDecoder.decode(it, "UTF-8") }
            if (opciones.size < 2) return null

            val votos = MutableList(opciones.size) { 0 }
            if (votosJson.isNotBlank()) {
                val partes = votosJson.split(",")
                for (i in 0 until minOf(partes.size, votos.size)) {
                    votos[i] = partes[i].trim().toIntOrNull()?.coerceAtLeast(0) ?: 0
                }
            }
            EncuestaData(
                opciones = opciones,
                votos = votos,
                totalVotos = votos.sum()
            )
        } catch (_: Exception) {
            null
        }
    }

    fun registrarVoto(
        votosJson: String,
        totalOpciones: Int,
        votoAnterior: Int,
        nuevoVoto: Int
    ): Pair<String, Int> {
        val votos = MutableList(totalOpciones) { 0 }
        if (votosJson.isNotBlank()) {
            val partes = votosJson.split(",")
            for (i in 0 until minOf(partes.size, totalOpciones)) {
                votos[i] = partes[i].trim().toIntOrNull()?.coerceAtLeast(0) ?: 0
            }
        }

        if (votoAnterior in 0 until totalOpciones) {
            votos[votoAnterior] = (votos[votoAnterior] - 1).coerceAtLeast(0)
        }
        val votoFinal = if (votoAnterior == nuevoVoto) {
            -1 // Tocar la misma opción quita el voto
        } else {
            if (nuevoVoto in 0 until totalOpciones) {
                votos[nuevoVoto] = votos[nuevoVoto] + 1
            }
            nuevoVoto
        }

        return votos.joinToString(",") to votoFinal
    }
}
