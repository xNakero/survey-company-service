package pl.wat.surveycompanyservice.domain.survey

import org.springframework.stereotype.Component
import pl.wat.surveycompanyservice.api.SurveyToPostDto
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileQueryParams
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileService
import pl.wat.surveycompanyservice.shared.ResearcherId
import pl.wat.surveycompanyservice.shared.SurveyId
import pl.wat.surveycompanyservice.shared.SurveyParticipationId
import java.time.Clock
import java.time.Instant
import java.util.*

@Component
class SurveyFacade(
    private val surveyService: SurveyService,
    private val personalProfileService: PersonalProfileService,
    private val completionCodeFactory: CompletionCodeFactory,
    private val surveyProperties: SurveyProperties,
    private val clock: Clock
) {
    fun saveSurvey(surveyDto: SurveyToPostDto, researcherId: ResearcherId) {
        val eligibleUsers = personalProfileService.findEligibleParticipantIds(surveyDto.queryParams)
            .also { if (it.isEmpty()) throw NoEligibleParticipantsException("There are no eligible participants.") }
        val completionCode = completionCodeFactory.generateCode(surveyProperties.codeLength)
        val survey = surveyDto.toSurvey(researcherId, eligibleUsers, completionCode, clock.instant())
        surveyService.saveSurvey(survey)
    }

    fun getNumberOfEligibleParticipants(request: PersonalProfileQueryParams): Int =
        personalProfileService.findEligibleParticipantIds(request).size

    fun findSurvey(surveyId: SurveyId): Survey =
        surveyService.findSurvey(surveyId)

    fun incrementSpotsTaken(surveyId: SurveyId, surveyParticipationId: SurveyParticipationId): Survey =
        surveyService.incrementSpotsTaken(surveyId, surveyParticipationId)
}

fun SurveyToPostDto.toSurvey(
    researcherId: ResearcherId,
    eligibleParticipantsIds: List<String>,
    completionCode: String,
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
    completionCode = completionCode,
    status = SurveyStatus.ACTIVE,
    startedAt = timestamp
)

fun determineTotalSpots(spots: Int, eligibleParticipantsIds: List<String>): Int =
    if (spots > eligibleParticipantsIds.size) eligibleParticipantsIds.size else spots

class NoEligibleParticipantsException(message: String?) : RuntimeException(message)