package com.estudos.users_api.exception

class NickAlreadyExistsException(nick: String)
    : RuntimeException("Nick '$nick' already exists")

