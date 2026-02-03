package com.estudos.users_api.model

import com.estudos.users_api.dto.StackItemResponse
import com.estudos.users_api.dto.UserResponse
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "users")
class User(
    @Id
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    val id: UUID? = null,

    @Column(nullable = false, length = 255)
    var name: String?,

    @Column(nullable = true, length = 255, unique = true)
    var nick: String? = null,

    @Column(nullable = false)
    var birthDate: LocalDate?,

    @ElementCollection
    @CollectionTable(name = "user_stack", joinColumns = [JoinColumn(name = "user_id")])
    var stack: List<StackItem>?
)

fun User.toResponse(): UserResponse =
    UserResponse(
        id = this.id.toString(),
        name = this.name,
        nick = this.nick,
        birthDate = this.birthDate,
        stack = this.stack?.map {
            StackItemResponse(it.name, it.skillLevel)
        }
    )
