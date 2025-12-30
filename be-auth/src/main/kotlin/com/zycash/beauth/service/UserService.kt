package com.zycash.beauth.service


import com.zycash.beauth.constant.Constant
import com.zycash.beauth.dto.LoginRequest
import com.zycash.beauth.dto.LoginResponse
import com.zycash.beauth.dto.Response
import com.zycash.beauth.dto.UserRequest
import com.zycash.beauth.entity.User
import com.zycash.beauth.exception.BadRequestCustomException
import com.zycash.beauth.exception.DataExistException
import com.zycash.beauth.exception.NotFoundException
import com.zycash.beauth.repository.UserRepository
import com.zycash.beauth.security.jwt.JwtUtils
import com.zycash.beauth.security.service.UserDetailsImpl
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UserService(
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository,
    private val authenticationManager: AuthenticationManager,
    private val jwtUtils: JwtUtils
) {

    private val log = LoggerFactory.getLogger(UserService::class.java)

    @Value("\${jwt.expirationMs}")
    private var jwtExpirationMs: Int = 0

    private val nowDate = Date()

    @Transactional
    fun register(userRequest: UserRequest): Response<Any> {
        val user = userRepository.findByUsername(userRequest.username ?: "")
        if (user != null) {
            throw DataExistException(Constant.Message.EXIST_DATA_MESSAGE)
        }

        val savedUser = userRepository.save(mappingUser(userRequest))
        return Response(
            responseCode = Constant.Response.SUCCESS_CODE,
            responseMessage = Constant.Response.SUCCESS_MESSAGE,
            data = savedUser
        )
    }

    fun login(loginRequest: LoginRequest): Response<Any> {
        val user = userRepository.findByUsername(loginRequest.username ?: "")
            ?: throw NotFoundException(Constant.Message.INVALID_LOGIN_MESSAGE)

        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password)
        )

        SecurityContextHolder.getContext().authentication = authentication
        val jwt = jwtUtils.generateJwtToken(authentication)

        val userDetails = authentication.principal as UserDetailsImpl
        val roles = userDetails.authorities.map { it.authority }

        val response = mappingLoginResponse(user, jwt)
        return Response(
            responseCode = Constant.Response.SUCCESS_CODE,
            responseMessage = Constant.Response.SUCCESS_MESSAGE,
            data = response
        )
    }

    fun validateAccessToken(accessToken: String): Response<Any> {
        val isValid = jwtUtils.validateJwtToken(accessToken)
        if (!isValid) {
            throw BadRequestCustomException(Constant.Message.INVALID_TOKEN_MESSAGE)
        }
        return Response(
            responseCode = Constant.Response.SUCCESS_CODE,
            responseMessage = Constant.Response.SUCCESS_VALID_TOKEN_MESSAGE
        )
    }

    private fun mappingLoginResponse(user: User, jwt: String): LoginResponse {
        return LoginResponse(
            userId = user.id,
            username = user.username,
            accessToken = jwt,
            tokenType = "Bearer",
            expiresIn = jwtExpirationMs,
            avatar = user.avatar
        )
    }

    private fun mappingUser(userRequest: UserRequest): User {
        return User(
            name = userRequest.name,
            phoneNumber = userRequest.phoneNumber,
            address = userRequest.address,
            email = userRequest.email,
            gender = userRequest.gender,
            username = userRequest.username,
            password = passwordEncoder.encode(userRequest.password),
            avatar = userRequest.avatar,
            createdBy = "",
            createdDate = nowDate,
            isDeleted = false
        )
    }

    private fun isValid(value: String, request: String): Boolean {
        val valueList = value.split("|")
        return valueList.any { it == request }
    }
}