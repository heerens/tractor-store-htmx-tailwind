package com.inauditech.tractorstore.checkout.presentation.checkout.cart

import com.inauditech.tractorstore.checkout.domain.SellableVariant
import com.inauditech.tractorstore.checkout.domain.VariantService
import com.inauditech.tractorstore.checkout.presentation.checkout.image.ImageView
import jakarta.servlet.http.HttpServletResponse
import mu.KLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class Cart(
    val variantService: VariantService,
) {
    @GetMapping("/checkout/fragments/v1/cart")
    fun render(
        @CookieValue("c_cart") cartCookieValue: String = "",
        httpServletResponse: HttpServletResponse,
        model: ModelMap,
    ): String {
        val view = createView(cartCookieValue)

        model.addAttribute("cartView", view)

        return "cart/Cart"
    }

    @DeleteMapping("/checkout/fragments/v1/cart")
    fun renderDelete(
        @CookieValue("c_cart") cartCookieValue: String = "",
        @RequestParam sku: String,
        httpServletResponse: HttpServletResponse,
        model: ModelMap,
    ): String {
        val cartCookie = CartCookie(cartCookieValue).removeVariant(sku)

        val view = createView(cartCookie.cookieValue)

        httpServletResponse.addCookie(cartCookie.toHttpCookie())
        model.addAttribute("cartView", view)

        return "cart/Cart"
    }

    fun createView(cartCookieValue: String): CartView {
        val cartCookie = CartCookie(cartCookieValue)
        val items =
            cartCookie.parse().mapNotNull { (sku, amount) ->
                variantService.findBySku(sku)?.let { variant ->
                    CartItemView(amount.toInt(), variant)
                }
            }
        val sum = items.sumOf { it.amount * it.variant.price }
        val view = CartView(items, sum)
        return view
    }

    companion object : KLogging()
}

data class CartView(
    val items: List<CartItemView>,
    val totalPrice: Int,
)

data class CartItemView(
    val amount: Int,
    val variant: SellableVariant,
) {
    val totalPrice: Int get() = variant.price * amount
    val imageView: ImageView
        get() =
            ImageView(
                url = variant.image,
                srcsetSizes = listOf(200, 400),
                sizes = "200px",
                alt = variant.name,
                width = 200,
                height = 200,
            )
}
