package com.zycash.beauth.controller.advice

import com.zycash.beauth.dto.Response
import com.zycash.beauth.exception.BadRequestCustomException
import com.zycash.beauth.exception.DataExistException
import com.zycash.beauth.exception.NotFoundException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(
        ex: MethodArgumentNotValidException
    ): ResponseEntity<Response<Any>> {

        val errorList = ex.bindingResult
            .fieldErrors
            .map(FieldError::getDefaultMessage)

        return ResponseEntity(
            mappingError(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.reasonPhrase,
                errorList
            ),
            HttpHeaders(),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralExceptions(
        ex: Exception
    ): ResponseEntity<Response<Any>> {

        val errorList = listOf(ex.message ?: "Unexpected error")

        return ResponseEntity(
            mappingError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
                errorList
            ),
            HttpHeaders(),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeExceptions(
        ex: RuntimeException
    ): ResponseEntity<Response<Any>> {

        val errorList = listOf(ex.message ?: "Runtime error")

        return ResponseEntity(
            mappingError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
                errorList
            ),
            HttpHeaders(),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }

    @ExceptionHandler(DataExistException::class)
    fun handleDataExistException(
        ex: DataExistException
    ): ResponseEntity<Response<Any>> {

        val errors = listOf(ex.message ?: "Data already exists")

        return ResponseEntity(
            mappingError(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.reasonPhrase,
                errors
            ),
            HttpHeaders(),
            HttpStatus.CONFLICT
        )
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(
        ex: NotFoundException
    ): ResponseEntity<Response<Any>> {

        val errors = listOf(ex.message ?: "Data not found")

        return ResponseEntity(
            mappingError(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.reasonPhrase,
                errors
            ),
            HttpHeaders(),
            HttpStatus.NOT_FOUND
        )
    }

    @ExceptionHandler(BadRequestCustomException::class)
    fun handleBadRequestCustomException(
        ex: BadRequestCustomException
    ): ResponseEntity<Response<Any>> {

        val errors = listOf(ex.message ?: "Bad request")

        return ResponseEntity(
            mappingError(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.reasonPhrase,
                errors
            ),
            HttpHeaders(),
            HttpStatus.BAD_REQUEST
        )
    }

    private fun mappingError(
        responseCode: Int,
        responseMessage: String,
        errorList: List<String?>
    ): Response<Any> {
        return Response(
            responseCode = responseCode,
            responseMessage = responseMessage,
            errorList = errorList.filterNotNull()
        )
    }

}
