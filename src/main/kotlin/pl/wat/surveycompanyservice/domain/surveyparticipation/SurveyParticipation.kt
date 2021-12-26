package pl.wat.surveycompanyservice.domain.surveyparticipation

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.SurveyId
import pl.wat.surveycompanyservice.shared.SurveyParticipationId
import java.time.Instant

data class SurveyParticipation(
    val id: SurveyParticipationId,
    val participantId: ParticipantId,
    val surveyId: SurveyId,
    val status: ParticipationStatus,
    val startedAt: Instant,
    val hasToFinishUntil: Instant,
    val completionCode: String?,
    val finishedAt: Instant?
    ) {
        fun toMongoSurveyParticipation(): MongoSurveyParticipation = MongoSurveyParticipation(
            id = id.raw,
            participantId = participantId.raw,
            surveyId = surveyId.raw,
            status = status.toString(),
            startedAt = startedAt,
            hasToFinishUntil = hasToFinishUntil,
            completionCode = completionCode,
            finishedAt = finishedAt
    )
}

@Document("survey_participation")
data class MongoSurveyParticipation(
    @Id val id: String,
    val participantId: String,
    val surveyId: String,
    val status: String,
    val startedAt: Instant,
    val hasToFinishUntil: Instant,
    val completionCode: String?,
    val finishedAt: Instant?
) {
    fun toSurveyParticipation(): SurveyParticipation = SurveyParticipation(
        id = SurveyParticipationId(id),
        participantId = ParticipantId(participantId),
        surveyId = SurveyId(surveyId),
        status = ParticipationStatus.valueOf(status),
        startedAt = startedAt,
        hasToFinishUntil = hasToFinishUntil,
        completionCode = completionCode,
        finishedAt = finishedAt
    )
}

enum class ParticipationStatus{
    COMPLETED, IN_PROGRESS, CANCELLED, TIMEOUT
}