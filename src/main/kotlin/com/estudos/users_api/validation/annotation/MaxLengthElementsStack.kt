package com.estudos.users_api.validation.annotation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass
import com.estudos.users_api.validation.validator.MaxLengthElementsStackValidator

@MustBeDocumented
@Constraint(validatedBy = [MaxLengthElementsStackValidator::class])
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class MaxLengthElementsStack(
    val max: Int = 32,
    val message: String = "stack cannot contain values with more than {max} characters",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
