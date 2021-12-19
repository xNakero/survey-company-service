package pl.wat.surveycompanyservice.domain.surveyparticipation

import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.SurveyParticipationId
import java.time.Instant

interface SurveyParticipationRepository {
    fun insert(surveyParticipation: SurveyParticipation)
    fun find(surveyParticipationId: SurveyParticipationId): SurveyParticipation
    fun update(surveyParticipationId: SurveyParticipationId, surveyStatus: SurveyStatus, completionCode: String?)
    fun findByParticipantId(participantId: ParticipantId): List<SurveyParticipation>
    fun finishAllUnfinishedInTime(timestamp: Instant)
}
