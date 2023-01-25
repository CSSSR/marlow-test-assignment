package com.assignment.atm

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class ServiceControllerAdvice : ResponseEntityExceptionHandler() {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFoundException(exception: NotFoundException): ProblemDetail {
        return ProblemDetail
            .forStatusAndDetail(HttpStatus.NOT_FOUND, exception.message ?: "")
            .apply { title = "Entity not found" }
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInsufficientBalanceException(exception: InsufficientBalanceException): ProblemDetail {
        return ProblemDetail
            .forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.message ?: "")
            .apply { title = "Insufficient balance" }
    }
}