package com.zycash.betransaction.common

import java.math.BigDecimal
import java.time.LocalDateTime

data class TransactionEvent(
    val eventType: String,              // "TRANSACTION_CREATED", "TRANSACTION_UPDATED", etc
    val transactionId: Long,
    val userId: String,
    val category: String,
    val amount: BigDecimal,
    val description: String?,
    val timestamp: LocalDateTime
)