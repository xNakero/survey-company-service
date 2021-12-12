package pl.wat.surveycompanyservice.domain.survey

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import pl.wat.surveycompanyservice.shared.ResearcherId
import pl.wat.surveycompanyservice.shared.SurveyId

data class Survey(
    val id: SurveyId,
    val researcherId: ResearcherId,
    val participationIds: List<String>,
    val eligibleParticipantsIds: List<String>,
    val title: String,
    val url: String,
    val timeToCompleteInSeconds: Int,
    val description: String,
    val spotsTotal: Int,
    val spotsTaken: Int,
    val completionCode: String
) {
    fun hasFreeSpots(): Boolean = spotsTaken < spotsTotal

    fun toMongoSurvey(): MongoSurvey = MongoSurvey(
        id = id.raw,
        researcherId = researcherId.raw,
        participationIds = participationIds,
        eligibleParticipantsIds = eligibleParticipantsIds,
        title = title,
        url = url,
        timeToCompleteInSeconds = timeToCompleteInSeconds,
        description = description,
        spotsTotal = spotsTotal,
        spotsTaken = spotsTaken,
        completionCode = completionCode
    )
}

@Document("surveys")
data class MongoSurvey(
    @Id val id: String,
    val researcherId: String,
    val participationIds: List<String>,
    val eligibleParticipantsIds: List<String>,
    val title: String,
    val url: String,
    val timeToCompleteInSeconds: Int,
    val description: String,
    val spotsTotal: Int,
    val spotsTaken: Int,
    val completionCode: String
) {
    fun toSurvey(): Survey = Survey(
        id = SurveyId(id),
        researcherId = ResearcherId(researcherId),
        participationIds = participationIds,
        eligibleParticipantsIds = eligibleParticipantsIds,
        title = title,
        url = url,
        timeToCompleteInSeconds = timeToCompleteInSeconds,
        description = description,
        spotsTotal = spotsTotal,
        spotsTaken = spotsTaken,
        completionCode = completionCode
    )
}