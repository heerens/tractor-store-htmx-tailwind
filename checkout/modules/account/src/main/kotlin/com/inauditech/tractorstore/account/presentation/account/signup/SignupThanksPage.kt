package com.inauditech.tractorstore.account.presentation.account.signup

import mu.KLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping

@Controller
class SignupThanksPage {
    @GetMapping("/account/signup/thanks")
    fun render(model: ModelMap): String = "signup/SignupThanksPage"

    companion object : KLogging()
}
