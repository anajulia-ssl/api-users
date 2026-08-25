package com.estudos.users_api.dto

import java.util.UUID

data class StackResponse(
    val id: UUID? = null,
    val name: String,
    val level: Int
)