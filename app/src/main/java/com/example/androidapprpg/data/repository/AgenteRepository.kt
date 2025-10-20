package com.example.androidapprpg.data.repository

import com.example.androidapprpg.data.remote.services.AgenteService
import com.google.gson.JsonParser
import javax.inject.Inject

class AgenteRepository @Inject constructor(private val service : AgenteService){
    suspend fun consultar(pergunta: String): String {
        val resp = service.consulta(pergunta)
        if (!resp.isSuccessful) {
            throw RuntimeException("Agente HTTP ${resp.code()}")
        }
        val raw = resp.body().orEmpty().trim()
        if (raw.isBlank()) return "Sem resposta do agente."

        // Tenta extrair campo "resposta" se vier JSON { "resposta": "..." }
        return try {
            val el = JsonParser.parseString(raw)
            if (el.isJsonObject) {
                val obj = el.asJsonObject
                when {
                    obj.has("resposta") -> obj.get("resposta").asString
                    obj.has("answer")   -> obj.get("answer").asString
                    else                -> raw
                }
            } else raw
        } catch (_: Exception) {
            // Não era JSON -> retorna corpo bruto
            raw
        }
    }
}