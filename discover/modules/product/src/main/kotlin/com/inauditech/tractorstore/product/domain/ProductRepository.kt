package com.inauditech.tractorstore.product.domain

interface ProductRepository {
    fun findAll(): List<Product>

    fun findById(id: ProductId): Product?
}
