package pl.wat.surveycompanyservice.domain.survey

import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.ResearcherId
import pl.wat.surveycompanyservice.shared.SurveyId
import pl.wat.surveycompanyservice.shared.SurveyParticipationId

interface SurveyRepository {
    fun saveSurvey(survey: Survey)
    fun find(surveyId: SurveyId): Survey
    fun saveParticipationToSurvey(surveyId: SurveyId, spotsToUpdate: Int, surveyParticipationId: SurveyParticipationId)
    fun findSurveysEligibleToFinish(): List<Survey>
    fun findBySurveyIds(surveyIds: List<SurveyId>): List<Survey>
    fun removeByIds(surveyIds: List<SurveyId>)
    fun findAllByResearcherId(researcherId: ResearcherId): List<Survey>
    fun findEligibleToParticipate(participantId: ParticipantId): List<Survey>
}