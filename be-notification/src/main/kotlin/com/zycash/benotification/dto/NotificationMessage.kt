package com.zycash.benotification.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class NotificationMessage(
    val id: String,
    val type: String,                    // "SUCCESS", "WARNING", "INFO"
    val title: String,
    val message: String,
    val category: String?,
    val amount: BigDecimal?,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val metadata: Map<String, Any>? = null
)