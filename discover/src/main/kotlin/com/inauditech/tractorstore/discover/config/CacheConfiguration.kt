package com.inauditech.tractorstore.discover.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class CacheConfiguration : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        response.setHeader("Cache-Control", "public,max-age=$SECONDS,s-maxage=$SECONDS")
        filterChain.doFilter(request, response)
    }

    companion object {
        const val SECONDS = 60L
    }
}
