package pl.wat.surveycompanyservice.api

import groovyx.net.http.HttpResponseDecorator
import pl.wat.surveycompanyservice.BaseIntegrationTest
import pl.wat.surveycompanyservice.domain.surveyparticipation.MongoSurveyParticipation
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyStatus
import spock.lang.Ignore
import spock.lang.Unroll

import static pl.wat.surveycompanyservice.IntegrationTestBuilders.COMPLETION_CODE
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.PARTICIPANT_USERNAME
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.PASSWORD
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.SPOTS_TOTAL
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.SURVEY_ID
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.SURVEY_PARTICIPATION_ID
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.survey
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.surveyParticipation
import static pl.wat.surveycompanyservice.api.Action.CANCEL
import static pl.wat.surveycompanyservice.api.Action.COMPLETE

class SurveyParticipationEndpointIntTest extends BaseIntegrationTest{

    def "should participate in a survey"() {
        given:
            String participantId = userRepository.findByUsername(PARTICIPANT_USERNAME).userId
            mongoSurveyRepository.saveSurvey(survey([eligibleParticipantIds: [participantId]]))
        when:
            HttpResponseDecorator response = participate()
        then:
            response.status == 200
        and:
            List participations = mongoOperations.findAll(MongoSurveyParticipation.class)
            participations.size() == 1
            participations.first().participantId == participantId
    }

    def "should return 409 when trying to participate in a survey that participants already participates in"() {
        given:
            mongoSurveyRepository.saveSurvey(survey())
            mongoSurveyParticipationRepository.insert(surveyParticipation())
        when:
            HttpResponseDecorator response = participate()
        then:
            response.status == 409
            response.data.errors.first() == "Participant with id: ${getPrincipalId()} did not qualify for the survey: 61cb2fbf-e83a-4fd2-9d7b-879686653699"

    }

    def "should return 409 when trying to participate in a survey a participant is not qualified to"() {
        given:
            mongoSurveyRepository.saveSurvey(survey([eligibleParticipantIds: ["98765"]]))
        when:
            HttpResponseDecorator response = participate()
        then:
            response.status == 409
            response.data.errors.first() == "Participant with id: ${getPrincipalId()} did not qualify for the survey: 61cb2fbf-e83a-4fd2-9d7b-879686653699"
    }

    def "should return 409 when there are no free spots in a survey"() {
        given:
            String participantId = userRepository.findByUsername(PARTICIPANT_USERNAME).userId
            mongoSurveyRepository.saveSurvey(survey([
                    eligibleParticipantIds: [participantId],
                    spotsTaken: SPOTS_TOTAL
            ]))
        when:
            HttpResponseDecorator response = participate()
        then:
            response.status == 409
            response.data.errors.first() == "There are no free spots for survey withId: 61cb2fbf-e83a-4fd2-9d7b-879686653699."

    }

//    def "should return 403 when trying to participate in a survey as researcher"() {
//
//    }

    @Unroll
    def "should #action survey participation"() {
        given:
            String participantId = userRepository.findByUsername(PARTICIPANT_USERNAME).userId
            mongoSurveyParticipationRepository.insert(surveyParticipation([participantId: participantId]))
            ParticipationModificationDto dto = new ParticipationModificationDto(action, COMPLETION_CODE)
        when:
            HttpResponseDecorator response = manageParticipation(SURVEY_PARTICIPATION_ID, dto)
        then:
            response.status == 200
        where:
            action << [CANCEL, COMPLETE ]
    }

    def "should return 400 when there is no completion code when completing"() {
        given:
            String participantId = userRepository.findByUsername(PARTICIPANT_USERNAME).userId
            mongoSurveyParticipationRepository.insert(surveyParticipation([participantId: participantId]))
            ParticipationModificationDto dto = new ParticipationModificationDto(COMPLETE, null)
        when:
            HttpResponseDecorator response = manageParticipation(SURVEY_PARTICIPATION_ID, dto)
        then:
            response.status == 400
            response.data.errors.first() == "There is no completion code on completion request"
    }

    def "should return 401 when trying to manage participation of the other user"() {
        given:
            String participantId = userRepository.findByUsername(PARTICIPANT_USERNAME).userId
            mongoSurveyParticipationRepository.insert(surveyParticipation([participantId: "957"]))
            ParticipationModificationDto dto = new ParticipationModificationDto(CANCEL, null)
        when:
            HttpResponseDecorator response = manageParticipation(SURVEY_PARTICIPATION_ID, dto)
        then:
            response.status == 403
            response.data.errors.first() == "Participant with id: ${getPrincipalId()} has no access to participation with id: 1-1"
    }

    def "should return 409 when trying to participate in a survey that is not in progress"() {
        given:
            String participantId = userRepository.findByUsername(PARTICIPANT_USERNAME).userId
            mongoSurveyParticipationRepository.insert(surveyParticipation([
                    participantId: participantId,
                    status: SurveyStatus.TIMEOUT
            ]))
            ParticipationModificationDto dto = new ParticipationModificationDto(CANCEL, null)
        when:
            HttpResponseDecorator response = manageParticipation(SURVEY_PARTICIPATION_ID, dto)
        then:
            response.status == 409
            response.data.errors.first() == "SurveyParticipation with id: 1-1 is not in progress."
    }

    @Ignore
    def "should validate if values in body are not proper"() {
        given:
//            Map body = [
//                    action: "NONEXISTENT",
//                    completionCode: "123"
//            ]
//        and:
//            authAs(PARTICIPANT_USERNAME, PASSWORD)
            ParticipationModificationDto dto = new ParticipationModificationDto(CANCEL, "123")
        when:
            HttpResponseDecorator response = manageParticipation(SURVEY_PARTICIPATION_ID, dto)
//            HttpResponseDecorator response = restClient.put([path: "/survey/SURVEY_ID/participation/$SURVEY_PARTICIPATION_ID", body: objectMapper.writeValueAsString(body)]) as HttpResponseDecorator
        then:
            response.status == 400
            response.data.errors.first() == " "
    }
//
//    def "should return 403 when trying to manage participation as researcher"() {
//
//    }

    private getPrincipalId() {
        return userRepository.findByUsername(PARTICIPANT_USERNAME).userId
    }

    private HttpResponseDecorator participate(String surveyId = SURVEY_ID) {
        authAs(PARTICIPANT_USERNAME, PASSWORD)
        return restClient.post([path: "/survey/$surveyId/participation"]) as HttpResponseDecorator
    }

    private HttpResponseDecorator manageParticipation(String participationId, ParticipationModificationDto body) {
        authAs(PARTICIPANT_USERNAME, PASSWORD)
        return restClient.put([path: "/survey/$SURVEY_ID/participation/$participationId", body: objectMapper.writeValueAsString(body)]) as HttpResponseDecorator
    }
}
