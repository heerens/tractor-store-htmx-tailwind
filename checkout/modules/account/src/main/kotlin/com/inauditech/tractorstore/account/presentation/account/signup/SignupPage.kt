package com.inauditech.tractorstore.account.presentation.account.signup

import mu.KLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping

@Controller
class SignupPage {
    @GetMapping("/account/signup")
    fun render(model: ModelMap): String {
        model.addAttribute(
            "signupPageView",
            SignupPageView(
                SignupForm(
                    customer =
                        CustomerForm(
                            fullName = "John Tractor",
                            email = "customer@example.com",
                            password = "leeT!shoPR123",
                        ),
                    company =
                        CompanyForm(
                            name = "John Tractor Inc.",
                            vatId = "US123456",
                        ),
                    address =
                        AddressForm(
                            street = "Main Street 42",
                            city = "Denver",
                            zip = "CO 80238",
                        ),
                ),
            ),
        )
        return "signup/SignupPage"
    }

    companion object : KLogging()
}

data class SignupPageView(
    val signupForm: SignupForm,
)
