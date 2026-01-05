package com.zycash.bereport.dto

import java.math.BigDecimal

data class CategoryPieChartData(
    val category: String,
    val totalAmount: BigDecimal,
    val percentage: Double,
    val transactionCount: Int,
    val color: String? = null
)