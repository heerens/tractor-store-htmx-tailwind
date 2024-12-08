package com.inauditech.tractorstore.product.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

data class Product(
    val id: ProductId,
    val name: String,
    val category: String,
    val highlights: List<String> = emptyList(),
    val variants: List<Variant> = emptyList(),
)

data class Variant(
    val sku: Sku,
    val name: String,
    val image: String,
    val color: String,
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
