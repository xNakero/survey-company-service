package pl.wat.surveycompanyservice.domain.surveyhistory

import pl.wat.surveycompanyservice.shared.HistoryEntryId
import java.time.Duration
import java.time.Instant
import java.time.LocalTime

data class SurveyHistoryDto(
    val type: String,
    val surveys: List<HistoryEntryDto>
)

open class HistoryEntryDto

data class ResearcherHistoryEntryDto(
    val id: String,
    val title: String,
    val url: String,
    val timeToComplete: String,
    val description: String,
    val spotsTotal: Int,
    val spotsTaken: Int,
    val validSubmissions: Int,
    val completionCode: String,
    val startedAt: Instant,
    val finishedAt: Instant
) : HistoryEntryDto()

data class ParticipantHistoryEntryDto(
    val id: String,
    val title: String,
    val timeToComplete: String,
    val description: String,
    val startedAt: Instant,
    val finishedAt: Instant,
    val completionCode: String?,
    val completedWithValidCode: Boolean
) : HistoryEntryDto()
