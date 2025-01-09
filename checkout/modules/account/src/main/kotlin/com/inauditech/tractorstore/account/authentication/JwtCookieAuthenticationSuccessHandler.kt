package com.inauditech.tractorstore.account.authentication

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class JwtCookieAuthenticationSuccessHandler(
    private val jwtUtils: JwtUtils,
) : AuthenticationSuccessHandler {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        val username = authentication.name
        val token = jwtUtils.generateToken(username)

        val jwtCookie =
            Cookie("jwt", token).apply {
                isHttpOnly = true
                path = "/"
                maxAge = 60 * 60 // 1 hour
                secure = true
            }

        response.addCookie(jwtCookie)

        // redirect after login
        response.sendRedirect("/account/dashboard")
    }
}
