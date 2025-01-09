package com.inauditech.tractorstore.account.authentication

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtCookieAuthenticationFilter(
    private val jwtUtils: JwtUtils,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val cookies = request.cookies
        val jwtCookie = cookies?.firstOrNull { it.name == "jwt" }

        if (jwtCookie != null) {
            val token = jwtCookie.value
            if (jwtUtils.validateToken(token)) {
                val username = jwtUtils.getUsernameFromToken(token)
                if (username != null) {
                    // just defaults, load real user here
                    val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
                    val auth = UsernamePasswordAuthenticationToken(username, null, authorities)
                    SecurityContextHolder.getContext().authentication = auth
                }
            }
        }
        filterChain.doFilter(request, response)
    }
}
