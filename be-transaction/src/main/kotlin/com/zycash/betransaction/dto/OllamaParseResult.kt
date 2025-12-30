package com.zycash.betransaction.dto

import com.fasterxml.jackson.annotation.JsonProperty


data class OllamaParseResult(
    @JsonProperty("transactions")
    val transactions: List<ParsedTransaction>
)