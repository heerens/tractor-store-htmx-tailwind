package com.inauditech.tractorstore.checkout.presentation.checkout.cart

import com.inauditech.tractorstore.checkout.domain.Sku
import jakarta.servlet.http.Cookie
import java.util.concurrent.atomic.AtomicInteger

data class CartCookie(
    val cookieValue: String,
) {
    fun size(): Int = parse().sumOf { it.second.toInt() }

    fun addVariant(sku: Sku): CartCookie {
        val list = parse().toMap()

        val updated =
            if (list.containsKey(sku)) {
                list.getValue(sku).incrementAndGet()
                list
            } else {
                list + (sku to AtomicInteger(1))
            }

        return CartCookie(toString(updated))
    }

    private fun toString(updated: Map<Sku, AtomicInteger>): String =
        updated.entries.joinToString("|") { (key, value) ->
            key.value + "_${value.toInt()}"
        }

    fun parse(): List<Pair<Sku, AtomicInteger>> {
        if (cookieValue.isEmpty()) return emptyList()

        val list =
            cookieValue
                .split("|")
                .map {
                    it
                        .split("_")
                        .let { skuAndAmount -> Pair(Sku(skuAndAmount[0]), AtomicInteger(skuAndAmount[1].toInt())) }
                }
        return list
    }

    fun removeVariant(sku: String): CartCookie {
        val list = cookieValue.split("|").filter { !it.contains(sku) }
        return CartCookie(list.joinToString("|"))
    }

    fun clear(): CartCookie = CartCookie("")

    fun toHttpCookie(): Cookie {
        val cookie = Cookie("c_cart", cookieValue)
        cookie.maxAge = 365 * 24 * 60 * 60; // expires in 365 days
        cookie.secure = true
        cookie.isHttpOnly = true
        cookie.path = "/"; // global cookie accessible every where
        return cookie
    }
}
