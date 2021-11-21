package pl.wat.surveycompanyservice

import groovy.transform.CompileStatic
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
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
import pl.wat.surveycompanyservice.domain.user.UserRepository
import spock.lang.Shared
import spock.lang.Specification

import java.time.Duration

import static java.util.stream.Collectors.toList
import static org.springframework.http.HttpHeaders.AUTHORIZATION
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

        clearUsersDbs()
        setupUsersDbs()

        clock.reset()
    }

    void authAs(String username, String password) {
        Map tokens = restClient.post(path: '/login', body: loginRequest([username: username, password: password]))
        restClient.headers.put(AUTHORIZATION, tokens['authorizationToken'])
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
        List userIds = userRepository.findAll().stream().map {it.userId.toString() }.collect(toList())
        userRepository.deleteAll()
        roleRepository.deleteAll()

        userIds.forEach {elasticsearchRestTemplate.delete(it, ElasticPersonalProfile.class)}
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
