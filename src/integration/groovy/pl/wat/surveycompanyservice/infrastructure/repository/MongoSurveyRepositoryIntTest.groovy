package pl.wat.surveycompanyservice.infrastructure.repository

import org.springframework.beans.factory.annotation.Autowired
import pl.wat.surveycompanyservice.BaseIntegrationTest
import pl.wat.surveycompanyservice.domain.survey.MongoSurvey
import pl.wat.surveycompanyservice.domain.survey.Survey

import static pl.wat.surveycompanyservice.IntegrationTestBuilders.SURVEY_ID
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.survey

class MongoSurveyRepositoryIntTest extends BaseIntegrationTest {

    def 'should save survey'() {
        given:
            Survey survey = survey()
        when:
            mongoSurveyRepository.saveSurvey(survey)
        then:
            mongoOperations.findAll(MongoSurvey.class).size() == 1
            with(mongoOperations.findAll(MongoSurvey.class).first()) {
                it.id == SURVEY_ID
            }
    }

    def 'should not save if a survey with the same id exists and not throw DuplicateKeyException'() {
        given:
            Survey survey1 = survey()
            Survey survey2 = survey(researcherId: '123')
        when:
            mongoSurveyRepository.saveSurvey(survey1)
            mongoSurveyRepository.saveSurvey(survey2)
        then:
            mongoOperations.findAll(MongoSurvey.class).size() == 1
    }
}
