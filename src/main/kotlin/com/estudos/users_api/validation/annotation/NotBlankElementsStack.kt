package com.estudos.users_api.validation.annotation

import com.estudos.users_api.validation.validator.NotBlankElementsStackValidator
import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [NotBlankElementsStackValidator::class])
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class NotBlankElementsStack(
    val message: String = "stack cannot contain empty values",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
