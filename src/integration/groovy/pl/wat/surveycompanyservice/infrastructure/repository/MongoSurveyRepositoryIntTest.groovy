package pl.wat.surveycompanyservice.infrastructure.repository


import org.springframework.dao.DuplicateKeyException
import pl.wat.surveycompanyservice.BaseIntegrationTest
import pl.wat.surveycompanyservice.domain.survey.MongoSurvey
import pl.wat.surveycompanyservice.domain.survey.Survey
import pl.wat.surveycompanyservice.shared.SurveyId
import pl.wat.surveycompanyservice.shared.SurveyParticipationId

import static pl.wat.surveycompanyservice.IntegrationTestBuilders.SURVEY_ID
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.SURVEY_PARTICIPATION_ID
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

    def 'should not save if a survey with the same id exists and throw DuplicateKeyException'() {
        given:
            Survey survey1 = survey()
            Survey survey2 = survey(researcherId: '123')
        when:
            mongoSurveyRepository.saveSurvey(survey1)
            mongoSurveyRepository.saveSurvey(survey2)
        then:
            thrown(DuplicateKeyException)
            mongoOperations.findAll(MongoSurvey.class).size() == 1
    }

    def 'should find survey by surveyId'() {
        given:
            MongoSurvey survey = survey().toMongoSurvey()
            mongoOperations.save(survey)
        when:
            Survey surveyFromRepo = mongoSurveyRepository.find(new SurveyId(SURVEY_ID))
        then:
            surveyFromRepo.id.raw == survey.id
    }

    def 'should throw NoSuchSurveyException if survey was not found'() {
        when:
            Survey surveyFromRepo = mongoSurveyRepository.find(new SurveyId(SURVEY_ID))
        then:
            thrown(NoSuchSurveyException)
            mongoOperations.findAll(MongoSurvey.class).size() == 0
    }

    def 'should update spotsTaken and add participationId to list'() {
        given:
            MongoSurvey survey = survey().toMongoSurvey()
            mongoOperations.save(survey)
        and:
            SurveyParticipationId surveyParticipationId = new SurveyParticipationId(SURVEY_PARTICIPATION_ID)
            int spotsToUpdate = 5
            SurveyId surveyId = new SurveyId(SURVEY_ID)
        when:
            mongoSurveyRepository.saveParticipationToSurvey(surveyId, spotsToUpdate, surveyParticipationId)
        then:
            with(mongoOperations.findAll(MongoSurvey.class).first()) {
                it.id == surveyId.raw
                it.participationIds.contains(surveyParticipationId.raw)
                it.spotsTaken == spotsToUpdate
            }
    }
}
