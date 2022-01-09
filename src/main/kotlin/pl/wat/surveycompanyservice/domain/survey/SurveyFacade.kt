package pl.wat.surveycompanyservice.domain.survey

import org.springframework.stereotype.Component
import pl.wat.surveycompanyservice.api.SurveyToPostDto
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileQueryParams
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileService
import pl.wat.surveycompanyservice.domain.role.AppRole.PARTICIPANT
import pl.wat.surveycompanyservice.domain.role.AppRole.RESEARCHER
import pl.wat.surveycompanyservice.domain.survey.SurveyStatus.ACTIVE
import pl.wat.surveycompanyservice.domain.surveyhistory.format
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipation
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipationService
import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.ResearcherId
import pl.wat.surveycompanyservice.shared.SurveyId
import pl.wat.surveycompanyservice.shared.SurveyParticipationId
import pl.wat.surveycompanyservice.shared.UserId
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit.SECONDS
import java.util.*

@Component
class SurveyFacade(
    private val surveyService: SurveyService,
    private val personalProfileService: PersonalProfileService,
    private val surveyParticipationService: SurveyParticipationService,
    private val clock: Clock
) {
    fun saveSurvey(surveyDto: SurveyToPostDto, researcherId: ResearcherId) {
        val eligibleUsers = personalProfileService.findEligibleParticipantIds(surveyDto.queryParams)
            .also { if (it.isEmpty()) throw NoEligibleParticipantsException("There are no eligible participants.") }
        val survey = surveyDto.toSurvey(researcherId, eligibleUsers, clock.instant())
        surveyService.saveSurvey(survey)
    }

    fun getNumberOfEligibleParticipants(request: PersonalProfileQueryParams): Int =
        personalProfileService.findEligibleParticipantIds(request).size

    fun findSurvey(surveyId: SurveyId): Survey? =
        surveyService.findSurvey(surveyId)

    fun incrementSpotsTaken(surveyId: SurveyId, surveyParticipationId: SurveyParticipationId): Survey =
        surveyService.incrementSpotsTaken(surveyId, surveyParticipationId)

    fun getSurveys(userId: UserId, role: String): SurveysWithTypeDto =
        SurveysWithTypeDto(
            role,
            getSurveysForRole(userId, role)
        )

    fun finishSurvey(surveyId: SurveyId, researcherId: ResearcherId) {
        surveyService.finishSurvey(surveyId, researcherId)
    }

    private fun getSurveysForRole(userId: UserId, role: String): SurveysDto =
        when (role) {
            PARTICIPANT.toString() -> surveysDtoForParticipant(ParticipantId(userId.raw))
            RESEARCHER.toString() -> surveysDtoForResearcher(ResearcherId(userId.raw))
            else -> SurveysDto()
        }

    private fun surveysDtoForResearcher(researcherId: ResearcherId): ResearcherSurveysDto =
        surveyService.findAllByResearcherId(researcherId)
            .sortedByDescending { it.startedAt }
            .let { ResearcherSurveysDto(it) }

    private fun surveysDtoForParticipant(participantId: ParticipantId): ParticipantSurveysDto {
        val participationInProgress = surveyParticipationService.findParticipationInProgress(participantId)
        val surveyInProgress = participationInProgress?.let { surveyService.findSurvey(it.surveyId) }
        val eligibleSurveys = surveyService.findEligibleSurveys(participantId)
            .filter { it.id.raw != surveyInProgress?.id?.raw }
        return ParticipantSurveysDto(
            surveyInProgress?.let { toParticipantSurveyDto(it, participationInProgress) },
            eligibleSurveys.map { toParticipantSurveyDto(it, null) }
        )
    }

    private fun toParticipantSurveyDto(survey: Survey, surveyParticipation: SurveyParticipation?): ParticipantSurveyDto =
        ParticipantSurveyDto(
            surveyId = survey.id.raw,
            participationId = surveyParticipation?.id?.raw,
            title = survey.title,
            url = survey.url,
            timeToComplete = Duration.of(survey.timeToCompleteInSeconds.toLong(), SECONDS).format(),
            description = survey.description,
            freeSpots = survey.freeSpots(),
            status = surveyParticipation?.status?.name,
            startedAt = surveyParticipation?.startedAt,
            hasToFinishUntil = surveyParticipation?.hasToFinishUntil,
            completionCode = surveyParticipation?.completionCode
        )
}

fun SurveyToPostDto.toSurvey(
    researcherId: ResearcherId,
    eligibleParticipantsIds: List<String>,
    timestamp: Instant
): Survey = Survey(
    id = SurveyId(UUID.randomUUID().toString()),
    researcherId = researcherId,
    participationIds = emptyList(),
    eligibleParticipantsIds = eligibleParticipantsIds,
    title = surveyParams.title,
    url = surveyParams.url,
    timeToCompleteInSeconds = surveyParams.timeToCompleteInSeconds,
    description = surveyParams.description,
    spotsTotal = determineTotalSpots(surveyParams.spots, eligibleParticipantsIds),
    spotsTaken = 0,
    completionCode = surveyParams.completionCode,
    status = ACTIVE,
    startedAt = timestamp
)

fun determineTotalSpots(spots: Int, eligibleParticipantsIds: List<String>): Int =
    if (spots > eligibleParticipantsIds.size) eligibleParticipantsIds.size else spots

class NoEligibleParticipantsException(message: String?) : RuntimeException(message)