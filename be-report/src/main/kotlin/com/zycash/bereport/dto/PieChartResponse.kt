package com.zycash.bereport.dto

import java.math.BigDecimal

data class PieChartResponse(
    val title: String,
    val period: String,
    val totalAmount: BigDecimal,
    val data: List<CategoryPieChartData>,
    val metadata: Map<String, Any>
)