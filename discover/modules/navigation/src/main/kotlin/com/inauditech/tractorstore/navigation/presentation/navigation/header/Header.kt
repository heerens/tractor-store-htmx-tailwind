package com.inauditech.tractorstore.navigation.presentation.navigation.header

import mu.KLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping

@Controller
class Header {
    @GetMapping("/navigation/fragments/v1/header")
    fun home(model: ModelMap): String = "header/Header"

    companion object : KLogging()
}
