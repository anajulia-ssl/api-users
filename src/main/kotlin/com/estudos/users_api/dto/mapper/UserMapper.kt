package com.estudos.users_api.dto.mapper

import com.estudos.users_api.dto.StackResponse
import com.estudos.users_api.dto.UserRequest
import com.estudos.users_api.dto.UserResponse
import com.estudos.users_api.model.User

fun User.toResponse(): UserResponse =
    UserResponse(
        id = this.id,
        name = this.name,
        nick = this.nick,
        birthDate = this.birthDate,
        stack = this.stack.map {
            StackResponse(id = it.id, name = it.name, level = it.level)
        }
    )

fun UserRequest.toEntity(): User {
    val user = User(
        name = this.name,
        nick = this.nick,
        birthDate = this.birthDate
    )
    user.stack = this.stack.map {
        it.toEntity(user)
    }.toMutableList()
    return user
}