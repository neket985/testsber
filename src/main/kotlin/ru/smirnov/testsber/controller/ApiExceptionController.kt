package ru.smirnov.testsber.controller

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ResponseStatusException
import ru.smirnov.testsber.entity.ApiException


@ControllerAdvice()
class ApiExceptionController {
    private val logger = LoggerFactory.getLogger(ApiExceptionController::class.java)

    @ExceptionHandler(Throwable::class)
    fun anyException(e: Throwable) =
            returnError(e, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validationException(e: MethodArgumentNotValidException) =
            returnError(e, HttpStatus.BAD_REQUEST, e.message)

    @ExceptionHandler(ResponseStatusException::class)
    fun validationException(e: ResponseStatusException) =
            returnError(e, e.status, e.reason ?: "Validation error")

    @ExceptionHandler(WebExchangeBindException::class)
    fun validationException(e: WebExchangeBindException) =
            returnError(e, e.status, e.reason + ". " + e.methodParameter)

    @ExceptionHandler(IllegalArgumentException::class)
    fun validationException(e: IllegalArgumentException) =
            returnError(e, HttpStatus.BAD_REQUEST, "Bad request")

    @ExceptionHandler(ApiException::class)
    fun apiException(e: ApiException) =
            returnError(e, HttpStatus.valueOf(e.code), e.desc)


    private fun returnError(e: Throwable, status: HttpStatus, message: String): ResponseEntity<Any?> {
        logger.error("Api exception", e)
        return ResponseEntity(message, status)
    }
}