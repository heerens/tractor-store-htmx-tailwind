package com.inauditech.tractorstore.navigation.presentation.navigation.header

import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping

@Controller
class Footer(
    @Value("\${app.git-sha:local}") val gitSha: String,
) {
    @GetMapping("/navigation/fragments/v1/footer")
    fun home(model: ModelMap): String {
        model.addAttribute("gitSha", gitSha)
        return "footer/Footer"
    }

    companion object : KLogging()
}
