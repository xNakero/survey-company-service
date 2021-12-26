package pl.wat.surveycompanyservice.scheduler

import pl.wat.surveycompanyservice.BaseIntegrationTest
import pl.wat.surveycompanyservice.domain.survey.Survey
import pl.wat.surveycompanyservice.domain.surveyhistory.MongoHistoryEntry
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipation

import static java.time.temporal.ChronoUnit.DAYS
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.SURVEY_ID
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.survey
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.surveyParticipation

class SurveySchedulerIntTest extends BaseIntegrationTest {

    def 'should process surveys that are eligible to finish and save them as HistoryEntry'() {
        given: "survey that is eligible to finish"
            Survey eligibleSurvey = survey([
                    startedAt: clock.instant().minus(7, DAYS).toString()
            ])
            mongoOperations.save(eligibleSurvey.toMongoSurvey())

            SurveyParticipation surveyParticipation1 = surveyParticipation([
                    surveyId: SURVEY_ID,
                    status: 'COMPLETED'
            ])
            mongoOperations.save(surveyParticipation1.toMongoSurveyParticipation())
        and: "survey that should be finished now but has participations in progress"
            Survey survey1 = survey([
                    surveyId: '123',
                    startedAt: clock.instant().minus(7, DAYS).toString()
            ])
            mongoOperations.save(survey1.toMongoSurvey())

            SurveyParticipation surveyParticipation2 = surveyParticipation([
                    surveyId: '123',
                    surveyParticipationId: '123'
            ])
            mongoOperations.save(surveyParticipation2.toMongoSurveyParticipation())
        and: "survey that is not eligible to be saved"
            Survey survey2 = survey([
                    surveyId: '456',
                    startedAt: clock.instant().minus(2, DAYS).toString()
            ])
            mongoOperations.save(survey2.toMongoSurvey())
        expect:
            conditions.eventually {
                mongoOperations.findAll(MongoHistoryEntry.class).size() == 1
                    with(mongoOperations.findAll(MongoHistoryEntry.class).first()) {
                        it.surveyId == eligibleSurvey.id.raw
                        it.historyParticipations.size() == 1
                        it.historyParticipations.first().id == surveyParticipation1.id.raw
                        !it.historyParticipations.first().completedWithValidCode
                    }
            }
    }
}
