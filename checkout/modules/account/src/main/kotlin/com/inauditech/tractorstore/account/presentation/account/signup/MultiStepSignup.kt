package com.inauditech.tractorstore.account.presentation.account.signup

import jakarta.servlet.http.HttpServletResponse
import mu.KLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.BindingResult
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import kotlin.math.max
import kotlin.math.min

@Controller
class MultiStepSignup {
    @PostMapping("/account/fragments/v1/signupform/next")
    fun submitFormNext(
        @Validated @ModelAttribute("signupForm") signupForm: SignupForm,
        bindingResult: BindingResult,
        httpServletResponse: HttpServletResponse,
        model: ModelMap,
    ): String {
        if (!bindingResult.hasErrors() && signupForm.step.isLast()) {
            httpServletResponse.setHeader("Hx-Redirect", "/account/signup/thanks")
            return "empty"
        }

        val submittedStep = signupForm.step
        // keep only errors for current step
        val errors = findErrorsForSubmittedStep(bindingResult, submittedStep)

        signupForm.step = if (errors.fieldErrors.isEmpty()) submittedStep.next() else submittedStep

        val steppedDirection = if (submittedStep != signupForm.step) Direction.NEXT else null

        model.addAttribute("steppedDirection", steppedDirection)
        model.addAttribute("errors", errors)
        return "signup/MultiStepSignup"
    }

    @PostMapping("/account/fragments/v1/signupform/prev")
    fun submitFormPrev(
        @Validated @ModelAttribute("signupForm") signupForm: SignupForm,
        bindingResult: BindingResult,
        httpServletResponse: HttpServletResponse,
        model: ModelMap,
    ): String {
        val submittedStep = signupForm.step
        val errors = removeAllErrors(bindingResult)
        signupForm.step = submittedStep.prev()

        model.addAttribute("steppedDirection", Direction.PREV)
        model.addAttribute("errors", errors)
        return "signup/MultiStepSignup"
    }

    fun findErrorsForSubmittedStep(
        originalBindingResult: BindingResult,
        submittedStep: Step,
    ): BindingResult {
        val errorsForStep = removeAllErrors(originalBindingResult)

        for (fieldError in originalBindingResult.fieldErrors) {
            if (fieldError.field.startsWith(submittedStep.name.lowercase())) {
                errorsForStep.addError(fieldError)
            }
        }

        for (globalError in originalBindingResult.globalErrors) {
            errorsForStep.addError(globalError)
        }

        return errorsForStep
    }

    private fun removeAllErrors(bindingResult: BindingResult) =
        BeanPropertyBindingResult(
            bindingResult.target,
            bindingResult.objectName,
        )

    companion object : KLogging()
}

enum class Direction {
    NEXT,
    PREV,
}

enum class Step {
    CUSTOMER,
    COMPANY,
    ADDRESS,
    ;

    fun isFirst() = ordinal == 0

    fun isLast() = ordinal == entries.lastIndex

    fun prev() = Step.entries[max(0, ordinal - 1)]

    fun next() = Step.entries[min(entries.lastIndex, ordinal + 1)]
}
