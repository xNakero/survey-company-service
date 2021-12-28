package pl.wat.surveycompanyservice.domain.surveyhistory

import org.springframework.stereotype.Component
import pl.wat.surveycompanyservice.domain.role.AppRole
import pl.wat.surveycompanyservice.domain.survey.MongoSurvey
import pl.wat.surveycompanyservice.domain.survey.Survey
import pl.wat.surveycompanyservice.domain.survey.SurveyService
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipation
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipationService
import pl.wat.surveycompanyservice.domain.user.AppUser
import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.ResearcherId
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.time.temporal.ChronoUnit.SECONDS

@Component
class HistoryEntryFacade(
    private val historyEntryService: HistoryEntryService,
    private val surveyParticipationService: SurveyParticipationService,
    private val surveyService: SurveyService
) {
    fun findSurveyHistory(user: AppUser): SurveyHistoryDto =
        SurveyHistoryDto(
            user.roles.first().name,
            user.surveyHistoryDto()
        )

    private fun AppUser.surveyHistoryDto(): List<HistoryEntryDto> =
        when (roles.first().name) {
            AppRole.PARTICIPANT.toString() -> surveyHistoryForParticipant(ParticipantId(userId.toString()))
            AppRole.RESEARCHER.toString() -> historyEntryService.getSurveysForResearcher(ResearcherId(userId.toString()))
            else -> emptyList()
        }

    private fun surveyHistoryForParticipant(participantId: ParticipantId): List<ParticipantHistoryEntryDto> {
        val historyEntries = historyEntryService.getSurveysForParticipant(participantId)
        val surveyParticipationsNotInProgress = surveyParticipationService.findNotInProgress(participantId)
        val surveys = surveyService.findBySurveyIds(surveyParticipationsNotInProgress.keys.toList())
        return surveys.keys
            .map {
                toSurveyHistoryForParticipant(
                    surveyParticipationsNotInProgress[it]!!,
                    surveys[it]!!
                )
            }.plus(historyEntries)
            .sortedByDescending { it.startedAt }
    }

    private fun toSurveyHistoryForParticipant(
        surveyParticipation: SurveyParticipation,
        survey: Survey
    ): ParticipantHistoryEntryDto =
        ParticipantHistoryEntryDto(
            id = surveyParticipation.id.raw,
            title = survey.title,
            timeToComplete = Duration.of(survey.timeToCompleteInSeconds.toLong(), SECONDS).format(),
            description = survey.description,
            startedAt = surveyParticipation.startedAt,
            finishedAt = surveyParticipation.finishedAt!!,
            completionCode = surveyParticipation.completionCode,
            completedWithValidCode = survey.completionCode == surveyParticipation.completionCode
        )
}