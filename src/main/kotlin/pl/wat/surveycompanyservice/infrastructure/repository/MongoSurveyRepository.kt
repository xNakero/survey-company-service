package pl.wat.surveycompanyservice.infrastructure.repository

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.UpdateDefinition
import org.springframework.stereotype.Component
import pl.wat.surveycompanyservice.domain.survey.MongoSurvey
import pl.wat.surveycompanyservice.domain.survey.Survey
import pl.wat.surveycompanyservice.domain.survey.SurveyRepository
import pl.wat.surveycompanyservice.shared.SurveyId
import pl.wat.surveycompanyservice.shared.SurveyParticipationId

@Component
class MongoSurveyRepository(
    private val mongoOperations: MongoOperations
): SurveyRepository {

    override fun saveSurvey(survey: Survey) {
        mongoOperations.insert(survey.toMongoSurvey())
    }

    override fun find(surveyId: SurveyId): Survey {
        val query = Query.query(Criteria.where(ID).`is`(surveyId.raw))
        val surveys = mongoOperations.find(query, MongoSurvey::class.java)
        if (surveys.size == 0) {
            throw NoSuchSurveyException("There is no survey with id: ${surveyId.raw}")
        }
        return surveys[0].toSurvey()
    }

    override fun saveParticipationToSurvey(
        surveyId: SurveyId,
        spotsToUpdate: Int,
        surveyParticipationId: SurveyParticipationId
    ) {
        val query = Query.query(Criteria.where(ID).`is`(surveyId.raw))
        val update = Update().set(SPOTS_TAKEN, spotsToUpdate)
            .push(PARTICIPATION_IDS, surveyParticipationId.raw)
        mongoOperations.updateFirst(query, update, MongoSurvey::class.java)
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(MongoSurveyRepository::class.java)
        const val ID = "_id"
        const val SPOTS_TAKEN = "spotsTaken"
        const val PARTICIPATION_IDS = "participationIds"
    }
}

class NoSuchSurveyException(message: String?) : RuntimeException(message)