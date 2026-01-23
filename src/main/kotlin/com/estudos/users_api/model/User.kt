package com.estudos.users_api.model

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Size
import org.hibernate.annotations.UuidGenerator
import org.jetbrains.annotations.NotNull
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "users")
class User(
    @Id @UuidGenerator
    @Column(nullable = false)
    val id: UUID,

    @NotNull
    @Size(min = 3, max = 255)
    @Column(nullable = false, length = 255)
    val name: String,

    @Size(min = 1, max = 255)
    @Column(nullable = true, length = 255, unique = true)
    val nick: String? = null,

    @NotNull
    @Past
    @Column(nullable = false)
    val birthDate: LocalDate,

    @ElementCollection
    @CollectionTable( name = "user_stack", joinColumns = [JoinColumn(name = "user_id")] )
    @Column(name = "stack_value", length = 32, nullable = false)
    val stack: List<@NotBlank @Size(max = 32) String>
) {
}