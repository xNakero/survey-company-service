package pl.wat.surveycompanyservice.domain.survey

import org.springframework.stereotype.Service
import pl.wat.surveycompanyservice.shared.SurveyId
import pl.wat.surveycompanyservice.shared.SurveyParticipationId

@Service
class SurveyService(
    private val surveyRepository: SurveyRepository
) {

    fun saveSurvey(survey: Survey) {
        surveyRepository.saveSurvey(survey)
    }

    fun findSurvey(surveyId: SurveyId): Survey =
        surveyRepository.find(surveyId)

    fun incrementSpotsTaken(surveyId: SurveyId, surveyParticipationId: SurveyParticipationId): Survey {
        val survey = surveyRepository.find(surveyId)
        surveyRepository.saveParticipationToSurvey(surveyId, survey.spotsTaken + 1, surveyParticipationId)
        return survey
    }
}
