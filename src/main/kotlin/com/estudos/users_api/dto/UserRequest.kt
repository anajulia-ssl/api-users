package com.estudos.users_api.dto

import com.estudos.users_api.validation.annotation.MaxLengthElementsStack
import com.estudos.users_api.validation.annotation.NotBlankElementsStack
import com.estudos.users_api.validation.annotation.UniqueStack
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Size
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

data class UserRequest(
    @field:NotBlank
    @field:Size(min = 3, max = 255)
    val name: String,

    @field:Size(min = 1, max = 255)
    val nick: String? = null,

    @field:NotNull
    @field:Past
    val birthDate: LocalDate,

    @field:Size(min = 1, message = "stack must contain at least one element")
    @field:NotBlankElementsStack
    @field:UniqueStack
    @field:MaxLengthElementsStack(max = 32)
    val stack: List<@Valid @NotBlank @Size(max = 32)String>
)