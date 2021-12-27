package pl.wat.surveycompanyservice.infrastructure.repository

import org.jetbrains.annotations.NotNull
import pl.wat.surveycompanyservice.domain.surveyhistory.HistoryEntry
import pl.wat.surveycompanyservice.domain.surveyhistory.HistoryEntryRepository
import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.ResearcherId

import java.util.concurrent.CopyOnWriteArraySet

class InMemoryHistoryEntryRepository implements HistoryEntryRepository{

    private Set<HistoryEntry> historyEntries = new CopyOnWriteArraySet<>()

    @Override
    void saveAll(@NotNull List<HistoryEntry> historyEntries) {
        this.historyEntries.addAll(historyEntries)
    }

    @Override
    List<HistoryEntry> findByParticipantId(@NotNull ParticipantId participantId) {
        return historyEntries.findAll { it.historyParticipations.participantId.raw.contains(participantId.raw)}.toList()
    }

    @Override
    List<HistoryEntry> findByResearcherId(@NotNull ResearcherId researcherId) {
        return historyEntries.findAll { it.researcherId.raw == researcherId.raw}.toList()
    }

    void insert(HistoryEntry historyEntry) {
        historyEntries.remove(
                historyEntries.find {
                    it.id.raw == historyEntry.id.raw
                }
        )
        historyEntries.add(historyEntry)
    }

    void clear() {
        historyEntries.clear()
    }

    Set findAll() {
        return historyEntries
    }
}
