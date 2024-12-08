package com.inauditech.tractorstore.checkout.domain

interface ProductApi {
    fun findAll(): List<SellableVariant>
}
