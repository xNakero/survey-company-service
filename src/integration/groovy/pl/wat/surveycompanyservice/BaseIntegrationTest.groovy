package pl.wat.surveycompanyservice

import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.elasticsearch.ElasticsearchContainer
import org.testcontainers.spock.Testcontainers
import pl.wat.surveycompanyservice.domain.user.UserRepository
import spock.lang.Shared
import spock.lang.Specification

@Testcontainers
@SpringBootTest(
        classes = SurveyCompanyServiceApplication,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BaseIntegrationTest extends Specification {

    @LocalServerPort
    protected int port

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

    @Autowired
    UserRepository userRepository

    def "ping"() {
        given:
            RESTClient restClient = new RESTClient("http://localhost:$port", ContentType.JSON)
        when:
            HttpResponseDecorator response =  restClient.get(path: "/ping")
        then:
            response.status == 200
    }
}
