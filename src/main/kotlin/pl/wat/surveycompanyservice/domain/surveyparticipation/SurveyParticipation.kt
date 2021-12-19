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
    val status: SurveyStatus,
    val startedAt: Instant,
    val hasToFinishUntil: Instant,
    val completionCode: String?
    ) {
        fun toMongoSurveyParticipation(): MongoSurveyParticipation = MongoSurveyParticipation(
            id = id.raw,
            participantId = participantId.raw,
            surveyId = surveyId.raw,
            status = status.toString(),
            startedAt = startedAt,
            hasToFinishUntil = hasToFinishUntil,
            completionCode = completionCode
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
    val completionCode: String?
) {
    fun toSurveyParticipation(): SurveyParticipation = SurveyParticipation(
        id = SurveyParticipationId(id),
        participantId = ParticipantId(participantId),
        surveyId = SurveyId(surveyId),
        status = SurveyStatus.valueOf(status),
        startedAt = startedAt,
        hasToFinishUntil = hasToFinishUntil,
        completionCode = completionCode
    )
}

enum class SurveyStatus{
    COMPLETED, IN_PROGRESS, CANCELLED, TIMEOUT
}