package pl.wat.surveycompanyservice.domain.surveyhistory

import org.springframework.stereotype.Service
import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.ResearcherId
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.SECONDS

@Service
class HistoryEntryService(
    private val historyEntryRepository: HistoryEntryRepository
) {

    fun saveAll(historyEntries: List<HistoryEntry>) {
        historyEntryRepository.saveAll(historyEntries)
    }

    fun getSurveysForParticipant(participantId: ParticipantId): List<ParticipantHistoryEntryDto> =
        historyEntryRepository.findByParticipantId(participantId)
            .map { it.toParticipantHistoryEntryDto(participantId) }

    fun getSurveysForResearcher(researcherId: ResearcherId): List<ResearcherHistoryEntryDto> =
        historyEntryRepository.findByResearcherId(researcherId)
            .map { it.toResearcherHistoryEntryDto() }

    private fun HistoryEntry.toParticipantHistoryEntryDto(participantId: ParticipantId): ParticipantHistoryEntryDto =
        ParticipantHistoryEntryDto(
            historyEntryId = id!!.raw,
            title = title,
            timeToComplete = Duration.of(timeToCompleteInSeconds.toLong(), SECONDS).format(),
            description = description,
            startedAt = historyParticipations.find { it.participantId.raw == participantId.raw  }!!.startedAt,
            finishedAt = historyParticipations.find { it.participantId.raw == participantId.raw  }!!.finishedAt,
            completionCode = historyParticipations.find { it.participantId.raw == participantId.raw  }!!.completionCode,
            completedWithValidCode = historyParticipations.find { it.participantId.raw == participantId.raw  }!!.completedWithValidCode
        )

    private fun HistoryEntry.toResearcherHistoryEntryDto(): ResearcherHistoryEntryDto =
        ResearcherHistoryEntryDto(
            historyEntryId = id!!.raw,
            title = title,
            url = url,
            timeToComplete = Duration.of(timeToCompleteInSeconds.toLong(), SECONDS).format(),
            description = description,
            spotsTotal = spotsTotal,
            spotsTaken = spotsTaken,
            validSubmissions = validSubmissions(),
            completionCode = completionCode,
            startedAt = startedAt,
            finishedAt = finishedAt
        )

    private fun Duration.format() =
        DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalTime.MIDNIGHT.plus(this.toSeconds(), SECONDS))
}