package com.estudos.users_api.service

import com.estudos.users_api.exception.NickAlreadyExistsException
import com.estudos.users_api.exception.UserNotFoundException
import com.estudos.users_api.model.User
import com.estudos.users_api.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.LocalDate
import java.util.*

class UserServiceTest {

    private val userRepository: UserRepository = mock(UserRepository::class.java)
    private val userService = UserService(userRepository)


    @Test
    fun `should create user when nick is unique`() {
        val user = User(id = UUID.randomUUID(), name = "Test", nick = "test", birthDate = LocalDate.of(1992, 3, 15), stack = mutableListOf())
        `when`(userRepository.findByNickExcludingId(user.nick!!, null)).thenReturn(null)
        `when`(userRepository.save(user)).thenReturn(user)

        val result = userService.create(user)

        assertEquals(user, result)
        verify(userRepository).save(user)
    }

    @Test
    fun `should throw NickAlreadyExistsException when nick already exists`() {
        val user = User(id = UUID.randomUUID(), name = "Test", nick = "test", birthDate = LocalDate.of(1995, 7, 20), stack = mutableListOf())
        `when`(userRepository.findByNickExcludingId(user.nick!!, null)).thenReturn(user)

        assertThrows<NickAlreadyExistsException> {
            userService.create(user)
        }
    }

    @Test
    fun `should return list of users with pagination`() {
        val user1 = User(id = UUID.randomUUID(), name = "Test 1", nick = "test1", birthDate = LocalDate.of(1990, 1, 1), stack = mutableListOf())
        val user2 = User(id = UUID.randomUUID(), name = "Test 2", nick = "test2", birthDate = LocalDate.of(1993, 5, 10), stack = mutableListOf())

        val pageable = PageRequest.of(0, 2, Sort.by("name").ascending())
        val page = org.springframework.data.domain.PageImpl(listOf(user1, user2), pageable, 2)

        `when`(userRepository.findAll(pageable)).thenReturn(page)

        val result = userService.findAll(pageable)

        assertEquals(2, result.content.size)
        assertEquals("Test 1", result.content[0].name)
        assertEquals("Test 2", result.content[1].name)
    }

    @Test
    fun `should return empty list when no users exist`() {
        val pageable = PageRequest.of(0, 5, Sort.by("name").ascending())
        val page = org.springframework.data.domain.PageImpl(emptyList<User>(), pageable, 0)

        `when`(userRepository.findAll(pageable)).thenReturn(page)

        val result = userService.findAll(pageable)

        assertTrue(result.content.isEmpty())
    }

    @Test
    fun `should find user by id`() {
        val user = User(id = UUID.randomUUID(), name = "Test", nick = "test", birthDate = LocalDate.of(1988, 11, 30), stack = mutableListOf())
        `when`(userRepository.findById(user.id!!)).thenReturn(Optional.of(user))

        val result = userService.findById(user.id!!)

        assertEquals(user, result)
    }

    @Test
    fun `should throw UserNotFoundException when user not found by id`() {
        val id = UUID.randomUUID()
        `when`(userRepository.findById(id)).thenReturn(Optional.empty())

        assertThrows<UserNotFoundException> {
            userService.findById(id)
        }
    }

    @Test
    fun `should update user when exists`() {
        val existing = User(id = UUID.randomUUID(), name = "Test", nick = "test", birthDate = LocalDate.of(1991, 4, 12), stack = mutableListOf())
        val updated = User(id = existing.id, name = "Test updated", nick = "test", birthDate = LocalDate.of(1991, 4, 12), stack = mutableListOf())

        `when`(userRepository.findById(existing.id!!)).thenReturn(Optional.of(existing))
        `when`(userRepository.findByNickExcludingId(updated.nick!!, existing.id)).thenReturn(null)
        `when`(userRepository.save(existing)).thenReturn(updated)

        val result = userService.update(existing.id!!, updated)

        assertEquals("Test updated", result.name)
        verify(userRepository).save(existing)
    }

    @Test
    fun `should throw UserNotFoundException when updating non-existing user`() {
        val id = UUID.randomUUID()
        val updated = User(id = UUID.randomUUID(), name = "Test", nick = "test", birthDate = LocalDate.of(1996, 9, 5), stack = mutableListOf())

        `when`(userRepository.findById(id)).thenReturn(Optional.empty())

        assertThrows<UserNotFoundException> {
            userService.update(id, updated)
        }
    }

    @Test
    fun `should delete user when exists`() {
        val id = UUID.randomUUID()
        `when`(userRepository.existsById(id)).thenReturn(true)

        userService.deleteById(id)

        verify(userRepository).deleteById(id)
    }

    @Test
    fun `should throw UserNotFoundException when deleting non-existing user`() {
        val id = UUID.randomUUID()
        `when`(userRepository.existsById(id)).thenReturn(false)

        assertThrows<UserNotFoundException> {
            userService.deleteById(id)
        }
    }

    @Test
    fun `should return stacks when user exists`() {
        val userId = UUID.randomUUID()
        val user = User(
            id = userId,
            name = "Test",
            nick = "test",
            birthDate = LocalDate.of(1994, 6, 6),
            stack = mutableListOf(
                com.estudos.users_api.model.Stack(name = "Kotlin", level = 5),
                com.estudos.users_api.model.Stack(name = "Spring Boot", level = 8)
            )
        )

        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))

        val result = userService.getUserStacks(userId)

        assertNotNull(result)
        assertEquals(2, result!!.size)
        assertEquals("Kotlin", result[0].name)
        assertEquals(5, result[0].level)
        assertEquals("Spring Boot", result[1].name)
        assertEquals(8, result[1].level)
    }

    @Test
    fun `should throw UserNotFoundException when getting stacks of non-existing user`() {
        val userId = UUID.randomUUID()
        `when`(userRepository.findById(userId)).thenReturn(Optional.empty())

        assertThrows<UserNotFoundException> {
            userService.getUserStacks(userId)
        }
    }
}
