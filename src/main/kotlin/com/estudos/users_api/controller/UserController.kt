package com.estudos.users_api.controller

import com.estudos.users_api.mapper.UserMapper
import com.estudos.users_api.dto.UserRequest
import com.estudos.users_api.dto.UserResponse
import com.estudos.users_api.service.UserService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/users")
class UserController(private val service: UserService) {

    //CREATE
    @PostMapping
    fun create(@RequestBody request: UserRequest): UserResponse {
        val entity = UserMapper.toEntity(request)
        val saved = service.create(entity)
        return UserMapper.toResponse(saved)
    }

    //READ
    @GetMapping
    fun findAll(): List<UserResponse> {
        return service.findAll().map {
            UserMapper.toResponse(it)
        }
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): UserResponse {
        val user = service.findById(id)
        return UserMapper.toResponse(user)
    }

    //UPDATE
    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @RequestBody request: UserRequest): UserResponse? {
        val entity = UserMapper.toEntity(request).copy(id = id)
        val updated = service.update(id, entity)
        return UserMapper.toResponse(updated)
    }

    //DELETE
    @DeleteMapping("/{id}")
    fun deleteById(@PathVariable id: UUID){
        service.deleteById(id)
    }
}