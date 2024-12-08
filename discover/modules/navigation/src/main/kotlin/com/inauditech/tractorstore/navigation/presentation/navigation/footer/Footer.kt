package com.inauditech.tractorstore.navigation.presentation.navigation.header

import mu.KLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping

@Controller
class Footer {
    @GetMapping("/navigation/fragments/v1/footer")
    fun home(model: ModelMap): String = "footer/Footer"

    companion object : KLogging()
}
