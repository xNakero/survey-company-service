package pl.wat.surveycompanyservice.domain.surveyhistory

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import pl.wat.surveycompanyservice.shared.HistoryEntryId
import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.SurveyId
import pl.wat.surveycompanyservice.shared.SurveyParticipationId
import java.time.Instant

data class HistoryEntry(
    val historyEntryId: HistoryEntryId?,
    val surveyId: SurveyId,
    val title: String,
    val url: String,
    val timeToCompleteInSeconds: Int,
    val description: String,
    val spotsTotal: Int,
    val spotsTaken: Int,
    val completionCode: String,
    val startedAt: Instant,
    val finishedAt: Instant,
    val historyParticipations: List<HistoryParticipation>
) {
    fun toMongoHistoryEntry(): MongoHistoryEntry = MongoHistoryEntry(
        id = historyEntryId?.raw,
        surveyId = surveyId.raw,
        title = title,
        url = url,
        timeToCompleteInSeconds = timeToCompleteInSeconds,
        description = description,
        spotsTotal = spotsTotal,
        spotsTaken = spotsTaken,
        completionCode = completionCode,
        startedAt = startedAt,
        finishedAt = finishedAt,
        historyParticipations = historyParticipations.map { it.toMongoHistoryParticipation() }
    )
}

data class HistoryParticipation(
    val participationId: SurveyParticipationId,
    val participantId: ParticipantId,
    val startedAt: Instant,
    val finishedAt: Instant,
    val completionCode: String?,
    val completedWithValidCode: Boolean
) {
    fun toMongoHistoryParticipation(): MongoHistoryParticipation = MongoHistoryParticipation(
        id = participationId.raw,
        participantId = participantId.raw,
        startedAt = startedAt,
        finishedAt = finishedAt,
        completionCode = completionCode,
        completedWithValidCode = completedWithValidCode
    )
}

@Document("history-entry")
data class MongoHistoryEntry(
    @Id val id: String?,
    val surveyId: String,
    val title: String,
    val url: String,
    val timeToCompleteInSeconds: Int,
    val description: String,
    val spotsTotal: Int,
    val spotsTaken: Int,
    val completionCode: String,
    val startedAt: Instant,
    val finishedAt: Instant,
    val historyParticipations: List<MongoHistoryParticipation>
) {
    fun toHistoryEntry(): HistoryEntry = HistoryEntry(
        historyEntryId = HistoryEntryId(id!!),
        surveyId = SurveyId(surveyId),
        title = title,
        url = url,
        timeToCompleteInSeconds = timeToCompleteInSeconds,
        description = description,
        spotsTotal = spotsTotal,
        spotsTaken = spotsTaken,
        completionCode = completionCode,
        startedAt = startedAt,
        finishedAt = finishedAt,
        historyParticipations = historyParticipations.map { it.toHistoryParticipation() }
    )
}

data class MongoHistoryParticipation(
    @Id val id: String,
    val participantId: String,
    val startedAt: Instant,
    val finishedAt: Instant,
    val completionCode: String?,
    val completedWithValidCode: Boolean
) {
    fun toHistoryParticipation(): HistoryParticipation = HistoryParticipation(
        participationId = SurveyParticipationId(id),
        participantId = ParticipantId(participantId),
        startedAt = startedAt,
        finishedAt = finishedAt,
        completionCode = completionCode,
        completedWithValidCode = completedWithValidCode
    )
}