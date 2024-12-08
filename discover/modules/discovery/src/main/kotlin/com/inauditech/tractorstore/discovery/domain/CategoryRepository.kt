package com.inauditech.tractorstore.discovery.domain

interface CategoryRepository {
    fun findAll(): List<Category>

    fun findById(key: CategoryKey): Category?
}
