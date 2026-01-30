package com.estudos.users_api.validation.validator

import com.estudos.users_api.validation.annotation.NotBlankElementsStack
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class NotBlankElementsStackValidator : ConstraintValidator<NotBlankElementsStack, List<String>> {
    override fun isValid(value: List<String>?, context: ConstraintValidatorContext): Boolean {
        return value?.all { it.isNotBlank() } ?: true
    }
}