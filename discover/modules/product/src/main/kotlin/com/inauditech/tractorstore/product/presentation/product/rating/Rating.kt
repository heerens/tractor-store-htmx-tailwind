package com.inauditech.tractorstore.product.presentation.product.rating

import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class Rating {
    @GetMapping("/product/fragments/v1/rating/{productId}")
    fun render(
        model: ModelMap,
        @PathVariable productId: String,
    ): String {
        model.addAttribute("ratingView", createView(productId))

        return "rating/Rating"
    }

    fun createView(productId: String): RatingView {
        val mockRating = productId.hashCode() % 5 + 1
        return RatingView(mockRating)
    }
}

data class RatingView(
    val rating: Int,
)
