package com.inauditech.tractorstore.checkout.domain

interface VariantRepository {
    fun saveAll(products: List<SellableVariant>)

    fun findAll(): List<SellableVariant>

    fun findBySku(sku: Sku): SellableVariant?
}
