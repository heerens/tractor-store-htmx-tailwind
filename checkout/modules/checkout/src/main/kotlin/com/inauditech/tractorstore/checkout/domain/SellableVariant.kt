package com.inauditech.tractorstore.checkout.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

data class SellableVariant(
    val productId: ProductId,
    val sku: Sku,
    val name: String,
    val image: String,
    val color: String,
    val inventory: Int,
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
