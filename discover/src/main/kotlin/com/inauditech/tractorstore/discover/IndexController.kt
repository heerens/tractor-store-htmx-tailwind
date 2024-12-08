package com.inauditech.tractorstore.discover

import mu.KLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping

@Controller
class IndexController {
    @GetMapping("/index")
    fun home(model: ModelMap): String = "index"

    companion object : KLogging()
}
