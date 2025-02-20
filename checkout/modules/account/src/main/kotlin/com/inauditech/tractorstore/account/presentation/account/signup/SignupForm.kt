package com.inauditech.tractorstore.account.presentation.account.signup

import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class SignupForm(
    @field:Valid
    val customer: CustomerForm = CustomerForm(),
    @field:Valid
    val company: CompanyForm = CompanyForm(),
    @field:Valid
    val address: AddressForm = AddressForm(),
    var step: Step = Step.entries.first(),
)

data class CustomerForm(
    @field:NotBlank(message = "Name cannot be blank")
    val fullName: String? = null,
    @field:NotBlank(message = "Email cannot be blank")
    @field:Email(message = "Must be a valid email address")
    val email: String? = null,
    @field:NotBlank(message = "Password cannot be blank")
    val password: String? = null,
)

data class CompanyForm(
    @field:NotBlank(message = "Name cannot be blank")
    val name: String? = null,
    @field:NotBlank(message = "VAT id cannot be blank")
    val vatId: String? = null,
)

data class AddressForm(
    @field:NotBlank(message = "Street cannot be blank")
    val street: String? = null,
    @field:NotBlank(message = "City cannot be blank")
    val city: String? = null,
    @field:NotBlank(message = "Zip cannot be blank")
    val zip: String? = null,
)
