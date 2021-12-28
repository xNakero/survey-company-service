package pl.wat.surveycompanyservice.api

import groovyx.net.http.HttpResponseDecorator
import pl.wat.surveycompanyservice.BaseIntegrationTest
import pl.wat.surveycompanyservice.IntegrationTestBuilders
import pl.wat.surveycompanyservice.domain.survey.Survey
import pl.wat.surveycompanyservice.domain.surveyhistory.HistoryEntry
import pl.wat.surveycompanyservice.domain.surveyhistory.HistoryParticipation
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipation

import java.time.temporal.ChronoUnit

import static java.time.temporal.ChronoUnit.MINUTES
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.COMPLETION_CODE
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.DESCRIPTION
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.FINISHED_AT
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.HISTORY_ENTRY_ID
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.PARTICIPANT_USERNAME
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.PASSWORD
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.RESEARCHER_USERNAME
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.SPOTS_TAKEN
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.SPOTS_TOTAL
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.STARTED_AT
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.SURVEY_PARTICIPATION_ID
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.TITLE
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.historyEntry
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.historyParticipation
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.survey
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.surveyParticipation

class HistoryEntryEndpointIntTest extends BaseIntegrationTest{

    def 'should return entries from history for participant'() {
        given:
            authAs(PARTICIPANT_USERNAME, PASSWORD)
            String principalId = getPrincipalId(PARTICIPANT_USERNAME).toString()
        and:
            HistoryEntry historyEntry1 = historyEntry()
            HistoryParticipation historyParticipation1 = historyParticipation([participantId: principalId, startedAt: STARTED_AT.plus(5, MINUTES).toString()])
            HistoryParticipation historyParticipation2 = historyParticipation()
            historyEntry1.historyParticipations.addAll([historyParticipation1, historyParticipation2])
            mongoOperations.save(historyEntry1.toMongoHistoryEntry())
        and:
            HistoryEntry historyEntry2 = historyEntry([historyEntryId: '123'])
            HistoryParticipation historyParticipation3 = historyParticipation([
                    participantId: principalId,
                    completedWithValidCode: false,
                    completionCode: "XD"
            ])
            historyEntry2.historyParticipations.addAll([historyParticipation3])
            mongoOperations.save(historyEntry2.toMongoHistoryEntry())
        and:
            Survey survey1 = survey()
            SurveyParticipation surveyParticipation = surveyParticipation([
                    participantId: principalId,
                    status: "COMPLETED",
                    startedAt: STARTED_AT.plus(10, MINUTES).toString()
            ])
            mongoOperations.save(survey1.toMongoSurvey())
            mongoOperations.save(surveyParticipation.toMongoSurveyParticipation())
        when:
            HttpResponseDecorator response = restClient.get([path: "/survey-history"])
        then:
            response.status == 200
            response.data == [
                    "type"      : "PARTICIPANT",
                    "surveys"   : [
                            [
                                    "id": SURVEY_PARTICIPATION_ID,
                                    "title": TITLE,
                                    "timeToComplete": "01:00:00",
                                    "description": DESCRIPTION,
                                    "startedAt": STARTED_AT.plus(10, MINUTES).toString(),
                                    "finishedAt": FINISHED_AT.toString(),
                                    "completionCode": null,
                                    "completedWithValidCode": false
                            ],
                            [
                                    "id"                    : HISTORY_ENTRY_ID,
                                    "title"                 : TITLE,
                                    "timeToComplete"        : "01:00:00",
                                    "description"           : DESCRIPTION,
                                    "startedAt"             : STARTED_AT.plus(5, MINUTES).toString(),
                                    "finishedAt"            : FINISHED_AT.toString(),
                                    "completionCode"        : null,
                                    "completedWithValidCode": true
                            ],
                            [
                                    "id"                    : "123",
                                    "title"                 : TITLE,
                                    "timeToComplete"        : "01:00:00",
                                    "description"           : DESCRIPTION,
                                    "startedAt"             : STARTED_AT.toString(),
                                    "finishedAt"            : FINISHED_AT.toString(),
                                    "completionCode"        : "XD",
                                    "completedWithValidCode": false
                            ]
                    ]
            ]
    }

    def 'should return entries from history for researcher'() {
        given:
            authAs(RESEARCHER_USERNAME, PASSWORD)
            String principalId = getPrincipalId(RESEARCHER_USERNAME).toString()
        and:
            HistoryEntry historyEntry1 = historyEntry([
                    researcherId: principalId,
                    startedAt: STARTED_AT.plus(5, MINUTES).toString()
            ])
            HistoryParticipation historyParticipation1 = historyParticipation([
                    participantId: '123',
                    completedWithValidCode: false
            ])
            HistoryParticipation historyParticipation2 = historyParticipation()
            historyEntry1.historyParticipations.addAll([historyParticipation1, historyParticipation2])
            mongoOperations.save(historyEntry1.toMongoHistoryEntry())
        and:
            HistoryEntry historyEntry2 = historyEntry([
                    historyEntryId: '123',
                    title: "new title",
                    researcherId: principalId
            ])
            HistoryParticipation historyParticipation3 = historyParticipation([completedWithValidCode: false])
            historyEntry2.historyParticipations.addAll([historyParticipation3])
            mongoOperations.save(historyEntry2.toMongoHistoryEntry())
        when:
            HttpResponseDecorator response = restClient.get([path: "/survey-history"]) as HttpResponseDecorator
        then:
            response.status == 200
            response.data == [
                    "type"      : "RESEARCHER",
                    "surveys"   : [
                            [
                                    "id"                : HISTORY_ENTRY_ID,
                                    "title"             : TITLE,
                                    "url"               : IntegrationTestBuilders.URL,
                                    "timeToComplete"    : "01:00:00",
                                    "description"       : DESCRIPTION,
                                    "spotsTotal"        : SPOTS_TOTAL,
                                    "spotsTaken"        : SPOTS_TAKEN,
                                    "validSubmissions"  : 1,
                                    "completionCode"    : COMPLETION_CODE,
                                    "startedAt"         : STARTED_AT.plus(5, MINUTES).toString(),
                                    "finishedAt"        : FINISHED_AT.toString()
                            ],
                            [
                                    "id"                : "123",
                                    "title"             : "new title",
                                    "url"               : IntegrationTestBuilders.URL,
                                    "timeToComplete"    : "01:00:00",
                                    "description"       : DESCRIPTION,
                                    "spotsTotal"        : SPOTS_TOTAL,
                                    "spotsTaken"        : SPOTS_TAKEN,
                                    "validSubmissions"  : 0,
                                    "completionCode"    : COMPLETION_CODE,
                                    "startedAt"         : STARTED_AT.toString(),
                                    "finishedAt"        : FINISHED_AT.toString()
                            ]
                    ]
            ]
    }
}
