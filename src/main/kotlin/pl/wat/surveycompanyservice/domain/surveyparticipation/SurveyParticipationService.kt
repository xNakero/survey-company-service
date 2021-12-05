package pl.wat.surveycompanyservice.domain.surveyparticipation

import org.springframework.stereotype.Service
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyStatus.IN_PROGRESS
import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.SurveyId
import pl.wat.surveycompanyservice.shared.SurveyParticipationId
import java.time.Clock

@Service
class SurveyParticipationService(
    private val surveyParticipationRepository: SurveyParticipationRepository,
    private val surveyParticipationIdFactory: SurveyParticipationIdFactory,
    private val clock: Clock
) {
    fun participate(participantId: ParticipantId, surveyId: SurveyId, timeToCompleteInSeconds: Int): SurveyParticipationId {
        val surveyParticipationId = surveyParticipationIdFactory.generateId(participantId, surveyId)
        val surveyParticipation = SurveyParticipation(
            surveyParticipationId,
            participantId,
            surveyId,
            IN_PROGRESS,
            clock.instant(),
            clock.instant().plusSeconds(timeToCompleteInSeconds.toLong()),
            null
        )
        surveyParticipationRepository.insert(surveyParticipation)
        return surveyParticipationId
    }

    fun manageParticipation(
        surveyParticipationId: SurveyParticipationId,
        surveyStatus: SurveyStatus,
        completionCode: String?,
        participantId: ParticipantId
    ) {
        val participation = surveyParticipationRepository.find(surveyParticipationId)
        if (participation.participantId.raw != participantId.raw) {
            throw WrongParticipantException("Participant with id: ${participantId.raw} has no access to participation with id: ${surveyParticipationId.raw}")
        }
        if (participation.hasToFinishUntil.isAfter(clock.instant()) && participation.status == IN_PROGRESS) {
            surveyParticipationRepository.update(surveyParticipationId, surveyStatus, completionCode)
        } else {
            throw SurveyParticipationNotInProgressException("SurveyParticipation with id: ${surveyParticipationId.raw} is not in progress.")
        }
    }

    fun hasNoOtherParticipation(participantId: ParticipantId): Boolean =
        surveyParticipationRepository.findByParticipantId(participantId).isEmpty()

}

class SurveyParticipationNotInProgressException(message: String?) : RuntimeException(message)
class WrongParticipantException(message: String?) : RuntimeException(message)