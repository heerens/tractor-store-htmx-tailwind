package com.inauditech.tractorstore.checkout.presentation.checkout.order

import com.inauditech.tractorstore.checkout.presentation.checkout.cart.CartCookie
import jakarta.servlet.http.HttpServletResponse
import mu.KLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.validation.BindingResult
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping

@Controller
class OrderDetails {
    @GetMapping("/checkout/fragments/v1/orderform")
    fun render(
        @CookieValue("c_cart") cartCookieValue: String = "",
        httpServletResponse: HttpServletResponse,
        model: ModelMap,
    ): String {
        //
        model.addAttribute("orderForm", OrderForm())
        return "order/OrderDetails"
    }

    @PostMapping("/checkout/fragments/v1/orderform/validate")
    fun validateForm(
        @Validated @ModelAttribute("orderForm") orderForm: OrderForm,
        bindingResult: BindingResult,
        model: ModelMap,
    ): String {
        model.addAttribute("errors", bindingResult)
        return "order/OrderDetails"
    }

    @PostMapping("/checkout/fragments/v1/orderform")
    fun submitForm(
        @Validated @ModelAttribute("orderForm") orderForm: OrderForm,
        bindingResult: BindingResult,
        @CookieValue("c_cart") cart: String = "",
        httpServletResponse: HttpServletResponse,
        model: ModelMap,
    ): String {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult)
            return "order/OrderDetails"
        }

        val cartCookie = CartCookie(cart).clear()
        httpServletResponse.addCookie(cartCookie.toHttpCookie())
        httpServletResponse.setHeader("Hx-Redirect", "/checkout/thanks")
        return "empty"
    }

    companion object : KLogging()
}
