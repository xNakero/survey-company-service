package pl.wat.surveycompanyservice.domain.surveyhistory

import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.ResearcherId

interface HistoryEntryRepository {
    fun saveAll(historyEntries: List<HistoryEntry>)
    fun findByParticipantId(participantId: ParticipantId): List<HistoryEntry>
    fun findByResearcherId(researcherId: ResearcherId): List<HistoryEntry>
}