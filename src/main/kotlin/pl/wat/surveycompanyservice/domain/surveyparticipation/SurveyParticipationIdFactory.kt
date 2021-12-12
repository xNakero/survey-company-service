package pl.wat.surveycompanyservice.domain.surveyparticipation

import org.springframework.stereotype.Component
import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.SurveyId
import pl.wat.surveycompanyservice.shared.SurveyParticipationId

@Component
class SurveyParticipationIdFactory {
    fun generateId(participantId: ParticipantId, surveyId: SurveyId): SurveyParticipationId =
        SurveyParticipationId("$participantId$surveyId")
}
