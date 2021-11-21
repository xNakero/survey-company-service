package pl.wat.surveycompanyservice.infrastructure.repository

import org.elasticsearch.action.DocWriteResponse
import org.jetbrains.annotations.NotNull
import pl.wat.surveycompanyservice.domain.profile.PersonalProfile
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileQueryParams
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileRepository
import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.UserId

import java.util.concurrent.CopyOnWriteArraySet

class InMemoryPersonalProfileRepository implements PersonalProfileRepository{

    Set<PersonalProfile> personalProfiles = new CopyOnWriteArraySet<>()

    @Override
    void save(PersonalProfile personalProfile) {
        personalProfiles.remove(
                personalProfiles.find {
                    it.participantId == personalProfile.participantId
                }
        )
        personalProfiles.add(personalProfile)
    }

    @Override
    DocWriteResponse.Result updateProfile(PersonalProfile personalProfile) {
        return null
    }

    @NotNull
    @Override
    PersonalProfile findProfile(@NotNull ParticipantId participantId) {
        return personalProfiles.find {it.participantId == userId}
    }

    @Override
    List<String> findEligibleParticipantIds(PersonalProfileQueryParams queryParams) {
        return null
    }

    void clear() {
        personalProfiles.clear()
    }

    Set<PersonalProfile> findAll() {
        return personalProfiles
    }

    boolean containsProfileWithId(String id) {
        return personalProfiles.findAll {it.participantId.raw == '1'}.size() == 1
    }
}