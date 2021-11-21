package pl.wat.surveycompanyservice.infrastructure.repository

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.stereotype.Component
import pl.wat.surveycompanyservice.domain.survey.Survey
import pl.wat.surveycompanyservice.domain.survey.SurveyRepository

@Component
class MongoSurveyRepository(
    private val mongoOperations: MongoOperations
): SurveyRepository {

    override fun saveSurvey(survey: Survey) {
        try {
            mongoOperations.insert(survey.toMongoSurvey())
        } catch (e: DuplicateKeyException) {
            logger.warn("Duplicated id: ${survey.id}")
        }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(MongoSurveyRepository::class.java)
    }
}