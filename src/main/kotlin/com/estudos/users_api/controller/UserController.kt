package com.estudos.users_api.controller

import com.estudos.users_api.model.User
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
    fun create(@RequestBody user: User): User {
        return service.create(user)
    }

    //READ
    @GetMapping
    fun findAll(): List<User> {
        return service.findAll()
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): User? {
        return service.findById(id)
    }

    //UPDATE
    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @RequestBody user: User): User? {
        return service.update(id, user)
    }

    //DELETE
    @DeleteMapping("/{id}")
    fun deleteById(@PathVariable id: UUID){
        service.deleteById(id)
    }
}