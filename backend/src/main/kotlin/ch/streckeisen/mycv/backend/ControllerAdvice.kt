package ch.streckeisen.mycv.backend

import ch.streckeisen.mycv.backend.exceptions.ValidationException
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.security.SignatureException
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AccountStatusException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ControllerAdvice : ResponseEntityExceptionHandler() {
    @ExceptionHandler
    fun handleValidationError(ex: ValidationException): ResponseEntity<ErrorDto> {
        return ResponseEntity.badRequest().body(ErrorDto(ex.message!!, ex.errors))
    }

    @ExceptionHandler
    fun handleDatabaseError(ex: DataAccessException): ResponseEntity<ErrorDto> {
        return ResponseEntity.badRequest()
            .body(ErrorDto("A database error occurred. Please contact the administrator"))
    }

    @ExceptionHandler
    fun handleExpiredJwtException(ex: ExpiredJwtException): ResponseEntity<ErrorDto> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorDto("Expired JWT token"))
    }

    @ExceptionHandler
    fun handleBadCredentialsException(ex: BadCredentialsException): ResponseEntity<ErrorDto> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorDto("Username and/or password are incorrect"))
    }

    @ExceptionHandler
    fun handleAccountStatusException(ex: AccountStatusException): ResponseEntity<ErrorDto> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorDto("Account is locked"))
    }

    @ExceptionHandler
    fun handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity<ErrorDto> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorDto("You are not authorized to access this resource"))
    }

    @ExceptionHandler
    fun handleSignatureException(ex: SignatureException): ResponseEntity<ErrorDto> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorDto("Invalid JWT signature"))
    }
}