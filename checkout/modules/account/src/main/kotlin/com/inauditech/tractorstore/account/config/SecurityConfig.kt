package com.inauditech.tractorstore.account.config

import com.inauditech.tractorstore.account.authentication.JwtCookieAuthenticationFilter
import com.inauditech.tractorstore.account.authentication.JwtCookieAuthenticationSuccessHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtCookieAuthenticationFilter: JwtCookieAuthenticationFilter,
    private val jwtCookieAuthenticationSuccessHandler: JwtCookieAuthenticationSuccessHandler,
    @Value("\${account.base-url}") val basedUrl: String = "",
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun userDetailsService(): UserDetailsService {
        val user =
            User
                .builder()
                .username("customer@example.com")
                .password(passwordEncoder().encode("leeT!shoPR123"))
                .roles("USER")
                .build()
        return InMemoryUserDetailsManager(user)
    }

    @Bean
    fun authenticationProvider(userDetailsService: UserDetailsService): DaoAuthenticationProvider {
        val provider = DaoAuthenticationProvider()
        provider.setUserDetailsService(userDetailsService)
        provider.setPasswordEncoder(passwordEncoder())
        return provider
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                // add more rules here
                it.requestMatchers("/account/dashboard").authenticated()
                it.anyRequest().permitAll()
            }.formLogin {
                it.loginPage("$basedUrl/account/login")
                it.loginProcessingUrl("/account/login/submit")
                it.failureUrl("$basedUrl/account/login?error=true")
                // store cookie
                it.successHandler(jwtCookieAuthenticationSuccessHandler)
                it.permitAll()
            }.logout { logout ->
                logout.logoutUrl("/account/logout")
                logout.deleteCookies("jwt")
                logout.logoutSuccessUrl("$basedUrl/account/login?logout")
                logout.permitAll()
            }
            // use JTW or user/password for authentication
            .addFilterBefore(jwtCookieAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
