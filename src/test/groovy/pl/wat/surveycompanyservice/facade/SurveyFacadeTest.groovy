package pl.wat.surveycompanyservice.facade

import pl.wat.surveycompanyservice.BaseUnitTest
import pl.wat.surveycompanyservice.TestBuilders
import pl.wat.surveycompanyservice.api.SurveyToPostDto
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileQueryParams
import pl.wat.surveycompanyservice.domain.role.AppRole
import pl.wat.surveycompanyservice.domain.survey.NoEligibleParticipantsException
import pl.wat.surveycompanyservice.domain.survey.Survey
import pl.wat.surveycompanyservice.domain.survey.SurveysWithTypeDto
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipation
import pl.wat.surveycompanyservice.shared.ResearcherId
import pl.wat.surveycompanyservice.shared.UserId

import static pl.wat.surveycompanyservice.TestBuilders.COUNTRY
import static pl.wat.surveycompanyservice.TestBuilders.PARTICIPANT_ID
import static pl.wat.surveycompanyservice.TestBuilders.RESEARCHER_ID
import static pl.wat.surveycompanyservice.TestBuilders.SURVEY_ID
import static pl.wat.surveycompanyservice.TestBuilders.personalProfile
import static pl.wat.surveycompanyservice.TestBuilders.personalProfileQueryParams
import static pl.wat.surveycompanyservice.TestBuilders.survey
import static pl.wat.surveycompanyservice.TestBuilders.surveyParticipation
import static pl.wat.surveycompanyservice.domain.role.AppRole.PARTICIPANT
import static pl.wat.surveycompanyservice.domain.role.AppRole.RESEARCHER

class SurveyFacadeTest extends BaseUnitTest {

    def 'should save survey'() {
        given:
            inMemoryPersonalProfileRepository.save(personalProfile())
            SurveyToPostDto surveyToPost = TestBuilders.surveyToPostDto([
                    personalProfileQueryParams: [
                            currentCountry: COUNTRY
                    ]
            ])
            ResearcherId researcherId = new ResearcherId('1')
        when:
            surveyFacade.saveSurvey(surveyToPost, researcherId)
        then:
            inMemorySurveyRepository.findAll().size() == 1
    }

    def 'should throw NoEligibleParticipantsException when trying to add survey with 0 eligible participants'() {
        given:
            SurveyToPostDto surveyToPost = TestBuilders.surveyToPostDto([
                    personalProfileQueryParams: [
                            currentCountry: COUNTRY
                    ]
            ])
            ResearcherId researcherId = new ResearcherId('1')
        when:
            surveyFacade.saveSurvey(surveyToPost, researcherId)
        then:
            thrown(NoEligibleParticipantsException)
    }

    def 'should use number of eligible participants if is bigger than participants declared by researcher'() {
        given:
            inMemoryPersonalProfileRepository.save(personalProfile())
            inMemoryPersonalProfileRepository.save(personalProfile([
                    participantId: '12345'
            ]))
            SurveyToPostDto surveyToPost = TestBuilders.surveyToPostDto([
                    personalProfileQueryParams: [
                            currentCountry: COUNTRY
                    ],
                    surveyParamsDto: [
                            spots: 100
                    ]
            ])
            ResearcherId researcherId = new ResearcherId('1')
        when:
            surveyFacade.saveSurvey(surveyToPost, researcherId)
        then:
            inMemorySurveyRepository.findAll().size() == 1
            inMemorySurveyRepository.findAll().first().spotsTotal == 2
    }

    def 'should get number of eligible participants'() {
        given:
            inMemoryPersonalProfileRepository.save(personalProfile())
            inMemoryPersonalProfileRepository.save(personalProfile([
                    participantId: '12345'
            ]))
            PersonalProfileQueryParams queryParams = personalProfileQueryParams([
                    currentCountry: COUNTRY
            ])
        when:
            int eligibleParticipants = surveyFacade.getNumberOfEligibleParticipants(queryParams)
        then:
            eligibleParticipants == 2
    }

    def 'should contain surveyInProgress null when there is no survey in progress'() {
        given:
            Survey survey1 = survey([eligibleParticipantIds: [PARTICIPANT_ID]])
            inMemorySurveyRepository.saveSurvey(survey1)
        when:
            SurveysWithTypeDto dto = surveyFacade.getSurveys(new UserId(PARTICIPANT_ID), PARTICIPANT.toString())
        then:
            dto.type == "PARTICIPANT"
            dto.surveys.surveyInProgress == null
            dto.surveys.availableSurveys.size() == 1
    }

    def 'should return available surveys and survey in progress, while not duplicating any survey'() {
        given:
            Survey survey1 = survey([eligibleParticipantIds: [PARTICIPANT_ID]])
            inMemorySurveyRepository.saveSurvey(survey1)
        and:
            Survey survey2 = survey([eligibleParticipantIds: [PARTICIPANT_ID], surveyId: '123'])
            SurveyParticipation surveyParticipation1 = surveyParticipation([surveyId: '123'])
            inMemorySurveyRepository.saveSurvey(survey2)
            inMemorySurveyParticipationRepository.insert(surveyParticipation1)
        when:
            SurveysWithTypeDto dto = surveyFacade.getSurveys(new UserId(PARTICIPANT_ID), PARTICIPANT.toString())
        then:
            dto.type == "PARTICIPANT"
            dto.surveys.surveyInProgress.surveyId == '123'
            dto.surveys.availableSurveys.size() == 1
            dto.surveys.availableSurveys.first().surveyId == SURVEY_ID
    }

    def 'should return all researchers surveys that are not archived yet'() {
        given:
            Survey survey1 = survey()
            inMemorySurveyRepository.saveSurvey(survey1)
        when:
            SurveysWithTypeDto dto = surveyFacade.getSurveys(new UserId(RESEARCHER_ID), RESEARCHER.toString())
        then:
            dto.type == "RESEARCHER"
            dto.surveys.activeSurveys.size() == 1
            dto.surveys.activeSurveys.first().surveyId == SURVEY_ID
    }

}
