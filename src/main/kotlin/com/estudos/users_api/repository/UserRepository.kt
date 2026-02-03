package com.estudos.users_api.repository

import com.estudos.users_api.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<User, UUID>{
    @Query("SELECT u FROM User u WHERE u.nick = :nick AND (:id IS NULL OR u.id <> :id)")
    fun findByNickExcludingId(@Param("nick") nick: String, @Param("id") id: UUID?): User?
}