package com.inauditech.tractorstore.navigation.presentation.navigation.image

data class ImageView(
    val url: String,
    val srcsetSizes: List<Int>,
    val sizes: String,
    val alt: String,
    val width: Int,
    val height: Int,
) {
    val src get() = url.replace("[size]", srcsetSizes.first().toString())
    val srcset get() = srcsetSizes.joinToString(",") { url.replace("[size]", it.toString()) + " w$it" }
}
