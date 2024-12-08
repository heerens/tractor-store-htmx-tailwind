package com.inauditech.tractorstore.product.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import com.inauditech.tractorstore.product.domain.Product
import com.inauditech.tractorstore.product.domain.ProductId
import com.inauditech.tractorstore.product.domain.ProductRepository
import jakarta.annotation.PostConstruct
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Repository

@Repository
class FileProductRepository(
    val mapper: ObjectMapper,
) : ProductRepository {
    private val products: MutableList<Product> = mutableListOf()

    @PostConstruct
    fun init() {
        val resource = ClassPathResource("database_product.json")
        val filetData: FiletData = resource.inputStream.use { mapper.readValue(it, FiletData::class.java) }
        products.addAll(filetData.products)
    }

    override fun findAll(): List<Product> = products

    override fun findById(id: ProductId): Product? = products.firstOrNull { it.id == id }
}

data class FiletData(
    val products: List<Product>,
)
