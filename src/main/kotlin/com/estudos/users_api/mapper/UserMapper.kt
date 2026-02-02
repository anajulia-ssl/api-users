package com.estudos.users_api.mapper

import com.estudos.users_api.dto.StackItemResponse
import com.estudos.users_api.dto.UserRequest
import com.estudos.users_api.dto.UserResponse
import com.estudos.users_api.model.StackItem
import com.estudos.users_api.model.User

object UserMapper {
    fun toEntity(request: UserRequest): User =
        User(
            name = request.name,
            nick = request.nick,
            birthDate = request.birthDate,
            stack = request.stack?.map { StackItem(it.name, it.skillLevel) }
        )

    fun toResponse(user: User): UserResponse =
        UserResponse(
            id = user.id.toString(),
            name = user.name,
            nick = user.nick,
            birthDate = user.birthDate,
            stack = user.stack?.map { StackItemResponse(it.name, it.skillLevel) }
        )
}
