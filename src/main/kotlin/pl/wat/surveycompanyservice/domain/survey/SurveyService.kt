package pl.wat.surveycompanyservice.domain.survey

import org.springframework.stereotype.Service

@Service
class SurveyService(
    private val surveyRepository: SurveyRepository
) {

    fun saveSurvey(survey: Survey) {
        surveyRepository.saveSurvey(survey)
    }
}