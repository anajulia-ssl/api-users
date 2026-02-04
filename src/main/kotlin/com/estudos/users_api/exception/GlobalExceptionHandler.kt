package com.estudos.users_api.exception

import com.estudos.users_api.dto.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(InvalidPaginationException::class)
    fun handlePagination(ex: InvalidPaginationException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            error = "invalid_pagination",
            description = "Invalid pagination parameters",
            details = listOf(ex.message ?: "")
        )
        return ResponseEntity.badRequest().body(error)
    }

    @ExceptionHandler(InvalidSortException::class)
    fun handleSort(ex: InvalidSortException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            error = "invalid_sort",
            description = "Invalid sorting parameters",
            details = listOf(ex.message ?: "")
        )
        return ResponseEntity.badRequest().body(error)
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            error = "not_found_exception",
            description = "User not found",
            details = listOf(ex.message ?: "")
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    @ExceptionHandler(NickAlreadyExistsException::class)
    fun handleNickAlreadyExists(ex: NickAlreadyExistsException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            error = "conflict_exception",
            description = "Nick already exists",
            details = listOf(ex.message ?: "")
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error)
    }

    @ExceptionHandler(InvalidStackException::class)
    fun handleInvalidStack(ex: InvalidStackException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            error = "business_exception",
            description = "Invalid stack",
            details = listOf(ex.message ?: "")
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val details = ex.bindingResult.fieldErrors.map { it.defaultMessage ?: "validation error" }
        val error = ErrorResponse(
            error = "validation_exception",
            description = "Invalid request",
            details = details
        )
        return ResponseEntity.badRequest().body(error)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            error = "internal_exception",
            description = "Unexpected error occurred",
            details = listOf(ex.message ?: "")
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }
}
