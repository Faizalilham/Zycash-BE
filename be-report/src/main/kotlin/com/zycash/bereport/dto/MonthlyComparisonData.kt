package com.zycash.bereport.dto

import java.math.BigDecimal

data class MonthlyComparisonData(
    val month: String,
    val totalAmount: BigDecimal,
    val transactionCount: Int
)