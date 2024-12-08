package com.inauditech.tractorstore.checkout.presentation.checkout.order

import jakarta.validation.constraints.NotBlank

data class OrderForm(
    @field:NotBlank(message = "First name cannot be blank")
    val firstName: String? = null,
    @field:NotBlank(message = "Last name cannot be blank")
    val lastName: String? = null,
    @field:NotBlank(message = "Store ID cannot be blank")
    val storeId: String? = null,
) {
    fun empty() = firstName == null && lastName == null && storeId == null
}
