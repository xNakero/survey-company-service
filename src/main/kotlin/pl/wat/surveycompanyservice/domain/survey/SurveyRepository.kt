package pl.wat.surveycompanyservice.domain.survey

interface SurveyRepository {
    fun saveSurvey(survey: Survey)
}