package com.inauditech.tractorstore.product.presentation.product.productpage

import com.inauditech.tractorstore.product.domain.Product
import com.inauditech.tractorstore.product.domain.ProductId
import com.inauditech.tractorstore.product.domain.ProductRepository
import com.inauditech.tractorstore.product.domain.Variant
import com.inauditech.tractorstore.product.presentation.product.image.ImageView
import com.inauditech.tractorstore.product.presentation.product.rating.Rating
import com.inauditech.tractorstore.product.presentation.product.rating.RatingView
import jakarta.servlet.http.HttpServletResponse
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.client.HttpServerErrorException

@Controller
class ProductDetails(
    val productRepository: ProductRepository,
    val rating: Rating,
) {
    @GetMapping("/product/fragments/v1/details/{productId}")
    fun render(
        model: ModelMap,
        @PathVariable productId: String,
        @RequestParam sku: String,
        httpServletResponse: HttpServletResponse,
    ): String {
        logger.info { "ProductController $productId $sku" }

        val productDetailsView = createView(productId, sku)
        model.addAttribute("productDetailsView", productDetailsView)

        httpServletResponse.setHeader("HX-Trigger", "variantChanged")

        return "productpage/ProductDetails"
    }

    fun createView(
        productId: String,
        sku: String?,
    ): ProductDetailsView {
        val product = productRepository.findById(ProductId(productId)) ?: throw HttpServerErrorException(HttpStatus.BAD_REQUEST)
        val selectedSku =
            sku ?: product.variants
                .first()
                .sku.value
        val variant = product.variants.firstOrNull { it.sku.value == selectedSku } ?: throw HttpServerErrorException(HttpStatus.BAD_REQUEST)
        val productDetailsView =
            ProductDetailsView(
                product = product,
                variant = variant,
                ratingView = rating.createView(productId),
            )
        return productDetailsView
    }

    data class ProductDetailsView(
        val product: Product,
        val variant: Variant,
        val ratingView: RatingView,
    ) {
        val fullName = "${product.name} - ${variant.name}"
        val imageView: ImageView
            get() =
                ImageView(
                    url = variant.image,
                    srcsetSizes = listOf(400, 800),
                    sizes = "400px",
                    alt = fullName,
                )
    }

    companion object : KLogging()
}
