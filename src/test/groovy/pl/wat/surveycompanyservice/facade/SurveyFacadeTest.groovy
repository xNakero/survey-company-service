package pl.wat.surveycompanyservice.facade

import pl.wat.surveycompanyservice.BaseUnitTest
import pl.wat.surveycompanyservice.TestBuilders
import pl.wat.surveycompanyservice.api.SurveyToPostDto
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileQueryParams
import pl.wat.surveycompanyservice.domain.survey.NoEligibleParticipantsException
import pl.wat.surveycompanyservice.shared.ResearcherId

import static pl.wat.surveycompanyservice.TestBuilders.COUNTRY
import static pl.wat.surveycompanyservice.TestBuilders.personalProfile
import static pl.wat.surveycompanyservice.TestBuilders.personalProfileQueryParams

class SurveyFacadeTest extends BaseUnitTest{

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

}
