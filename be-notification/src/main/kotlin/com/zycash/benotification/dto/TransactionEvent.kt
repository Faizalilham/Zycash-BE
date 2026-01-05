package com.zycash.benotification.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class TransactionEvent(
    val eventType: String,
    val transactionId: Long,
    val userId: String,
    val category: String,
    val amount: BigDecimal,
    val description: String?,
    val timestamp: LocalDateTime
)