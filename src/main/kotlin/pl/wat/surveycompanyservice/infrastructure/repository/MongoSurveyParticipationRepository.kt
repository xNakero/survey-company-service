package pl.wat.surveycompanyservice.infrastructure.repository

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.BulkOperations.BulkMode.UNORDERED
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.remove
import org.springframework.stereotype.Component
import pl.wat.surveycompanyservice.domain.surveyparticipation.MongoSurveyParticipation
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipation
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipationRepository
import pl.wat.surveycompanyservice.domain.surveyparticipation.ParticipationStatus
import pl.wat.surveycompanyservice.domain.surveyparticipation.ParticipationStatus.IN_PROGRESS
import pl.wat.surveycompanyservice.domain.surveyparticipation.ParticipationStatus.TIMEOUT
import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.SurveyId
import pl.wat.surveycompanyservice.shared.SurveyParticipationId
import java.time.Clock
import java.time.Instant

@Component
class MongoSurveyParticipationRepository(
    val mongoOperations: MongoOperations
) : SurveyParticipationRepository {

    override fun insert(surveyParticipation: SurveyParticipation) {
        mongoOperations.insert(surveyParticipation.toMongoSurveyParticipation())
    }

    override fun update(
        surveyParticipationId: SurveyParticipationId,
        participationStatus: ParticipationStatus,
        completionCode: String?,
        timestamp: Instant?
    ) {
        val query = Query.query(Criteria.where(ID).`is`(surveyParticipationId.raw))
        val update = Update()
            .set(STATUS, participationStatus.toString())
            .set(COMPLETION_CODE, completionCode)
            .set(FINISHED_AT, timestamp)
        mongoOperations.updateFirst(query, update, MongoSurveyParticipation::class.java)
    }

    override fun findByParticipantId(participantId: ParticipantId): List<SurveyParticipation> {
        val query = Query.query(Criteria.where(PARTICIPANT_ID).`is`(participantId.raw))
        return mongoOperations.find(query, MongoSurveyParticipation::class.java)
            .map { it.toSurveyParticipation() }
    }

    override fun finishAllUnfinishedInTime(timestamp: Instant) {
        val query = Query.query(
            Criteria.where(STATUS).`is`(IN_PROGRESS.toString())
                .and(HAS_TO_FINISH_UNTIL).lte(timestamp)
        )
        val update = Update.update(STATUS, TIMEOUT.toString()).set(FINISHED_AT, timestamp)

        mongoOperations.bulkOps(UNORDERED, MongoSurveyParticipation::class.java)
            .updateMulti(query, update)
            .execute()
    }

    override fun findInProgressBySurveyIds(surveyIds: List<SurveyId>): List<SurveyParticipation> {
        val query = Query.query(
            Criteria.where(SURVEY_ID).`in`(surveyIds.map { it.raw })
                .and(STATUS).`is`(IN_PROGRESS)
        )
        return mongoOperations.find(query, MongoSurveyParticipation::class.java).map { it.toSurveyParticipation() }
    }

    override fun findBySurveyIds(surveyIds: List<SurveyId>): List<SurveyParticipation> {
        val query = Query.query(Criteria.where(SURVEY_ID).`in`(surveyIds.map { it.raw }))
        return mongoOperations.find(query, MongoSurveyParticipation::class.java).map { it.toSurveyParticipation() }
    }

    override fun removeBySurveyIds(surveyIds: List<SurveyId>) {
        val query = Query.query(Criteria.where(SURVEY_ID).`in`(surveyIds.map { it.raw }))
        mongoOperations.remove(query, MongoSurveyParticipation::class.java)
    }

    override fun findInProgressByParticipantId(participantId: ParticipantId): SurveyParticipation? {
        val query = Query.query(
            Criteria.where(PARTICIPANT_ID).`is`(participantId.raw)
                .and(STATUS).`is`(IN_PROGRESS)
        )
        return mongoOperations.find(query, MongoSurveyParticipation::class.java).firstOrNull()?.toSurveyParticipation()
    }

    override fun find(surveyParticipationId: SurveyParticipationId): SurveyParticipation {
        val query = Query.query(Criteria.where(ID).`is`(surveyParticipationId.raw))
        val result = mongoOperations.find(query, MongoSurveyParticipation::class.java)
        return result[0].toSurveyParticipation()
    }

    companion object {
        const val ID = "_id"
        const val STATUS = "status"
        const val COMPLETION_CODE = "completionCode"
        const val PARTICIPANT_ID = "participantId"
        const val HAS_TO_FINISH_UNTIL = "hasToFinishUntil"
        const val FINISHED_AT = "finishedAt"
        const val SURVEY_ID = "surveyId"
    }
}