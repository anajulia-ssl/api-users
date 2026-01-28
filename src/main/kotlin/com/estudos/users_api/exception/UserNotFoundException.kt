package com.estudos.users_api.exception

import java.util.UUID

class UserNotFoundException(id: UUID)
    : RuntimeException("User with id '$id' not found")
