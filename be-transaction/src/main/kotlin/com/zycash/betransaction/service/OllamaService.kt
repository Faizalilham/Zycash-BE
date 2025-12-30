package com.zycash.betransaction.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.zycash.betransaction.dto.OllamaParseResult
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class OllamaService(
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
    @Value("\${ollama.url:http://localhost:11434}") private val ollamaUrl: String,
    @Value("\${ollama.model:llama3.2:3b}") private val ollamaModel: String
) {

    private val log = LoggerFactory.getLogger(OllamaService::class.java)

    fun validateIntent(text: String): Pair<String, Double> {
        return try {
            val prompt = """
                Klasifikasikan teks berikut sebagai salah satu dari:
                - TRANSACTION: jika tentang pengeluaran, pembelian, pembayaran, atau pencatatan keuangan
                - INVALID: jika pertanyaan umum, chitchat, atau tidak berhubungan dengan transaksi keuangan
                
                Teks: "$text"
                
                Berikan response dalam format JSON:
                {
                  "intent": "TRANSACTION atau INVALID",
                  "confidence": 0.0-1.0
                }
                
                Hanya berikan JSON, tanpa penjelasan tambahan.
            """.trimIndent()

            val response = callOllama(prompt)
            val rootNode = objectMapper.readTree(response)
            val ollamaResponse = rootNode.get("response").asText()
            val jsonStr = extractJson(ollamaResponse)
            val resultNode = objectMapper.readTree(jsonStr)

            val intent = resultNode.get("intent").asText()
            val confidence = resultNode.get("confidence").asDouble()

            log.info("Intent validation - Intent: $intent, Confidence: $confidence")
            Pair(intent, confidence)
        } catch (e: Exception) {
            log.error("Error validating intent: ${e.message}", e)
            // Default to TRANSACTION jika error, biar user experience tidak terganggu
            Pair("TRANSACTION", 0.5)
        }
    }

    fun parseTransaction(text: String): OllamaParseResult {
        return try {
            val prompt = buildPrompt(text)
            val response = callOllama(prompt)
            parseResponse(response)
        } catch (e: Exception) {
            log.error("Error parsing transaction with Ollama: ${e.message}", e)
            throw RuntimeException("Failed to parse transaction", e)
        }
    }

    private fun buildPrompt(text: String): String {
        return """
            Kamu adalah asisten keuangan. Analisis teks berikut dan ekstrak SEMUA informasi transaksi yang ada.
            Jika ada MULTIPLE transaksi dalam satu teks, pisahkan menjadi array.
            
            Teks: "$text"
            
            Aturan penting:
            1. Jika harga TIDAK disebutkan explicit, set amount = null dan needs_price_lookup = true
            2. Untuk bensin:
               - Default adalah Pertalite Pertamina jika tidak menyebut tipe/merek
               - Jika menyebut Shell/Pertamax/dll, sertakan dalam search_query
               - Extract quantity dan unit (contoh: "1 liter" -> quantity=1, unit="liter")
            3. Kategori yang valid: Makanan, Transport, Belanja, Tagihan, Hiburan, Kesehatan, Lainnya
            4. Generate search_query yang spesifik untuk lookup harga
            
            Berikan response dalam format JSON:
            {
              "transactions": [
                {
                  "category": "kategori",
                  "amount": nominal atau null,
                  "description": "deskripsi singkat",
                  "needs_price_lookup": true/false,
                  "search_query": "query untuk search harga (jika needs_price_lookup=true)",
                  "quantity": angka quantity (opsional),
                  "unit": "satuan seperti liter, kg, porsi (opsional)"
                }
              ]
            }
            
            Contoh:
            Input: "beli bensin shell 2 liter, ayam penyet 20rb"
            Output:
            {
              "transactions": [
                {
                  "category": "Transport",
                  "amount": null,
                  "description": "bensin shell 2 liter",
                  "needs_price_lookup": true,
                  "search_query": "harga bensin shell per liter hari ini jakarta",
                  "quantity": 2,
                  "unit": "liter"
                },
                {
                  "category": "Makanan",
                  "amount": 20000,
                  "description": "ayam penyet",
                  "needs_price_lookup": false,
                  "quantity": null,
                  "unit": null
                }
              ]
            }
            
            Hanya berikan JSON, tanpa penjelasan tambahan.
        """.trimIndent()
    }

    private fun callOllama(prompt: String): String {
        val url = "$ollamaUrl/api/generate"

        val request = mapOf(
            "model" to ollamaModel,
            "prompt" to prompt,
            "stream" to false
        )

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }

        val entity = HttpEntity(request, headers)

        log.info("Calling Ollama with model: $ollamaModel")

        val response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            String::class.java
        )

        return response.body ?: throw RuntimeException("Empty response from Ollama")
    }

    private fun parseResponse(response: String): OllamaParseResult {
        val rootNode = objectMapper.readTree(response)
        val ollamaResponse = rootNode.get("response").asText()

        log.info("Ollama raw response: $ollamaResponse")

        // Extract JSON dari response
        val jsonStr = extractJson(ollamaResponse)
        log.info("Extracted JSON: $jsonStr")

        return objectMapper.readValue(jsonStr, OllamaParseResult::class.java)
    }

    private fun extractJson(text: String): String {
        val start = text.indexOf("{")
        val end = text.lastIndexOf("}") + 1
        return if (start >= 0 && end > start) {
            text.substring(start, end)
        } else {
            text
        }
    }
}