package com.zycash.bereport.consumer


import com.zycash.bereport.dto.TransactionEvent
import com.zycash.bereport.service.ReportService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class TransactionEventConsumer(
    private val reportService: ReportService
) {

    private val log = LoggerFactory.getLogger(TransactionEventConsumer::class.java)

    @KafkaListener(
        topics = ["transaction-events"],
        groupId = "report-service-group"
    )
    fun consumeTransactionEvent(event: TransactionEvent) {
        log.info("Received transaction event: ${event.eventType} for ID: ${event.transactionId}")

        try {
            when (event.eventType) {
                "TRANSACTION_CREATED" -> reportService.handleTransactionCreated(event)
                "TRANSACTION_UPDATED" -> reportService.handleTransactionUpdated(event)
                "TRANSACTION_DELETED" -> reportService.handleTransactionDeleted(event)
                else -> log.warn("Unknown event type: ${event.eventType}")
            }
        } catch (e: Exception) {
            log.error("Error processing transaction event", e)
            // TODO: Implement retry logic or dead letter queue
        }
    }
}