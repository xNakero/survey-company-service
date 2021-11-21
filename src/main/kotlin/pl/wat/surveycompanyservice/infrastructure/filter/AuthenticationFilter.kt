package pl.wat.surveycompanyservice.infrastructure.filter

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import pl.wat.surveycompanyservice.api.AppException
import pl.wat.surveycompanyservice.infrastructure.token.TokenService
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AuthenticationFilter(
    authenticationManager: AuthenticationManager?,
    private val tokenService: TokenService,
    private val objectMapper: ObjectMapper
) : BasicAuthenticationFilter(authenticationManager) {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        try {
            val token = request.getHeader(AUTHORIZATION).replace("Bearer ", "")
            SecurityContextHolder.getContext().authentication = token.let { tokenService.authenticate(token) }
            chain.doFilter(request, response)
        } catch (e: NullPointerException) {
            response.status = HttpStatus.UNAUTHORIZED.value()
            AppException(
                httpStatus = HttpStatus.UNAUTHORIZED,
                statusCode = HttpStatus.UNAUTHORIZED.value(),
                errors = listOf("No Authorization header was found.")
            ).let { response.writer.write(it.toJson()) }
        }
    }

    private fun AppException.toJson(): String =
        objectMapper.writeValueAsString(this)
}