package pl.wat.surveycompanyservice.facade

import pl.wat.surveycompanyservice.BaseUnitTest
import pl.wat.surveycompanyservice.TestBuilders
import pl.wat.surveycompanyservice.domain.role.Role
import pl.wat.surveycompanyservice.domain.survey.Survey
import pl.wat.surveycompanyservice.domain.surveyhistory.HistoryEntry
import pl.wat.surveycompanyservice.domain.surveyhistory.HistoryParticipation
import pl.wat.surveycompanyservice.domain.surveyhistory.SurveyHistoryDto
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipation
import pl.wat.surveycompanyservice.domain.user.AppUser

import java.time.temporal.ChronoUnit

import static java.time.temporal.ChronoUnit.MINUTES
import static pl.wat.surveycompanyservice.TestBuilders.COMPLETION_CODE
import static pl.wat.surveycompanyservice.TestBuilders.DESCRIPTION
import static pl.wat.surveycompanyservice.TestBuilders.HISTORY_ENTRY_ID
import static pl.wat.surveycompanyservice.TestBuilders.PARTICIPATION_FINISHED_AT
import static pl.wat.surveycompanyservice.TestBuilders.SPOTS_TAKEN
import static pl.wat.surveycompanyservice.TestBuilders.SPOTS_TOTAL
import static pl.wat.surveycompanyservice.TestBuilders.STARTED_AT
import static pl.wat.surveycompanyservice.TestBuilders.SURVEY_PARTICIPATION_ID
import static pl.wat.surveycompanyservice.TestBuilders.TITLE

import static pl.wat.surveycompanyservice.TestBuilders.USER_ID
import static pl.wat.surveycompanyservice.TestBuilders.appUser
import static pl.wat.surveycompanyservice.TestBuilders.historyEntry
import static pl.wat.surveycompanyservice.TestBuilders.historyParticipation
import static pl.wat.surveycompanyservice.TestBuilders.survey
import static pl.wat.surveycompanyservice.TestBuilders.surveyParticipation
import static pl.wat.surveycompanyservice.domain.role.AppRole.RESEARCHER

class HistoryEntryFacadeTest extends BaseUnitTest{

    def 'should return dto with history entries for participant'() {
        given:
            AppUser user = appUser()
        and:
            HistoryParticipation historyParticipation1 = historyParticipation([
                    surveyParticipationId: '123',
                    participantId: '12345'
            ])
            HistoryParticipation historyParticipation2 = historyParticipation()
            HistoryEntry entry = historyEntry()
            entry.historyParticipations.addAll([historyParticipation1, historyParticipation2])
            historyEntryRepository.saveAll([entry])
        when:
            SurveyHistoryDto dto = historyEntryFacade.findSurveyHistory(user)
        then:
            dto.type == "PARTICIPANT"
            dto.surveys.size() == 1
        and:
            with(dto.surveys.first()) {
                it.id == HISTORY_ENTRY_ID
                it.title == TITLE
                it.timeToComplete == "00:10:00"
                it.startedAt == STARTED_AT
                it.finishedAt == PARTICIPATION_FINISHED_AT
                it.description == DESCRIPTION
                it.completionCode == null
                it.completedWithValidCode
            }
    }

    def 'should return dto with details of a survey and its participation when survey is not yet archived'() {
        given:
            AppUser user = appUser()
        and:
            SurveyParticipation participation1 = surveyParticipation([status: "COMPLETED"])
            Survey survey1 = survey()
            inMemorySurveyRepository.saveSurvey(survey1)
            inMemorySurveyParticipationRepository.insert(participation1)
        when:
            SurveyHistoryDto dto = historyEntryFacade.findSurveyHistory(user)
        then:
            dto.type == "PARTICIPANT"
            with(dto.surveys.first()) {
                it.id == SURVEY_PARTICIPATION_ID
                it.title == TITLE
                it.timeToComplete == "00:10:00"
                it.description == DESCRIPTION
                it.startedAt == STARTED_AT
                it.finishedAt == PARTICIPATION_FINISHED_AT
                it.completionCode == null
                it.completedWithValidCode == false
            }
    }

    def 'should sort entries by latest startedAt'() {
        given:
            AppUser user = appUser()
        and:
            SurveyParticipation participation1 = surveyParticipation([status: "COMPLETED"])
            Survey survey1 = survey()
            inMemorySurveyRepository.saveSurvey(survey1)
            inMemorySurveyParticipationRepository.insert(participation1)
        and:
            HistoryParticipation historyParticipation1 = historyParticipation([
                    startedAt: STARTED_AT.minus(2, MINUTES).toString()
            ])
            HistoryEntry entry = historyEntry()
            entry.historyParticipations.addAll([historyParticipation1])
            historyEntryRepository.saveAll([entry])
        when:
            SurveyHistoryDto dto = historyEntryFacade.findSurveyHistory(user)
        then:
            dto.surveys.size() == 2
            dto.surveys[0].startedAt == STARTED_AT
            dto.surveys[1].startedAt == STARTED_AT.minus(2, MINUTES)
    }

    def 'should return dto with history entries for researcher'() {
        given:
            AppUser user = appUser()
            user.roles.clear()
            user.roles.add(new Role(1L, RESEARCHER.toString(), [] as Set))
        and:
            HistoryParticipation historyParticipation1 = historyParticipation([
                    surveyParticipationId: '123',
                    participantId: '12345',
                    completedWithValidCode: false
            ])
            HistoryParticipation historyParticipation2 = historyParticipation()
            HistoryEntry entry = historyEntry([researcherId: USER_ID.toString()])
            entry.historyParticipations.addAll([historyParticipation1, historyParticipation2])
            historyEntryRepository.saveAll([entry])
        when:
            SurveyHistoryDto dto = historyEntryFacade.findSurveyHistory(user)
        then:
            dto.type == "RESEARCHER"
            dto.surveys.size() == 1
        and:
            with(dto.surveys.first()) {
                it.id == HISTORY_ENTRY_ID
                it.title == TITLE
                it.url == TestBuilders.URL
                it.timeToComplete == "00:10:00"
                it.description == DESCRIPTION
                it.spotsTotal == SPOTS_TOTAL
                it.spotsTaken == SPOTS_TAKEN
                it.validSubmissions == 1
                it.completionCode == COMPLETION_CODE
                it.startedAt == STARTED_AT
                it.finishedAt == PARTICIPATION_FINISHED_AT
            }
    }

}
