package pl.wat.surveycompanyservice.infrastructure.repository

import org.jetbrains.annotations.NotNull
import pl.wat.surveycompanyservice.domain.survey.Survey
import pl.wat.surveycompanyservice.domain.survey.SurveyRepository

import java.util.concurrent.CopyOnWriteArraySet

class InMemorySurveyRepository implements SurveyRepository{

    Set<Survey> surveys = new CopyOnWriteArraySet<>()

    @Override
    void saveSurvey(@NotNull Survey survey) {
        surveys.remove(
                surveys.find {
                    it.id.raw == survey.id.raw
                }
        )
        surveys.add(survey)
    }

    void clear() {
        surveys.clear()
    }

    Set findAll() {
        return surveys
    }
}
