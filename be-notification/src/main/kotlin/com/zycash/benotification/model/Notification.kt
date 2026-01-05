package com.zycash.benotification.model


import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "notifications")
data class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "user_id", nullable = false)
    var userId: String = "",

    @Column(nullable = false)
    var type: String = "",              // SUCCESS, INFO, WARNING, ERROR, REMINDER

    @Column(nullable = false)
    var category: String = "",          // TRANSACTION, BUDGET, INCOME, BILL, ACHIEVEMENT

    @Column(nullable = false, length = 200)
    var title: String = "",

    @Column(nullable = false, length = 1000)
    var message: String = "",

    @Column(name = "is_read", nullable = false)
    var isRead: Boolean = false,

    @Column(name = "is_sent", nullable = false)
    var isSent: Boolean = false,

    @Column(name = "related_transaction_id")
    var relatedTransactionId: Long? = null,

    @Column(name = "metadata", columnDefinition = "TEXT")
    var metadata: String? = null,       // JSON string for extra data

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "sent_at")
    var sentAt: LocalDateTime? = null,

    @Column(name = "read_at")
    var readAt: LocalDateTime? = null
)