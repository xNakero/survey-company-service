package pl.wat.surveycompanyservice.infrastructure.mongo

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("mongo")
class MongoDbProperties {
    var port: Int = 27017
    var host: String = "localhost"
    var username: String = "username"
    var password: String = "password"
    var database: String = "scs"
}