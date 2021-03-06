package pl.wat.surveycompanyservice

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.crypto.password.PasswordEncoder
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileFacade
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileService
import pl.wat.surveycompanyservice.domain.role.RoleRepository
import pl.wat.surveycompanyservice.domain.survey.SurveyFacade
import pl.wat.surveycompanyservice.domain.survey.SurveyService
import pl.wat.surveycompanyservice.domain.surveyhistory.HistoryEntryFacade
import pl.wat.surveycompanyservice.domain.surveyhistory.HistoryEntryService
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipationFacade
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipationIdFactory
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipationService
import pl.wat.surveycompanyservice.domain.user.UserFacade
import pl.wat.surveycompanyservice.domain.user.UserRepository
import pl.wat.surveycompanyservice.domain.user.UserService
import pl.wat.surveycompanyservice.domain.user.UserServiceImpl
import pl.wat.surveycompanyservice.infrastructure.filter.AuthenticationFilter
import pl.wat.surveycompanyservice.infrastructure.repository.InMemoryHistoryEntryRepository
import pl.wat.surveycompanyservice.infrastructure.repository.InMemoryPersonalProfileRepository
import pl.wat.surveycompanyservice.infrastructure.repository.InMemorySurveyParticipationRepository
import pl.wat.surveycompanyservice.infrastructure.repository.InMemorySurveyRepository
import pl.wat.surveycompanyservice.infrastructure.token.TokenProperties
import pl.wat.surveycompanyservice.infrastructure.token.TokenService
import pl.wat.surveycompanyservice.scheduler.SurveyParticipationScheduler
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class BaseUnitTest extends Specification{

    PollingConditions conditions = new PollingConditions(timeout: 10, delay: 0.1, factor: 2.0)

    protected UserRepository userRepository = Stub()
    protected RoleRepository roleRepository = Stub()
    protected PasswordEncoder passwordEncoder = Stub()
    protected AuthenticationManager authenticationManager = Stub()
    protected TestClock clock = new TestClock()
    protected UserService userService = new UserService(userRepository, roleRepository, passwordEncoder, authenticationManager)
    protected TokenProperties tokenProperties = new TokenProperties()
    protected ObjectMapper objectMapper = new ObjectMapper()
    protected UserServiceImpl userServiceImpl = new UserServiceImpl(userRepository)
    protected TokenService tokenService = new TokenService(tokenProperties, objectMapper, userServiceImpl)
    protected InMemoryPersonalProfileRepository inMemoryPersonalProfileRepository = new InMemoryPersonalProfileRepository()
    protected InMemorySurveyRepository inMemorySurveyRepository = new InMemorySurveyRepository()
    protected InMemorySurveyParticipationRepository inMemorySurveyParticipationRepository = new InMemorySurveyParticipationRepository(clock)
    protected InMemoryHistoryEntryRepository historyEntryRepository = new InMemoryHistoryEntryRepository()
    protected PersonalProfileService personalProfileService = new PersonalProfileService(inMemoryPersonalProfileRepository)
    protected PersonalProfileFacade personalProfileFacade = new PersonalProfileFacade(personalProfileService)
    protected UserFacade userFacade = new UserFacade(userService, tokenService, personalProfileFacade)
    protected SurveyService surveyService = new SurveyService(inMemorySurveyRepository)

    protected SurveyParticipationIdFactory surveyParticipationIdFactory = new SurveyParticipationIdFactory()
    protected SurveyParticipationService surveyParticipationService = new SurveyParticipationService(
            inMemorySurveyParticipationRepository,
            surveyParticipationIdFactory,
            clock
    )
    protected SurveyFacade surveyFacade = new SurveyFacade(
            surveyService,
            personalProfileService,
            surveyParticipationService,
            clock
    )
    protected AuthenticationFilter authenticationFilter = new AuthenticationFilter(authenticationManager, tokenService, objectMapper)
    protected SurveyParticipationFacade surveyParticipationFacade = new SurveyParticipationFacade(
            surveyParticipationService,
            surveyFacade
    )
    protected SurveyParticipationScheduler surveyParticipationScheduler = new SurveyParticipationScheduler(
            inMemorySurveyParticipationRepository,
            clock
    )
    protected HistoryEntryService historyEntryService = new HistoryEntryService(historyEntryRepository)
    protected HistoryEntryFacade historyEntryFacade = new HistoryEntryFacade(
            historyEntryService,
            surveyParticipationService,
            surveyService
    )

    def setup() {
        inMemoryPersonalProfileRepository.clear()
        inMemorySurveyRepository.clear()
        inMemorySurveyParticipationRepository.clear()
        historyEntryRepository.clear()
        clock.reset()
    }
}