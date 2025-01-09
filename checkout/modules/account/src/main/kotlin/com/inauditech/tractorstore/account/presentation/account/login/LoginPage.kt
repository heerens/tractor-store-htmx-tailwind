package com.inauditech.tractorstore.account.presentation.account.login

import mu.KLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class LoginPage {
    @GetMapping("/account/login")
    fun render(
        @RequestParam(required = false)
        error: Boolean = false,
        model: ModelMap,
    ): String {
        model.addAttribute("loginPageView", LoginPageView(error))
        return "login/LoginPage"
    }

    companion object : KLogging()
}

data class LoginPageView(
    val error: Boolean,
)
