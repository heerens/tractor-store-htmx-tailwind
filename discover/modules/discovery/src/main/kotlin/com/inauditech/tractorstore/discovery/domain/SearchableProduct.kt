package com.inauditech.tractorstore.discovery.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

data class SearchableProduct(
    val id: ProductId,
    val name: String,
    val category: String,
    val highlights: List<String> = emptyList(),
    val startPrice: Int,
    val image: String,
)

data class SearchableVariant(
    val sku: Sku,
    val name: String,
    val image: String,
    val price: Int,
)

data class ProductId
    @JsonCreator
    constructor(
        @get:JsonValue val value: String,
    )

data class Sku
    @JsonCreator
    constructor(
        @get:JsonValue val value: String,
    )
