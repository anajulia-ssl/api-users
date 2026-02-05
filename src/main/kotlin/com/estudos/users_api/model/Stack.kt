package com.estudos.users_api.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
@Table(name = "user_stack")
class Stack(
    @Id
    @UuidGenerator
    @Column(nullable = false)
    val id: UUID? = null,

    @Column(nullable = false, length = 32)
    var name: String,

    @Column(name = "stack_level", nullable = false)
    var level: Int,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null
)
