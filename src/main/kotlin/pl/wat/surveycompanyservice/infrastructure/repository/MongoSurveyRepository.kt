package pl.wat.surveycompanyservice.infrastructure.repository

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component
import pl.wat.surveycompanyservice.domain.survey.MongoSurvey
import pl.wat.surveycompanyservice.domain.survey.Survey
import pl.wat.surveycompanyservice.domain.survey.SurveyRepository
import pl.wat.surveycompanyservice.domain.survey.SurveyStatus.ACTIVE
import pl.wat.surveycompanyservice.domain.survey.SurveyStatus.SCHEDULED_TO_FINISH
import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.ResearcherId
import pl.wat.surveycompanyservice.shared.SurveyId
import pl.wat.surveycompanyservice.shared.SurveyParticipationId
import java.time.Clock
import java.time.temporal.ChronoUnit.DAYS

@Component
class MongoSurveyRepository(
    private val mongoOperations: MongoOperations,
    private val clock: Clock
) : SurveyRepository {

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

    override fun findSurveysEligibleToFinish(): List<Survey> {
        val queryByDate = Query.query(Criteria.where(STARTED_AT).lte(clock.instant().minus(7, DAYS)))
        val queryByStatus = Query.query(Criteria.where(STATUS).`is`(SCHEDULED_TO_FINISH))
        return mongoOperations.find(queryByDate, MongoSurvey::class.java).map { it.toSurvey() }
            .plus(mongoOperations.find(queryByStatus, MongoSurvey::class.java).map { it.toSurvey() })
    }

    override fun findBySurveyIds(surveyIds: List<SurveyId>): List<Survey> {
        val query = Query.query(Criteria.where(ID).`in`(surveyIds.map { it.raw }))
        return mongoOperations.find(query, MongoSurvey::class.java).map { it.toSurvey() }
    }

    override fun removeByIds(surveyIds: List<SurveyId>) {
        val query = Query.query(Criteria.where(ID).`in`(surveyIds.map { it.raw }))
        mongoOperations.remove(query, MongoSurvey::class.java)
    }

    override fun findAllActiveByResearcherId(researcherId: ResearcherId): List<Survey> {
        val query = Query.query(
            Criteria.where(RESEARCHER_ID).`is`(researcherId.raw)
                .and(STATUS).`is`(ACTIVE)
        )
        return mongoOperations.find(query, MongoSurvey::class.java).map { it.toSurvey() }
    }

    override fun findEligibleToParticipate(participantId: ParticipantId): List<Survey> {
        val query = Query.query(Criteria.where(ELIGIBLE_PARTICIPANTS_IDS).all(participantId.raw))
        return mongoOperations.find(query, MongoSurvey::class.java).map { it.toSurvey() }
    }

    override fun scheduleToFinish(surveyId: SurveyId, researcherId: ResearcherId) {
        val query = Query.query(Criteria.where(ID).`is`(surveyId.raw).and(RESEARCHER_ID).`is`(researcherId.raw))
        val update = Update().set(STATUS, SCHEDULED_TO_FINISH)
        mongoOperations.updateFirst(query, update, MongoSurvey::class.java).modifiedCount
            .takeIf { it > 0 }
            ?: throw NoSurveyForResearcherException("Researcher with id: ${researcherId.raw} doesn't have a survey with id ${surveyId.raw}")
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(MongoSurveyRepository::class.java)
        const val ID = "_id"
        const val SPOTS_TAKEN = "spotsTaken"
        const val PARTICIPATION_IDS = "participationIds"
        const val STARTED_AT = "startedAt"
        const val RESEARCHER_ID = "ResearcherId"
        const val ELIGIBLE_PARTICIPANTS_IDS = "eligibleParticipantsIds"
        const val STATUS = "status"
    }
}

class NoSuchSurveyException(message: String?) : RuntimeException(message)
class NoSurveyForResearcherException(message: String?) : RuntimeException(message)