package com.inauditech.tractorstore.checkout.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import com.inauditech.tractorstore.checkout.domain.Inventory
import com.inauditech.tractorstore.checkout.domain.InventoryRepository
import com.inauditech.tractorstore.checkout.domain.Sku
import jakarta.annotation.PostConstruct
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Repository

@Repository
class FileProductRepository(
    val mapper: ObjectMapper,
) : InventoryRepository {
    private val inventories: MutableList<Inventory> = mutableListOf()

    @PostConstruct
    fun init() {
        val resource = ClassPathResource("database_inventory.json")
        val data: InventoryData = resource.inputStream.use { mapper.readValue(it, InventoryData::class.java) }
        inventories.addAll(data.variants)
    }

    override fun findBySku(sku: Sku): Inventory? = inventories.find { it.sku == sku }
}

data class InventoryData(
    val variants: List<Inventory>,
)
