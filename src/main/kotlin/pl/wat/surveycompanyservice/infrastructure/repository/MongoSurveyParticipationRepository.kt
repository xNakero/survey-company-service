package pl.wat.surveycompanyservice.infrastructure.repository

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component
import pl.wat.surveycompanyservice.domain.surveyparticipation.MongoSurveyParticipation
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipation
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipationRepository
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyStatus
import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.SurveyParticipationId

@Component
class MongoSurveyParticipationRepository(
    val mongoOperations: MongoOperations
): SurveyParticipationRepository {

    override fun insert(surveyParticipation: SurveyParticipation) {
        mongoOperations.insert(surveyParticipation.toMongoSurveyParticipation())
    }

    override fun update(surveyParticipationId: SurveyParticipationId, surveyStatus: SurveyStatus, completionCode: String?) {
        val query = Query.query(Criteria.where(ID).`is`(surveyParticipationId.raw))
        val update = Update().set(STATUS, surveyStatus.toString()).set(COMPLETION_CODE, completionCode)
        mongoOperations.updateFirst(query, update, MongoSurveyParticipation::class.java)
    }

    override fun findByParticipantId(participantId: ParticipantId): List<SurveyParticipation> {
        val query = Query.query(Criteria.where(PARTICIPANT_ID).`is`(participantId.raw))
        return mongoOperations.find(query, MongoSurveyParticipation::class.java)
            .map { it.toSurveyParticipation() }
    }

    override fun find(surveyParticipationId: SurveyParticipationId): SurveyParticipation {
        val query = Query.query(Criteria.where(ID).`is`(surveyParticipationId.raw))
        val result = mongoOperations.find(query, MongoSurveyParticipation::class.java)
        return result[0].toSurveyParticipation()
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(MongoSurveyParticipationRepository::class.java)
        const val ID = "_id"
        const val STATUS = "status"
        const val COMPLETION_CODE = "completionCode"
        const val PARTICIPANT_ID = "participantId"
    }
}

class SurveyParticipationNotUpdatedException(message: String?) : RuntimeException(message)