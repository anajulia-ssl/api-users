package com.estudos.users_api.dto

import com.estudos.users_api.validation.annotation.UniqueStack
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class UserRequest(
    @field:NotBlank(message = "Name must not be blank")
    @field:Size(min = 3, max = 255, message = "Name size must be between 3 and 255")
    val name: String,

    @field:Size(min = 1, max = 255, message = "Nick size must be between 1 and 255")
    val nick: String? = null,

    @field:Past(message = "Birth date must be a past date")
    val birthDate: LocalDate,

    @field:Size(min = 1, message = "Stack must contain at least 1 element")
    @field:Size(max = 100, message = "Stack must contain a maximum of 100 elements")
    @field:UniqueStack
    @field:Valid
    val stack: List<StackRequest>
)
