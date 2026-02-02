package com.estudos.users_api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.Range

data class StackItemRequest(
    @field:NotBlank(message = "stack item name must not be blank")
    @field:NotNull(message = "stack item name must not be null")
    @field:Size(max = 32, message = "stack item name size must be less than or equal to 32")
    val name: String?,

    @field:NotNull(message = "stack item skill_level must not be null")
    @field:Range(min = 1, max = 10, message = "stack item skill_level must be between 1 and 10")
    val skillLevel: Int?
)
