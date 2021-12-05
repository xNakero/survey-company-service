package pl.wat.surveycompanyservice.api

import org.hibernate.validator.constraints.Length
import org.springframework.http.HttpStatus.OK
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipationFacade
import pl.wat.surveycompanyservice.domain.user.AppUser
import pl.wat.surveycompanyservice.infrastructure.validator.Enum
import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.SurveyId
import pl.wat.surveycompanyservice.shared.SurveyParticipationId

@RestController
class SurveyParticipationEndpoint(
    val surveyParticipationFacade: SurveyParticipationFacade,
) {

    @PostMapping("/survey/{surveyId}/participation")
    @ResponseStatus(OK)
    fun participate(
        @AuthenticationPrincipal user: AppUser,
        @PathVariable surveyId: String
    ) = surveyParticipationFacade.participate(ParticipantId(user.userId.toString()), SurveyId(surveyId))

    @PutMapping("/survey/{surveyId}/participation/{participationId}")
    @ResponseStatus(OK)
    fun manageParticipation(
        @AuthenticationPrincipal user: AppUser,
        @PathVariable surveyId: String,
        @PathVariable participationId: String,
        @RequestBody request: ParticipationModificationDto
    ) = surveyParticipationFacade.manageParticipation(
        ParticipantId(user.userId.toString()),
        SurveyParticipationId(participationId),
        request
    )
}

data class ParticipationModificationDto(
    @field:Enum(Action::class, message = "There is no such action.") val action: Action,
    @field:Length(min = 32, max = 32, message = "Completion code has to have length of 32 characters.")
    val completionCode: String?
)

enum class Action {
    COMPLETE, CANCEL
}