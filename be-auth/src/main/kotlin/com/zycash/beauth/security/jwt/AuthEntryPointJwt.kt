package com.zycash.beauth.security.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import com.zycash.beauth.constant.Constant
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class AuthEntryPointJwt : AuthenticationEntryPoint {

    private val log = LoggerFactory.getLogger(AuthEntryPointJwt::class.java)

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        log.error("Unauthorized error : ${authException.message}")

        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.status = HttpServletResponse.SC_UNAUTHORIZED

        val error = if (authException.message == "Bad credentials") {
            Constant.Message.INVALID_LOGIN_MESSAGE
        } else {
            authException.message ?: "Unauthorized"
        }

        val bodyMap = mapOf(
            "responseCode" to HttpServletResponse.SC_UNAUTHORIZED,
            "ResponseMessage" to "Unauthorized",
            "errorList" to listOf(error),
            "path" to request.servletPath
        )

        val mapper = ObjectMapper()
        mapper.writeValue(response.outputStream, bodyMap)
    }
}