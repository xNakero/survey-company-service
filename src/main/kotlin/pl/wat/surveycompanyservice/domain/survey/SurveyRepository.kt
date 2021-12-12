package pl.wat.surveycompanyservice.domain.survey

import pl.wat.surveycompanyservice.shared.SurveyId
import pl.wat.surveycompanyservice.shared.SurveyParticipationId

interface SurveyRepository {
    fun saveSurvey(survey: Survey)
    fun find(surveyId: SurveyId): Survey
    fun saveParticipationToSurvey(surveyId: SurveyId, spotsToUpdate: Int, surveyParticipationId: SurveyParticipationId)
}