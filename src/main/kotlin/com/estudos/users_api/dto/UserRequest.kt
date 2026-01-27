package com.estudos.users_api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Size
import org.jetbrains.annotations.NotNull
import java.time.LocalDate

data class UserRequest(
    @NotNull
    @Size(min = 3, max = 255)
    val name: String,

    @Size(min = 1, max = 255)
    val nick: String? = null,

    @NotNull
    @Past
    val birthDate: LocalDate,

    val stack: List<@NotBlank @Size(max = 32) String>
)