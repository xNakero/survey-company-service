package pl.wat.surveycompanyservice.scheduler

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("survey-participation-scheduler")
class SurveyParticipationSchedulerProperties {
    var initDelayInMillis: Long = 120_000
    var periodInMillis: Long = 200
    var errorDelayInMillis: Long = 5000
    var threadPoolSize: Int = 2
}