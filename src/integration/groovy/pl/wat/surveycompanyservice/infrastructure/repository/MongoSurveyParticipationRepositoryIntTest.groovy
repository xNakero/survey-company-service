package pl.wat.surveycompanyservice.infrastructure.repository

import pl.wat.surveycompanyservice.BaseIntegrationTest
import pl.wat.surveycompanyservice.domain.surveyparticipation.MongoSurveyParticipation
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipation
import pl.wat.surveycompanyservice.domain.surveyparticipation.ParticipationStatus
import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.SurveyId
import pl.wat.surveycompanyservice.shared.SurveyParticipationId

import java.time.Instant

import static pl.wat.surveycompanyservice.IntegrationTestBuilders.PARTICIPANT_ID
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.SURVEY_ID
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.SURVEY_PARTICIPATION_ID
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.surveyParticipation
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.surveyParticipation
import static pl.wat.surveycompanyservice.domain.surveyparticipation.ParticipationStatus.CANCELLED
import static pl.wat.surveycompanyservice.domain.surveyparticipation.ParticipationStatus.COMPLETED
import static pl.wat.surveycompanyservice.domain.surveyparticipation.ParticipationStatus.IN_PROGRESS
import static pl.wat.surveycompanyservice.domain.surveyparticipation.ParticipationStatus.TIMEOUT

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

    def 'should update SurveyParticipation status, finishedAt and completion code'() {
        given:
            mongoOperations.save(surveyParticipation().toMongoSurveyParticipation())
            String completionCode = 'COMPLETED'
            ParticipationStatus status = COMPLETED
        Instant finishedAt = clock.instant()
        when:
            mongoSurveyParticipationRepository.update(new SurveyParticipationId(SURVEY_PARTICIPATION_ID), status, completionCode, finishedAt)
        then:
            with(mongoOperations.findAll(MongoSurveyParticipation.class).find {it.id == SURVEY_PARTICIPATION_ID}) {
                it.status == status.toString()
                it.completionCode == completionCode
                it.finishedAt == finishedAt
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

    def 'should update all surveyParticipation that have timed out'() {
        given:
            mongoOperations.save(surveyParticipation().toMongoSurveyParticipation())
            mongoOperations.save(surveyParticipation([
                    surveyParticipationId: "2-2",
                    hasToFinishUntil: clock.instant().minusSeconds(1).toString()
            ]).toMongoSurveyParticipation())
        when:
            mongoSurveyParticipationRepository.finishAllUnfinishedInTime(clock.instant())
        then:
            List results = mongoOperations.findAll(MongoSurveyParticipation.class)
            results.size() == 2
        and:
            results.find {it.id == "2-2"}.status == TIMEOUT.toString()
            results.find {it.id == SURVEY_PARTICIPATION_ID}.status == IN_PROGRESS.toString()
    }

    def 'should find surveyParticipations in progress only of particular surveys'() {
        given:
            MongoSurveyParticipation participation1 = surveyParticipation().toMongoSurveyParticipation()
            MongoSurveyParticipation participation2 = surveyParticipation([
                    status: 'CANCELLED',
                    surveyParticipationId: '123'
            ]).toMongoSurveyParticipation()
            MongoSurveyParticipation participation3 = surveyParticipation([
                    surveyParticipationId: '456',
                    surveyId: '12345'
            ]).toMongoSurveyParticipation()
        and:
            mongoOperations.save(participation1)
            mongoOperations.save(participation2)
            mongoOperations.save(participation3)
        when:
            List result = mongoSurveyParticipationRepository.findInProgressBySurveyIds([new SurveyId(SURVEY_ID)])
        then:
            result.size() == 1
            result.first().id.raw == SURVEY_PARTICIPATION_ID
    }

    def 'should find surveyParticipations by surveyIds'() {
        given:
            MongoSurveyParticipation participation1 = surveyParticipation().toMongoSurveyParticipation()
            MongoSurveyParticipation participation2 = surveyParticipation([
                    surveyParticipationId: '123'
            ]).toMongoSurveyParticipation()
            MongoSurveyParticipation participation3 = surveyParticipation([
                    surveyParticipationId: '456',
                    surveyId: '12345'
            ]).toMongoSurveyParticipation()
        and:
            mongoOperations.save(participation1)
            mongoOperations.save(participation2)
            mongoOperations.save(participation3)
        when:
            List result = mongoSurveyParticipationRepository.findBySurveyIds([new SurveyId(SURVEY_ID)])
        then:
            result.size() == 2
            result.id.raw.containsAll([SURVEY_PARTICIPATION_ID, '123'])
    }

    def 'should remove surveys by surveyIds'() {
        given:
            MongoSurveyParticipation participation1 = surveyParticipation().toMongoSurveyParticipation()
            MongoSurveyParticipation participation2 = surveyParticipation([
                    surveyParticipationId: '123'
            ]).toMongoSurveyParticipation()
            MongoSurveyParticipation participation3 = surveyParticipation([
                    surveyParticipationId: '456',
                    surveyId: '12345'
            ]).toMongoSurveyParticipation()
        and:
            mongoOperations.save(participation1)
            mongoOperations.save(participation2)
            mongoOperations.save(participation3)
        when:
            mongoSurveyParticipationRepository.removeBySurveyIds([new SurveyId(SURVEY_ID)])
        then:
            mongoOperations.findAll(MongoSurveyParticipation.class).size() == 1
    }

    def 'should return survey participations in progress by participantIds'() {
        given:
            MongoSurveyParticipation surveyParticipation1 = surveyParticipation().toMongoSurveyParticipation()
            MongoSurveyParticipation surveyParticipation2 = surveyParticipation([
                    status: COMPLETED.toString(),
                    surveyParticipationId: '123'
            ]).toMongoSurveyParticipation()
            MongoSurveyParticipation surveyParticipation3 = surveyParticipation([
                    surveyParticipationId: '456',
                    participantId: '123'
            ]).toMongoSurveyParticipation()
        and:
            mongoOperations.save(surveyParticipation1)
            mongoOperations.save(surveyParticipation2)
            mongoOperations.save(surveyParticipation3)
        when:
            SurveyParticipation participation = mongoSurveyParticipationRepository.findInProgressByParticipantId(new ParticipantId(PARTICIPANT_ID))
        then:
            participation.id.raw == SURVEY_PARTICIPATION_ID
    }

    def 'should return participations with different statuses than IN_PROGRESS'() {
        given:
            MongoSurveyParticipation surveyParticipation1 = surveyParticipation().toMongoSurveyParticipation()
            MongoSurveyParticipation surveyParticipation2 = surveyParticipation([
                    surveyParticipationId: '123',
                    status: COMPLETED.toString()
            ]).toMongoSurveyParticipation()
            MongoSurveyParticipation surveyParticipation3 = surveyParticipation([
                    surveyParticipationId: '456',
                    status: TIMEOUT.toString()
            ]).toMongoSurveyParticipation()
            MongoSurveyParticipation surveyParticipation4 = surveyParticipation([
                    surveyParticipationId: '789',
                    status: CANCELLED.toString()
            ]).toMongoSurveyParticipation()
        and:
            mongoOperations.save(surveyParticipation1)
            mongoOperations.save(surveyParticipation2)
            mongoOperations.save(surveyParticipation3)
            mongoOperations.save(surveyParticipation4)
        when:
            List result = mongoSurveyParticipationRepository.findNotInProgress(new ParticipantId(PARTICIPANT_ID))
        then:
            result.size() == 3
            result.id.raw as Set == ['123', '456', '789'] as Set
    }
}
