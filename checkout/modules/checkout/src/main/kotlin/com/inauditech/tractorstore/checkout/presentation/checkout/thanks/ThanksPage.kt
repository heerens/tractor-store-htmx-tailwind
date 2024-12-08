package com.inauditech.tractorstore.checkout.presentation.checkout.thanks

import jakarta.servlet.http.HttpServletResponse
import mu.KLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ThanksPage {
    @GetMapping("/checkout/thanks")
    fun render(
        httpServletResponse: HttpServletResponse,
        model: ModelMap,
    ): String = "thanks/ThanksPage"

    companion object : KLogging()
}
