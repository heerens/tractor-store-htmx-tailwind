package com.inauditech.tractorstore.discovery.domain

interface SearchableProductRepository {
    fun saveAll(products: List<SearchableProduct>)

    fun findByIds(productIds: List<ProductId>): List<SearchableProduct>

    fun findAll(): List<SearchableProduct>
}
