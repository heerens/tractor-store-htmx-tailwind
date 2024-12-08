package com.inauditech.tractorstore.checkout.presentation.checkout.order

import jakarta.servlet.http.HttpServletResponse
import mu.KLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping

@Controller
class OrderDetailsPage {
    @GetMapping("/checkout/checkout")
    fun render(
        @CookieValue("c_cart") cartCookieValue: String = "",
        httpServletResponse: HttpServletResponse,
        model: ModelMap,
    ): String {
        //
        model.addAttribute("orderForm", OrderForm())
        return "order/OrderDetailsPage"
    }

    companion object : KLogging()
}
