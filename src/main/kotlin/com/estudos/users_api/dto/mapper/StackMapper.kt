package com.estudos.users_api.dto.mapper

import com.estudos.users_api.dto.StackRequest
import com.estudos.users_api.model.Stack
import com.estudos.users_api.model.User

fun StackRequest.toEntity(user: User): Stack =
    Stack(
        name = this.name,
        level = this.level,
        user = user
    )
