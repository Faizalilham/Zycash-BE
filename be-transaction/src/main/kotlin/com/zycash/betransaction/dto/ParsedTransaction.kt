package com.zycash.betransaction.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal


data class ParsedTransaction(
    @JsonProperty("category")
    val category: String,

    @JsonProperty("amount")
    val amount: BigDecimal?,

    @JsonProperty("description")
    val description: String,

    @JsonProperty("needs_price_lookup")
    val needsPriceLookup: Boolean = false,

    @JsonProperty("search_query")
    val searchQuery: String? = null,

    @JsonProperty("quantity")
    val quantity: BigDecimal? = null,

    @JsonProperty("unit")
    val unit: String? = null
)