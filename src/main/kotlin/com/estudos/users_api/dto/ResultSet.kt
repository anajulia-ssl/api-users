package com.estudos.users_api.dto

data class ResultSet<T>(
    val total: Long,
    val offset: Int,
    val limit: Int,
    val items: List<T>
)
