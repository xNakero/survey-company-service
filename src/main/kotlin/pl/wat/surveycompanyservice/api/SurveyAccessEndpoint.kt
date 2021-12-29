package pl.wat.surveycompanyservice.api

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.wat.surveycompanyservice.domain.survey.SurveyFacade
import pl.wat.surveycompanyservice.domain.survey.SurveysDto
import pl.wat.surveycompanyservice.domain.survey.SurveysWithTypeDto
import pl.wat.surveycompanyservice.domain.user.AppUser
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
}