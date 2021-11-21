package pl.wat.surveycompanyservice.api

import com.auth0.jwt.exceptions.JWTVerificationException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
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
    fun userAlreadyExistsExceptionHandler(exception: UserAlreadyExistsException) =
        AppException(
            httpStatus = CONFLICT,
            statusCode = CONFLICT.value(),
            errors = listOf(exception.message)
        )

    @ExceptionHandler(RoleNotFoundException::class)
    @ResponseStatus(BAD_REQUEST)
    fun roleNotFoundExceptionHandler(exception: RoleNotFoundException) =
        AppException(
            httpStatus = BAD_REQUEST,
            statusCode = BAD_REQUEST.value(),
            errors = listOf(exception.message)
        )

    @ExceptionHandler(JWTVerificationException::class)
    @ResponseStatus(BAD_REQUEST)
    fun jwtVerificationExceptionHandler(exception: JWTVerificationException) =
        AppException(
            httpStatus = BAD_REQUEST,
            statusCode = BAD_REQUEST.value(),
            errors = listOf(exception.message)
        )

    @ExceptionHandler(AuthenticationException::class)
    @ResponseStatus(UNAUTHORIZED)
    fun authenticationExceptionHandler(exception: AuthenticationException) =
        AppException(
            httpStatus = UNAUTHORIZED,
            statusCode = UNAUTHORIZED.value(),
            errors = listOf(exception.message)
        )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(BAD_REQUEST)
    fun methodArgumentNotValidExceptionHandler(exception: MethodArgumentNotValidException) =
        AppException(
            httpStatus = BAD_REQUEST,
            statusCode = BAD_REQUEST.value(),
            errors = exception.bindingResult.allErrors.map { it.defaultMessage }
        )

    @ExceptionHandler(BadCredentialsException::class)
    @ResponseStatus(UNAUTHORIZED)
    fun badCredentialsExceptionHandler(exception: BadCredentialsException) =
        AppException(
            httpStatus = UNAUTHORIZED,
            statusCode = UNAUTHORIZED.value(),
            errors = listOf(exception.message)
        )
}

data class AppException(
    val httpStatus: HttpStatus,
    val statusCode: Int,
    val errors: List<String?>
)