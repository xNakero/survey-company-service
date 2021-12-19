package pl.wat.surveycompanyservice.scheduler

import pl.wat.surveycompanyservice.BaseIntegrationTest
import pl.wat.surveycompanyservice.domain.surveyparticipation.MongoSurveyParticipation
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyStatus

import static pl.wat.surveycompanyservice.IntegrationTestBuilders.SURVEY_PARTICIPATION_ID
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.SURVEY_STATUS
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.surveyParticipation
import static pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyStatus.IN_PROGRESS
import static pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyStatus.TIMEOUT

class SurveyParticipationSchedulerIntTest extends BaseIntegrationTest{

    def "should timeout survey participation that should have ended by now"() {
        given:
            mongoOperations.save(surveyParticipation().toMongoSurveyParticipation())
            mongoOperations.save(surveyParticipation([
                    surveyParticipationId: "2-2",
                    hasToFinishUntil: clock.instant().plusSeconds(1).toString()
            ]).toMongoSurveyParticipation())
        when:
            clock.setNow(clock.instant().plusSeconds(2))
        then:
            conditions.eventually {
                mongoOperations.findAll(MongoSurveyParticipation.class).find {it.id == "2-2"}.status == TIMEOUT.toString()
                mongoOperations.findAll(MongoSurveyParticipation.class).find {it.id == SURVEY_PARTICIPATION_ID}.status == IN_PROGRESS.toString()
            }
        when:
            clock.setNow(clock.instant().plusSeconds(1199))
        then:
        conditions.eventually {
            mongoOperations.findAll(MongoSurveyParticipation.class).find {it.id == "2-2"}.status == TIMEOUT.toString()
            mongoOperations.findAll(MongoSurveyParticipation.class).find {it.id == SURVEY_PARTICIPATION_ID}.status == TIMEOUT.toString()
        }
    }
}
