package com.estudos.users_api.controller

import com.estudos.users_api.dto.ResultSet
import com.estudos.users_api.dto.StackItemResponse
import com.estudos.users_api.dto.UserRequest
import com.estudos.users_api.dto.UserResponse
import com.estudos.users_api.dto.toEntity
import com.estudos.users_api.model.toResponse
import com.estudos.users_api.service.UserService
import jakarta.validation.Valid
import org.springframework.data.domain.Sort
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
        val entity = request.toEntity()
        val saved = service.create(entity)

        return ResponseEntity.status(HttpStatus.CREATED).body(saved.toResponse())
    }

    @GetMapping
    fun findAll(
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "name:asc") sort: List<String>
    ): ResponseEntity<ResultSet<UserResponse>> {
        val orders = sort.map {
            val parts = it.split(":")
            Sort.Order(Sort.Direction.fromString(parts[1]), parts[0])
        }
        val sortObj = Sort.by(orders)

        val users = service.findAll(offset, limit, sortObj).map { it.toResponse() }
        val total = service.count()

        val resultSet = ResultSet(total, offset, limit, users)
        return ResponseEntity.ok(resultSet)
    }


    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): ResponseEntity<UserResponse> {
        val user = service.findById(id)

        return ResponseEntity.ok(user.toResponse())
    }

    @GetMapping("/{userId}/stacks")
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
        val entity = request.toEntity()
        val updated = service.update(id, entity)

        return ResponseEntity.ok(updated.toResponse())
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        service.deleteById(id)

        return ResponseEntity.noContent().build()
    }
}
