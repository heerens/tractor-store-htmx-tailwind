package com.inauditech.tractorstore.discovery.domain

import com.inauditech.tractorstore.discover.contracts.product.ApiProduct
import com.inauditech.tractorstore.discover.contracts.product.ProductApi
import jakarta.annotation.PostConstruct
import mu.KLogging
import org.springframework.stereotype.Component

@Component
class ProductImporter(
    val productApi: ProductApi,
    val productRepository: SearchableProductRepository,
    val recommendationRepository: RecommendationRepository,
) {
    /**
     * This is just a demo setup because we only have 50 products.
     * Replace this with an async 'Data Feed' import job pulling FULL or DELTA updates.
     */
    @PostConstruct
    fun pullFullProductData() {
        val products = productApi.findAll().products
        logger.info { "Full import pulled ${products.size} ApiProducts" }
        productRepository.saveAll(products.map { it.toProduct() })
        recommendationRepository.saveAll(products.flatMap { it.toRecommendation() })
    }

    private fun ApiProduct.toProduct(): SearchableProduct =
        SearchableProduct(
            id = ProductId(this.id),
            name = this.name,
            category = this.category,
            highlights = this.highlights,
            startPrice = this.variants.minOf { it.price },
            image = this.variants.first().image,
        )

    private fun ApiProduct.toRecommendation(): List<Recommendation> =
        this.variants.map { v ->
            Recommendation(
                productId = ProductId(this.id),
                sku = Sku(v.sku),
                name = this.name + " " + v.name,
                image = v.image,
                color = v.color,
                rgbColor = hexToRgb(v.color),
                price = v.price,
            )
        }

    private fun hexToRgb(hex: String): Triple<Int, Int, Int> {
        val sanitizedHex = hex.removePrefix("#")
        require(sanitizedHex.length == 6) { "Invalid hex color format" }
        val r = sanitizedHex.substring(0, 2).toInt(16)
        val g = sanitizedHex.substring(2, 4).toInt(16)
        val b = sanitizedHex.substring(4, 6).toInt(16)
        return Triple(r, g, b)
    }

    companion object : KLogging()
}
