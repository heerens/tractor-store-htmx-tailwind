package com.inauditech.tractorstore.discovery.domain

data class Recommendation(
    val productId: ProductId,
    val sku: Sku,
    val name: String,
    val image: String,
    val color: String,
    val rgbColor: Triple<Int, Int, Int>,
    val price: Int,
)
