package pl.wat.surveycompanyservice.domain.surveyhistory

import org.springframework.stereotype.Component
import pl.wat.surveycompanyservice.domain.role.AppRole
import pl.wat.surveycompanyservice.domain.user.AppUser
import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.ResearcherId

@Component
class HistoryEntryFacade(
    private val historyEntryService: HistoryEntryService
) {
    fun findSurveyHistory(user: AppUser): SurveyHistoryDto =
        SurveyHistoryDto(
            user.roles.first().name,
            user.surveyHistoryDto()
        )

    private fun AppUser.surveyHistoryDto(): List<HistoryEntryDto> =
        when(roles.first().name) {
            AppRole.PARTICIPANT.toString() -> historyEntryService.getSurveysForParticipant(ParticipantId(userId.toString()))
            AppRole.RESEARCHER.toString() -> historyEntryService.getSurveysForResearcher(ResearcherId(userId.toString()))
            else -> emptyList()
        }
}