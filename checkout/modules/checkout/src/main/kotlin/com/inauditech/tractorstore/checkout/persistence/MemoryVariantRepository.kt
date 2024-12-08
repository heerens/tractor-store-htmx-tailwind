package com.inauditech.tractorstore.checkout.persistence

import com.inauditech.tractorstore.checkout.domain.SellableVariant
import com.inauditech.tractorstore.checkout.domain.Sku
import com.inauditech.tractorstore.checkout.domain.VariantRepository
import org.springframework.stereotype.Repository

@Repository
class MemoryVariantRepository : VariantRepository {
    var data: MutableList<SellableVariant> = mutableListOf()

    override fun saveAll(products: List<SellableVariant>) {
        data.clear()
        data.addAll(products)
    }

    override fun findAll(): List<SellableVariant> = data.toList()

    override fun findBySku(sku: Sku): SellableVariant? = data.find { it.sku == sku }
}
