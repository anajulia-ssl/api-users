package com.estudos.users_api.validation.validator

import com.estudos.users_api.validation.annotation.UniqueStack
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class UniqueStackValidator : ConstraintValidator<UniqueStack, List<String>> {
    override fun isValid(value: List<String>?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true
        return value.size == value.toSet().size
    }
}
