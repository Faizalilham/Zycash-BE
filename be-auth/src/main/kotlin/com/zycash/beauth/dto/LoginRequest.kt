package com.zycash.beauth.dto


import com.zycash.beauth.constant.Constant
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

class LoginRequest(

    @field:NotBlank(message = "Username is mandatory, please fill it!")
    @field:NotNull(message = "Username mandatory, please fill it!")
    @field:Pattern(
        regexp = Constant.Regex.ALPHANUMERIC,
        message = "Invalid format username"
    )
    val username: String?,

    @field:NotBlank(message = "Password is mandatory, please fill it!")
    @field:NotNull(message = "Password mandatory, please fill it!")
    val password: String
)
