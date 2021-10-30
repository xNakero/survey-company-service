package pl.wat.surveycompanyservice.domain.user

import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import pl.wat.surveycompanyservice.api.TokensDto
import pl.wat.surveycompanyservice.domain.role.AppRole
import pl.wat.surveycompanyservice.infrastructure.token.TokenService

@Component
class UserFacade(
    private val userService: UserService,
    private val tokenService: TokenService
) {

    fun login(username: String, password: String): TokensDto {
        val authentication = userService.login(username, password)
        val authorizationToken = tokenService.getToken(authentication)
        val refreshToken = tokenService.getRefreshToken(authentication)
        return TokensDto(authorizationToken, refreshToken)
    }

    fun createUser(username: String, password: String, role: AppRole) =
        userService.createUser(username, password, role)

    fun renewToken(user: Authentication): TokensDto {
        val authorizationToken = tokenService.getToken(user)
        val refreshToken = tokenService.getRefreshToken(user)
        return TokensDto(authorizationToken, refreshToken)
    }
}
