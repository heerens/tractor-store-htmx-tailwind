package com.inauditech.tractorstore.checkout.domain

import org.springframework.stereotype.Component

@Component
class VariantService(
    val variantRepository: VariantRepository,
    val inventoryRepository: InventoryRepository,
) {
    fun findBySku(sku: Sku): SellableVariant? =
        variantRepository.findBySku(sku)?.let {
            it.copy(inventory = inventoryRepository.findBySku(it.sku)?.inventory ?: 0)
        }
}
