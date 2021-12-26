package pl.wat.surveycompanyservice.scheduler

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import pl.wat.surveycompanyservice.domain.survey.Survey
import pl.wat.surveycompanyservice.domain.survey.SurveyService
import pl.wat.surveycompanyservice.domain.surveyhistory.HistoryEntry
import pl.wat.surveycompanyservice.domain.surveyhistory.HistoryEntryService
import pl.wat.surveycompanyservice.domain.surveyhistory.HistoryParticipation
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipation
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipationService
import pl.wat.surveycompanyservice.shared.SurveyId
import java.time.Clock

@Component
class SurveyScheduler(
    private val surveyService: SurveyService,
    private val surveyParticipationService: SurveyParticipationService,
    private val historyEntryService: HistoryEntryService,
    private val clock: Clock
) {

    @Scheduled(fixedDelay = 10000, initialDelay = 0)
    fun finishSurvey() {
        val surveyIds = surveyService.findSurveysEligibleToFinish().map { it.id }
        val surveyParticipationIdsInProgress = surveyParticipationService.findInProgress(surveyIds)
        val surveyIdsToKeep = surveyParticipationIdsInProgress.map { it.surveyId.raw }
        val surveyIdsToArchive = surveyIds.filter { it.raw !in surveyIdsToKeep }
        val surveys = surveyService.findBySurveyIds(surveyIdsToArchive)
        val surveyParticipations = surveyParticipationService.findBySurveyIds(surveyIdsToArchive)
        val historyEntries = surveyIdsToArchive.map { it.toSurveyHistoryEntry(surveys[it]!!, surveyParticipations[it]) }
        historyEntryService.saveAll(historyEntries)
        surveyService.removeByIds(surveyIdsToArchive)
        surveyParticipationService.removeBySurveyIds(surveyIdsToArchive)
    }

    private fun SurveyId.toSurveyHistoryEntry(survey: Survey, surveyParticipations: List<SurveyParticipation>?) =
        HistoryEntry(
            historyEntryId = null,
            surveyId = survey.id,
            title = survey.title,
            url = survey.url,
            timeToCompleteInSeconds = survey.timeToCompleteInSeconds,
            description = survey.description,
            spotsTotal = survey.spotsTotal,
            spotsTaken = survey.spotsTaken,
            completionCode = survey.completionCode,
            startedAt = survey.startedAt,
            finishedAt = clock.instant(),
            historyParticipations = surveyParticipations
                ?.map { it.toHistoryParticipation(survey.completionCode) }
                ?: emptyList()
        )


    private fun SurveyParticipation.toHistoryParticipation(surveyCompletionCode: String): HistoryParticipation =
        HistoryParticipation(
            id,
            participantId,
            startedAt,
            clock.instant(),
            completionCode,
            isCodeValid(surveyCompletionCode)
        )

    private fun SurveyParticipation.isCodeValid(validCode: String): Boolean =
        completionCode?.let { it == validCode } ?: false
}

