package com.estudos.users_api.controller

import com.estudos.users_api.dto.StackItemRequest
import com.estudos.users_api.dto.UserRequest
import jakarta.transaction.Transactional
import org.hamcrest.Matchers
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import tools.jackson.databind.ObjectMapper
import java.time.LocalDate
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerIntegrationTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper
) {

    @Nested
    inner class CreateTests {

        @Test
        fun `should create user when valid request`() {
            val request = UserRequest(
                name = "Ana",
                nick = "ana12345",
                birthDate = LocalDate.of(1995, 1, 1),
                stack = listOf(StackItemRequest("Kotlin", 5))
            )

            mockMvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").isNotEmpty)
                .andExpect(jsonPath("$.name").value("Ana"))
                .andExpect(jsonPath("$.nick").value("ana12345"))
                .andExpect(jsonPath("$.birth_date").value("1995-01-01"))
                .andExpect(jsonPath("$.stack[0].name").value("Kotlin"))
                .andExpect(jsonPath("$.stack[0].skill_level").value(5))
        }

        @Test
        fun `should create user without nick`() {
            val request = UserRequest(
                name = "Ana",
                nick = null,
                birthDate = LocalDate.of(1995, 1, 1),
                stack = listOf(StackItemRequest("Kotlin", 5))
            )

            mockMvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").isNotEmpty)
                .andExpect(jsonPath("$.name").value("Ana"))
                .andExpect(jsonPath("$.nick").doesNotExist())
                .andExpect(jsonPath("$.birth_date").value("1995-01-01"))
                .andExpect(jsonPath("$.stack[0].name").value("Kotlin"))
                .andExpect(jsonPath("$.stack[0].skill_level").value(5))
        }

        @Test
        fun `should return 409 when nick already exists`() {
            val request1 = UserRequest(
                name = "João",
                nick = "duplicado",
                birthDate = LocalDate.of(1990, 1, 1),
                stack = listOf(StackItemRequest("Spring", 5))
            )
            val request2 = UserRequest(
                name = "Maria",
                nick = "duplicado",
                birthDate = LocalDate.of(1992, 2, 2),
                stack = listOf(StackItemRequest("Kotlin", 7))
            )

            mockMvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request1))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").isNotEmpty)
                .andExpect(jsonPath("$.name").value("João"))
                .andExpect(jsonPath("$.nick").value("duplicado"))
                .andExpect(jsonPath("$.birth_date").value("1990-01-01"))
                .andExpect(jsonPath("$.stack[0].name").value("Spring"))
                .andExpect(jsonPath("$.stack[0].skill_level").value(5))

            mockMvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request2))
            )
                .andExpect(status().isConflict)
                .andExpect(jsonPath("$.error").value("conflict_exception"))
                .andExpect(jsonPath("$.description").value("Nick already exists"))
                .andExpect(jsonPath("$.details").value("nick 'duplicado' already exists"))
        }

        @ParameterizedTest
        @MethodSource("com.estudos.users_api.controller.UserControllerIntegrationTest#invalidCreateRequests")
        fun `should return 400 with error response when invalid create request`(
            request: UserRequest,
            expectedDetail: String
        ) {
            mockMvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error").value("validation_exception"))
                .andExpect(jsonPath("$.description").value("Invalid request"))
                .andExpect(jsonPath("$.details").isArray)
                .andExpect(jsonPath("$.details").value(org.hamcrest.Matchers.hasItem(expectedDetail)))
        }

        @ParameterizedTest
        @CsvSource("1", "10")
        fun `should accept stack skill level at limits`(skill: Int) {
            val request = UserRequest(
                name = "Boundary",
                nick = "boundary$skill",
                birthDate = LocalDate.of(1990,1,1),
                stack = listOf(StackItemRequest("Java", skill))
            )
            mockMvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.stack[0].skill_level").value(skill))
        }

        @Test
        fun `should reject duplicate stack items case-insensitive`() {
            val request = UserRequest(
                name = "Case",
                nick = "caseStack",
                birthDate = LocalDate.of(1990,1,1),
                stack = listOf(StackItemRequest("Java",5), StackItemRequest("java",6))
            )
            mockMvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error").value("validation_exception"))
                .andExpect(jsonPath("$.details", Matchers.hasItem("stack cannot contain duplicate values")))
        }
    }

    @Nested
    inner class ReadTests {

        @Test
        fun `should return 200 with empty list when no users exist`() {
            mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$", Matchers.hasSize<Any>(0)))
        }

        @Test
        fun `should return 200 with list of users when users exist`() {
            val request = UserRequest("Teste", "teste123", LocalDate.of(1995,1,1),
                listOf(StackItemRequest("Kotlin", 5)))

            mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated)

            mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$", Matchers.hasSize<Any>(1)))
                .andExpect(jsonPath("$[0].id").isNotEmpty)
                .andExpect(jsonPath("$[0].name").value("Teste"))
                .andExpect(jsonPath("$[0].nick").value("teste123"))
                .andExpect(jsonPath("$[0].birth_date").value("1995-01-01"))
                .andExpect(jsonPath("$[0].stack[0].name").value("Kotlin"))
                .andExpect(jsonPath("$[0].stack[0].skill_level").value(5))
        }

        @Test
        fun `should return 200 when user exists by id`() {
            val request = UserRequest(
                name = "Carlos",
                nick = "carlos123",
                birthDate = LocalDate.of(1985, 5, 5),
                stack = listOf(StackItemRequest("Oracle", 5))
            )

            val result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn()

            val id = objectMapper.readTree(result.response.contentAsString).get("id").asText()

            mockMvc.perform(get("/api/users/$id"))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Carlos"))
                .andExpect(jsonPath("$.nick").value("carlos123"))
                .andExpect(jsonPath("$.birth_date").value("1985-05-05"))
                .andExpect(jsonPath("$.stack[0].name").value("Oracle"))
                .andExpect(jsonPath("$.stack[0].skill_level").value(5))
        }

        @Test
        fun `should return 404 when user not exist by id`() {
            val randomId = UUID.randomUUID()

            mockMvc.perform(get("/api/users/$randomId"))
                .andExpect(status().isNotFound)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("not_found_exception"))
                .andExpect(jsonPath("$.description").value("User not found"))
                .andExpect(jsonPath("$.details[0]").value("user with id '$randomId' not found"))

        }

        @ParameterizedTest
        @CsvSource(
            "/api/users?offset=-1&limit=5, invalid_pagination, Invalid pagination parameters",
            "/api/users?offset=0&limit=0, invalid_pagination, Invalid pagination parameters",
            "/api/users?offset=0&limit=5&sort=unknown:asc, invalid_sort, Invalid sorting parameters"
        )
        fun `should return 400 with error response when invalid pagination or sort`(
            url: String,
            expectedError: String,
            expectedDescription: String
        ) {
            mockMvc.perform(get(url))
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value(expectedError))
                .andExpect(jsonPath("$.description").value(expectedDescription))
                .andExpect(jsonPath("$.details").isArray)
                .andExpect(jsonPath("$.details[0]").isNotEmpty)
        }

        @Test
        fun `should sort users by name asc`() {
            val r1 = UserRequest("Bruno", "b1", LocalDate.of(1990,1,1), listOf(StackItemRequest("Java",5)))
            val r2 = UserRequest("Ana", "a1", LocalDate.of(1990,1,1), listOf(StackItemRequest("Java",5)))

            mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(r1)))
                .andExpect(status().isCreated)

            mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(r2)))
                .andExpect(status().isCreated)

            mockMvc.perform(get("/api/users?offset=0&limit=10&sort=name:asc"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$[0].name").value("Ana"))
                .andExpect(jsonPath("$[1].name").value("Bruno"))
        }
    }

    @Nested
    inner class UpdateTests {

        @Test
        fun `should update user when valid request`() {
            val request = UserRequest(
                name = "Ana",
                nick = "ana12345",
                birthDate = LocalDate.of(1992, 2, 2),
                stack = listOf(StackItemRequest("Java", 5))
            )

            val result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn()

            val id = objectMapper.readTree(result.response.contentAsString).get("id").asText()

            val updateRequest = UserRequest(
                name = "Ana Paula",
                nick = "ana12345",
                birthDate = LocalDate.of(1992, 2, 2),
                stack = listOf(StackItemRequest("Spring", 7))
            )

            mockMvc.perform(put("/api/users/$id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Ana Paula"))
                .andExpect(jsonPath("$.nick").value("ana12345"))
                .andExpect(jsonPath("$.birth_date").value("1992-02-02"))
                .andExpect(jsonPath("$.stack[0].name").value("Spring"))
                .andExpect(jsonPath("$.stack[0].skill_level").value(7))
        }

        @Test
        fun `should return 404 when updating not existing user`() {
            val updateRequest = UserRequest(
                name = "Teste",
                nick = "teste",
                birthDate = LocalDate.of(1990, 1, 1),
                stack = listOf(StackItemRequest("Kotlin", 5))
            )

            val randomId = UUID.randomUUID()

            mockMvc.perform(
                put("/api/users/$randomId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest))
            )
                .andExpect(status().isNotFound)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("not_found_exception"))
                .andExpect(jsonPath("$.description").value("User not found"))
                .andExpect(jsonPath("$.details[0]").value("user with id '$randomId' not found"))
        }

        @Test
        fun `should return 409 when updating with duplicate nick`() {
            val request1 = UserRequest("User1", "nick1", LocalDate.of(1990,1,1),
                listOf(StackItemRequest("Java", 5)))
            val request2 = UserRequest("User2", "nick2", LocalDate.of(1991,1,1),
                listOf(StackItemRequest("Spring", 5)))

            val result1 = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andReturn()
            val id1 = objectMapper.readTree(result1.response.contentAsString).get("id").asText()

            mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated)

            val updateRequest = UserRequest("User1 Updated", "nick2", LocalDate.of(1990,1,1),
                listOf(StackItemRequest("Java", 5)))

            mockMvc.perform(put("/api/users/$id1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("conflict_exception"))
                .andExpect(jsonPath("$.description").value("Nick already exists"))
                .andExpect(jsonPath("$.details[0]").value("nick 'nick2' already exists"))
        }

        @Test
        fun `should update nick to null when allowed`() {
            val create = UserRequest("User", "nickx", LocalDate.of(1990,1,1), listOf(StackItemRequest("Java",5)))
            val result = mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(create)))
                .andReturn()
            val id = objectMapper.readTree(result.response.contentAsString)["id"].asText()

            val update = UserRequest("User", null, LocalDate.of(1990,1,1), listOf(StackItemRequest("Java",5)))
            mockMvc.perform(put("/api/users/$id").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.nick").doesNotExist())
        }

        @ParameterizedTest
        @MethodSource("com.estudos.users_api.controller.UserControllerIntegrationTest#invalidCreateRequests")
        fun `should return 400 when invalid update request`(
            request: UserRequest,
            expectedDetail: String
        ) {
            val valid = UserRequest(
                name = "Pedro",
                nick = "pedro123",
                birthDate = LocalDate.of(1991,3,3),
                stack = listOf(StackItemRequest("Java", 5))
            )

            val result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(valid)))
                .andReturn()

            val id = objectMapper.readTree(result.response.contentAsString).get("id").asText()

            mockMvc.perform(put("/api/users/$id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("validation_exception"))
                .andExpect(jsonPath("$.description").value("Invalid request"))
                .andExpect(jsonPath("$.details").isArray)
                .andExpect(jsonPath("$.details", Matchers.hasItem(expectedDetail)))
        }

    }

    @Nested
    inner class DeleteTests {

        @Test
        fun `should delete user when exists`() {
            val request = UserRequest(
                name = "Lucas",
                nick = "lucas123",
                birthDate = LocalDate.of(1993,4,4),
                stack = listOf(StackItemRequest("Spring", 5))
            )

            val result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn()

            val id = objectMapper.readTree(result.response.contentAsString).get("id").asText()

            mockMvc.perform(delete("/api/users/$id"))
                .andExpect(status().isNoContent)
                .andExpect(content().string("")) // corpo vazio
        }

        @Test
        fun `should return 404 when deleting not existing user`() {
            val randomId = UUID.randomUUID()

            mockMvc.perform(delete("/api/users/$randomId"))
                .andExpect(status().isNotFound)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("not_found_exception"))
                .andExpect(jsonPath("$.description").value("User not found"))
                .andExpect(jsonPath("$.details[0]").value("user with id '$randomId' not found"))
        }
    }

    @Nested
    inner class StackTests {

        @Test
        fun `should return stacks when user exists`() {
            val request = UserRequest(
                name = "Julia",
                nick = "julia123",
                birthDate = LocalDate.of(1994,6,6),
                stack = listOf(
                    StackItemRequest("Kotlin", 5),
                    StackItemRequest("Spring Boot", 8)
                )
            )

            val result = mockMvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            ).andReturn()

            val id = objectMapper.readTree(result.response.contentAsString).get("id").asText()

            mockMvc.perform(get("/api/users/$id/stacks"))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$", Matchers.hasSize<Any>(2)))
                .andExpect(jsonPath("$[0].name").value("Kotlin"))
                .andExpect(jsonPath("$[0].skill_level").value(5))
                .andExpect(jsonPath("$[1].name").value("Spring Boot"))
                .andExpect(jsonPath("$[1].skill_level").value(8))
        }

        @Test
        fun `should return 404 when user not exist for stacks`() {
            val randomId = UUID.randomUUID()

            mockMvc.perform(get("/api/users/$randomId/stacks"))
                .andExpect(status().isNotFound)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("not_found_exception"))
                .andExpect(jsonPath("$.description").value("User not found"))
                .andExpect(jsonPath("$.details[0]").value("user with id '$randomId' not found"))
        }

    }


    companion object {
        @JvmStatic
        fun invalidCreateRequests() = listOf(
            Arguments.of(
                UserRequest("", "nick", LocalDate.of(1990,1,1), listOf(StackItemRequest("Java",5))),
                "name must not be blank"
            ),
            Arguments.of(
                UserRequest("An", "nick", LocalDate.of(1990,1,1), listOf(StackItemRequest("Java",5))),
                "name size must be between 3 and 255"
            ),
            Arguments.of(
                UserRequest("A".repeat(256), "nick", LocalDate.of(1990,1,1), listOf(StackItemRequest("Java",5))),
                "name size must be between 3 and 255"
            ),
            Arguments.of(
                UserRequest("Ana", "", LocalDate.of(1990,1,1), listOf(StackItemRequest("Java",5))),
                "nick size must be between 1 and 255"
            ),
            Arguments.of(
                UserRequest("Ana", "a".repeat(256), LocalDate.of(1990,1,1), listOf(StackItemRequest("Java",5))),
                "nick size must be between 1 and 255"
            ),
            Arguments.of(
                UserRequest("Ana", "nick", null, listOf(StackItemRequest("Java",5))),
                "birth date must not be null"
            ),
            Arguments.of(
                UserRequest("Ana", "nick", LocalDate.now().plusDays(1), listOf(StackItemRequest("Java",5))),
                "birth date must be a past date"
            ),
            Arguments.of(
                UserRequest("Ana", "nick", LocalDate.of(1990,1,1), null),
                "stack must not be null"
            ),
            Arguments.of(
                UserRequest("Ana", "nick", LocalDate.of(1990,1,1), emptyList()),
                "stack must contain at least one element"
            ),
            Arguments.of(
                UserRequest("Ana", "nick", LocalDate.of(1990,1,1), listOf(StackItemRequest("Java",5), StackItemRequest("Java",7))),
                "stack cannot contain duplicate values"
            ),
            Arguments.of(
                UserRequest("Ana", "nick", LocalDate.of(1990,1,1), listOf(StackItemRequest(null,5))),
                "stack item name must not be null"
            ),
            Arguments.of(
                UserRequest("Ana", "nick", LocalDate.of(1990,1,1), listOf(StackItemRequest(" ",5))),
                "stack item name must not be blank"
            ),
            Arguments.of(
                UserRequest("Ana", "nick", LocalDate.of(1990,1,1), listOf(StackItemRequest("A".repeat(33),5))),
                "stack item name size must be less than or equal to 32"
            ),
            Arguments.of(
                UserRequest("Ana", "nick", LocalDate.of(1990,1,1), listOf(StackItemRequest("Java",null))),
                "stack item skill level must not be null"
            ),
            Arguments.of(
                UserRequest("Ana", "nick", LocalDate.of(1990,1,1), listOf(StackItemRequest("Java",0))),
                "stack item skill level must be between 1 and 10"
            ),
            Arguments.of(
                UserRequest("Ana", "nick", LocalDate.of(1990,1,1), listOf(StackItemRequest("Java",11))),
                "stack item skill level must be between 1 and 10"
            )
        )
    }
}
