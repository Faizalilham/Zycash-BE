package com.zycash.beauth.security.jwt

import com.zycash.beauth.security.service.UserDetailsImpl
import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

@Component
class JwtUtils {

    private val log = LoggerFactory.getLogger(JwtUtils::class.java)

    @Value("\${jwt.secret}")
    private lateinit var jwtSecret: String

    @Value("\${jwt.expirationMs}")
    private var jwtExpirationMs: Int = 0

    fun generateJwtToken(authentication: Authentication): String {
        val userPrincipal = authentication.principal as UserDetailsImpl

        return Jwts.builder()
            .setSubject(userPrincipal.username)
            .setIssuedAt(Date())
            .setExpiration(Date(Date().time + jwtExpirationMs))
            .signWith(key(), SignatureAlgorithm.HS256)
            .compact()
    }

    private fun key(): Key {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret))
    }

    fun getUserNameFromJwtToken(token: String): String {
        return Jwts.parser()
            .setSigningKey(key())
            .build()
            .parseClaimsJws(token)
            .body
            .subject
    }

    fun validateJwtToken(authToken: String): Boolean {
        return try {
            Jwts.parser()
                .setSigningKey(key())
                .build()
                .parse(authToken)
            true
        } catch (e: MalformedJwtException) {
            log.error("Invalid JWT token: ${e.message}")
            false
        } catch (e: ExpiredJwtException) {
            log.error("JWT token is expired: ${e.message}")
            false
        } catch (e: UnsupportedJwtException) {
            log.error("JWT token is unsupported: ${e.message}")
            false
        } catch (e: IllegalArgumentException) {
            log.error("JWT claims string is empty: ${e.message}")
            false
        }
    }
}