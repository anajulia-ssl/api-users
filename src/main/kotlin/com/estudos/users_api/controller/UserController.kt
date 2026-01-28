package com.estudos.users_api.controller

import com.estudos.users_api.mapper.UserMapper
import com.estudos.users_api.dto.UserRequest
import com.estudos.users_api.dto.UserResponse
import com.estudos.users_api.exception.NickAlreadyExistsException
import com.estudos.users_api.exception.UserNotFoundException
import com.estudos.users_api.exception.InvalidStackException
import com.estudos.users_api.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/users")
class UserController(private val service: UserService) {

    @PostMapping
    fun create(@Valid @RequestBody request: UserRequest): ResponseEntity<Any> {
        return try {
            val entity = UserMapper.toEntity(request)
            val saved = service.create(entity)
            ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserMapper.toResponse(saved))
        } catch (ex: NickAlreadyExistsException) {
            ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(mapOf("error" to ex.message))
        } catch (ex: InvalidStackException) {
            ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to ex.message))
        }
    }

    @GetMapping
    fun findAll(): ResponseEntity<List<UserResponse>> {
        val users = service.findAll()
            .map(UserMapper::toResponse)
        return ResponseEntity.ok(users)
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): ResponseEntity<Any> {
        return try {
            val user = service.findById(id)
            ResponseEntity.ok(UserMapper.toResponse(user))
        } catch (ex: UserNotFoundException) {
            ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to ex.message))
        }
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @Valid @RequestBody request: UserRequest): ResponseEntity<Any> {
        return try {
            val entity = UserMapper.toEntity(request)
            val updated = service.update(id, entity)
            ResponseEntity.ok(UserMapper.toResponse(updated))
        } catch (ex: UserNotFoundException) {
            ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to ex.message))
        } catch (ex: NickAlreadyExistsException) {
            ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(mapOf("error" to ex.message))
        } catch (ex: InvalidStackException) {
            ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to ex.message))
        }
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID): ResponseEntity<Any> {
        return try {
            service.deleteById(id)
            ResponseEntity.noContent().build()
        } catch (ex: UserNotFoundException) {
            ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to ex.message))
        }
    }
}
