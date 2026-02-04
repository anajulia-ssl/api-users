package com.estudos.users_api.dto

data class ErrorResponse(
    val error: String,
    val description: String,
    val details: List<String>? = null
)
