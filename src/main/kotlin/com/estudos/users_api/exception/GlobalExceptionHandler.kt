package com.estudos.users_api.exception

import com.estudos.users_api.dto.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException): ResponseEntity<List<ErrorResponse>> {
        val error = ErrorResponse(
            error = "not_found_exception",
            description = ex.message ?: "User not found"
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(listOf(error))
    }

    @ExceptionHandler(NickAlreadyExistsException::class)
    fun handleNickAlreadyExists(ex: NickAlreadyExistsException): ResponseEntity<List<ErrorResponse>> {
        val error = ErrorResponse(
            error = "conflict_exception",
            description = ex.message ?: "Nick already exists"
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(listOf(error))
    }

    @ExceptionHandler(InvalidStackException::class)
    fun handleInvalidStack(ex: InvalidStackException): ResponseEntity<List<ErrorResponse>> {
        val error = ErrorResponse(
            error = "business_exception",
            description = ex.message ?: "Invalid stack"
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(listOf(error))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<List<ErrorResponse>> {
        val errors = ex.bindingResult.fieldErrors.map {
            ErrorResponse(
                error = "validation_exception",
                description = "${it.defaultMessage}"
            )
        }
        return ResponseEntity.badRequest().body(errors)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<List<ErrorResponse>> {
        val error = ErrorResponse(
            error = "internal_exception",
            description = "Unexpected error: ${ex.message}"
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(listOf(error))
    }
}
