package com.estudos.users_api.model

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class StackItem(
    @Column(name = "name", length = 32, nullable = false)
    var name: String?,

    @Column(name = "skill_level", nullable = false)
    var skillLevel: Int?
)

