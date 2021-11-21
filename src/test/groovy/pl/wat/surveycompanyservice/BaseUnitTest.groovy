package pl.wat.surveycompanyservice

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.crypto.password.PasswordEncoder
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileFacade
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileService
import pl.wat.surveycompanyservice.domain.role.RoleRepository
import pl.wat.surveycompanyservice.domain.user.UserFacade
import pl.wat.surveycompanyservice.domain.user.UserRepository
import pl.wat.surveycompanyservice.domain.user.UserService
import pl.wat.surveycompanyservice.domain.user.UserServiceImpl
import pl.wat.surveycompanyservice.infrastructure.filter.AuthenticationFilter
import pl.wat.surveycompanyservice.infrastructure.repository.InMemoryPersonalProfileRepository
import pl.wat.surveycompanyservice.infrastructure.token.TokenProperties
import pl.wat.surveycompanyservice.infrastructure.token.TokenService
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class BaseUnitTest extends Specification{

    PollingConditions conditions = new PollingConditions(timeout: 10, delay: 0.1, factor: 2.0)

    protected UserRepository userRepository = Stub()
    protected RoleRepository roleRepository = Stub()
    protected PasswordEncoder passwordEncoder = Stub()
    protected AuthenticationManager authenticationManager = Stub()
    protected UserService userService = new UserService(userRepository, roleRepository, passwordEncoder, authenticationManager)
    protected TokenProperties tokenProperties = new TokenProperties()
    protected ObjectMapper objectMapper = new ObjectMapper()
    protected UserServiceImpl userServiceImpl = new UserServiceImpl(userRepository)
    protected TokenService tokenService = new TokenService(tokenProperties, objectMapper, userServiceImpl)
    protected inMemoryPersonalProfileRepository = new InMemoryPersonalProfileRepository()
    protected PersonalProfileService personalProfileService = new PersonalProfileService(inMemoryPersonalProfileRepository)
    protected PersonalProfileFacade personalProfileFacade = new PersonalProfileFacade(personalProfileService)
    protected UserFacade userFacade = new UserFacade(userService, tokenService, personalProfileFacade)

    protected AuthenticationFilter authenticationFilter = new AuthenticationFilter(authenticationManager, tokenService, objectMapper)

    def setup() {
        inMemoryPersonalProfileRepository.clear()
    }
}