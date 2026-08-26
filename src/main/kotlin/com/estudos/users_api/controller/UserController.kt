package com.estudos.users_api.controller

import com.estudos.users_api.dto.pagination.PageQuery
import com.estudos.users_api.dto.pagination.PageResponse
import com.estudos.users_api.dto.pagination.Paginator
import com.estudos.users_api.dto.StackResponse
import com.estudos.users_api.dto.UserRequest
import com.estudos.users_api.dto.UserResponse
import com.estudos.users_api.dto.mapper.toEntity
import com.estudos.users_api.dto.pagination.toPageable
import com.estudos.users_api.dto.mapper.toResponse
import com.estudos.users_api.model.User
import com.estudos.users_api.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.UUID

@RestController
@RequestMapping("/api/users")
class UserController(private val service: UserService) {

    @PostMapping
    fun create(@Valid @RequestBody request: UserRequest): ResponseEntity<UserResponse> {
        val entity = request.toEntity()
        val saved = service.create(entity)
        val location = URI.create("/api/users/${saved.id}")
        return ResponseEntity.created(location).body(saved.toResponse())
    }

    @GetMapping
    fun findAll(query: PageQuery): ResponseEntity<PageResponse<UserResponse>> =
        ResponseEntity.ok(
            Paginator.build(
                query = query,
                page = service.findAll(query.toPageable(User::class)),
                mapper = { it.toResponse() }
            )
        )


    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): ResponseEntity<UserResponse> {
        val user = service.findById(id)
        return ResponseEntity.ok(user.toResponse())
    }

    @GetMapping("/{userId}/stacks")
    fun getUserStacks(@PathVariable userId: UUID): ResponseEntity<List<StackResponse>> {
        val stacks = service.getUserStacks(userId)
        return ResponseEntity.ok(stacks.map { it.toResponse() })
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
