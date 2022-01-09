package pl.wat.surveycompanyservice.infrastructure.repository

import org.springframework.dao.DuplicateKeyException
import pl.wat.surveycompanyservice.BaseIntegrationTest
import pl.wat.surveycompanyservice.domain.survey.MongoSurvey
import pl.wat.surveycompanyservice.domain.survey.Survey
import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.ResearcherId
import pl.wat.surveycompanyservice.shared.SurveyId
import pl.wat.surveycompanyservice.shared.SurveyParticipationId

import static java.time.temporal.ChronoUnit.DAYS
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.PARTICIPANT_ID
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.RESEARCHER_ID
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.SURVEY_ID
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.SURVEY_PARTICIPATION_ID
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.survey
import static pl.wat.surveycompanyservice.domain.survey.SurveyStatus.SCHEDULED_TO_FINISH

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

    def 'should find only surveys eligible to finish'() {
        given:
            MongoSurvey notEligibleSurvey = survey().toMongoSurvey()
            MongoSurvey eligibleSurvey = survey([
                    surveyId: '123',
                    startedAt: clock.instant().minus(7, DAYS).toString()
            ]).toMongoSurvey()
            MongoSurvey scheduledSurvey = survey([
                    surveyId: '456',
                    status: SCHEDULED_TO_FINISH.toString()
            ]).toMongoSurvey()
        and:
            mongoOperations.save(notEligibleSurvey)
            mongoOperations.save(eligibleSurvey)
            mongoOperations.save(scheduledSurvey)
        when:
            List result = mongoSurveyRepository.findSurveysEligibleToFinish()
        then:
            result.size() == 2
            result.id.raw.containsAll('123', '456')
    }

    def 'should find surveys by ids'() {
        given:
            MongoSurvey survey1 = survey().toMongoSurvey()
            MongoSurvey survey2 = survey([surveyId: '123']).toMongoSurvey()
            MongoSurvey survey3 = survey([surveyId: '456']).toMongoSurvey()
        and:
            mongoOperations.save(survey1)
            mongoOperations.save(survey2)
            mongoOperations.save(survey3)
        when:
            List result = mongoSurveyRepository.findBySurveyIds([new SurveyId('123'), new SurveyId('456')])
        then:
            result.size() == 2
            result.id.raw.containsAll(['123', '456'])
    }

    def 'should remove surveys by ids'() {
        given:
            MongoSurvey survey1 = survey().toMongoSurvey()
            mongoOperations.save(survey1)
        when:
            mongoSurveyRepository.removeByIds([new SurveyId(SURVEY_ID)])
        then:
            mongoOperations.findAll(MongoSurvey.class).size() == 0
    }

    def 'should return active surveys by researcherId'() {
        given:
            MongoSurvey survey1 = survey().toMongoSurvey()
            MongoSurvey survey2 = survey([surveyId: '123']).toMongoSurvey()
            MongoSurvey survey3 = survey([
                    surveyId: '456',
                    researcherId: '12345'
            ]).toMongoSurvey()
            MongoSurvey survey4 = survey([
                    surveyId: '789',
                    status: SCHEDULED_TO_FINISH.toString()
            ]).toMongoSurvey()
        and:
            mongoOperations.save(survey1)
            mongoOperations.save(survey2)
            mongoOperations.save(survey3)
            mongoOperations.save(survey4)
        when:
            List result = mongoSurveyRepository.findAllActiveByResearcherId(new ResearcherId(RESEARCHER_ID))
        then:
            result.size() == 2
            result.id.raw.containsAll([SURVEY_ID, '123'])
    }

    def 'should return surveys where participantId is eligible'() {
        given:
            MongoSurvey survey1 = survey().toMongoSurvey()
            MongoSurvey survey2 = survey([
                    eligibleParticipantIds: PARTICIPANT_ID,
                    surveyId: '123'
            ]).toMongoSurvey()
        and:
            mongoOperations.save(survey1)
            mongoOperations.save(survey2)
        when:
            List result = mongoSurveyRepository.findEligibleToParticipate(new ParticipantId(PARTICIPANT_ID))
        then:
            result.size() == 1
            result.first().id.raw == '123'
    }

    def 'should update survey status'() {
        given:
            MongoSurvey survey1 = survey().toMongoSurvey()
            mongoOperations.save(survey1)
        when:
            mongoSurveyRepository.scheduleToFinish(new SurveyId(SURVEY_ID), new ResearcherId(RESEARCHER_ID))
        then:
            mongoOperations.findAll(MongoSurvey.class).first().status == SCHEDULED_TO_FINISH.toString()
    }
}
