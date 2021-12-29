package pl.wat.surveycompanyservice.api

import org.hibernate.validator.constraints.Length
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.OK
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pl.wat.surveycompanyservice.domain.role.AppRole
import pl.wat.surveycompanyservice.domain.user.UserFacade
import pl.wat.surveycompanyservice.infrastructure.validator.Enum
import javax.validation.Valid
import javax.validation.constraints.Email

@RestController
@CrossOrigin
class UserAccessEndpoint(
    private val userFacade: UserFacade
) {

    @PostMapping("/login")
    @ResponseStatus(OK)
    fun login(@RequestBody request: LoginRequest): TokensDto =
        userFacade.login(request.username, request.password)

    @PostMapping("/register")
    @ResponseStatus(CREATED)
    fun register(@Valid @RequestBody request: RegistrationRequest) =
        userFacade.createUser(
            username = request.username,
            password = request.password,
            role = AppRole.valueOf(request.role)
        )

    @PostMapping("/refresh")
    @ResponseStatus(OK)
    fun renewToken(): TokensDto =
        userFacade.renewToken(SecurityContextHolder.getContext().authentication)
}

data class LoginRequest(
    @field:Email(message = "Provided email is not an email.") val username: String,
    @field:Length(min = 8, max = 32, message = "Password has to be between 8 and 32 characters.") val password: String
)

data class RegistrationRequest(
    @field:Email(message = "Provided email is not an email.") val username: String,
    @field:Length(min = 8, max = 32, message = "Password has to be between 8 and 32 characters.") val password: String,
    @field:Enum(AppRole::class, message = "There is no such role.") val role: String
)


data class TokensDto(
    val authorizationToken: String,
    val refreshToken: String
)
