package com.estudos.users_api.controller

import com.estudos.users_api.dto.StackItemResponse
import com.estudos.users_api.mapper.UserMapper
import com.estudos.users_api.dto.UserRequest
import com.estudos.users_api.dto.UserResponse
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
    fun create(@Valid @RequestBody request: UserRequest): ResponseEntity<UserResponse> {
        println("Request recebido: $request")
        val entity = UserMapper.toEntity(request)
        val saved = service.create(entity)

        return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.toResponse(saved))
    }

    @GetMapping
    fun findAll(): ResponseEntity<List<UserResponse>> {
        val users = service.findAll().map(UserMapper::toResponse)

        return ResponseEntity.ok(users)
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): ResponseEntity<UserResponse> {
        val user = service.findById(id)

        return ResponseEntity.ok(UserMapper.toResponse(user))
    }

    fun getUserStacks(@PathVariable userId: UUID): ResponseEntity<List<StackItemResponse>> {
        val stacks = service.getUserStacks(userId)
        return if (stacks == null) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.ok(stacks)
        }
    }


    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @Valid @RequestBody request: UserRequest): ResponseEntity<UserResponse> {
        val entity = UserMapper.toEntity(request)
        val updated = service.update(id, entity)

        return ResponseEntity.ok(UserMapper.toResponse(updated))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        service.deleteById(id)

        return ResponseEntity.noContent().build()
    }
}
