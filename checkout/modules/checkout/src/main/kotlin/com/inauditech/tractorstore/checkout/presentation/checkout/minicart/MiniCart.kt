package com.inauditech.tractorstore.checkout.presentation.checkout.minicart

import com.inauditech.tractorstore.checkout.presentation.checkout.cart.CartCookie
import jakarta.servlet.http.HttpServletResponse
import mu.KLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping

@Controller
class MiniCart {
    @GetMapping("/checkout/fragments/v1/minicart")
    fun render(
        model: ModelMap,
        @CookieValue("c_cart") cart: String = "",
        httpServletResponse: HttpServletResponse,
    ): String {
        val cartCookie = CartCookie(cart)

        model.addAttribute("miniCartView", MiniCartView(cartCookie.size()))

        return "minicart/MiniCart"
    }

    @GetMapping("/checkout/fragments/v1/minicart/amount")
    fun renderAmount(
        model: ModelMap,
        @CookieValue("c_cart") cart: String = "",
        httpServletResponse: HttpServletResponse,
    ): String {
        val cartCookie = CartCookie(cart)

        model.addAttribute("amount", cartCookie.size())

        return "minicart/MiniCartAmount"
    }

    companion object : KLogging()
}

data class MiniCartView(
    val amount: Int,
)
