package com.estudos.users_api.dto

import java.time.LocalDate
import java.util.UUID

data class UserResponse(
    val id: UUID?,
    val name: String?,
    val nick: String?,
    val birthDate: LocalDate?,
    val stack: List<StackItemResponse>?
)
