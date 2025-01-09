package com.inauditech.tractorstore.account.presentation.account.overviewpage

import mu.KLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import java.security.Principal

@Controller
class OverviewPage {
    @GetMapping("/account/dashboard")
    fun render(
        user: Principal?,
        model: ModelMap,
    ): String {
        // placeholder for account page
        logger.info { "Welcome to the Account page. Logged in as: ${user?.name}" }
        return "overviewpage/OverviewPage"
    }

    companion object : KLogging()
}
