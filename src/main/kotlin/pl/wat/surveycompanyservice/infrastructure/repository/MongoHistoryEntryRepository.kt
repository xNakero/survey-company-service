package pl.wat.surveycompanyservice.infrastructure.repository

import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.stereotype.Component
import pl.wat.surveycompanyservice.domain.surveyhistory.HistoryEntry
import pl.wat.surveycompanyservice.domain.surveyhistory.HistoryEntryRepository

@Component
class MongoHistoryEntryRepository(
    private val mongoOperations: MongoOperations
): HistoryEntryRepository {

    override fun saveAll(historyEntries: List<HistoryEntry>) {
        historyEntries.map { it.toMongoHistoryEntry() }
            .let { mongoOperations.insertAll(it) }
    }
}