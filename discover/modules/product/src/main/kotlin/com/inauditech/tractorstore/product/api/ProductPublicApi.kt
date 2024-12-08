package com.inauditech.tractorstore.product.api

import com.inauditech.tractorstore.discover.contracts.product.ApiProduct
import com.inauditech.tractorstore.discover.contracts.product.ApiResponse
import com.inauditech.tractorstore.discover.contracts.product.ApiVariant
import com.inauditech.tractorstore.discover.contracts.product.ProductApi
import com.inauditech.tractorstore.product.domain.Product
import com.inauditech.tractorstore.product.domain.ProductRepository
import com.inauditech.tractorstore.product.domain.Variant
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductPublicApi(
    val productRepository: ProductRepository,
) : ProductApi {
    @GetMapping("/product/api/v1/feed")
    override fun findAll(): ApiResponse = ApiResponse(productRepository.findAll().map { it.toApiProduct() })

    private fun Product.toApiProduct(): ApiProduct =
        ApiProduct(
            id = this.id.value,
            name = this.name,
            category = this.category,
            highlights = this.highlights,
            variants = this.variants.map { it.toApiVariant() },
        )

    private fun Variant.toApiVariant(): ApiVariant =
        ApiVariant(
            sku = this.sku.value,
            name = this.name,
            image = this.image,
            color = this.color,
            price = this.price,
        )
}
