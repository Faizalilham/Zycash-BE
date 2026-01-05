package com.zycash.bereport.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "daily_summary")
data class DailySummary(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "user_id", nullable = false)
    var userId: String = "",

    @Column(nullable = false)
    var date: LocalDate = LocalDate.now(),

    @Column(name = "total_amount", nullable = false)
    var totalAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "transaction_count", nullable = false)
    var transactionCount: Int = 0
)
