package com.inauditech.tractorstore.account.presentation.account.miniaccount

import jakarta.servlet.http.HttpServletResponse
import mu.KLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import java.security.Principal

@Controller
class MiniAccount {
    @GetMapping("/account/fragments/v1/miniaccount")
    fun render(
        user: Principal?,
        model: ModelMap,
        httpServletResponse: HttpServletResponse,
    ): String {
        model.addAttribute("miniAccountView", MiniAccountView(user != null))

        return "miniaccount/MiniAccount"
    }

    companion object : KLogging()
}

data class MiniAccountView(
    val authenticated: Boolean,
)
