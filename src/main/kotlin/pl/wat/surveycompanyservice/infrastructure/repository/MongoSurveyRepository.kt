package pl.wat.surveycompanyservice.infrastructure.repository

import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.stereotype.Component
import pl.wat.surveycompanyservice.domain.survey.Survey
import pl.wat.surveycompanyservice.domain.survey.SurveyRepository

@Component
class MongoSurveyRepository(
    private val mongoOperations: MongoOperations
): SurveyRepository {

    override fun saveSurvey(survey: Survey) {
        mongoOperations.insert(survey.toMongoSurvey())
    }
}