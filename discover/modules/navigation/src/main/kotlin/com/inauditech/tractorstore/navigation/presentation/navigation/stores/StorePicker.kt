package com.inauditech.tractorstore.navigation.presentation.navigation.stores

import com.inauditech.tractorstore.navigation.domain.StoreId
import com.inauditech.tractorstore.navigation.domain.StoreRepository
import jakarta.servlet.http.HttpServletResponse
import mu.KLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class StorePicker(
    val storeRepository: StoreRepository,
) {
    @GetMapping("/navigation/fragments/v1/storepicker")
    fun render(
        @RequestParam(required = false) storeId: String? = null,
        model: ModelMap,
    ): String {
        val view = storeId?.let { createView(it) }

        model.addAttribute("storeView", view)
        return "stores/StorePicker"
    }

    @PostMapping("/navigation/fragments/v1/storepicker")
    fun renderSelect(
        @RequestParam storeId: String,
        model: ModelMap,
        httpServletResponse: HttpServletResponse,
    ): String {
        val view = createView(storeId)

        model.addAttribute("storeView", view)

        httpServletResponse.setHeader("HX-Trigger", "{\"storeSelected\":{\"storeId\" : \"$storeId\"}}")
        return "stores/StorePicker"
    }

    fun createView(storeId: String): StoreView? {
        val view =
            storeRepository.findById(StoreId(storeId))?.let {
                StoreView(it)
            }
        return view
    }

    companion object : KLogging()
}
