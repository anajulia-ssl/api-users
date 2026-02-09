package com.estudos.users_api.exception

import com.estudos.users_api.dto.ErrorResponse
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.beans.TypeMismatchException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    // EXCEÇÕES DE NEGÓCIO
    @ExceptionHandler(UserNotFoundException::class)
    fun userNotFound(ex: UserNotFoundException): ResponseEntity<List<ErrorResponse>> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            listOf(ErrorResponse("not_found_exception", ex.message!!))
        )
    }

    @ExceptionHandler(NickAlreadyExistsException::class)
    fun nickAlreadyExists(ex: NickAlreadyExistsException): ResponseEntity<List<ErrorResponse>> {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            listOf(ErrorResponse("conflict_exception", ex.message!!))
        )
    }

    @ExceptionHandler(
        InvalidPaginationException::class,
        InvalidSortException::class,
        InvalidStackException::class
    )
    fun businessError(ex: RuntimeException): ResponseEntity<List<ErrorResponse>> {
        return ResponseEntity.badRequest().body(
            listOf(ErrorResponse("parameter_exception", ex.message!!))
        )
    }

    // JSON / BODY INVÁLIDO
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun invalidJson(ex: HttpMessageNotReadableException): ResponseEntity<List<ErrorResponse>> {

        val message = ex.cause?.message ?: ex.message ?: ""
        log.warn("Invalid JSON: {}", message)

        // campo null em propriedade não-nula
        if (
            message.contains("non-null", true) ||
            message.contains("Cannot map `null` into type", true)
        ) {
            return ResponseEntity.badRequest().body(
                listOf(
                    ErrorResponse(
                        "parameter_exception",
                        "There is a required field that cannot be null"
                    )
                )
            )
        }

        // tipo inválido
        if (message.contains("Cannot deserialize value of type", true)) {
            return ResponseEntity.badRequest().body(
                listOf(
                    ErrorResponse(
                        "parameter_exception",
                        "There is a field with an invalid type in the request body"
                    )
                )
            )
        }

        // json malformado
        if (message.contains("JsonParseException", true)) {
            return ResponseEntity.badRequest().body(
                listOf(
                    ErrorResponse(
                        "parameter_exception",
                        "Request body is malformed"
                    )
                )
            )
        }

        // fallback
        return ResponseEntity.badRequest().body(
            listOf(
                ErrorResponse(
                    "parameter_exception",
                    "Request body could not be processed"
                )
            )
        )
    }

    // @Valid (BODY)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validationError(ex: MethodArgumentNotValidException): ResponseEntity<List<ErrorResponse>> {
        val errors = ex.bindingResult.fieldErrors.map {
            ErrorResponse(
                "validation_exception",
                "${it.field}: ${it.defaultMessage}"
            )
        }
        return ResponseEntity.badRequest().body(errors)
    }

    // QUERY / PATH
    @ExceptionHandler(ConstraintViolationException::class)
    fun constraintViolation(ex: ConstraintViolationException): ResponseEntity<List<ErrorResponse>> {
        val errors = ex.constraintViolations.map {
            ErrorResponse(
                "validation_exception",
                "${it.propertyPath}: ${it.message}"
            )
        }
        return ResponseEntity.badRequest().body(errors)
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun missingParameter(ex: MissingServletRequestParameterException): ResponseEntity<List<ErrorResponse>> {
        return ResponseEntity.badRequest().body(
            listOf(
                ErrorResponse(
                    "parameter_exception",
                    "Missing required parameter '${ex.parameterName}'"
                )
            )
        )
    }

    @ExceptionHandler(
        MethodArgumentTypeMismatchException::class,
        TypeMismatchException::class
    )
    fun typeMismatch(ex: Exception): ResponseEntity<List<ErrorResponse>> {

        val name =
            if (ex is MethodArgumentTypeMismatchException) ex.name else "parameter"

        val type =
            if (ex is MethodArgumentTypeMismatchException)
                ex.requiredType?.simpleName ?: "type"
            else "type"

        return ResponseEntity.badRequest().body(
            listOf(
                ErrorResponse(
                    "parameter_exception",
                    "Parameter '$name' must be of type $type"
                )
            )
        )
    }

    // MÉTODO / CONTENT TYPE
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun methodNotAllowed(): ResponseEntity<List<ErrorResponse>> {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(
            listOf(
                ErrorResponse(
                    "method_not_allowed",
                    "HTTP method not supported"
                )
            )
        )
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun mediaTypeNotSupported(): ResponseEntity<List<ErrorResponse>> {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(
            listOf(
                ErrorResponse(
                    "unsupported_media_type",
                    "Content-Type not supported"
                )
            )
        )
    }

    // GENÉRICO
    @ExceptionHandler(Exception::class)
    fun genericError(ex: Exception): ResponseEntity<List<ErrorResponse>> {
        log.error("Unexpected error", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            listOf(
                ErrorResponse(
                    "internal_exception",
                    "Unexpected error occurred"
                )
            )
        )
    }
}