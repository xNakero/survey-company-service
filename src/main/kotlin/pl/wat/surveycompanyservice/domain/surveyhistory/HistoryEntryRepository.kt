package pl.wat.surveycompanyservice.domain.surveyhistory

interface HistoryEntryRepository {
    fun saveAll(historyEntries: List<HistoryEntry>)
}