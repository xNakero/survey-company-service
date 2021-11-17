package pl.wat.surveycompanyservice.domain.user

import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import pl.wat.surveycompanyservice.api.TokensDto
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileFacade
import pl.wat.surveycompanyservice.domain.role.AppRole
import pl.wat.surveycompanyservice.domain.role.AppRole.INTERVIEWEE
import pl.wat.surveycompanyservice.infrastructure.token.TokenService
import pl.wat.surveycompanyservice.shared.UserId
import javax.transaction.Transactional

@Component
class UserFacade(
    private val userService: UserService,
    private val tokenService: TokenService,
    private val personalProfileFacade: PersonalProfileFacade
) {

    fun login(username: String, password: String): TokensDto {
        val authentication = userService.login(username, password)
        val authorizationToken = tokenService.getToken(authentication)
        val refreshToken = tokenService.getRefreshToken(authentication)
        return TokensDto(authorizationToken, refreshToken)
    }

    @Transactional
    fun createUser(username: String, password: String, role: AppRole) {
        val user = userService.createUser(username, password, role)
        if (role == INTERVIEWEE) {
            personalProfileFacade.createEmptyProfile(UserId(user.userId.toString()))
        }
    }

    fun renewToken(user: Authentication): TokensDto {
        val authorizationToken = tokenService.getToken(user)
        val refreshToken = tokenService.getRefreshToken(user)
        return TokensDto(authorizationToken, refreshToken)
    }
}
