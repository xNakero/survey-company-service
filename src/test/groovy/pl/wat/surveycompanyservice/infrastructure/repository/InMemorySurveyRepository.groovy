package pl.wat.surveycompanyservice.infrastructure.repository


import pl.wat.surveycompanyservice.domain.survey.Survey
import pl.wat.surveycompanyservice.domain.survey.SurveyRepository
import pl.wat.surveycompanyservice.shared.SurveyId
import pl.wat.surveycompanyservice.shared.SurveyParticipationId

import java.util.concurrent.CopyOnWriteArraySet

class InMemorySurveyRepository implements SurveyRepository {

    Set<Survey> surveys = new CopyOnWriteArraySet<>()

    @Override
    void saveSurvey(Survey survey) {
        surveys.remove(
                surveys.find {
                    it.id.raw == survey.id.raw
                }
        )
        surveys.add(survey)
    }

    @Override
    Survey find(SurveyId surveyId) {
        return surveys.find {surveyId.raw == it.id.raw}
    }

    @Override
    void saveParticipation(
            SurveyId surveyId,
            int spotsToUpdate,
            SurveyParticipationId surveyParticipationId
    ) {
        Survey survey = surveys.find {it.id.raw == surveyId.raw}
        List participationIds = survey.participationIds
        participationIds.add(surveyParticipationId.raw)
        Survey updatedSurvey = new Survey(
                survey.id,
                survey.researcherId,
                survey.participationIds,
                participationIds,
                survey.title,
                survey.url,
                survey.timeToCompleteInSeconds,
                survey.description,
                survey.spotsTotal,
                survey.spotsTaken + 1,
                survey.completionCode
        )
        saveSurvey(updatedSurvey)
    }

    void clear() {
        surveys.clear()
    }

    Set findAll() {
        return surveys
    }
}
