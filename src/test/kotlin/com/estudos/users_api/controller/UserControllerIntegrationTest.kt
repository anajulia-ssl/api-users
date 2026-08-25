package com.estudos.users_api.controller

import com.estudos.users_api.dto.StackRequest
import com.estudos.users_api.dto.UserRequest
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
import org.springframework.transaction.annotation.Transactional
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
            val userRequest = UserRequest(
                name = "User Test",
                nick = "user123",
                birthDate = LocalDate.of(1995, 1, 1),
                stack = listOf(StackRequest("Kotlin", 5))
            )

            mockMvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").isNotEmpty)
                .andExpect(jsonPath("$.name").value("User Test"))
                .andExpect(jsonPath("$.nick").value("user123"))
                .andExpect(jsonPath("$.birth_date").value("1995-01-01"))
                .andExpect(jsonPath("$.stack[0].name").value("Kotlin"))
                .andExpect(jsonPath("$.stack[0].level").value(5))
        }

        @Test
        fun `should create user without nick`() {
            val userRequest = UserRequest(
                name = "User Test",
                nick = null,
                birthDate = LocalDate.of(1995, 1, 1),
                stack = listOf(StackRequest("Kotlin", 5))
            )

            mockMvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").isNotEmpty)
                .andExpect(jsonPath("$.name").value("User Test"))
                .andExpect(jsonPath("$.nick").doesNotExist())
                .andExpect(jsonPath("$.birth_date").value("1995-01-01"))
                .andExpect(jsonPath("$.stack[0].name").value("Kotlin"))
                .andExpect(jsonPath("$.stack[0].level").value(5))
        }

        @Test
        fun `should return 409 when nick already exists`() {
            val firstUserRequest = UserRequest(
                name = "User 1",
                nick = "duplicatedNick",
                birthDate = LocalDate.of(1990, 1, 1),
                stack = listOf(StackRequest("Spring", 5))
            )
            val duplicateNickRequest = UserRequest(
                name = "User 2",
                nick = "duplicatedNick",
                birthDate = LocalDate.of(1992, 2, 2),
                stack = listOf(StackRequest("Kotlin", 7))
            )

            mockMvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(firstUserRequest))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").isNotEmpty)
                .andExpect(jsonPath("$.name").value("User 1"))
                .andExpect(jsonPath("$.nick").value("duplicatedNick"))
                .andExpect(jsonPath("$.birth_date").value("1990-01-01"))
                .andExpect(jsonPath("$.stack[0].name").value("Spring"))
                .andExpect(jsonPath("$.stack[0].level").value(5))

            mockMvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(duplicateNickRequest))
            )
                .andExpect(status().isConflict)
                .andExpect(jsonPath("$[*].error").value(Matchers.hasItem("conflict_exception")))
            .andExpect(jsonPath("$[*].description").value(Matchers.hasItem("Nick '${duplicateNickRequest.nick}' already exists")))
        }

        @ParameterizedTest
        @MethodSource("com.estudos.users_api.controller.UserControllerIntegrationTest#invalidCreateRequests")
        fun `should return 400 with error response when invalid create request`(
            userRequest: UserRequest,
            expectedDescription: String
        ) {
            mockMvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$[*].error").value(Matchers.hasItem("validation_exception")))
                .andExpect(jsonPath("$[*].description").value(Matchers.hasItem(expectedDescription)))
        }

        @ParameterizedTest
        @CsvSource("1", "10")
        fun `should accept stack level at limits`(level: Int) {
            val userRequest = UserRequest(
                name = "Limit User",
                nick = "limit$level",
                birthDate = LocalDate.of(1990, 1, 1),
                stack = listOf(StackRequest("Java", level))
            )
            mockMvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.stack[0].level").value(level))
        }

        @Test
        fun `should reject duplicate stack items case insensitive`() {
            val userRequest = UserRequest(
                name = "User Test",
                nick = "userStackCase",
                birthDate = LocalDate.of(1990, 1, 1),
                stack = listOf(StackRequest("Java", 5), StackRequest("java", 6))
            )
            mockMvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$[*].error").value(Matchers.hasItem("validation_exception")))
                .andExpect(jsonPath("$[*].description").value(Matchers.hasItem("Stack cannot contain duplicate values")))
        }
    }

    @Nested
    inner class ReadTests {

        @Test
        fun `should return 200 with empty list when no users exist`() {
            mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items").isArray)
                .andExpect(jsonPath("$.items", Matchers.hasSize<Any>(0)))
        }

        @Test
        fun `should return 200 with list of users when users exist`() {
            val userRequest = UserRequest(
                name = "User Test",
                nick = "user123",
                birthDate = LocalDate.of(1995, 1, 1),
                stack = listOf(StackRequest("Kotlin", 5))
            )

            mockMvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest))
            )
                .andExpect(status().isCreated)

            mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items").isArray)
                .andExpect(jsonPath("$.items", Matchers.hasSize<Any>(1)))
                .andExpect(jsonPath("$.items[0].id").isNotEmpty)
                .andExpect(jsonPath("$.items[0].name").value("User Test"))
                .andExpect(jsonPath("$.items[0].nick").value("user123"))
                .andExpect(jsonPath("$.items[0].birth_date").value("1995-01-01"))
                .andExpect(jsonPath("$.items[0].stack[0].name").value("Kotlin"))
                .andExpect(jsonPath("$.items[0].stack[0].level").value(5))
        }

        @Test
        fun `should return 200 when user exists by id`() {
            val userRequest = UserRequest(
                name = "User Test",
                nick = "userById",
                birthDate = LocalDate.of(1985, 5, 5),
                stack = listOf(StackRequest("Oracle", 5))
            )

            val createdUserResult = mockMvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest))
            ).andReturn()

            val userId = objectMapper.readTree(createdUserResult.response.contentAsString).get("id").asText()

            mockMvc.perform(get("/api/users/$userId"))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("User Test"))
                .andExpect(jsonPath("$.nick").value("userById"))
                .andExpect(jsonPath("$.birth_date").value("1985-05-05"))
                .andExpect(jsonPath("$.stack[0].name").value("Oracle"))
                .andExpect(jsonPath("$.stack[0].level").value(5))
        }

        @Test
        fun `should return 404 when user not exist by id`() {
            val randomId = UUID.randomUUID()

            mockMvc.perform(get("/api/users/$randomId"))
                .andExpect(status().isNotFound)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[*].error").value(Matchers.hasItem("not_found_exception")))
                .andExpect(jsonPath("$[*].description").value(Matchers.hasItem("User with id '$randomId' not found")))
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
                .andExpect(jsonPath("$[*].error").value(Matchers.hasItem(expectedError)))
                .andExpect(jsonPath("$[*].description").value(Matchers.hasItem(expectedDescription)))
        }

        @Test
        fun `should sort users by name asc`() {
            val firstUserRequest = UserRequest(
                name = "User 1",
                nick = "user1",
                birthDate = LocalDate.of(1990, 1, 1),
                stack = listOf(StackRequest("Java", 5))
            )
            val secondUserRequest = UserRequest(
                name = "User 2",
                nick = "user2",
                birthDate = LocalDate.of(1990, 1, 1),
                stack = listOf(StackRequest("Java", 5))
            )

            mockMvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(firstUserRequest))
            )
                .andExpect(status().isCreated)

            mockMvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(secondUserRequest))
            )
                .andExpect(status().isCreated)

            mockMvc.perform(get("/api/users?offset=0&limit=10&sort=name:asc"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.items[0].name").value("User 1"))
                .andExpect(jsonPath("$.items[1].name").value("User 2"))
        }
    }

    @Nested
    inner class UpdateTests {

        @Test
        fun `should update user when valid request`() {
            val userRequest = UserRequest(
                name = "User Test",
                nick = "userToUpdate",
                birthDate = LocalDate.of(1992, 2, 2),
                stack = listOf(StackRequest("Java", 5))
            )

            val createdUserResult = mockMvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest))
            ).andReturn()

            val userId = objectMapper.readTree(createdUserResult.response.contentAsString).get("id").asText()

            val updateUserRequest = UserRequest(
                name = "User Updated",
                nick = "userToUpdate",
                birthDate = LocalDate.of(1992, 2, 2),
                stack = listOf(StackRequest("Spring", 7))
            )

            mockMvc.perform(
                put("/api/users/$userId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateUserRequest))
            )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("User Updated"))
                .andExpect(jsonPath("$.nick").value("userToUpdate"))
                .andExpect(jsonPath("$.birth_date").value("1992-02-02"))
                .andExpect(jsonPath("$.stack[0].name").value("Spring"))
                .andExpect(jsonPath("$.stack[0].level").value(7))
        }

        @Test
        fun `should return 404 when updating not existing user`() {
            val updateUserRequest = UserRequest(
                name = "User Test",
                nick = "user123",
                birthDate = LocalDate.of(1990, 1, 1),
                stack = listOf(StackRequest("Kotlin", 5))
            )

            val randomId = UUID.randomUUID()

            mockMvc.perform(
                put("/api/users/$randomId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateUserRequest))
            )
                .andExpect(status().isNotFound)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[*].error").value(Matchers.hasItem("not_found_exception")))
                .andExpect(jsonPath("$[*].description").value(Matchers.hasItem("User with id '$randomId' not found")))
        }

        @Test
        fun `should return 409 when updating with duplicate nick`() {
            val firstUserRequest = UserRequest(
                name = "User 1",
                nick = "userNick1",
                birthDate = LocalDate.of(1990, 1, 1),
                stack = listOf(StackRequest("Java", 5))
            )
            val secondUserRequest = UserRequest(
                name = "User 2",
                nick = "userNick2",
                birthDate = LocalDate.of(1991, 1, 1),
                stack = listOf(StackRequest("Spring", 5))
            )

            val createdFirstUserResult = mockMvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(firstUserRequest))
            ).andReturn()
            val firstUserId = objectMapper.readTree(createdFirstUserResult.response.contentAsString).get("id").asText()

            mockMvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(secondUserRequest))
            )
                .andExpect(status().isCreated)

            val updateFirstUserRequest = UserRequest(
                name = "User 1 Updated",
                nick = "userNick2",
                birthDate = LocalDate.of(1990, 1, 1),
                stack = listOf(StackRequest("Java", 5))
            )

            mockMvc.perform(
                put("/api/users/$firstUserId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateFirstUserRequest))
            )
                .andExpect(status().isConflict)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[*].error").value(Matchers.hasItem("conflict_exception")))
                .andExpect(jsonPath("$[*].description").value(Matchers.hasItem("Nick '${secondUserRequest.nick}' already exists")))
        }

        @Test
        fun `should update nick to null when allowed`() {
            val createUserRequest = UserRequest(
                name = "User Nick Null",
                nick = "nick",
                birthDate = LocalDate.of(1990, 1, 1),
                stack = listOf(StackRequest("Java", 5))
            )
            val createdUserResult = mockMvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createUserRequest))
            ).andReturn()
            val userId = objectMapper.readTree(createdUserResult.response.contentAsString).get("id").asText()

            val updateUserRequest = UserRequest(
                name = "User Nick Null",
                nick = null,
                birthDate = LocalDate.of(1990, 1, 1),
                stack = listOf(StackRequest("Java", 5))
            )
            mockMvc.perform(
                put("/api/users/$userId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateUserRequest))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.nick").doesNotExist())
        }

        @ParameterizedTest
        @MethodSource("com.estudos.users_api.controller.UserControllerIntegrationTest#invalidCreateRequests")
        fun `should return 400 when invalid update request`(
            invalidUpdateRequest: UserRequest,
            expectedDescription: String
        ) {
            val validUserRequest = UserRequest(
                name = "Valid User",
                nick = "validNick",
                birthDate = LocalDate.of(1991, 3, 3),
                stack = listOf(StackRequest("Java", 5))
            )

            val createdUserResult = mockMvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUserRequest))
            ).andReturn()

            val userId = objectMapper.readTree(createdUserResult.response.contentAsString).get("id").asText()

            mockMvc.perform(
                put("/api/users/$userId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidUpdateRequest))
            )
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[*].error").value(Matchers.hasItem("validation_exception")))
                .andExpect(jsonPath("$[*].description").value(Matchers.hasItem(expectedDescription)))
        }
    }

    @Nested
    inner class DeleteTests {

        @Test
        fun `should delete user when exists`() {
            val userRequest = UserRequest(
                name = "User Delete",
                nick = "toDelete",
                birthDate = LocalDate.of(1993, 4, 4),
                stack = listOf(StackRequest("Spring", 5))
            )

            val createdUserResult = mockMvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest))
            ).andReturn()

            val userId = objectMapper.readTree(createdUserResult.response.contentAsString).get("id").asText()

            mockMvc.perform(delete("/api/users/$userId"))
                .andExpect(status().isNoContent)
                .andExpect(content().string(""))
        }

        @Test
        fun `should return 404 when deleting not existing user`() {
            val randomId = UUID.randomUUID()

            mockMvc.perform(delete("/api/users/$randomId"))
                .andExpect(status().isNotFound)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[*].error").value(Matchers.hasItem("not_found_exception")))
                .andExpect(jsonPath("$[*].description").value(Matchers.hasItem("User with id '$randomId' not found")))
        }
    }

    @Nested
    inner class StackTests {

        @Test
        fun `should return stacks when user exists`() {
            val userRequest = UserRequest(
                name = "User Stacks",
                nick = "userStacks",
                birthDate = LocalDate.of(1994, 6, 6),
                stack = listOf(
                    StackRequest("Kotlin", 5),
                    StackRequest("Spring Boot", 8)
                )
            )

            val createdUserResult = mockMvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest))
            ).andReturn()

            val userId = objectMapper.readTree(createdUserResult.response.contentAsString).get("id").asText()

            mockMvc.perform(get("/api/users/$userId/stacks"))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$", Matchers.hasSize<Any>(2)))
                .andExpect(jsonPath("$[0].name").value("Kotlin"))
                .andExpect(jsonPath("$[0].level").value(5))
                .andExpect(jsonPath("$[1].name").value("Spring Boot"))
                .andExpect(jsonPath("$[1].level").value(8))
        }

        @Test
        fun `should return 404 when user not exist for stacks`() {
            val randomId = UUID.randomUUID()

            mockMvc.perform(get("/api/users/$randomId/stacks"))
                .andExpect(status().isNotFound)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[*].error").value(Matchers.hasItem("not_found_exception")))
                .andExpect(jsonPath("$[*].description").value(Matchers.hasItem("User with id '$randomId' not found")))
        }
    }

    companion object {
        @JvmStatic
        fun invalidCreateRequests() = listOf(
            Arguments.of(
                UserRequest("", "test", LocalDate.of(1990, 1, 1), listOf(StackRequest("Java", 5))),
                "Name must not be blank"
            ),
            Arguments.of(
                UserRequest("Te", "test", LocalDate.of(1990, 1, 1), listOf(StackRequest("Java", 5))),
                "Name size must be between 3 and 255"
            ),
            Arguments.of(
                UserRequest("T".repeat(256), "test", LocalDate.of(1990, 1, 1), listOf(StackRequest("Java", 5))),
                "Name size must be between 3 and 255"
            ),
            Arguments.of(
                UserRequest("Test", "", LocalDate.of(1990, 1, 1), listOf(StackRequest("Java", 5))),
                "Nick size must be between 1 and 255"
            ),
            Arguments.of(
                UserRequest("Test", "t".repeat(256), LocalDate.of(1990, 1, 1), listOf(StackRequest("Java", 5))),
                "Nick size must be between 1 and 255"
            ),
//            Arguments.of(
//                UserRequest("Test", "test", null, listOf(StackRequest("Java", 5))),
//                "Birth date must not be null"
//            ),
            Arguments.of(
                UserRequest("Test", "test", LocalDate.now().plusDays(1), listOf(StackRequest("Java", 5))),
                "Birth date must be a past date"
            ),
//            Arguments.of(
//                UserRequest("Test", "test", LocalDate.of(1990, 1, 1), null),
//                "Stack must not be null"
//            ),
            Arguments.of(
                UserRequest("Test", "test", LocalDate.of(1990, 1, 1), emptyList()),
                "Stack must contain at least 1 element"
            ),
            Arguments.of(
                UserRequest("Test", "test", LocalDate.of(1990, 1, 1), listOf(StackRequest("Java", 5), StackRequest("Java", 7))),
                "Stack cannot contain duplicate values"
            ),
//            Arguments.of(
//                UserRequest("Test", "test", LocalDate.of(1990, 1, 1), listOf(StackRequest(null, 5))),
//                "Stack item name must not be null"
//            ),
            Arguments.of(
                UserRequest("Test", "test", LocalDate.of(1990, 1, 1), listOf(StackRequest(" ", 5))),
                "Stack item name must not be blank"
            ),
            Arguments.of(
                UserRequest("Test", "test", LocalDate.of(1990, 1, 1), listOf(StackRequest("A".repeat(33), 5))),
                "Stack item name size must be less than or equal to 32"
            ),
//            Arguments.of(
//                UserRequest("Test", "test", LocalDate.of(1990, 1, 1), listOf(StackRequest("Java", null))),
//                "Stack item level must not be null"
//            ),
            Arguments.of(
                UserRequest("Test", "test", LocalDate.of(1990, 1, 1), listOf(StackRequest("Java", 0))),
                "Stack item level must be between 1 and 10"
            ),
            Arguments.of(
                UserRequest("Test", "test", LocalDate.of(1990, 1, 1), listOf(StackRequest("Java", 11))),
                "Stack item level must be between 1 and 10"
            )
        )
    }
}