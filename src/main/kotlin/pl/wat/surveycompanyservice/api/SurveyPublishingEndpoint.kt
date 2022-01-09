package pl.wat.surveycompanyservice.api

import org.hibernate.validator.constraints.Length
import org.hibernate.validator.constraints.URL
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.OK
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileQueryParams
import pl.wat.surveycompanyservice.domain.survey.SurveyFacade
import pl.wat.surveycompanyservice.domain.user.AppUser
import pl.wat.surveycompanyservice.shared.ResearcherId
import javax.validation.Valid
import javax.validation.constraints.Max
import javax.validation.constraints.Min

@RestController
@RequestMapping("/surveys")
@CrossOrigin
class SurveyPublishingEndpoint(
    private val surveyFacade: SurveyFacade
) {

    @PostMapping
    @ResponseStatus(CREATED)
    fun postSurvey(
        @AuthenticationPrincipal user: AppUser,
        @Valid @RequestBody request: SurveyToPostDto
    ) = surveyFacade.saveSurvey(request, ResearcherId(user.userId.toString()))

    @PostMapping("/participants-count")
    @ResponseStatus(OK)
    fun getNumberOfEligibleParticipants(
        @Valid @RequestBody request: PersonalProfileQueryParams
    ): EligibleUsersDto =
        EligibleUsersDto(surveyFacade.getNumberOfEligibleParticipants(request))
}

data class SurveyToPostDto(
    @field:Valid val surveyParams: SurveyParamsDto,
    @field:Valid val queryParams: PersonalProfileQueryParams
)

data class SurveyParamsDto(
    @field:Length(min = 3, max = 100, message = "Length of title has to be between 3 and 100 characters.") val title: String,
    @field:URL(message = "url field is not a url.") val url: String,
    @field:Min(value = 300, message = "Minimum time to complete has to be at least 5 minutes.")
    @field:Max(value = 10800, message = "Maximum time to complete has to be no longer than 3 hours.")
    val timeToCompleteInSeconds: Int,
    @field:Length(min = 100, max = 2000, message = "Description has to be between 100 and 2000 characters.") val description: String,
    @field:Min(value = 1, message = "There is at least one spot required for a survey.") val spots: Int,
    @field:Length(max = 32, message = "Completion code has to be maximum 32 characters") val completionCode: String
)

data class EligibleUsersDto(
    val numOfUsers: Int
)
