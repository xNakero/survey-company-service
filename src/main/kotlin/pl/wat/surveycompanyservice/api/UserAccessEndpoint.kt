package pl.wat.surveycompanyservice.api

import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.OK
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import pl.wat.surveycompanyservice.domain.role.AppRole
import pl.wat.surveycompanyservice.domain.user.AppUser
import pl.wat.surveycompanyservice.domain.user.UserFacade

@RestController
class UserAccessEndpoint(
    private val userFacade: UserFacade
) {

    @PostMapping("/login")
    @ResponseStatus(OK)
    fun login(@RequestBody request: LoginRequest): TokensDto =
        userFacade.login(request.username, request.password)

    @PostMapping("/register")
    @ResponseStatus(CREATED)
    fun register(@RequestBody request: RegistrationRequest) =
        userFacade.createUser(
            username = request.username,
            password = request.password,
            role = AppRole.valueOf(request.role)
        )

    @PostMapping("/renew")
    @ResponseStatus(OK)
    fun renewToken(): TokensDto =
        userFacade.renewToken(SecurityContextHolder.getContext().authentication)
}

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegistrationRequest(
    val username: String,
    val password: String,
    val role: String
)


data class TokensDto(
    val authorizationToken: String,
    val refreshToken: String
)
