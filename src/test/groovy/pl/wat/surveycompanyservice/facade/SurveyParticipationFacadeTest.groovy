package pl.wat.surveycompanyservice.facade

import pl.wat.surveycompanyservice.BaseUnitTest
import pl.wat.surveycompanyservice.TestBuilders
import pl.wat.surveycompanyservice.api.ParticipationModificationDto
import pl.wat.surveycompanyservice.domain.surveyparticipation.AlreadyParticipatesInOtherSurveyException
import pl.wat.surveycompanyservice.domain.surveyparticipation.NoCompletionCodeException
import pl.wat.surveycompanyservice.domain.surveyparticipation.NoFreeSpotsException
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipationNotInProgressException
import pl.wat.surveycompanyservice.domain.surveyparticipation.UnqualifiedParticipantException
import pl.wat.surveycompanyservice.domain.surveyparticipation.WrongParticipantException
import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.SurveyId
import pl.wat.surveycompanyservice.shared.SurveyParticipationId

import static pl.wat.surveycompanyservice.TestBuilders.HAS_TO_FINISH_UNTIL
import static pl.wat.surveycompanyservice.TestBuilders.PARTICIPANT_ID
import static pl.wat.surveycompanyservice.TestBuilders.SPOTS_TOTAL
import static pl.wat.surveycompanyservice.TestBuilders.SURVEY_ID
import static pl.wat.surveycompanyservice.TestBuilders.SURVEY_PARTICIPATION_ID
import static pl.wat.surveycompanyservice.TestBuilders.survey
import static pl.wat.surveycompanyservice.TestBuilders.surveyParticipation
import static pl.wat.surveycompanyservice.api.Action.CANCEL
import static pl.wat.surveycompanyservice.api.Action.COMPLETE
import static pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyStatus.CANCELLED
import static pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyStatus.COMPLETED
import static pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyStatus.IN_PROGRESS

class SurveyParticipationFacadeTest extends BaseUnitTest {

    def 'should add participation'() {
        given:
            inMemorySurveyRepository.saveSurvey(survey([
                    eligibleParticipantIds: PARTICIPANT_ID
            ]))
        when:
            surveyParticipationFacade.participate(new ParticipantId(PARTICIPANT_ID), new SurveyId(SURVEY_ID))
        then:
            inMemorySurveyParticipationRepository.findAll().size() == 1
            with(inMemorySurveyParticipationRepository.findAll().first()) {
                it.startedAt == clock.instant()
                it.participantId.raw == PARTICIPANT_ID
                it.surveyId.raw == SURVEY_ID
                it.hasToFinishUntil == clock.instant().plusSeconds(TestBuilders.TIME_TO_COMPLETE_IN_SECONDS)
            }
    }

    def 'should throw UnqualifiedParticipantException if participant is not on eligible participants list'() {
        given:
            inMemorySurveyRepository.saveSurvey(survey())
        when:
            surveyParticipationFacade.participate(new ParticipantId(PARTICIPANT_ID), new SurveyId(SURVEY_ID))
        then:
            thrown(UnqualifiedParticipantException)
            inMemorySurveyParticipationRepository.findAll().size() == 0
    }

    def 'should throw NoFreeSpotsException if there are no free spots for a survey'() {
        given:
            inMemorySurveyRepository.saveSurvey(survey([
                    spotsTaken: SPOTS_TOTAL,
                    eligibleParticipantIds: PARTICIPANT_ID
            ]))
        when:
            surveyParticipationFacade.participate(new ParticipantId(PARTICIPANT_ID), new SurveyId(SURVEY_ID))
        then:
            thrown(NoFreeSpotsException)
            inMemorySurveyParticipationRepository.findAll().size() == 0
    }

    def 'should throw AlreadyParticipatesInOtherSurveyException if participant currently does other survey'() {
        given:
            inMemorySurveyRepository.saveSurvey(survey())
            inMemorySurveyParticipationRepository.insert(surveyParticipation())
        when:
            surveyParticipationFacade.participate(new ParticipantId(PARTICIPANT_ID), new SurveyId(SURVEY_ID))
        then:
            thrown(AlreadyParticipatesInOtherSurveyException)
            inMemorySurveyParticipationRepository.findAll().size() == 1
    }

    def 'should update participation for cancel request'() {
        given:
            inMemorySurveyParticipationRepository.insert(surveyParticipation())
            ParticipationModificationDto participationModificationDto = new ParticipationModificationDto(CANCEL, null)
        when:
            surveyParticipationFacade.manageParticipation(
                    new ParticipantId(PARTICIPANT_ID),
                    new SurveyParticipationId(SURVEY_PARTICIPATION_ID),
                    participationModificationDto
            )
        then:
            inMemorySurveyParticipationRepository.findAll().find {it.id.raw == SURVEY_PARTICIPATION_ID}.status == CANCELLED
    }

    def 'should update participation for completion request when completionCode is present'() {
        given:
            inMemorySurveyParticipationRepository.insert(surveyParticipation())
            ParticipationModificationDto participationModificationDto = new ParticipationModificationDto(COMPLETE, 'COMPLETIONCODE')
        when:
            surveyParticipationFacade.manageParticipation(
                    new ParticipantId(PARTICIPANT_ID),
                    new SurveyParticipationId(SURVEY_PARTICIPATION_ID),
                    participationModificationDto
            )
        then:
            inMemorySurveyParticipationRepository.findAll().find {it.id.raw == SURVEY_PARTICIPATION_ID}.status == COMPLETED
    }

    def 'should throw NoCompletionCodeException if there is no completionCode on completion request'() {
        given:
            inMemorySurveyParticipationRepository.insert(surveyParticipation())
            ParticipationModificationDto participationModificationDto = new ParticipationModificationDto(COMPLETE, null)
        when:
            surveyParticipationFacade.manageParticipation(
                    new ParticipantId(PARTICIPANT_ID),
                    new SurveyParticipationId(SURVEY_PARTICIPATION_ID),
                    participationModificationDto
            )
        then:
            thrown(NoCompletionCodeException)
            inMemorySurveyParticipationRepository.findAll().find {it.id.raw == SURVEY_PARTICIPATION_ID}.status == IN_PROGRESS
    }

    def 'should throw SurveyParticipationNotInProgressException if time to finish survey has passed'() {
        given:
            inMemorySurveyParticipationRepository.insert(surveyParticipation())
            ParticipationModificationDto participationModificationDto = new ParticipationModificationDto(CANCEL, null)
        and:
            clock.setNow(HAS_TO_FINISH_UNTIL.plusSeconds(1))
        when:
            surveyParticipationFacade.manageParticipation(
                    new ParticipantId(PARTICIPANT_ID),
                    new SurveyParticipationId(SURVEY_PARTICIPATION_ID),
                    participationModificationDto
            )
        then:
            thrown(SurveyParticipationNotInProgressException)
    }

    def 'should throw SurveyParticipationNotInProgressException if status is not in progress'() {
        given:
            inMemorySurveyParticipationRepository.insert(surveyParticipation([status: COMPLETED]))
            ParticipationModificationDto participationModificationDto = new ParticipationModificationDto(CANCEL, null)
        when:
            surveyParticipationFacade.manageParticipation(
                    new ParticipantId(PARTICIPANT_ID),
                    new SurveyParticipationId(SURVEY_PARTICIPATION_ID),
                    participationModificationDto
            )
        then:
            thrown(SurveyParticipationNotInProgressException)
    }

    def 'should throw WrongParticipantException when trying to access other participant survey participation'() {
        given:
            inMemorySurveyParticipationRepository.insert(surveyParticipation([status: COMPLETED]))
            ParticipationModificationDto participationModificationDto = new ParticipationModificationDto(CANCEL, null)
        when:
            surveyParticipationFacade.manageParticipation(
                    new ParticipantId('123456789'),
                    new SurveyParticipationId(SURVEY_PARTICIPATION_ID),
                    participationModificationDto
            )
        then:
            thrown(WrongParticipantException)
    }
}

