package com.inauditech.tractorstore.account.presentation.account.overviewpage

import mu.KLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping

@Controller
class OverviewPage {
    @GetMapping("/account")
    fun home(model: ModelMap): String {
        // placeholder for account page
        return "overviewpage/OverviewPage"
    }

    companion object : KLogging()
}
