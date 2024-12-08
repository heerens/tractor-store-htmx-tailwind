package com.inauditech.tractorstore.checkout.presentation.checkout.cart

import jakarta.servlet.http.HttpServletResponse
import mu.KLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping

@Controller
class CartPage(
    val cart: Cart,
) {
    @GetMapping("/checkout/cart")
    fun render(
        @CookieValue("c_cart") cartCookieValue: String = "",
        httpServletResponse: HttpServletResponse,
        model: ModelMap,
    ): String {
        model.addAttribute("cartView", cart.createView(cartCookieValue))
        return "cart/CartPage"
    }

    companion object : KLogging()
}
