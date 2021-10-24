package pl.wat.surveycompanyservice

import groovyx.net.http.HttpResponseDecorator
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.elasticsearch.ElasticsearchContainer
import org.testcontainers.junit.jupiter.Testcontainers
import spock.lang.Specification
import spock.lang.Subject.Container

@SpringBootTest(classes = SurveyCompanyServiceApplication,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class BaseIntegrationTest extends Specification{

    @Container
    private static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:latest")

    @Container
    private static ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer('docker.elastic.co/elasticsearch/elasticsearch:7.15.0')

    @Container
    private static PostgreSQLContainer postgresContainer = new PostgreSQLContainer("postgres:latest")

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer.&getJdbcUrl)
        registry.add("spring.datasource.username", postgresContainer.&getUsername)
        registry.add("spring.datasource.password", postgresContainer.&getPassword)
        registry.add("spring.data.mongodb.uri", mongoContainer.&getReplicaSetUrl)
        registry.add("spring.elasticsearch.rest.uris", elasticsearchContainer.&getHttpHostAddress)
    }

    def setupSpec() {
        mongoContainer.start()
        elasticsearchContainer.start()
        mongoContainer.start()
    }

}
