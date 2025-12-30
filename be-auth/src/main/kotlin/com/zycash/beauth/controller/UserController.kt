package com.zycash.beauth.controller

import com.zycash.beauth.dto.LoginRequest
import com.zycash.beauth.dto.UserRequest
import com.zycash.beauth.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/verif")
class UserController(
    private val userService: UserService
) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody userRequest: UserRequest): ResponseEntity<Any> {
        return ResponseEntity.ok(userService.register(userRequest))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<Any> {
        return ResponseEntity.ok(userService.login(loginRequest))
    }

    @GetMapping("/validateAccessToken")
    fun validateAccessToken(
        @RequestParam(value = "accessToken", defaultValue = "") accessToken: String
    ): ResponseEntity<Any> {
        return ResponseEntity.ok(userService.validateAccessToken(accessToken))
    }

    @GetMapping("/test")
    fun doTest(): ResponseEntity<Any> {
        return ResponseEntity.ok("Success Test")
    }
}