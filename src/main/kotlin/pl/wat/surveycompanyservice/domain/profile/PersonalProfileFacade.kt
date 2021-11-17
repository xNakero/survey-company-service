package pl.wat.surveycompanyservice.domain.profile

import org.springframework.stereotype.Component
import pl.wat.surveycompanyservice.api.PersonalProfileDto
import pl.wat.surveycompanyservice.shared.UserId

@Component
class PersonalProfileFacade(
    private val personalProfileService: PersonalProfileService
){
    fun createEmptyProfile(userId: UserId) =
        personalProfileService.createEmptyProfile(userId)

    fun updateProfile(personalProfile: PersonalProfile): PersonalProfileDto =
        personalProfileService.updateProfile(personalProfile)

    fun clearProfileData(userId: UserId) =
        personalProfileService.clearProfileData(userId)

    fun getProfileData(userId: UserId): PersonalProfileDto =
        personalProfileService.getProfileData(userId)
}