package pl.wat.surveycompanyservice.domain.profile

import org.elasticsearch.action.DocWriteResponse.Result
import pl.wat.surveycompanyservice.shared.ParticipantId

interface PersonalProfileRepository {
    fun save(personalProfile: PersonalProfile)
    fun updateProfile(personalProfile: PersonalProfile)
    fun findProfile(participantId: ParticipantId): PersonalProfile
    fun findEligibleParticipantIds(queryParams: PersonalProfileQueryParams): List<String>
}