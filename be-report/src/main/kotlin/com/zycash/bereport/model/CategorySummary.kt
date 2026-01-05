package com.zycash.bereport.model


import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "category_summary")
data class CategorySummary(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "user_id", nullable = false)
    var userId: String = "",

    @Column(nullable = false)
    var category: String = "",

    @Column(nullable = false)
    var totalAmount: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    var transactionCount: Int = 0,

    @Column(nullable = false)
    var averageAmount: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    var date: LocalDate = LocalDate.now(),

    @Column(nullable = false)
    var period: String = "DAILY" // DAILY, WEEKLY, MONTHLY, YEARLY
)
