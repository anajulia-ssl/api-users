package com.estudos.users_api

import com.estudos.users_api.dto.UserRequest
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Test
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
class UserIntegrationTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper
) {

    // POST
    @Test
    fun `should create user when valid request`() {
        val request = UserRequest(
            name = "Maria Silva",
            nick = "maria123",
            birthDate = LocalDate.of(1990, 1, 1),
            stack = listOf("Java", "Kotlin")
        )

        mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("Maria Silva"))
    }

    @Test
    fun `should return 404 when invalid request`() {
        val request = UserRequest(
            name = "",
            nick = "nick",
            birthDate = LocalDate.of(1990, 1, 1),
            stack = listOf("Java")
        )

        mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return 409 when nick already exists`() {
        val request = UserRequest(
            name = "João",
            nick = "duplicado",
            birthDate = LocalDate.of(1990, 1, 1),
            stack = listOf("Spring")
        )

        mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isCreated)

        mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isConflict)
    }

    // GET
    @Test
    fun `should return 200 with a list of users`() {
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk)
    }

    @Test
    fun `should return 200 when user exist by id`() {
        val request = UserRequest(
            name = "Carlos",
            nick = "carlos123",
            birthDate = LocalDate.of(1985, 5, 5),
            stack = listOf("Oracle")
        )

        val result = mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val id = objectMapper.readTree(result.response.contentAsString).get("id").asText()

        mockMvc.perform(get("/api/users/$id"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Carlos"))
    }

    @Test
    fun `should return 404 when user not exist by id`() {
        mockMvc.perform(get("/api/users/${UUID.randomUUID()}"))
            .andExpect(status().isNotFound)
    }

    // PUT
    @Test
    fun `should update user when valid request`() {
        val request = UserRequest(
            name = "Ana",
            nick = "ana123",
            birthDate = LocalDate.of(1992, 2, 2),
            stack = listOf("Java")
        )

        val result = mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val id = objectMapper.readTree(result.response.contentAsString).get("id").asText()

        val updateRequest = UserRequest(
            name = "Ana Paula",
            nick = "ana123",
            birthDate = LocalDate.of(1992, 2, 2),
            stack = listOf("Java", "Spring")
        )

        mockMvc.perform(
            put("/api/users/$id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Ana Paula"))
    }

    @Test
    fun `should return 404 when updating not existing user`() {
        val updateRequest = UserRequest(
            name = "Teste",
            nick = "teste",
            birthDate = LocalDate.of(1990, 1, 1),
            stack = listOf("Kotlin")
        )

        mockMvc.perform(
            put("/api/users/${UUID.randomUUID()}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should return 400 when updating with invalid request`() {
        val request = UserRequest(
            name = "Pedro",
            nick = "pedro123",
            birthDate = LocalDate.of(1991, 3, 3),
            stack = listOf("Java")
        )

        val result = mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val id = objectMapper.readTree(result.response.contentAsString).get("id").asText()

        val invalidUpdate = UserRequest(
            name = "",
            nick = "pedro123",
            birthDate = LocalDate.of(1991, 3, 3),
            stack = listOf("Java")
        )

        mockMvc.perform(
            put("/api/users/$id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUpdate))
        )
            .andExpect(status().isBadRequest)
    }

    // DELETE
    @Test
    fun `should delete user when exists`() {
        val request = UserRequest(
            name = "Lucas",
            nick = "lucas123",
            birthDate = LocalDate.of(1993, 4, 4),
            stack = listOf("Spring")
        )

        val result = mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val id = objectMapper.readTree(result.response.contentAsString).get("id").asText()

        mockMvc.perform(delete("/api/users/$id"))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `should return 404 when deleting not existing user`() {
        mockMvc.perform(delete("/api/users/${UUID.randomUUID()}"))
            .andExpect(status().isNotFound)
    }
}
