package pl.wat.surveycompanyservice.infrastructure.repository

import pl.wat.surveycompanyservice.BaseIntegrationTest
import pl.wat.surveycompanyservice.domain.surveyparticipation.MongoSurveyParticipation
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipation
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyStatus
import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.SurveyParticipationId

import static pl.wat.surveycompanyservice.IntegrationTestBuilders.PARTICIPANT_ID
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.SURVEY_PARTICIPATION_ID
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.surveyParticipation
import static pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyStatus.COMPLETED

class MongoSurveyParticipationRepositoryIntTest extends BaseIntegrationTest{

    def 'should save SurveyParticipation'() {
        given:
            SurveyParticipation surveyParticipation = surveyParticipation()
        when:
            mongoSurveyParticipationRepository.insert(surveyParticipation)
        then:
            mongoOperations.findAll(MongoSurveyParticipation.class).size() == 1
            mongoOperations.findAll(MongoSurveyParticipation.class).first().id == SURVEY_PARTICIPATION_ID
    }

    def 'should update SurveyParticipation status and completion code'() {
        given:
            mongoOperations.save(surveyParticipation().toMongoSurveyParticipation())
            String completionCode = 'COMPLETED'
            SurveyStatus status = COMPLETED
        when:
            mongoSurveyParticipationRepository.update(new SurveyParticipationId(SURVEY_PARTICIPATION_ID), status, completionCode)
        then:
            with(mongoOperations.findAll(MongoSurveyParticipation.class).find {it.id == SURVEY_PARTICIPATION_ID}) {
                it.status == status.toString()
                it.completionCode == completionCode
            }
    }

    def 'should find list of SurveyParticipation by surveyId'() {
        given:
            mongoOperations.save(surveyParticipation().toMongoSurveyParticipation())
            mongoOperations.save(surveyParticipation([
                    surveyParticipationId: '2-2'
            ]).toMongoSurveyParticipation())
        when:
            List surveyParticipations = mongoSurveyParticipationRepository.findByParticipantId(new ParticipantId(PARTICIPANT_ID))
        then:
            surveyParticipations.id.raw as Set == [SURVEY_PARTICIPATION_ID, '2-2'] as Set
    }

    def 'should find by SurveyParticipationId'() {
        given:
            mongoOperations.save(surveyParticipation().toMongoSurveyParticipation())
        when:
            SurveyParticipation surveyParticipation = mongoSurveyParticipationRepository.find(new SurveyParticipationId(SURVEY_PARTICIPATION_ID))
        then:
            surveyParticipation.id.raw == SURVEY_PARTICIPATION_ID
    }
}
