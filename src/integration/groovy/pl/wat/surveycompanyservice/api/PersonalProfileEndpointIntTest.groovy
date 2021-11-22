package pl.wat.surveycompanyservice.api

import groovyx.net.http.HttpResponseDecorator
import pl.wat.surveycompanyservice.BaseIntegrationTest

import static pl.wat.surveycompanyservice.IntegrationTestBuilders.CIVIL_STATUS
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.COUNTRY
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.DATE_OF_BIRTH
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.EDUCATION_LEVEL
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.EMPLOYMENT_STATUS
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.FORM_OF_EMPLOYMENT
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.INDUSTRY
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.IS_STUDENT
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.LANGUAGE
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.MONTHLY_INCOME
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.PARTICIPANT_USERNAME
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.PASSWORD
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.POLITICAL_SIDE
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.RESEARCHER_USERNAME
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.personalProfileDto

class PersonalProfileEndpointIntTest extends BaseIntegrationTest {

    def 'should update PersonalProfile'() {
        given:
            String profileDto = objectMapper.writeValueAsString(personalProfileDto())
            authAs(PARTICIPANT_USERNAME, PASSWORD)
        when:
            HttpResponseDecorator response = updateProfile(profileDto)
        then:
            response.status == 200
            with(response.data) {
                basicInformation.dateOfBirth == DATE_OF_BIRTH.toString()
                basicInformation.civilStatus == CIVIL_STATUS
                demographics.countryOfBirth == COUNTRY
                demographics.nationality == COUNTRY
                demographics.currentCountry == COUNTRY
                demographics.firstLanguage == LANGUAGE
                education.highestEducationLevelAchieved == EDUCATION_LEVEL
                education.isStudent == IS_STUDENT
                work.monthlyIncome == MONTHLY_INCOME
                work.employmentStatus == EMPLOYMENT_STATUS
                work.formOfEmployment == FORM_OF_EMPLOYMENT
                work.industry == INDUSTRY
                politicalViews.politicalSide == POLITICAL_SIDE
            }
    }

    def 'should return 400 if validation values on request are not valid'() {
        given:
            String profileDto = objectMapper.writeValueAsString(personalProfileDto([
                    civilStatus: 'STATUS',
                    countryOfBirth: 'COUNTRY',
                    nationality: 'COUNTRY',
                    currentCountry: 'COUNTRY',
                    firstLanguage: 'LANGUAGE',
                    highestEducationLevelAchieved: 'EDUCATION',
                    monthlyIncome: -1000,
                    employmentStatus: 'STATUS',
                    formOfEmployment: 'FORM',
                    industry: 'INDUSTRY',
                    politicalSide: 'SIDE'
            ]))
            authAs(PARTICIPANT_USERNAME, PASSWORD)
        when:
            HttpResponseDecorator response = updateProfile(profileDto)
        then:
            response.status == 400
            response.data.errors.containsAll([
                    'There is no such civil status.',
                    'Minimum income has to be 0.',
                    'There is no such country for currentCountry.',
                    'There is no such political side.',
                    'There is no such country for countryOBirth.',
                    'There is no such industry.',
                    'There is no such education level.',
                    'There is no such employment status.',
                    'There is no such country for nationality.',
                    'There is no such form of employment.',
                    'There is no such language.'
            ] as Set)
    }

    def 'should return profile for a user'() {
        given:
            authAs(PARTICIPANT_USERNAME, PASSWORD)
            Thread.sleep(1000)
        when:
            HttpResponseDecorator response = getProfile()
        then:
            response.status == 200
    }

//    def 'should return 403 when researcher tries to access personal profile'() {
//        given:
//            authAs(RESEARCHER_USERNAME, PASSWORD)
//        when:
//            HttpResponseDecorator response = getProfile()
//        then:
//            response.status == 403
//    }

    private HttpResponseDecorator getProfile() {
        return restClient.get(path: '/personal-profile')
    }

    private HttpResponseDecorator updateProfile(String body) {
        return restClient.put(path: '/personal-profile', body: body)
    }
}
