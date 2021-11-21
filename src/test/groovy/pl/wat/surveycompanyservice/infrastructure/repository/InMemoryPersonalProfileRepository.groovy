package pl.wat.surveycompanyservice.infrastructure.repository

import org.elasticsearch.action.DocWriteResponse
import pl.wat.surveycompanyservice.domain.profile.PersonalProfile
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileQueryParams
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileRepository
import pl.wat.surveycompanyservice.shared.UserId

import java.util.concurrent.CopyOnWriteArraySet
import java.util.stream.Collectors

class InMemoryPersonalProfileRepository implements PersonalProfileRepository{

    Set<PersonalProfile> personalProfiles = new CopyOnWriteArraySet<>()

    @Override
    void createProfile(PersonalProfile personalProfile) {
        personalProfiles.remove(
                personalProfiles.find {
                    it.userId == personalProfile.userId
                }
        )
        personalProfiles.add(personalProfile)
    }

    @Override
    DocWriteResponse.Result updateProfile(PersonalProfile personalProfile) {
        return null
    }

    @Override
    PersonalProfile getProfile(UserId userId) {
        return personalProfiles.find {it.userId == userId}
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
        return personalProfiles.findAll {it.userId.raw == '1'}.size() == 1
    }
}