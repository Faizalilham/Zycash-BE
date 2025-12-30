package com.zycash.beauth.dto

import com.zycash.beauth.constant.Constant
import jakarta.validation.constraints.*

data class UserRequest(
    @field:NotBlank(message = "Name is mandatory, please fill it!")
    @field:NotNull(message = "Name is mandatory, please fill it!")
    @field:Pattern(regexp = Constant.Regex.ALPHANUMERIC_WITH_DOT_AND_SPACE, message = "Invalid format name")
    val name: String? = null,

    @field:NotBlank(message = "Phone number is mandatory, please fill it!")
    @field:NotNull(message = "Phone number is mandatory, please fill it!")
    @field:Pattern(regexp = Constant.Regex.NUMERIC, message = "Invalid format phone number")
    @field:Size(min = 8, max = 16, message = "Invalid length phone number")
    val phoneNumber: String? = null,

    val address: String? = null,

    @field:NotBlank(message = "Email is mandatory, please fill it!")
    @field:NotNull(message = "Email is mandatory, please fill it!")
    @field:Email(message = "Invalid format email")
    val email: String? = null,

    @field:NotBlank(message = "Gender is mandatory, please fill it!")
    @field:NotNull(message = "Gender is mandatory, please fill it!")
    @field:Pattern(regexp = Constant.Regex.ALPHABET, message = "Invalid format gender")
    val gender: String? = null,

    @field:NotBlank(message = "Username is mandatory, please fill it!")
    @field:NotNull(message = "Username mandatory, please fill it!")
    @field:Pattern(regexp = Constant.Regex.ALPHANUMERIC, message = "Invalid format username")
    val username: String? = null,

    @field:NotBlank(message = "Password is mandatory, please fill it!")
    @field:NotNull(message = "Password mandatory, please fill it!")
    val password: String? = null,

    @field:NotBlank(message = "Avatar is mandatory, please fill it!")
    @field:NotNull(message = "Avatar mandatory, please fill it!")
    val avatar: String? = null
)