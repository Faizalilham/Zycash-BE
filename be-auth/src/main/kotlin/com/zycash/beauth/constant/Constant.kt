package com.zycash.beauth.constant

object Constant {
    val AUTH_WHITELIST = arrayOf(
        "/swagger-ui/**",
        "/api-docs/**",
        "/h2-console/**",
        "/swagger-ui.html",
        "/api/verif/register",
        "/api/verif/login",
        "/api/verif/validateAccessToken"
    )

    object Response {
        const val SUCCESS_CODE = 200
        const val SUCCESS_MESSAGE = "Success"
        const val SUCCESS_VALID_TOKEN_MESSAGE = "Access token valid"
    }

    object Message {
        const val EXIST_DATA_MESSAGE = "data already exist"
        const val NOT_FOUND_DATA_MESSAGE = "data not found"
        const val FORBIDDEN_REQUEST_MESSAGE = "Different {value} with exist data is forbidden"
        const val INVALID_LOGIN_MESSAGE = "Username / Password wrong"
        const val INVALID_TOKEN_MESSAGE = "Invalid access token"
    }

    object Regex {
        const val NUMERIC = "\\d+"
        const val ALPHANUMERIC = "^[a-zA-Z0-9]+$"
        const val ALPHABET = "^[a-zA-Z]+$"
        const val ALPHANUMERIC_WITH_DOT_AND_SPACE = "^[a-zA-Z0-9.' ]+$"

        // Email regex
        const val EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"

        // Phone number regex (8-16 digits)
        const val PHONE = "^\\d{8,16}$"

        const val EMAIL_OR_PHONE = "^([A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}|\\d{8,16})$"
    }
}