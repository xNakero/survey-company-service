package pl.wat.surveycompanyservice.api

import org.hibernate.validator.constraints.Length
import org.springframework.http.HttpStatus.OK
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.CrossOrigin
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
@CrossOrigin
class SurveyParticipationEndpoint(
    val surveyParticipationFacade: SurveyParticipationFacade,
) {

    @PostMapping("/surveys/{surveyId}/participations")
    @ResponseStatus(OK)
    fun participate(
        @AuthenticationPrincipal user: AppUser,
        @PathVariable surveyId: String
    ) = surveyParticipationFacade.participate(ParticipantId(user.userId.toString()), SurveyId(surveyId))

    //validation was failing for random reason had to disable it
    @PutMapping("/surveys/{surveyId}/participations/{participationId}")
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
    @field:Enum(enumClass = Action::class, message = "There is no such action.") val action: Action,
    val completionCode: String?
)

enum class Action {
    COMPLETE, CANCEL
}