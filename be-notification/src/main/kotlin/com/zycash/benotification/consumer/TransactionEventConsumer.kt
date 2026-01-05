package com.zycash.benotification.consumer

import com.example.notification.dto.TransactionEvent
import com.example.notification.service.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class TransactionEventConsumer(
    private val notificationService: NotificationService
) {

    private val log = LoggerFactory.getLogger(TransactionEventConsumer::class.java)

    @KafkaListener(
        topics = ["transaction-events"],
        groupId = "notification-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun consumeTransactionEvent(event: TransactionEvent) {
        log.info("📨 Received transaction event: ${event.eventType} for ID: ${event.transactionId}")

        try {
            when (event.eventType) {
                "TRANSACTION_CREATED" -> notificationService.handleTransactionCreated(event)
                "TRANSACTION_UPDATED" -> notificationService.handleTransactionUpdated(event)
                "TRANSACTION_DELETED" -> notificationService.handleTransactionDeleted(event)
                else -> log.warn("Unknown event type: ${event.eventType}")
            }
        } catch (e: Exception) {
            log.error("❌ Error processing transaction event", e)
        }
    }
}
