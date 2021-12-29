package pl.wat.surveycompanyservice.api

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.wat.surveycompanyservice.domain.surveyhistory.HistoryEntryFacade
import pl.wat.surveycompanyservice.domain.surveyhistory.SurveyHistoryDto
import pl.wat.surveycompanyservice.domain.user.AppUser

@RestController
@RequestMapping("/survey-history")
@CrossOrigin
class HistoryEntryEndpoint(
    private val historyEntryFacade: HistoryEntryFacade
) {
    @GetMapping
    fun getSurveyHistory(@AuthenticationPrincipal user: AppUser): SurveyHistoryDto =
        historyEntryFacade.findSurveyHistory(user)
}