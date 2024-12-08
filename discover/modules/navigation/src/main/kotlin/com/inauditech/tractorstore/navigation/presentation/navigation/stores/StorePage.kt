package com.inauditech.tractorstore.navigation.presentation.navigation.stores

import com.inauditech.tractorstore.navigation.domain.Store
import com.inauditech.tractorstore.navigation.domain.StoreRepository
import com.inauditech.tractorstore.navigation.presentation.navigation.image.ImageView
import mu.KLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping

@Controller
class StorePage(
    val storeRepository: StoreRepository,
) {
    @GetMapping("/navigation/stores")
    fun home(model: ModelMap): String {
        val view = createView()

        model.addAttribute("storePageView", view)

        return "stores/StorePage"
    }

    fun createView(): StorePageView {
        val stores = storeRepository.findAll()
        val view = StorePageView(stores.map { StoreView(it) })
        return view
    }

    companion object : KLogging()
}

data class StorePageView(
    val stores: List<StoreView>,
)

data class StoreView(
    val store: Store,
) {
    val imageView: ImageView
        get() =
            ImageView(
                url = store.image,
                srcsetSizes = listOf(200, 400),
                sizes = "200px",
                alt = store.name,
                width = 200,
                height = 200,
            )
}
