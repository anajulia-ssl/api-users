package com.estudos.users_api.dto

import com.estudos.users_api.model.StackItem
import com.estudos.users_api.model.User
import com.estudos.users_api.validation.annotation.UniqueStack
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Size
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

data class UserRequest(

    @field:NotNull(message = "name must not be null")
    @field:NotBlank(message = "name must not be blank")
    @field:Size(min = 3, max = 255, message = "name size must be between 3 and 255")
    val name: String?,

    @field:Size(min = 1, max = 255, message = "nick size must be between 1 and 255")
    val nick: String? = null,

    @field:NotNull(message = "birth date must not be null")
    @field:Past(message = "birth date must be a past date")
    val birthDate: LocalDate?,

    @field:Size(min = 1, message = "stack must contain at least one element")
    @field:NotNull(message = "stack must not be null")
    @field:UniqueStack
    @field:Valid
    val stack: List<StackItemRequest>?
)

fun UserRequest.toEntity(): User =
    User(
        name = this.name,
        nick = this.nick,
        birthDate = this.birthDate,
        stack = this.stack?.map {
            StackItem(it.name, it.skillLevel)
        }
    )