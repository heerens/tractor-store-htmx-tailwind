package com.inauditech.tractorstore.checkout

import jakarta.annotation.PostConstruct
import mu.KLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping

@Controller
class IndexController {
    @PostConstruct
    fun init() {
        logger.info { "IndexController init" }
        println("IndexController init")
    }

    @GetMapping("/index")
    fun home(model: ModelMap): String {
        logger.info { "IndexController home" }
        model.addAttribute(
            "model",
            DemoModel("Hello JTE World..."),
        )
        return "index"
    }

    companion object : KLogging()
}

data class DemoModel(
    val text: String,
)
