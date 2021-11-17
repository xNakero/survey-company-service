package pl.wat.surveycompanyservice.api

import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.OK
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileQueryParams
import pl.wat.surveycompanyservice.domain.survey.SurveyFacade
import pl.wat.surveycompanyservice.domain.user.AppUser
import pl.wat.surveycompanyservice.shared.ResearcherId

@RestController
@RequestMapping("/survey")
class SurveyPublishingEndpoint(
    private val surveyFacade: SurveyFacade
) {

    @PostMapping
    @ResponseStatus(CREATED)
    fun postSurvey(
        @AuthenticationPrincipal user: AppUser,
        @RequestBody request: SurveyToPostDto
    ) = surveyFacade.saveSurvey(request, ResearcherId(user.userId.toString()))

    @PostMapping("/participants-count")
    @ResponseStatus(OK)
    fun getNumberOfEligibleParticipants(
        @RequestBody request: PersonalProfileQueryParams
    ): EligibleUsersDto =
        EligibleUsersDto(surveyFacade.getNumberOfEligibleParticipants(request))

}

data class SurveyToPostDto(
    val surveyParams: SurveyParamsDto,
    val queryParams: PersonalProfileQueryParams
)

data class SurveyParamsDto(
    val title: String,
    val url: String,
    val timeToCompleteInSeconds: Int,
    val description: String,
    val spots: Int
)

data class EligibleUsersDto(
    val numOfUsers: Int
)
