package pl.wat.surveycompanyservice.domain.profile

import org.springframework.stereotype.Component
import pl.wat.surveycompanyservice.api.PersonalProfileDto
import pl.wat.surveycompanyservice.shared.ParticipantId

@Component
class PersonalProfileFacade(
    private val personalProfileService: PersonalProfileService
){
    fun createEmptyProfile(participantId: ParticipantId) =
        personalProfileService.createEmptyProfile(participantId)

    fun updateProfile(personalProfile: PersonalProfile): PersonalProfileDto =
        personalProfileService.updateProfile(personalProfile)

    fun getProfileData(participantId: ParticipantId): PersonalProfileDto =
        personalProfileService.getProfileData(participantId)
}