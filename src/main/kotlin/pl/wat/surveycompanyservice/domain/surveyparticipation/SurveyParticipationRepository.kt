package pl.wat.surveycompanyservice.domain.surveyparticipation

import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.SurveyId
import pl.wat.surveycompanyservice.shared.SurveyParticipationId
import java.time.Instant

interface SurveyParticipationRepository {
    fun insert(surveyParticipation: SurveyParticipation)
    fun find(surveyParticipationId: SurveyParticipationId): SurveyParticipation
    fun update(surveyParticipationId: SurveyParticipationId, participationStatus: ParticipationStatus, completionCode: String?, timestamp: Instant?)
    fun findByParticipantId(participantId: ParticipantId): List<SurveyParticipation>
    fun finishAllUnfinishedInTime(timestamp: Instant)
    fun findInProgressBySurveyIds(surveyIds: List<SurveyId>): List<SurveyParticipation>
    fun findBySurveyIds(surveyIds: List<SurveyId>): List<SurveyParticipation>
    fun removeBySurveyIds(surveyIds: List<SurveyId>)
    fun findInProgressByParticipantId(participantId: ParticipantId): SurveyParticipation?
    fun findNotInProgress(participantId: ParticipantId): List<SurveyParticipation>
}
