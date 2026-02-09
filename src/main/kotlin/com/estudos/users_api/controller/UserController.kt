package com.estudos.users_api.controller

import com.estudos.users_api.dto.PageQuery
import com.estudos.users_api.dto.pagination.Paginator
import com.estudos.users_api.dto.StackResponse
import com.estudos.users_api.dto.UserRequest
import com.estudos.users_api.dto.UserResponse
import com.estudos.users_api.dto.toEntity
import com.estudos.users_api.dto.toPageable
import com.estudos.users_api.model.User
import com.estudos.users_api.model.toResponse
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
        val entity = request.toEntity()
        val saved = service.create(entity)
        return ResponseEntity.status(HttpStatus.CREATED).body(saved.toResponse())
    }

    @GetMapping
    fun findAll(@Valid query: PageQuery) =
        Paginator.build(
            query = query,
            page = service.findAll(query.toPageable(User::class)),
            mapper = { it.toResponse() }
        )


    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): ResponseEntity<UserResponse> {
        val user = service.findById(id)
        return ResponseEntity.ok(user.toResponse())
    }

    @GetMapping("/{userId}/stacks")
    fun getUserStacks(@PathVariable userId: UUID): ResponseEntity<List<StackResponse>> {
        val stacks = service.getUserStacks(userId)
        return if (stacks.isEmpty()) {
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
