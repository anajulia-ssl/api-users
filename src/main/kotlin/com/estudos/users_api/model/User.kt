package com.estudos.users_api.model

import com.estudos.users_api.annotation.Sortable
import com.estudos.users_api.dto.StackResponse
import com.estudos.users_api.dto.UserResponse
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
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
    @Sortable
    var name: String,

    @Column(nullable = true, length = 255, unique = true)
    @Sortable
    var nick: String?  = null,

    @Column(name = "birth_date", nullable = false)
    @Sortable(external = "birth_date")
    var birthDate: LocalDate,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var stack: MutableList<Stack> = mutableListOf()
)

fun User.toResponse(): UserResponse =
    UserResponse(
        id = this.id,
        name = this.name,
        nick = this.nick,
        birthDate = this.birthDate,
        stack = this.stack.map {
            StackResponse(id = it.id, name = it.name, level = it.level)
        }
    )
