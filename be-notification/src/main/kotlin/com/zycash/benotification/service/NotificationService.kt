package com.zycash.benotification.service


import com.zycash.benotification.dto.NotificationMessage
import com.zycash.benotification.dto.TransactionEvent
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class NotificationService(
    private val messagingTemplate: SimpMessagingTemplate
) {

    private val log = LoggerFactory.getLogger(NotificationService::class.java)

    companion object {
        const val HIGH_AMOUNT_THRESHOLD = 100000
        const val WARNING_AMOUNT_THRESHOLD = 500000
    }

    fun handleTransactionCreated(event: TransactionEvent) {
        log.info("💰 Processing transaction created for user: ${event.userId}")

        // Determine notification type based on amount
        val notificationType = when {
            event.amount.toInt() >= WARNING_AMOUNT_THRESHOLD -> "WARNING"
            event.amount.toInt() >= HIGH_AMOUNT_THRESHOLD -> "INFO"
            else -> "SUCCESS"
        }

        // Build notification message
        val notification = NotificationMessage(
            id = UUID.randomUUID().toString(),
            type = notificationType,
            title = buildTitle(event, notificationType),
            message = buildMessage(event, notificationType),
            category = event.category,
            amount = event.amount,
            timestamp = LocalDateTime.now(),
            metadata = mapOf(
                "transactionId" to event.transactionId,
                "category" to event.category,
                "description" to (event.description ?: "")
            )
        )

        // Send to user-specific topic
        sendToUser(event.userId, notification)

        // Also send to global topic for admin dashboard
        sendToTopic("/topic/transactions", notification)

        log.info("✅ Notification sent to user ${event.userId}")
    }

    fun handleTransactionUpdated(event: TransactionEvent) {
        val notification = NotificationMessage(
            id = UUID.randomUUID().toString(),
            type = "INFO",
            title = "Transaksi Diperbarui",
            message = "Transaksi ${event.category} sebesar ${formatCurrency(event.amount)} telah diperbarui",
            category = event.category,
            amount = event.amount
        )
        sendToUser(event.userId, notification)
    }

    fun handleTransactionDeleted(event: TransactionEvent) {
        val notification = NotificationMessage(
            id = UUID.randomUUID().toString(),
            type = "INFO",
            title = "Transaksi Dihapus",
            message = "Transaksi ${event.category} telah dihapus",
            category = event.category,
            amount = event.amount
        )
        sendToUser(event.userId, notification)
    }

    private fun buildTitle(event: TransactionEvent, type: String): String {
        return when (type) {
            "WARNING" -> "⚠️ Pengeluaran Besar!"
            "INFO" -> "💰 Pengeluaran Tinggi"
            else -> "✅ Transaksi Berhasil"
        }
    }

    private fun buildMessage(event: TransactionEvent, type: String): String {
        val formattedAmount = formatCurrency(event.amount)

        return when (type) {
            "WARNING" -> "Anda baru saja mengeluarkan $formattedAmount untuk ${event.category}. " +
                    "Pastikan pengeluaran ini sesuai dengan budget Anda!"
            "INFO" -> "Pengeluaran $formattedAmount untuk ${event.category} telah tercatat."
            else -> "${event.category} sebesar $formattedAmount berhasil dicatat."
        }
    }

    private fun formatCurrency(amount: java.math.BigDecimal): String {
        return "Rp ${String.format("%,.0f", amount.toDouble())}"
    }

    private fun sendToUser(userId: String, notification: NotificationMessage) {
        try {
            // Send to user-specific destination
            messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/notifications",
                notification
            )
            log.debug("Sent notification to user $userId: ${notification.title}")
        } catch (e: Exception) {
            log.error("Failed to send notification to user $userId", e)
        }
    }

    private fun sendToTopic(destination: String, notification: NotificationMessage) {
        try {
            messagingTemplate.convertAndSend(destination, notification)
            log.debug("Sent notification to topic $destination")
        } catch (e: Exception) {
            log.error("Failed to send notification to topic $destination", e)
        }
    }
}
