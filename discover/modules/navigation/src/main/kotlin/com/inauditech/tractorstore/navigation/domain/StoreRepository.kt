package com.inauditech.tractorstore.navigation.domain

interface StoreRepository {
    fun findAll(): List<Store>

    fun findById(storeId: StoreId): Store?
}
