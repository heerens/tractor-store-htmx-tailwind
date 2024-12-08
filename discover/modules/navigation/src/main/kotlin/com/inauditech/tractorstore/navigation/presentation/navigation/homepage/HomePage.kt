package com.inauditech.tractorstore.navigation.presentation.navigation.homepage

import mu.KLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HomePage {
    @GetMapping("/navigation")
    fun home(model: ModelMap): String = "homepage/HomePage"

    companion object : KLogging()
}
