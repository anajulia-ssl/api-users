package com.estudos.users_api.validation.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import com.estudos.users_api.validation.annotation.MaxLengthElementsStack

class MaxLengthElementsStackValidator : ConstraintValidator<MaxLengthElementsStack, List<String>> {
    private var maxLength: Int = 32

    override fun initialize(annotation: MaxLengthElementsStack) {
        maxLength = annotation.max
    }

    override fun isValid(value: List<String>?, context: ConstraintValidatorContext): Boolean {
        return value?.all { it.length <= maxLength } ?: true
    }
}
