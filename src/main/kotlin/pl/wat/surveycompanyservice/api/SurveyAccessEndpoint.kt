package pl.wat.surveycompanyservice.api

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.wat.surveycompanyservice.domain.survey.SurveyFacade
import pl.wat.surveycompanyservice.domain.survey.SurveysDto
import pl.wat.surveycompanyservice.domain.survey.SurveysWithTypeDto
import pl.wat.surveycompanyservice.domain.user.AppUser
import pl.wat.surveycompanyservice.infrastructure.validator.Enum
import pl.wat.surveycompanyservice.shared.ResearcherId
import pl.wat.surveycompanyservice.shared.SurveyId
import pl.wat.surveycompanyservice.shared.UserId

@RestController
@RequestMapping("/surveys")
@CrossOrigin
class SurveyAccessEndpoint(
    private val surveyFacade: SurveyFacade
) {

    @GetMapping
    fun getSurveys(@AuthenticationPrincipal principal: AppUser): SurveysWithTypeDto =
        surveyFacade.getSurveys(
            UserId(principal.userId.toString()),
            principal.roles.first().name
        )

    @PutMapping("/{surveyId}")
    fun finishSurvey(
        @AuthenticationPrincipal principal: AppUser,
        @PathVariable surveyId: String,
        @RequestBody body: SurveyActionDto
    ) =
        surveyFacade.finishSurvey(SurveyId(surveyId), ResearcherId(principal.userId.toString()))
}

data class SurveyActionDto(
    @Enum(SurveyAction::class, message = "There is no action on the survey") val action: SurveyAction
)

enum class SurveyAction {
    FINISH
}
