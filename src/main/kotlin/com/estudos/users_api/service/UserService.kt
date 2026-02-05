package com.estudos.users_api.service

import com.estudos.users_api.dto.StackResponse
import com.estudos.users_api.exception.InvalidPaginationException
import com.estudos.users_api.exception.InvalidSortException
import com.estudos.users_api.exception.NickAlreadyExistsException
import com.estudos.users_api.exception.UserNotFoundException
import com.estudos.users_api.model.Stack
import com.estudos.users_api.model.User
import com.estudos.users_api.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
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
    fun create(user: User): User {
        validate(user)
        return userRepository.save(user)
    }

    // READ
    fun findAll(offset: Int, limit: Int, sort: String): List<User> {
        if (offset < 0 || limit <= 0) {
            throw InvalidPaginationException("offset must be >= 0 and limit > 0")
        }

        val sortableFields = listOf("name", "birth_date", "nick")

        val sortOrders = sort.split(",").map { part ->
            val parts = part.split(":")
            if (parts.size != 2) throw InvalidSortException("expected format: field:direction")

            val field = parts[0]
            val direction = parts[1]

            if (!sortableFields.contains(field)) throw InvalidSortException("field $field is not sortable")
            if (direction !in listOf("asc", "desc")) throw InvalidSortException("invalid direction: $direction")

            if (direction == "asc") Sort.Order.asc(field) else Sort.Order.desc(field)
        }

        val sortObj = Sort.by(sortOrders)
        val page = if (limit > 0) offset / limit else 0
        val pageable = PageRequest.of(page, limit, sortObj)

        return userRepository.findAll(pageable).content
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
    fun deleteById(id: UUID) {
        if (!userRepository.existsById(id)) {
            throw UserNotFoundException(id)
        }
        userRepository.deleteById(id)
    }
}
