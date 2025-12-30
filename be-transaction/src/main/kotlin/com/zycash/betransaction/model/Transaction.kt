package com.zycash.betransaction.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "transactions")
data class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var category: String = "",

    @Column(nullable = false)
    var amount: BigDecimal = BigDecimal.ZERO,

    @Column(length = 500)
    var description: String? = null,

    @Column(name = "original_text", length = 500)
    var originalText: String = "",

    @Column(name = "transaction_date", nullable = false)
    var transactionDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)