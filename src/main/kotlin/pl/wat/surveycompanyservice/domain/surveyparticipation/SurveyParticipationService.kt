package pl.wat.surveycompanyservice.domain.surveyparticipation

import org.springframework.stereotype.Service
import pl.wat.surveycompanyservice.domain.surveyparticipation.ParticipationStatus.IN_PROGRESS
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
            null,
            null
        )
        surveyParticipationRepository.insert(surveyParticipation)
        return surveyParticipationId
    }

    fun manageParticipation(
        surveyParticipationId: SurveyParticipationId,
        participationStatus: ParticipationStatus,
        completionCode: String?,
        participantId: ParticipantId
    ) {
        val participation = surveyParticipationRepository.find(surveyParticipationId)
        if (participation.participantId.raw != participantId.raw) {
            throw WrongParticipantException("Participant with id: ${participantId.raw} has no access to participation with id: ${surveyParticipationId.raw}")
        }
        if (participation.hasToFinishUntil.isAfter(clock.instant()) && participation.status == IN_PROGRESS) {
            surveyParticipationRepository.update(surveyParticipationId, participationStatus, completionCode, clock.instant())
        } else {
            throw SurveyParticipationNotInProgressException("SurveyParticipation with id: ${surveyParticipationId.raw} is not in progress.")
        }
    }

    fun hasNoOtherParticipation(participantId: ParticipantId): Boolean =
        surveyParticipationRepository.findByParticipantId(participantId).isEmpty()

    fun findInProgress(surveyIds: List<SurveyId>): List<SurveyParticipation> =
        surveyParticipationRepository.findInProgressBySurveyIds(surveyIds)

    fun findBySurveyIds(surveyIds: List<SurveyId>): Map<SurveyId, List<SurveyParticipation>> =
        surveyParticipationRepository.findBySurveyIds(surveyIds)
            .groupBy { it.surveyId }
            .mapValues { it.value.map { sp -> sp } }

    fun removeBySurveyIds(surveyIds: List<SurveyId>) {
        surveyParticipationRepository.removeBySurveyIds(surveyIds)
    }

    fun findParticipationInProgress(participantId: ParticipantId): SurveyParticipation? =
        surveyParticipationRepository.findInProgressByParticipantId(participantId)

    fun findNotInProgress(participantId: ParticipantId): Map<SurveyId, SurveyParticipation> =
        surveyParticipationRepository.findNotInProgress(participantId)
            .associateBy( {it.surveyId}, {it})
}

class SurveyParticipationNotInProgressException(message: String?) : RuntimeException(message)
class WrongParticipantException(message: String?) : RuntimeException(message)