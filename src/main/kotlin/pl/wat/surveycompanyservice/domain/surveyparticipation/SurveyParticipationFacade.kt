package pl.wat.surveycompanyservice.domain.surveyparticipation

import org.springframework.stereotype.Component
import pl.wat.surveycompanyservice.api.Action.CANCEL
import pl.wat.surveycompanyservice.api.Action.COMPLETE
import pl.wat.surveycompanyservice.api.ParticipationModificationDto
import pl.wat.surveycompanyservice.domain.survey.SurveyFacade
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyStatus.CANCELLED
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyStatus.COMPLETED
import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.SurveyId
import pl.wat.surveycompanyservice.shared.SurveyParticipationId

@Component
class SurveyParticipationFacade(
    private val surveyParticipationService: SurveyParticipationService,
    private val surveyFacade: SurveyFacade
) {

    fun participate(participantId: ParticipantId, surveyId: SurveyId) {
        if (!surveyParticipationService.hasNoOtherParticipation(participantId)) {
            throw AlreadyParticipatesInOtherSurveyException("Participant already participates in a different survey.")
        }

        val survey = surveyFacade.findSurvey(surveyId)
        if (!survey.eligibleParticipantsIds.contains(participantId.raw)) {
            throw UnqualifiedParticipantException("Participant: ${participantId.raw} did not qualify for the survey: ${surveyId.raw}")
        }
        if (!survey.hasFreeSpots()) {
            throw NoFreeSpotsException("There are no free spots for survey withId: ${surveyId.raw}.")
        }

        val participationId = surveyParticipationService.participate(participantId, surveyId, survey.timeToCompleteInSeconds)
        surveyFacade.incrementSpotsTaken(surveyId, participationId)
    }

    fun manageParticipation(
        participantId: ParticipantId,
        surveyParticipationId: SurveyParticipationId,
        participation: ParticipationModificationDto
    ) {
        when (participation.action) {
            COMPLETE -> {
                participation.completionCode
                    ?.let { surveyParticipationService.manageParticipation(surveyParticipationId, COMPLETED, it, participantId) }
                    ?: throw NoCompletionCodeException("There is no completion code on completion request")
            }
            CANCEL -> surveyParticipationService.manageParticipation(
                surveyParticipationId,
                CANCELLED,
                null,
                participantId
            )
        }
    }
}

class AlreadyParticipatesInOtherSurveyException(message: String?) : RuntimeException(message)
class UnqualifiedParticipantException(message: String?) : RuntimeException(message)
class NoCompletionCodeException(message: String?) : RuntimeException(message)
class NoFreeSpotsException(message: String?) : RuntimeException(message)