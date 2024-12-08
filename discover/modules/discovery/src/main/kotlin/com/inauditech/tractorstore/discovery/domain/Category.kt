package com.inauditech.tractorstore.discovery.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

data class Category(
    val key: CategoryKey,
    val name: String,
    val products: List<ProductReference> = emptyList(),
)

data class CategoryKey
    @JsonCreator
    constructor(
        @get:JsonValue val value: String,
    )

data class ProductReference(
    val id: ProductId,
)
