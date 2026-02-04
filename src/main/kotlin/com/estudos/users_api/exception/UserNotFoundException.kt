package com.estudos.users_api.exception

import java.util.UUID

class UserNotFoundException(id: UUID)
    : RuntimeException("user with id '$id' not found")
