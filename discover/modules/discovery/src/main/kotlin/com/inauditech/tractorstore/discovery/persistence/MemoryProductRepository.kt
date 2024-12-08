package com.inauditech.tractorstore.discovery.persistence

import com.inauditech.tractorstore.discovery.domain.ProductId
import com.inauditech.tractorstore.discovery.domain.SearchableProduct
import com.inauditech.tractorstore.discovery.domain.SearchableProductRepository
import org.springframework.stereotype.Repository

@Repository
class MemoryProductRepository : SearchableProductRepository {
    var data: MutableList<SearchableProduct> = mutableListOf()

    override fun saveAll(products: List<SearchableProduct>) {
        data.clear()
        data.addAll(products)
    }

    override fun findByIds(productIds: List<ProductId>): List<SearchableProduct> {
        val lookup = data.associateBy { it.id }
        return productIds.mapNotNull { lookup[it] }
    }

    override fun findAll(): List<SearchableProduct> = data.toList()
}
