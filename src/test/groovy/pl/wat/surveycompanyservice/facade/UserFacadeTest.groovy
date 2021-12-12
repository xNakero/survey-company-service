package pl.wat.surveycompanyservice.facade

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import pl.wat.surveycompanyservice.BaseUnitTest
import pl.wat.surveycompanyservice.api.TokensDto
import pl.wat.surveycompanyservice.domain.role.Role
import pl.wat.surveycompanyservice.domain.user.AppUser
import pl.wat.surveycompanyservice.domain.user.RoleNotFoundException
import pl.wat.surveycompanyservice.domain.user.UserAlreadyExistsException

import static pl.wat.surveycompanyservice.TestBuilders.PASSWORD
import static pl.wat.surveycompanyservice.TestBuilders.USERNAME
import static pl.wat.surveycompanyservice.domain.role.AppRole.PARTICIPANT
import static pl.wat.surveycompanyservice.domain.role.AppRole.RESEARCHER

class UserFacadeTest extends BaseUnitTest {

    def 'should create participant account with personalProfile'() {
        given:
            roleRepository.findByName(PARTICIPANT.toString()) >> new Role(1, PARTICIPANT.toString(), [] as Set)
            userRepository.findByUsername(USERNAME) >> null
            userRepository.save(_) >> new AppUser(
                    1,
                    USERNAME,
                    PASSWORD,
                    [new Role(1, PARTICIPANT.toString(), [] as Set)] as Set
            )
        when:
            userFacade.createUser(USERNAME, PASSWORD, PARTICIPANT)
        then:
            inMemoryPersonalProfileRepository.containsProfileWithId('1')
    }

    def 'should create researcher account without personalProfile'() {
        given:
            roleRepository.findByName(RESEARCHER.toString()) >> new Role(1, RESEARCHER.toString(), [] as Set)
            userRepository.findByUsername(USERNAME) >> null
            userRepository.save(_) >> new AppUser(
                    1,
                    USERNAME,
                    PASSWORD,
                    [new Role(1, RESEARCHER.toString(), [] as Set)] as Set
            )
        when:
            userFacade.createUser(USERNAME, PASSWORD, RESEARCHER)
        then:
            !inMemoryPersonalProfileRepository.containsProfileWithId('1')
    }

    def 'should not create account if user exists'() {
        given:
            roleRepository.findByName(RESEARCHER.toString()) >> new Role(1, RESEARCHER.toString(), [] as Set)
            userRepository.findByUsername(USERNAME) >> new AppUser(
                    1,
                    USERNAME,
                    PASSWORD,
                    [new Role(1, RESEARCHER.toString(), [] as Set)] as Set
            )
        when:
            userFacade.createUser(USERNAME, PASSWORD, RESEARCHER)
        then:
            thrown(UserAlreadyExistsException)
    }

    def 'should not register if role is not in database'() {
        given:
            roleRepository.findByName(RESEARCHER.toString()) >> null
        when:
            userFacade.createUser(USERNAME, PASSWORD, RESEARCHER)
        then:
            thrown(RoleNotFoundException)
    }

    def 'should return tokens if authentication is successful'() {

    }

    def 'should renew tokens for authenticated user'() {
        given:
            AppUser user = new AppUser(
                    1L,
                    "user",
                    "password",
                    [new Role(1L, PARTICIPANT.toString(), [] as Set)] as Set
            )
            Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
        when:
            TokensDto tokens = userFacade.renewToken(authentication)
        then:
            tokens.authorizationToken.matches('^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$')
            tokens.refreshToken.matches('^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$')
    }
}
