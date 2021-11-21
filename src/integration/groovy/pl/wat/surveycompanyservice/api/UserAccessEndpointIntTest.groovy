package pl.wat.surveycompanyservice.api

import groovyx.net.http.HttpResponseDecorator
import org.springframework.beans.factory.annotation.Autowired
import pl.wat.surveycompanyservice.BaseIntegrationTest
import pl.wat.surveycompanyservice.domain.profile.ElasticPersonalProfile
import pl.wat.surveycompanyservice.domain.user.AppUser
import pl.wat.surveycompanyservice.infrastructure.token.TokenProperties
import spock.lang.Unroll

import static org.springframework.http.HttpHeaders.AUTHORIZATION
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.JWT_REGEX
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.loginRequest
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.participantRegistrationRequest
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.researcherRegistrationRequest
class UserAccessEndpointIntTest extends BaseIntegrationTest {

    @Autowired
    TokenProperties tokenProperties

    def "should create researcher account without empty PersonalProfile"() {
        given:
            String username = 'user@gmail.com'
            Map body = researcherRegistrationRequest([username: username])
        when:
            HttpResponseDecorator response = register(body)
        then:
            response.status == 201
            AppUser user = userRepository.findByUsername(username)
            user != null
            elasticsearchRestTemplate.get(user.userId.toString(), ElasticPersonalProfile.class) == null
    }

    def "should create participant account with empty PersonalProfile"() {
        given:
            String username = 'user@gmail.com'
            Map body = participantRegistrationRequest([username: username])
        when:
            HttpResponseDecorator response = register(body)
        then:
            response.status == 201
            AppUser user = userRepository.findByUsername(username)
            user != null
            with(elasticsearchRestTemplate.get(user.userId.toString(), ElasticPersonalProfile.class)) {
                userId == user.userId.toString()
                dateOfBirth == null
                civilStatus == null
                countryOfBirth == null
                nationality == null
                currentCountry == null
                firstLanguage == null
                highestEducationLevelAchieved == null
                isStudent() == null
                monthlyIncome == null
                employmentStatus == null
                formOfEmployment == null
                industry == null
                politicalSide == null
            }
    }

    def "should return 400 when email is not an email"() {
        given:
            Map body = researcherRegistrationRequest([username: "user"])
        when:
            HttpResponseDecorator response = register(body)
        then:
            response.status == 400
            response.data.errors.first() == "Provided email is not an email."
    }

    def "should return 400 when role is invalid"() {
        given:
            Map body = [
                    username: "email@email",
                    password: "password",
                    role    : "NOT A RESEARCHER"
            ]
        when:
            HttpResponseDecorator response = register(body)
        then:
            response.status == 400
            response.data.errors.first() == "There is no such role."
    }

    def "if user already exists should return 409"() {
        given:
            Map body = participantRegistrationRequest()
        when:
            HttpResponseDecorator response = register(body)
        then:
            response.status == 409
            response.data.errors.first() == "User with username=participant@gmail.com already exists."
    }

    def 'should get tokens if login data is correct'() {
        given:
            String username = 'user@gmail.com'
            Map registrationBody = researcherRegistrationRequest([username: username])
            Map loginBody = loginRequest([username: username])
        when:
            HttpResponseDecorator registrationResponse = register(registrationBody)
        then:
            registrationResponse.status == 201
        when:
            HttpResponseDecorator loginResponse = login(loginBody)
        then:
            loginResponse.status == 200
            loginResponse.data['authorizationToken'].matches(JWT_REGEX)
            loginResponse.data['refreshToken'].matches(JWT_REGEX)
    }

    @Unroll
    def 'should not get tokens if user with given username or password doesnt exist'() {
        given:
            Map body = loginRequest(username: username, password: password)
        when:
            HttpResponseDecorator response = login(body)
        then:
            response.status == 401
        where:
            username                    | password
            'participant@gmail.com'     | 'nopassword'
            'doesntExist@gmail.com'     | 'password'
    }

    def 'should refresh token if user is authenticated'() {
        given:
            Map body = loginRequest()
        when:
            HttpResponseDecorator loginResponse = login(body)
        then:
            loginResponse.status == 200
            String token = loginResponse.data['authorizationToken']
        when:
            restClient.headers.put(AUTHORIZATION, token)
            HttpResponseDecorator refreshResponse = refreshToken()
        then:
            refreshResponse.status == 200
            refreshResponse.data['authorizationToken'].matches(JWT_REGEX)
            refreshResponse.data['refreshToken'].matches(JWT_REGEX)
    }

    def 'should not refresh token if user is unauthenticated'() {
        when:
            HttpResponseDecorator refreshResponse = refreshToken()
        then:
            refreshResponse.status == 401
            refreshResponse.data.errors.first() == 'No Authorization header was found.'
    }

    private HttpResponseDecorator register(Map body) {
        return restClient.post(path: "/register", body: body)
    }

    private HttpResponseDecorator login(Map body) {
        return restClient.post(path: '/login', body: body)
    }

    private HttpResponseDecorator refreshToken() {
        return restClient.post(path: '/refresh')
    }
}
