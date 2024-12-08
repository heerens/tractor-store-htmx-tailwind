package com.inauditech.tractorstore.checkout.domain

interface InventoryRepository {
    fun findBySku(sku: Sku): Inventory?
}
