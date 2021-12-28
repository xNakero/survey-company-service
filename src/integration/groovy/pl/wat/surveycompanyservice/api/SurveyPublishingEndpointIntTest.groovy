package pl.wat.surveycompanyservice.api

import groovyx.net.http.HttpResponseDecorator
import pl.wat.surveycompanyservice.BaseIntegrationTest
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileQueryParams
import pl.wat.surveycompanyservice.domain.survey.MongoSurvey

import static pl.wat.surveycompanyservice.IntegrationTestBuilders.COUNTRY
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.PASSWORD
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.RESEARCHER_USERNAME
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.TITLE
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.personalProfile
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.personalProfileQueryParams
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.surveyToPostDto

class SurveyPublishingEndpointIntTest extends BaseIntegrationTest{

    def 'should post survey'() {
        given:
            authAs(RESEARCHER_USERNAME, PASSWORD)
        and:
            elasticPersonalProfileRepository.save(personalProfile())
            SurveyToPostDto surveyToPost = surveyToPostDto([
                    personalProfileQueryParams: [
                            currentCountry: COUNTRY
                    ]
            ])
            String body = objectMapper.writeValueAsString(surveyToPost)
        when:
            HttpResponseDecorator response = postSurvey(body)
        then:
            response.status == 201
        and:
            mongoOperations.findAll(MongoSurvey.class).size() == 1
            with(mongoOperations.findAll(MongoSurvey.class).first()) {
                it.title == TITLE
                it.spotsTotal == 1
            }
    }

    def 'should throw 400 if there are no eligible participants'() {
        given:
            authAs(RESEARCHER_USERNAME, PASSWORD)
        and:
            SurveyToPostDto surveyToPost = surveyToPostDto([
                    personalProfileQueryParams: [
                            currentCountry: COUNTRY
                    ]
            ])
            String body = objectMapper.writeValueAsString(surveyToPost)
        when:
            HttpResponseDecorator response = postSurvey(body)
        then:
            response.status == 400
            response.data.errors.first() == "There are no eligible participants."
    }

    def 'should throw 400 if validation on request values fails'() {
        given:
            authAs(RESEARCHER_USERNAME, PASSWORD)
        and:
            SurveyToPostDto surveyToPost = surveyToPostDto([
                    personalProfileQueryParams: [
                            currentCountry: 'RUSSIA'
                    ],
                    surveyParamsDto: [
                            title: 't'
                    ]
            ])
            String body = objectMapper.writeValueAsString(surveyToPost)
        when:
            HttpResponseDecorator response = postSurvey(body)
        then:
            response.status == 400
            response.data.errors.containsAll([
                    "Length of title has to be between 3 and 100 characters.",
                    "There is no such country."
            ] as Set)
    }

    def 'should return number of eligible participants'() {
        given:
            authAs(RESEARCHER_USERNAME, PASSWORD)
        and:
            PersonalProfileQueryParams queryParams = personalProfileQueryParams([
                    countryOfBirth: COUNTRY
            ])
            String body = objectMapper.writeValueAsString(queryParams)
            elasticPersonalProfileRepository.save(personalProfile())
        when:
            HttpResponseDecorator response = getParticipantsCount(body)
        then:
            response.status == 200
            response.data.numOfUsers == 1
    }

    private HttpResponseDecorator postSurvey(String body) {
        return restClient.post(path: '/surveys', body: body)
    }

    private HttpResponseDecorator getParticipantsCount(String body) {
        return restClient.post(path: '/surveys/participants-count', body: body)
    }

}
