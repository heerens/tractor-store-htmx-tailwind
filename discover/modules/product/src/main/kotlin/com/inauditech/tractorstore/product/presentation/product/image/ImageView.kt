package com.inauditech.tractorstore.product.presentation.product.image

data class ImageView(
    val url: String,
    val srcsetSizes: List<Int>,
    val sizes: String,
    val alt: String,
) {
    val src get() = url.replace("[size]", srcsetSizes.first().toString())
    val srcset get() = srcsetSizes.joinToString(",") { url.replace("[size]", it.toString()) + " ${it}w" }
}
