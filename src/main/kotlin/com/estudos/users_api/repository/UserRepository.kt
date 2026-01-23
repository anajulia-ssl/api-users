package com.estudos.users_api.repository

import com.estudos.users_api.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID>{
    fun findByNick(nick: String): User?
}