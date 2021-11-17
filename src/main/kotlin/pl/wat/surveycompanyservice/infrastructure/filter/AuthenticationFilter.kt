package pl.wat.surveycompanyservice.infrastructure.filter

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import pl.wat.surveycompanyservice.infrastructure.token.TokenService
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AuthenticationFilter(
    authenticationManager: AuthenticationManager?,
    private val tokenService: TokenService
) : BasicAuthenticationFilter(authenticationManager) {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val token = request.getHeader("Authorization").replace("Bearer ", "")
        SecurityContextHolder.getContext().authentication = tokenService.authenticate(token)
        chain.doFilter(request, response)
    }
}