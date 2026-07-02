package com.estudos.users_api.service

import com.estudos.users_api.dto.StackResponse
import com.estudos.users_api.exception.NickAlreadyExistsException
import com.estudos.users_api.exception.UserNotFoundException
import com.estudos.users_api.model.Stack
import com.estudos.users_api.model.User
import com.estudos.users_api.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository
) {

    private fun validate(user: User, currentId: UUID? = null) {
        user.nick?.let { nick ->
            val existingUser = userRepository.findByNickExcludingId(nick, currentId)
            if (existingUser != null) {
                throw NickAlreadyExistsException(nick)
            }
        }
    }

    // CREATE
    @Transactional
    fun create(user: User): User {
        validate(user)
        return userRepository.save(user)
    }

    // READ
    fun findAll(pageable: Pageable): Page<User> {
        return userRepository.findAll(pageable)
    }

    fun findById(id: UUID): User {
        return userRepository.findById(id).orElseThrow {
            UserNotFoundException(id)
        }
    }

    fun getUserStacks(userId: UUID): List<StackResponse> {
        val user = userRepository.findById(userId).orElseThrow {
            UserNotFoundException(userId)
        }

        return user.stack.map { StackResponse(id = it.id, name = it.name, level = it.level) }
    }

    // UPDATE
    @Transactional
    fun update(id: UUID, updatedUser: User): User {
        val existing = userRepository.findById(id).orElseThrow {
            UserNotFoundException(id)
        }

        validate(updatedUser, existing.id)

        existing.name = updatedUser.name
        existing.nick = updatedUser.nick
        existing.birthDate = updatedUser.birthDate

        existing.stack.clear()
        updatedUser.stack.forEach { stack ->
            existing.stack.add(
                Stack(
                    id = stack.id,
                    name = stack.name,
                    level = stack.level,
                    user = existing
                )
            )
        }

        return userRepository.save(existing)
    }

    // DELETE
    @Transactional
    fun deleteById(id: UUID) {
        if (!userRepository.existsById(id)) {
            throw UserNotFoundException(id)
        }
        userRepository.deleteById(id)
    }
}
