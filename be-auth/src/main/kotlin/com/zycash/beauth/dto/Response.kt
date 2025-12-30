package com.zycash.beauth.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Response<T>(
    val responseCode: Int = 0,
    val responseMessage: String? = null,
    val data: T? = null,
    val errorList: List<String>? = null,
    val pageNumber: Int? = null,
    val pageSize: Int? = null,
    val totalPage: Int? = null,
    val totalData: Long? = null
)