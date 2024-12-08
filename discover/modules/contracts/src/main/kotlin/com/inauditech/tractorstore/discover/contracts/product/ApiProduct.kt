package com.inauditech.tractorstore.discover.contracts.product

data class ApiResponse(
    val products: List<ApiProduct>,
)

data class ApiProduct(
    val id: String,
    val name: String,
    val category: String,
    val highlights: List<String> = emptyList(),
    val variants: List<ApiVariant> = emptyList(),
)

data class ApiVariant(
    val sku: String,
    val name: String,
    val image: String,
    val color: String,
    val price: Int,
)
