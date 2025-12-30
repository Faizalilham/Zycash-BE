package com.zycash.betransaction.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.zycash.betransaction.dto.ParsedTransaction
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal

@Service
class PriceEnrichmentService(
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
    @Value("\${serp.api.key:}") private val serpApiKey: String,
    @Value("\${serp.api.url:https://serpapi.com/search}") private val serpApiUrl: String
) {

    private val log = LoggerFactory.getLogger(PriceEnrichmentService::class.java)

    fun enrichPrice(transaction: ParsedTransaction): ParsedTransaction {
        // Jika sudah ada amount, tidak perlu lookup
        if (transaction.amount != null || !transaction.needsPriceLookup) {
            return transaction
        }

        return try {
            log.info("Looking up price for: ${transaction.description}")

            val searchQuery = transaction.searchQuery ?: buildDefaultQuery(transaction)
            val priceInfo = searchPrice(searchQuery)

            if (priceInfo != null) {
                val totalAmount = calculateAmount(priceInfo, transaction.quantity)
                log.info("Found price: $priceInfo/unit, total: $totalAmount")

                transaction.copy(amount = totalAmount)
            } else {
                log.warn("Price not found for: ${transaction.description}, returning with null amount")
                transaction
            }
        } catch (e: Exception) {
            log.error("Error enriching price: ${e.message}", e)
            transaction // Return original if error
        }
    }

    private fun searchPrice(query: String): BigDecimal? {
        // Skip if no API key configured
        if (serpApiKey.isBlank()) {
            log.warn("SERP API key not configured, skipping price lookup")
            return null
        }

        return try {
            val url = "$serpApiUrl?q=${query}&api_key=$serpApiKey&engine=google&num=3"

            val response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                String::class.java
            )

            val body = response.body ?: return null
            extractPriceFromSerpResult(body)
        } catch (e: Exception) {
            log.error("Error calling SERP API: ${e.message}", e)
            null
        }
    }

    private fun extractPriceFromSerpResult(jsonResponse: String): BigDecimal? {
        return try {
            val root = objectMapper.readTree(jsonResponse)

            // Try to get from answer box (featured snippet)
            val answerBox = root.get("answer_box")
            if (answerBox != null) {
                val answer = answerBox.get("answer")?.asText()
                if (answer != null) {
                    val price = extractPriceFromText(answer)
                    if (price != null) return price
                }
            }

            // Try from organic results
            val organicResults = root.get("organic_results")
            if (organicResults != null && organicResults.isArray) {
                for (result in organicResults) {
                    val snippet = result.get("snippet")?.asText() ?: ""
                    val title = result.get("title")?.asText() ?: ""

                    val price = extractPriceFromText("$title $snippet")
                    if (price != null) return price
                }
            }

            null
        } catch (e: Exception) {
            log.error("Error extracting price from SERP result: ${e.message}", e)
            null
        }
    }

    private fun extractPriceFromText(text: String): BigDecimal? {
        // Pattern untuk match harga Indonesia: Rp 10.000, Rp10000, 10000, 10.000, etc
        val patterns = listOf(
            Regex("""Rp\s*([0-9]{1,3}(?:[.,][0-9]{3})*(?:[.,][0-9]+)?)"""),
            Regex("""IDR\s*([0-9]{1,3}(?:[.,][0-9]{3})*(?:[.,][0-9]+)?)"""),
            Regex("""([0-9]{1,3}(?:[.,][0-9]{3})+)""") // Angka dengan separator
        )

        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                val priceStr = match.groupValues[1]
                    .replace(".", "")  // Remove thousand separator
                    .replace(",", ".") // Convert decimal separator if any

                try {
                    val price = BigDecimal(priceStr)
                    // Validasi range harga wajar (100 - 10 juta)
                    if (price >= BigDecimal("100") && price <= BigDecimal("10000000")) {
                        return price
                    }
                } catch (e: Exception) {
                    continue
                }
            }
        }

        return null
    }

    private fun calculateAmount(unitPrice: BigDecimal, quantity: BigDecimal?): BigDecimal {
        return if (quantity != null && quantity > BigDecimal.ZERO) {
            unitPrice.multiply(quantity)
        } else {
            unitPrice
        }
    }

    private fun buildDefaultQuery(transaction: ParsedTransaction): String {
        return when {
            transaction.description.contains("bensin", ignoreCase = true) -> {
                "harga bensin pertalite pertamina per liter hari ini jakarta"
            }
            else -> {
                "harga ${transaction.description} hari ini"
            }
        }
    }
}