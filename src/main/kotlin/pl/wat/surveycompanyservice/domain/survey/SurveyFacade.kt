package pl.wat.surveycompanyservice.domain.survey

import org.springframework.stereotype.Component
import pl.wat.surveycompanyservice.api.SurveyToPostDto
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileQueryParams
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileService
import pl.wat.surveycompanyservice.shared.ResearcherId
import pl.wat.surveycompanyservice.shared.SurveyId
import java.util.*

@Component
class SurveyFacade(
    private val surveyService: SurveyService,
    private val personalProfileService: PersonalProfileService,
    private val completionCodeFactory: CompletionCodeFactory,
    private val surveyProperties: SurveyProperties
) {
    fun saveSurvey(surveyDto: SurveyToPostDto, researcherId: ResearcherId) {
        val eligibleUsers = personalProfileService.findEligibleParticipantIds(surveyDto.queryParams)
        val completionCode = completionCodeFactory.generateCode(surveyProperties.codeLength)
        val survey = surveyDto.toSurvey(researcherId, eligibleUsers, completionCode)
        surveyService.saveSurvey(survey)
    }

    fun getNumberOfEligibleParticipants(request: PersonalProfileQueryParams): Int =
        personalProfileService.findEligibleParticipantIds(request).size

}

fun SurveyToPostDto.toSurvey(
    researcherId: ResearcherId,
    eligibleParticipantsIds: List<String>,
    completionCode: String
): Survey = Survey(
    id = SurveyId(UUID.randomUUID().toString()),
    researcherId = researcherId,
    participantIds = emptyList(),
    eligibleParticipantsIds = eligibleParticipantsIds,
    title = surveyParams.title,
    url = surveyParams.url,
    timeToCompleteInSeconds = surveyParams.timeToCompleteInSeconds,
    description = surveyParams.description,
    spotsTotal = determineTotalSpots(surveyParams.spots, eligibleParticipantsIds),
    spotsTaken = 0,
    completionCode = completionCode
)

fun determineTotalSpots(spots: Int, eligibleParticipantsIds: List<String>): Int =
    if (spots > eligibleParticipantsIds.size) eligibleParticipantsIds.size else spots
