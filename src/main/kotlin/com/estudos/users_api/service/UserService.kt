package com.estudos.users_api.service

import com.estudos.users_api.model.User
import com.estudos.users_api.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository
) {
    private fun validate(user: User, currentId: UUID? = null) {
        if (user.nick != null) {
            val existingUser = userRepository.findByNick(user.nick!!)
            if (existingUser != null && existingUser.id != currentId) {
                throw IllegalArgumentException("Nick já existe")
            }
        }

        if (user.stack.any { it.isBlank() })
            throw IllegalArgumentException("Stack contém valores nulos ou vazios")

        if (user.stack.any { it.length > 32 })
            throw IllegalArgumentException("Stack contém valores maiores que o limite de caracteres")

        if (user.stack.size != user.stack.distinct().size)
            throw IllegalArgumentException("Stack contém duplicados")
    }


    // CREATE
    fun create(user: User): User {
        validate(user)
        return userRepository.save(user)
    }

    // READ
    fun findAll(): List<User> {
        return userRepository.findAll()
    }

    fun findById(id: UUID): User {
        return userRepository.findById(id).orElseThrow { IllegalArgumentException("User not found") }
    }

    private fun findByNick(nick: String): User? {
        return userRepository.findByNick(nick)
    }

    // UPDATE
    fun update(id: UUID, updatedUser: User): User {
        val existing = userRepository.findById(id).orElseThrow {
            IllegalArgumentException("User not found")
        }

        validate(updatedUser, existing.id)

        val userToSave = updatedUser.copy(id = existing.id)
        return userRepository.save(userToSave)
    }

    // DELETE
    fun deleteById(id: UUID) {
        userRepository.deleteById(id)
    }
}
