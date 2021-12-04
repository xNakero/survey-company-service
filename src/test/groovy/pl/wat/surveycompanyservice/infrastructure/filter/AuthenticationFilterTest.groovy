package pl.wat.surveycompanyservice.infrastructure.filter

import com.auth0.jwt.exceptions.JWTVerificationException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import pl.wat.surveycompanyservice.BaseUnitTest
import pl.wat.surveycompanyservice.TestBuilders
import pl.wat.surveycompanyservice.domain.role.Role
import pl.wat.surveycompanyservice.domain.user.AppUser

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static org.springframework.http.HttpHeaders.AUTHORIZATION
import static org.springframework.http.HttpStatus.UNAUTHORIZED
import static pl.wat.surveycompanyservice.TestBuilders.PASSWORD
import static pl.wat.surveycompanyservice.TestBuilders.USERNAME
import static pl.wat.surveycompanyservice.TestBuilders.appUser
import static pl.wat.surveycompanyservice.domain.role.AppRole.PARTICIPANT

class AuthenticationFilterTest extends BaseUnitTest {

    private HttpServletRequest request = Stub()
    private HttpServletResponse response = Mock(HttpServletResponse)
    private FilterChain chain = Mock(FilterChain)

    def 'should not authenticate if token is expired'() {
        given:
            String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyOEBnbWFpbC5jb20iLCJyb2xlcyI6IklOVEVS" +
                    "VklFV0VFIiwiaXNzIjoic3VydmV5LWFwcCIsImV4cCI6MTYzNTkwMjA3MiwiaWF0IjoxNjM1ODE1NjcyfQ.rF1o6Z4jaJtj" +
                    "WR41jokD_t3tD5fSWHu14CuUjif3pno5xXyGh1s3G343U7pWCZRM34idD_mKOiYAP-bJQWG4pQ"
            AppUser user = appUser()
            request.getHeader(AUTHORIZATION) >> token
            tokenService.authenticate(token) >> new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    user.getAuthorities()
            )
        when:
            authenticationFilter.doFilterInternal(request, response, chain)
        then:
            def e = thrown(JWTVerificationException)
            e.message == 'The Token has expired on Wed Nov 03 02:14:32 CET 2021.'
    }

    def 'should not authenticate if token is null'() {
        given:
            request.getHeader(AUTHORIZATION) >> null
        when:
            authenticationFilter.doFilterInternal(request, response, chain)
        then:
            thrown(NullPointerException)
            1 * response.writer
            1 * response.setStatus(UNAUTHORIZED.value())
    }
}
