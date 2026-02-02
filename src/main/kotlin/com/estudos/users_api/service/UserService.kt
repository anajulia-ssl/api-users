package com.estudos.users_api.service

import com.estudos.users_api.dto.StackItemResponse
import com.estudos.users_api.exception.NickAlreadyExistsException
import com.estudos.users_api.exception.UserNotFoundException
import com.estudos.users_api.model.User
import com.estudos.users_api.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository
) {
    private fun validate(user: User, currentId: UUID? = null) {
        user.nick?.let { nick ->
            val existingUser = userRepository.findByNick(nick)
            if (existingUser != null && existingUser.id != currentId) {

                throw NickAlreadyExistsException(nick)
            }
        }
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
        return userRepository.findById(id).orElseThrow {
            UserNotFoundException(id)
        }
    }

    fun getUserStacks(userId: UUID): List<StackItemResponse>? {
        val user = userRepository.findById(userId).orElseThrow {
            UserNotFoundException(userId)
        }

        return user.stack?.map { StackItemResponse(it.name, it.skillLevel) }
    }


    private fun findByNick(nick: String): User? {
        return userRepository.findByNick(nick)
    }

    // UPDATE
    fun update(id: UUID, updatedUser: User): User {
        val existing = userRepository.findById(id).orElseThrow {
            UserNotFoundException(id)
        }

        validate(updatedUser, existing.id)

        existing.name = updatedUser.name
        existing.nick = updatedUser.nick
        existing.birthDate = updatedUser.birthDate
        existing.stack = updatedUser.stack

        return userRepository.save(existing)
    }

    // DELETE
    fun deleteById(id: UUID) {
        if (!userRepository.existsById(id)) {
            throw UserNotFoundException(id)
        }
        userRepository.deleteById(id)
    }

}
