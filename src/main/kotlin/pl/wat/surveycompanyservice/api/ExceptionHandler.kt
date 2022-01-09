package pl.wat.surveycompanyservice.api

import com.auth0.jwt.exceptions.JWTVerificationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import pl.wat.surveycompanyservice.domain.survey.NoEligibleParticipantsException
import pl.wat.surveycompanyservice.domain.surveyparticipation.AlreadyParticipatesInOtherSurveyException
import pl.wat.surveycompanyservice.domain.surveyparticipation.NoCompletionCodeException
import pl.wat.surveycompanyservice.domain.surveyparticipation.NoFreeSpotsException
import pl.wat.surveycompanyservice.domain.surveyparticipation.NoSurveyFoundException
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipationNotInProgressException
import pl.wat.surveycompanyservice.domain.surveyparticipation.UnqualifiedParticipantException
import pl.wat.surveycompanyservice.domain.surveyparticipation.WrongParticipantException
import pl.wat.surveycompanyservice.domain.user.RoleNotFoundException
import pl.wat.surveycompanyservice.domain.user.UserAlreadyExistsException
import pl.wat.surveycompanyservice.infrastructure.repository.IndexingErrorException
import pl.wat.surveycompanyservice.infrastructure.repository.NoSurveyForResearcherException
import pl.wat.surveycompanyservice.infrastructure.repository.UpdatingErrorException

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

    @ExceptionHandler(IndexingErrorException::class)
    @ResponseStatus(FORBIDDEN)
    fun indexingErrorExceptionHandler(exception: IndexingErrorException) =
        AppException(
            httpStatus = INTERNAL_SERVER_ERROR,
            statusCode = INTERNAL_SERVER_ERROR.value(),
            errors = listOf(exception.message)
        )

    @ExceptionHandler(UpdatingErrorException::class)
    @ResponseStatus(FORBIDDEN)
    fun updatingErrorExceptionHandler(exception: UpdatingErrorException) =
        AppException(
            httpStatus = INTERNAL_SERVER_ERROR,
            statusCode = INTERNAL_SERVER_ERROR.value(),
            errors = listOf(exception.message)
        )

    @ExceptionHandler(NoEligibleParticipantsException::class)
    @ResponseStatus(BAD_REQUEST)
    fun noEligibleParticipantsExceptionHandler(exception: NoEligibleParticipantsException) =
        AppException(
            httpStatus = BAD_REQUEST,
            statusCode = BAD_REQUEST.value(),
            errors = listOf(exception.message)
        )

    @ExceptionHandler(DuplicateKeyException::class)
    @ResponseStatus(CONFLICT)
    fun duplicateKeyExceptionHandler(exception: DuplicateKeyException) =
        AppException(
            httpStatus = CONFLICT,
            statusCode = CONFLICT.value(),
            errors = listOf(exception.message)
        )

    @ExceptionHandler(NoFreeSpotsException::class)
    @ResponseStatus(CONFLICT)
    fun noFreeSpotsExceptionHandler(exception: NoFreeSpotsException) =
        AppException(
            httpStatus = CONFLICT,
            statusCode = CONFLICT.value(),
            errors = listOf(exception.message)
        )

    @ExceptionHandler(SurveyParticipationNotInProgressException::class)
    @ResponseStatus(CONFLICT)
    fun surveyParticipationNotInProgressExceptionHandler(exception: SurveyParticipationNotInProgressException) =
        AppException(
            httpStatus = CONFLICT,
            statusCode = CONFLICT.value(),
            errors = listOf(exception.message)
        )

    @ExceptionHandler(NoCompletionCodeException::class)
    @ResponseStatus(BAD_REQUEST)
    fun noCompletionCodeExceptionHandler(exception: NoCompletionCodeException) =
        AppException(
            httpStatus = BAD_REQUEST,
            statusCode = BAD_REQUEST.value(),
            errors = listOf(exception.message)
        )

    @ExceptionHandler(AlreadyParticipatesInOtherSurveyException::class)
    @ResponseStatus(CONFLICT)
    fun alreadyParticipatesInOtherSurveyExceptionHandler(exception: AlreadyParticipatesInOtherSurveyException) =
        AppException(
            httpStatus = CONFLICT,
            statusCode = CONFLICT.value(),
            errors = listOf(exception.message)
        )

    @ExceptionHandler(WrongParticipantException::class)
    @ResponseStatus(FORBIDDEN)
    fun wrongParticipantExceptionHandler(exception: WrongParticipantException) =
        AppException(
            httpStatus = FORBIDDEN,
            statusCode = FORBIDDEN.value(),
            errors = listOf(exception.message)
        )

    @ExceptionHandler(UnqualifiedParticipantException::class)
    @ResponseStatus(CONFLICT)
    fun unqualifiedParticipantExceptionHandler(exception: UnqualifiedParticipantException) =
        AppException(
            httpStatus = FORBIDDEN,
            statusCode = FORBIDDEN.value(),
            errors = listOf(exception.message)
        )

    @ExceptionHandler(NoSurveyFoundException::class)
    @ResponseStatus(BAD_REQUEST)
    fun noSurveyFoundExceptionHandler(exception: NoSurveyFoundException) =
        AppException(
            httpStatus = BAD_REQUEST,
            statusCode = BAD_REQUEST.value(),
            errors = listOf(exception.message)
        )

    @ExceptionHandler(NoSurveyForResearcherException::class)
    @ResponseStatus(FORBIDDEN)
    fun noSurveyForResearcherExceptionHandler(exception: NoSurveyForResearcherException) =
        AppException(
            httpStatus = FORBIDDEN,
            statusCode = FORBIDDEN.value(),
            errors = listOf(exception.message)
        )
}

data class AppException(
    val httpStatus: HttpStatus,
    val statusCode: Int,
    val errors: List<String?>
)