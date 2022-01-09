package pl.wat.surveycompanyservice.domain.survey

import org.springframework.stereotype.Service
import pl.wat.surveycompanyservice.domain.surveyhistory.format
import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.ResearcherId
import pl.wat.surveycompanyservice.shared.SurveyId
import pl.wat.surveycompanyservice.shared.SurveyParticipationId
import java.time.Duration
import java.time.temporal.ChronoUnit.SECONDS

@Service
class SurveyService(
    private val surveyRepository: SurveyRepository
) {

    fun saveSurvey(survey: Survey) {
        surveyRepository.saveSurvey(survey)
    }

    fun findSurvey(surveyId: SurveyId): Survey? =
        surveyRepository.find(surveyId)

    fun incrementSpotsTaken(surveyId: SurveyId, surveyParticipationId: SurveyParticipationId): Survey {
        val survey = surveyRepository.find(surveyId)
        surveyRepository.saveParticipationToSurvey(surveyId, survey.spotsTaken + 1, surveyParticipationId)
        return survey
    }

    fun findSurveysEligibleToFinish(): List<Survey> =
        surveyRepository.findSurveysEligibleToFinish()

    fun findBySurveyIds(surveyIds: List<SurveyId>): Map<SurveyId, Survey> =
        surveyRepository.findBySurveyIds(surveyIds)
            .associateBy( { it.id }, { it })

    fun removeByIds(surveyIds: List<SurveyId>) {
        surveyRepository.removeByIds(surveyIds)
    }

    fun findAllByResearcherId(researcherId: ResearcherId): List<ResearcherSurveyDto> =
        surveyRepository.findAllActiveByResearcherId(researcherId)
            .map { it.toResearcherSurveyDto() }

    fun findEligibleSurveys(participantId: ParticipantId): List<Survey> =
        surveyRepository.findEligibleToParticipate(participantId)

    private fun Survey.toResearcherSurveyDto(): ResearcherSurveyDto =
        ResearcherSurveyDto(
            surveyId = id.raw,
            title = title,
            url = url,
            timeToComplete = Duration.of(timeToCompleteInSeconds.toLong(), SECONDS).format(),
            description = description,
            spotsTotal = spotsTotal,
            spotsTaken = spotsTaken,
            completionCode = completionCode,
            startedAt = startedAt
        )

    fun finishSurvey(surveyId: SurveyId, researcherId: ResearcherId) {
        surveyRepository.scheduleToFinish(surveyId, researcherId)
    }
}
