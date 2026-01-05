package com.zycash.benotification.dto

import java.time.LocalDateTime

data class NotificationResponse(
    val id: Long,
    val type: String,
    val category: String,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val relatedTransactionId: Long?,
    val metadata: Map<String, Any>?,
    val createdAt: LocalDateTime,
    val readAt: LocalDateTime?
)

data class NotificationStatsResponse(
    val totalCount: Long,
    val unreadCount: Long,
    val byCategory: Map<String, Long>,
    val byType: Map<String, Long>
)