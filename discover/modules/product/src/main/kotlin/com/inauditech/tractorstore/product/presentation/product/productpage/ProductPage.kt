package com.inauditech.tractorstore.product.presentation.product.productpage

import mu.KLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@Controller
class ProductPage(
    val productDetails: ProductDetails,
) {
    @GetMapping("/product/{productId}")
    fun render(
        model: ModelMap,
        @PathVariable productId: String,
        @RequestParam(required = false) sku: String?,
    ): String {
        logger.info { "ProductController $productId $sku" }

        val productPageView = createView(productId, sku)
        model.addAttribute("productPageView", productPageView)

        return "productpage/ProductPage"
    }

    fun createView(
        productId: String,
        sku: String?,
    ): ProductPageView {
        val productDetailsView = productDetails.createView(productId, sku)
        val productPageView = ProductPageView(productDetailsView = productDetailsView)
        return productPageView
    }

    data class ProductPageView(
        val productDetailsView: ProductDetails.ProductDetailsView,
    )

    companion object : KLogging()
}
