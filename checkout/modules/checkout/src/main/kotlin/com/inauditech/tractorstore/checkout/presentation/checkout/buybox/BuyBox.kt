package com.inauditech.tractorstore.checkout.presentation.checkout.buybox

import com.inauditech.tractorstore.checkout.domain.SellableVariant
import com.inauditech.tractorstore.checkout.domain.Sku
import com.inauditech.tractorstore.checkout.domain.VariantService
import com.inauditech.tractorstore.checkout.presentation.checkout.cart.Cart
import com.inauditech.tractorstore.checkout.presentation.checkout.cart.CartCookie
import jakarta.servlet.http.HttpServletResponse
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.client.HttpServerErrorException

@Controller
class BuyBox(
    val variantService: VariantService,
) {
    @GetMapping("/checkout/fragments/v1/buybox/{sku}")
    fun render(
        model: ModelMap,
        @PathVariable sku: String,
    ): String {
        val variant = variantService.findBySku(Sku(sku)) ?: throw HttpServerErrorException(HttpStatus.BAD_REQUEST)
        model.addAttribute("buyBoxView", BuyBoxView(variant))

        return "buybox/BuyBox"
    }

    @PutMapping("/checkout/fragments/v1/buybox")
    fun add(
        model: ModelMap,
        @RequestParam sku: String,
        @CookieValue("c_cart") cart: String = "",
        httpServletResponse: HttpServletResponse,
    ): String {
        val cartCookie = CartCookie(cart).addVariant(Sku(sku))

        val variant = variantService.findBySku(Sku(sku)) ?: throw HttpServerErrorException(HttpStatus.BAD_REQUEST)
        model.addAttribute("buyBoxView", BuyBoxView(variant, true))

        httpServletResponse.addCookie(cartCookie.toHttpCookie())
        httpServletResponse.setHeader("HX-Trigger", Cart.CART_ITEM_ADDED_EVENT)
        return "buybox/BuyBox"
    }

    companion object : KLogging() {
    }
}

data class BuyBoxView(
    val variant: SellableVariant,
    val showUpdateHint: Boolean = false,
)
