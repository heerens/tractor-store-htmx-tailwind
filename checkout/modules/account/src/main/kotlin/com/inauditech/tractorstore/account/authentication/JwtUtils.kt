package com.inauditech.tractorstore.account.authentication

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.security.Key
import java.util.Date

@Component
class JwtUtils {
    // In production, load this secret from a secure location (vault, environment, etc.)
    private val key: Key = Keys.secretKeyFor(SignatureAlgorithm.HS256)
    // private val key: Key = Keys.hmacShaKeyFor("secret".toByteArray())

    fun generateToken(username: String): String {
        val now = Date()
        val expiry = Date(now.time + 3_600_000) // 1 hour validity

        return Jwts
            .builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(key)
            .compact()
    }

    fun validateToken(token: String): Boolean =
        try {
            val claims =
                Jwts
                    .parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
            // optionally check claims details (expiration, etc.)
            true
        } catch (ex: Exception) {
            false
        }

    fun getUsernameFromToken(token: String): String? =
        try {
            val claims =
                Jwts
                    .parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .body
            claims.subject
        } catch (ex: Exception) {
            null
        }
}
