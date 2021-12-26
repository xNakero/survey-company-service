package pl.wat.surveycompanyservice.domain.surveyhistory

import org.springframework.stereotype.Service

@Service
class HistoryEntryService(
    private val historyEntryRepository: HistoryEntryRepository
) {

    fun saveAll(historyEntries: List<HistoryEntry>) {
        historyEntryRepository.saveAll(historyEntries)
    }
}