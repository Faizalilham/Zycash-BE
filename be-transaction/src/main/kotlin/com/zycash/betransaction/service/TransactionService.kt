package com.zycash.betransaction.service

import com.zycash.betransaction.common.TransactionEvent
import com.zycash.betransaction.constant.Constant
import com.zycash.betransaction.dto.Response
import com.zycash.betransaction.dto.TransactionRequest
import com.zycash.betransaction.model.Transaction
import com.zycash.betransaction.repository.TransactionRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val ollamaService: OllamaService,
    private val priceEnrichmentService: PriceEnrichmentService,
    private val kafkaTemplate: KafkaTemplate<String, TransactionEvent>
) {

    private val log = LoggerFactory.getLogger(TransactionService::class.java)

    companion object {
        const val TOPIC_TRANSACTION_EVENTS = "transaction-events"
    }

    @Transactional
    fun createTransaction(request: TransactionRequest): Response<List<Transaction>> {
        return try {
            log.info("Processing transaction text: ${request.text}")

            // Step 1: Validate intent
            val (intent, confidence) = ollamaService.validateIntent(request.text)

            if (intent == "INVALID") {
                return Response(
                    responseCode = 400,
                    responseMessage = "Input tidak valid",
                    errorList = listOf(
                        "Maaf, saya hanya membantu mencatat pengeluaran. " +
                                "Ceritakan pengeluaran Anda, misalnya: 'Beli makan 25ribu, isi bensin 50ribu'"
                    )
                )
            }

            if (confidence < 0.6) {
                return Response(
                    responseCode = 400,
                    responseMessage = "Input kurang jelas",
                    errorList = listOf(
                        "Hmm, saya kurang paham. Bisa diperjelas? " +
                                "Contoh: 'Bayar listrik 200ribu' atau 'Beli kopi 15ribu'"
                    )
                )
            }

            // Step 2: Parse transactions dengan Ollama
            val parseResult = ollamaService.parseTransaction(request.text)

            if (parseResult.transactions.isEmpty()) {
                return Response(
                    responseCode = 400,
                    responseMessage = "Tidak ada transaksi yang terdeteksi",
                    errorList = listOf("Silakan coba lagi dengan format yang lebih jelas")
                )
            }

            // Step 3: Enrich prices untuk yang needs_price_lookup
            val enrichedTransactions = parseResult.transactions.map { parsedTx ->
                if (parsedTx.needsPriceLookup) {
                    priceEnrichmentService.enrichPrice(parsedTx)
                } else {
                    parsedTx
                }
            }

            // Step 4: Save to database
            val savedTransactions = mutableListOf<Transaction>()
            val skippedTransactions = mutableListOf<String>()

            enrichedTransactions.forEach { parsedTx ->
                if (parsedTx.amount != null) {
                    val transaction = Transaction(
                        category = parsedTx.category,
                        amount = parsedTx.amount,
                        description = parsedTx.description,
                        originalText = request.text,
                        transactionDate = LocalDateTime.now()
                    )
                    val saved = transactionRepository.save(transaction)
                    savedTransactions.add(saved)

                    // ✅ Step 5: Publish event ke Kafka untuk setiap transaction
                    publishTransactionEvent(saved)
                } else {
                    skippedTransactions.add(parsedTx.description)
                }
            }

            log.info("Saved ${savedTransactions.size} transactions, skipped ${skippedTransactions.size}")

            // Step 6: Build response
            val message = buildResponseMessage(savedTransactions, skippedTransactions)
            val warnings = if (skippedTransactions.isNotEmpty()) {
                listOf("Beberapa transaksi memerlukan konfirmasi harga: ${skippedTransactions.joinToString(", ")}")
            } else null

            Response(
                responseCode = if (savedTransactions.isNotEmpty()) Constant.Response.CREATED_CODE else 400,
                responseMessage = message,
                data = savedTransactions,
                errorList = warnings
            )
        } catch (e: Exception) {
            log.error("Error creating transaction: ${e.message}", e)
            Response(
                responseCode = 500,
                responseMessage = "Internal Server Error",
                errorList = listOf(e.message ?: "Failed to process transaction")
            )
        }
    }

    private fun publishTransactionEvent(transaction: Transaction) {
        try {
            val event = TransactionEvent(
                eventType = "TRANSACTION_CREATED",
                transactionId = transaction.id!!,
                userId = "888",
                category = transaction.category,
                amount = transaction.amount,
                description = transaction.description,
                timestamp = LocalDateTime.now()
            )

            kafkaTemplate.send(TOPIC_TRANSACTION_EVENTS, transaction.id.toString(), event)
            log.info("Published transaction event for ID: ${transaction.id}")
        } catch (e: Exception) {
            log.error("Failed to publish transaction event", e)
            // Don't throw - event publishing failure shouldn't fail the transaction
        }
    }

    private fun buildResponseMessage(
        saved: List<Transaction>,
        skipped: List<String>
    ): String {
        return when {
            saved.isEmpty() && skipped.isEmpty() -> "Tidak ada transaksi yang berhasil dicatat"
            saved.isEmpty() -> "Gagal mencatat transaksi. Silakan coba lagi."
            skipped.isEmpty() -> {
                val total = saved.sumOf { it.amount }
                "Berhasil mencatat ${saved.size} transaksi. Total: Rp ${"%,d".format(total.toLong())}"
            }
            else -> {
                val total = saved.sumOf { it.amount }
                "Berhasil mencatat ${saved.size} transaksi (Total: Rp ${"%,d".format(total.toLong())}). " +
                        "${skipped.size} transaksi memerlukan konfirmasi."
            }
        }
    }

    fun getAllTransactions(): Response<List<Transaction>> {
        return try {
            val transactions = transactionRepository.findAllOrderByDateDesc()

            Response(
                responseCode = Constant.Response.SUCCESS_CODE,
                responseMessage = Constant.Response.SUCCESS_MESSAGE,
                data = transactions,
                totalData = transactions.size.toLong()
            )
        } catch (e: Exception) {
            log.error("Error fetching transactions: ${e.message}", e)
            Response(
                responseCode = 500,
                responseMessage = "Internal Server Error",
                errorList = listOf(e.message ?: "Failed to fetch transactions")
            )
        }
    }

    fun getTransactionsByCategory(category: String): Response<List<Transaction>> {
        return try {
            val transactions = transactionRepository.findByCategory(category)

            Response(
                responseCode = Constant.Response.SUCCESS_CODE,
                responseMessage = Constant.Response.SUCCESS_MESSAGE,
                data = transactions,
                totalData = transactions.size.toLong()
            )
        } catch (e: Exception) {
            log.error("Error fetching transactions by category: ${e.message}", e)
            Response(
                responseCode = 500,
                responseMessage = "Internal Server Error",
                errorList = listOf(e.message ?: "Failed to fetch transactions")
            )
        }
    }

    fun getTransactionById(id: Long): Response<Transaction> {
        return try {
            val transaction = transactionRepository.findById(id)
                .orElseThrow { RuntimeException(Constant.Message.NOT_FOUND_MESSAGE) }

            Response(
                responseCode = Constant.Response.SUCCESS_CODE,
                responseMessage = Constant.Response.SUCCESS_MESSAGE,
                data = transaction
            )
        } catch (e: Exception) {
            log.error("Error fetching transaction by id: ${e.message}", e)
            Response(
                responseCode = 404,
                responseMessage = Constant.Message.NOT_FOUND_MESSAGE,
                errorList = listOf(e.message ?: Constant.Message.NOT_FOUND_MESSAGE)
            )
        }
    }

    @Transactional
    fun deleteTransaction(id: Long): Response<Unit> {
        return try {
            if (!transactionRepository.existsById(id)) {
                return Response(
                    responseCode = 404,
                    responseMessage = Constant.Message.NOT_FOUND_MESSAGE,
                    errorList = listOf("Transaction with id $id not found")
                )
            }

            transactionRepository.deleteById(id)
            log.info("Transaction deleted with ID: $id")

            Response(
                responseCode = Constant.Response.SUCCESS_CODE,
                responseMessage = Constant.Response.DELETED_MESSAGE
            )
        } catch (e: Exception) {
            log.error("Error deleting transaction: ${e.message}", e)
            Response(
                responseCode = 500,
                responseMessage = "Internal Server Error",
                errorList = listOf(e.message ?: "Failed to delete transaction")
            )
        }
    }
}