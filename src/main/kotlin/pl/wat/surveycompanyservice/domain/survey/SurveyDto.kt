package pl.wat.surveycompanyservice.domain.survey

import java.time.Instant

data class SurveysWithTypeDto (
    val type: String,
    val surveys: SurveysDto
)

open class SurveysDto

data class ParticipantSurveysDto(
    val surveyInProgress: ParticipantSurveyDto?,
    val availableSurveys: List<ParticipantSurveyDto>,
): SurveysDto()

data class ParticipantSurveyDto(
    val surveyId: String,
    val participationId: String?,
    val title: String,
    val url: String,
    val timeToComplete: String,
    val description: String,
    val freeSpots: Int,
    val status: String?,
    val startedAt: Instant?,
    val hasToFinishUntil: Instant?,
    val completionCode: String?
)

data class ResearcherSurveysDto(
    val activeSurveys: List<ResearcherSurveyDto>
): SurveysDto()

data class ResearcherSurveyDto(
    val surveyId: String,
    val title: String,
    val url: String,
    val timeToComplete: String,
    val description: String,
    val spotsTotal: Int,
    val spotsTaken: Int,
    val completionCode: String,
    val startedAt: Instant
)