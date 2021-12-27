package pl.wat.surveycompanyservice.infrastructure.repository

import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component
import pl.wat.surveycompanyservice.domain.surveyhistory.HistoryEntry
import pl.wat.surveycompanyservice.domain.surveyhistory.HistoryEntryRepository
import pl.wat.surveycompanyservice.domain.surveyhistory.MongoHistoryEntry
import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.ResearcherId

@Component
class MongoHistoryEntryRepository(
    private val mongoOperations: MongoOperations
): HistoryEntryRepository {

    override fun saveAll(historyEntries: List<HistoryEntry>) {
        historyEntries.map { it.toMongoHistoryEntry() }
            .let { mongoOperations.insertAll(it) }
    }

    override fun findByParticipantId(participantId: ParticipantId): List<HistoryEntry> {
        val query = Query.query(Criteria.where(PARTICIPANT_ID).`is`(participantId.raw))
        return mongoOperations.find(query, MongoHistoryEntry::class.java).map { it.toHistoryEntry() }
    }

    override fun findByResearcherId(researcherId: ResearcherId): List<HistoryEntry> {
        val query = Query.query(Criteria.where(RESEARCHER_ID).`is`(researcherId.raw))
        return mongoOperations.find(query, MongoHistoryEntry::class.java).map { it.toHistoryEntry() }
    }

    companion object {
        const val RESEARCHER_ID = "researcherId"
        const val PARTICIPANT_ID = "historyParticipations.participantId"
    }
}