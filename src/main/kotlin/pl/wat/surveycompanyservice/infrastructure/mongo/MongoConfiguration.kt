package pl.wat.surveycompanyservice.infrastructure.mongo

import com.mongodb.MongoCredential
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoClientFactoryBean
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@Configuration
@EnableMongoRepositories(basePackages = ["pl.wat.surveycompanyservice"])
class MongoConfiguration(
    private val properties: MongoDbProperties
) {
    @Bean
    fun mongo(): MongoClientFactoryBean {
        val mongo = MongoClientFactoryBean()
        mongo.setHost(properties.host)
        mongo.setPort(properties.port)
        mongo.setCredential(
            arrayOf(
                MongoCredential.createCredential(
                    properties.username,
                    properties.database,
                    properties.password.toCharArray()
                )
            )
        )
        return mongo
    }
}