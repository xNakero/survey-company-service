package pl.wat.surveycompanyservice

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileStatic
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import org.elasticsearch.ElasticsearchException
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.support.master.AcknowledgedResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.RestClients
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Query
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.elasticsearch.ElasticsearchContainer
import org.testcontainers.spock.Testcontainers
import pl.wat.surveycompanyservice.domain.profile.ElasticPersonalProfile
import pl.wat.surveycompanyservice.domain.role.AppRole
import pl.wat.surveycompanyservice.domain.role.Role
import pl.wat.surveycompanyservice.domain.role.RoleRepository
import pl.wat.surveycompanyservice.domain.survey.MongoSurvey
import pl.wat.surveycompanyservice.domain.surveyhistory.MongoHistoryEntry
import pl.wat.surveycompanyservice.domain.surveyparticipation.MongoSurveyParticipation
import pl.wat.surveycompanyservice.domain.user.UserRepository
import pl.wat.surveycompanyservice.infrastructure.repository.ElasticPersonalProfileRepository
import pl.wat.surveycompanyservice.infrastructure.repository.MongoHistoryEntryRepository
import pl.wat.surveycompanyservice.infrastructure.repository.MongoSurveyParticipationRepository
import pl.wat.surveycompanyservice.infrastructure.repository.MongoSurveyRepository
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.time.Duration
import java.time.LocalDate

import static java.util.stream.Collectors.toList
import static org.elasticsearch.client.RequestOptions.DEFAULT
import static org.springframework.http.HttpHeaders.AUTHORIZATION
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.PARTICIPANT_USERNAME
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.loginRequest
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.participantRegistrationRequest
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.researcherRegistrationRequest
import static pl.wat.surveycompanyservice.domain.role.AppRole.PARTICIPANT
import static pl.wat.surveycompanyservice.domain.role.AppRole.RESEARCHER

@Testcontainers
@SpringBootTest(
        classes = [SurveyCompanyServiceApplication, ElasticIntegrationTestConfig, IntegrationTestConfig],
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class BaseIntegrationTest extends Specification {

    PollingConditions conditions = new PollingConditions(timeout: 10, delay: 0.1, factor: 2.0)

    @LocalServerPort
    protected int port

    RESTClient restClient

    @Autowired
    TestClock clock

    @Autowired
    MongoOperations mongoOperations

    @Autowired
    UserRepository userRepository

    @Autowired
    RoleRepository roleRepository

    @Autowired
    ElasticsearchRestTemplate elasticsearchRestTemplate

    @Autowired
    RestHighLevelClient restHighLevelClient

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    ElasticPersonalProfileRepository elasticPersonalProfileRepository

    @Autowired
    MongoSurveyRepository mongoSurveyRepository

    @Autowired
    MongoSurveyParticipationRepository mongoSurveyParticipationRepository

    @Autowired
    MongoHistoryEntryRepository historyEntryRepository

    @Shared
    private static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:latest")

    @Shared
    private static ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer('docker.elastic.co/elasticsearch/elasticsearch:7.15.0')

    @Shared
    private static PostgreSQLContainer postgresContainer = new PostgreSQLContainer("postgres:latest")

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        postgresContainer.start()
        mongoContainer.start()
        elasticsearchContainer.start()
        registry.add("spring.datasource.url", postgresContainer.&getJdbcUrl)
        registry.add("spring.datasource.username", postgresContainer.&getUsername)
        registry.add("spring.datasource.password", postgresContainer.&getPassword)
        registry.add("spring.data.mongodb.uri", mongoContainer.&getReplicaSetUrl)
        registry.add("spring.elasticsearch.rest.uris", elasticsearchContainer.&getHttpHostAddress)
    }

    def setupSpec() {
        postgresContainer.withInitScript("schema.sql");
    }

    def setup() {
        restClient = new RESTClient("http://localhost:$port", ContentType.JSON)
        restClient.handler.failure = restClient.handler.success

        mongoOperations.remove(new Query(), MongoSurvey.class)
        mongoOperations.remove(new Query(), MongoSurveyParticipation.class)
        mongoOperations.remove(new Query(), MongoHistoryEntry.class)
        clearUsersDbs()
        clearPersonalProfiles()
        setupUsersDbs()

        clock.reset()
    }

    void authAs(String username, String password) {
        HttpResponseDecorator response = restClient.post(path: '/login', body: loginRequest([username: username, password: password]))
        Map tokens = response.data
        restClient.headers.put(AUTHORIZATION, tokens['authorizationToken'])
    }

    Long getPrincipalId() {
        return userRepository.findByUsername(PARTICIPANT_USERNAME).userId
    }

    Long getPrincipalId(String username) {
        return userRepository.findByUsername(username).userId
    }

    private void setupUsersDbs() {
        roleRepository.saveAll([
                new Role(0L, PARTICIPANT.toString(), [] as Set),
                new Role(0L, RESEARCHER.toString(), [] as Set)
        ])
        stubUser(PARTICIPANT, [:])
        stubUser(RESEARCHER, [:])
    }

    private void clearUsersDbs() {
        List userIds = userRepository.findAll()
                .stream()
                .map {it.userId.toString() }
                .collect(toList())

        userRepository.deleteAll()
        roleRepository.deleteAll()

        userIds.forEach {elasticsearchRestTemplate.delete(it, ElasticPersonalProfile.class)}
    }

    private void clearPersonalProfiles() {
        DeleteIndexRequest request = new DeleteIndexRequest("personal_profile");
        try {
            AcknowledgedResponse deleteIndexResponse = restHighLevelClient.indices().delete(request, DEFAULT);
        } catch (ElasticsearchException e) {
            e.printStackTrace()
        }
    }

    private void stubUser(AppRole role, Map params = [:]) {
        Map body
        if (role == PARTICIPANT) {
            body = participantRegistrationRequest(params)
        } else {
            body = researcherRegistrationRequest(params)
        }
        restClient.post(path: "/register", body: body)
    }

    @Configuration
    @CompileStatic
    @EnableElasticsearchRepositories(basePackages = ["pl.wat.surveycompanyservice"])
    static class ElasticIntegrationTestConfig extends AbstractElasticsearchConfiguration {

        @Primary
        @Bean
        @Override
        RestHighLevelClient elasticsearchClient() {
            ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                    .connectedTo(elasticsearchContainer.getHttpHostAddress())
                    .withConnectTimeout(Duration.ofSeconds(1))
                    .withSocketTimeout(Duration.ofSeconds(1))
                    .build()
            return RestClients.create(clientConfiguration).rest()
        }

        @Primary
        @Bean
        ElasticsearchRestTemplate elasticSearchRestTemplate() {
            return new ElasticsearchRestTemplate(elasticsearchClient())
        }
    }

    @Configuration
    @CompileStatic
    static class IntegrationTestConfig {
        @Primary
        @Bean
        TestClock clock() {
            return new TestClock()
        }
    }
}
