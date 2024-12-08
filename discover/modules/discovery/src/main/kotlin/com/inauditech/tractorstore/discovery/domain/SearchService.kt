package com.inauditech.tractorstore.discovery.domain

import org.springframework.stereotype.Component

@Component
class SearchService(
    val categoryRepository: CategoryRepository,
    val searchableProductRepository: SearchableProductRepository,
) {
    fun findAllByCategory(key: CategoryKey?): List<SearchableProduct> {
        if (key == null) return searchableProductRepository.findAll()

        return categoryRepository.findById(key)?.let { c ->
            searchableProductRepository.findByIds(c.products.map { it.id })
        } ?: emptyList()
    }
}
