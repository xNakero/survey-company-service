package pl.wat.surveycompanyservice.domain.profile

import org.elasticsearch.action.DocWriteResponse.Result
import pl.wat.surveycompanyservice.shared.UserId

interface PersonalProfileRepository {
    fun createEmptyProfile(personalProfile: PersonalProfile)
    fun updateProfile(personalProfile: PersonalProfile): Result
    fun getProfile(userId: UserId): PersonalProfile
    fun findEligibleParticipantIds(queryParams: PersonalProfileQueryParams): List<String>
}