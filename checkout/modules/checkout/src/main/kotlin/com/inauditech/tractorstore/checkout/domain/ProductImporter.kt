package com.inauditech.tractorstore.checkout.domain

import jakarta.annotation.PostConstruct
import mu.KLogging
import org.springframework.stereotype.Component

@Component
class ProductImporter(
    val variantRepository: VariantRepository,
    val productApi: ProductApi,
) {
    /**
     * This is just a demo setup because we only have 50 products.
     * Replace this with an async 'Data Feed' import job pulling FULL or DELTA updates.
     */
    @PostConstruct
    fun pullFullProductData() {
        val variants = productApi.findAll()
        logger.info { "Full import pulled ${variants.size} SellableVariants" }
        variantRepository.saveAll(variants)
    }

    companion object : KLogging()
}
