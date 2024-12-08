package com.inauditech.tractorstore.navigation.presentation.navigation.stores

import com.inauditech.tractorstore.navigation.domain.StoreRepository
import mu.KLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping

@Controller
class StoreModal(
    val storeRepository: StoreRepository,
) {
    @GetMapping("/navigation/fragments/v1/storepicker/modal")
    fun home(model: ModelMap): String {
        val view = createView()

        model.addAttribute("storePageView", view)

        return "stores/StoreModal"
    }

    fun createView(): StorePageView {
        val stores = storeRepository.findAll()
        val view = StorePageView(stores.map { StoreView(it) })
        return view
    }

    companion object : KLogging()
}
