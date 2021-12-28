package pl.wat.surveycompanyservice.api

import groovyx.net.http.HttpResponseDecorator
import pl.wat.surveycompanyservice.BaseIntegrationTest
import pl.wat.surveycompanyservice.IntegrationTestBuilders
import pl.wat.surveycompanyservice.domain.survey.Survey
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipation

import static pl.wat.surveycompanyservice.IntegrationTestBuilders.COMPLETION_CODE
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.DESCRIPTION
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.HAS_TO_FINISH_UNTIL
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.PARTICIPANT_USERNAME
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.PARTICIPATION_STATUS
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.PASSWORD
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.RESEARCHER_USERNAME
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.SPOTS_TAKEN
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.SPOTS_TOTAL
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.STARTED_AT
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.SURVEY_ID
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.SURVEY_PARTICIPATION_ID
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.SURVEY_STARTED_AT
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.TITLE
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.survey
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.surveyParticipation

class SurveyAccessEndpointIntTest extends BaseIntegrationTest{

    def 'should return active surveys for researcher'() {
        given:
            authAs(RESEARCHER_USERNAME, PASSWORD)
            String principalId = getPrincipalId(RESEARCHER_USERNAME).toString()
        and:
            Survey survey1 = survey([researcherId: principalId])
            Survey survey2 = survey([researcherId: principalId, surveyId: '123'])
        and:
            mongoOperations.save(survey1.toMongoSurvey())
            mongoOperations.save(survey2.toMongoSurvey())
        when:
            HttpResponseDecorator response = restClient.get([path: "/surveys"]) as HttpResponseDecorator
        then:
            response.status == 200
            response.data == [
                    "type"      : "RESEARCHER",
                    "surveys"   : [
                            "activeSurveys": [
                                    [
                                            "surveyId": SURVEY_ID,
                                            "title": TITLE,
                                            "url": IntegrationTestBuilders.URL,
                                            "timeToComplete": "01:00:00",
                                            "description": DESCRIPTION,
                                            "spotsTotal": SPOTS_TOTAL,
                                            "spotsTaken": SPOTS_TAKEN,
                                            "completionCode": COMPLETION_CODE,
                                            "startedAt": SURVEY_STARTED_AT.toString()
                                    ],
                                    [
                                            "surveyId": "123",
                                            "title": TITLE,
                                            "url": IntegrationTestBuilders.URL,
                                            "timeToComplete": "01:00:00",
                                            "description": DESCRIPTION,
                                            "spotsTotal": SPOTS_TOTAL,
                                            "spotsTaken": SPOTS_TAKEN,
                                            "completionCode": COMPLETION_CODE,
                                            "startedAt": SURVEY_STARTED_AT.toString()
                                    ]
                            ]
                    ]
            ]
    }

    def 'should return surveys for participant'() {
        given:
            authAs(PARTICIPANT_USERNAME, PASSWORD)
            String principalId = getPrincipalId(PARTICIPANT_USERNAME).toString()
        and:
            SurveyParticipation participationInProgress = surveyParticipation([participantId: principalId])
            Survey survey1 = survey([participationIds: [principalId]])
            mongoOperations.save(participationInProgress.toMongoSurveyParticipation())
            mongoOperations.save(survey1.toMongoSurvey())
        and:
            Survey survey2 = survey([surveyId: '123', eligibleParticipantIds: [principalId]])
            mongoOperations.save(survey2.toMongoSurvey())
        and:
            Survey survey3 = survey([surveyId: '456'])
            mongoOperations.save(survey3.toMongoSurvey())
        when:
            HttpResponseDecorator response = restClient.get([path: "/surveys"]) as HttpResponseDecorator
        then:
            response.status == 200
            response.data == [
                    "type"      : "PARTICIPANT",
                    "surveys"   : [
                            "surveyInProgress": [
                                    "surveyId": SURVEY_ID,
                                    "participationId": SURVEY_PARTICIPATION_ID,
                                    "title": TITLE,
                                    "url": IntegrationTestBuilders.URL,
                                    "timeToComplete": "01:00:00",
                                    "description": DESCRIPTION,
                                    "freeSpots": 12,
                                    "status": PARTICIPATION_STATUS,
                                    "startedAt": STARTED_AT.toString(),
                                    "hasToFinishUntil": HAS_TO_FINISH_UNTIL.toString(),
                                    "completionCode": null
                            ],
                            "availableSurveys": [
                                    [
                                            "surveyId": "123",
                                            "participationId": null,
                                            "title": TITLE,
                                            "url": IntegrationTestBuilders.URL,
                                            "timeToComplete": "01:00:00",
                                            "description": DESCRIPTION,
                                            "freeSpots": 12,
                                            "status": null,
                                            "startedAt": null,
                                            "hasToFinishUntil": null,
                                            "completionCode": null
                                    ]
                            ]
                    ]
            ]
    }
}
