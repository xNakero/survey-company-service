package pl.wat.surveycompanyservice.api

import com.auth0.jwt.exceptions.JWTVerificationException
import org.springframework.http.HttpStatus.*
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import pl.wat.surveycompanyservice.domain.user.RoleNotFoundException
import pl.wat.surveycompanyservice.domain.user.UserAlreadyExistsException
import javax.naming.AuthenticationException

@RestControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException::class)
    @ResponseStatus(CONFLICT)
    fun conflictResponseExceptionHandler(exception: RuntimeException) =
        AppException(
            statusCode = CONFLICT.toString(),
            message = exception.message
        )

    @ExceptionHandler(RoleNotFoundException::class, JWTVerificationException::class)
    @ResponseStatus(BAD_REQUEST)
    fun badRequestResponseExceptionHandler(exception: RuntimeException) =
        AppException(
            statusCode = BAD_REQUEST.toString(),
            message = exception.message
        )

    @ExceptionHandler(AuthenticationException::class)
    @ResponseStatus(UNAUTHORIZED)
    fun unauthorizedResponseExceptionHandler(exception: RuntimeException) =
        AppException(
            statusCode = UNAUTHORIZED.toString(),
            message =  exception.message
        )
}

data class AppException(
    val statusCode: String?,
    val message: String?
)