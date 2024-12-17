package com.inauditech.tractorstore.discovery.presentation.discovery.recommendation

import com.inauditech.tractorstore.discovery.domain.Recommendation
import com.inauditech.tractorstore.discovery.domain.RecommendationService
import com.inauditech.tractorstore.discovery.domain.Sku
import com.inauditech.tractorstore.discovery.presentation.discovery.image.ImageView
import mu.KLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class RecommendationList(
    val recommendationService: RecommendationService,
) {
    @GetMapping("/discovery/fragments/v1/recommendations/{sku}")
    fun render(
        model: ModelMap,
        @PathVariable sku: String,
    ): String {
        val views = createViewSimilar(Sku(sku))
        model.addAttribute("recommendationListViews", views)
        return "recommendation/RecommendationList"
    }

    @GetMapping("/discovery/fragments/v1/recommendations/top")
    fun renderTop(model: ModelMap): String {
        val views = createViewTop()
        model.addAttribute("recommendationListViews", views)
        return "recommendation/RecommendationList"
    }

    private fun createViewTop(): List<RecommendationListView> {
        val top4 = recommendationService.findTop().take(4)
        return top4.map { RecommendationListView(it) }
    }

    private fun createViewSimilar(sku: Sku): List<RecommendationListView> {
        val top4 = recommendationService.findSimilar(sku).take(4)
        return top4.map { RecommendationListView(it) }
    }

    companion object : KLogging()
}

data class RecommendationListView(
    val recommendation: Recommendation,
) {
    val imageView: ImageView
        get() =
            ImageView(
                url = recommendation.image,
                srcsetSizes = listOf(200, 400),
                sizes = "200px",
                alt = "",
                width = 200,
                height = 200,
            )
}
