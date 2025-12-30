package com.zycash.beauth.dto

data class LoginResponse(
    val userId: String? = null,
    val username: String? = null,
    val accessToken: String? = null,
    val tokenType: String? = null,
    val expiresIn: Int = 0,
    val avatar: String? = null
)