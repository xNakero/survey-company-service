package pl.wat.surveycompanyservice.infrastructure.repository

import org.elasticsearch.action.DocWriteResponse
import org.jetbrains.annotations.NotNull
import pl.wat.surveycompanyservice.domain.profile.PersonalProfile
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileQueryParams
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileRepository
import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.UserId

import java.time.LocalDate
import java.util.concurrent.CopyOnWriteArraySet

class InMemoryPersonalProfileRepository implements PersonalProfileRepository {

    Set<PersonalProfile> personalProfiles = new CopyOnWriteArraySet<>()

    @Override
    void save(PersonalProfile personalProfile) {
        personalProfiles.remove(
                personalProfiles.find {
                    it.participantId.raw == personalProfile.participantId.raw
                }
        )
        personalProfiles.add(personalProfile)
    }

    @Override
    void updateProfile(PersonalProfile personalProfile) {
        personalProfiles.remove(
                personalProfiles.find {
                    it.participantId.raw == personalProfile.participantId.raw
                }
        )
        personalProfiles.add(personalProfile)
    }

    @NotNull
    @Override
    PersonalProfile findProfile(@NotNull ParticipantId participantId) {
        return personalProfiles.find {it.participantId.raw == participantId.raw}
    }

    @Override
    List<String> findEligibleParticipantIds(PersonalProfileQueryParams queryParams) {
        CopyOnWriteArraySet<PersonalProfile> participants = personalProfiles
        if (queryParams.olderOrEqualThan != null) {
            participants = participants.findAll {
                it.dateOfBirth <= LocalDate.parse(queryParams.olderOrEqualThan)
                        .minusDays(1)
                        .minusYears(queryParams.olderOrEqualThan - 1)
            }
        }
        if (queryParams.youngerOrEqualThan != null) {
            participants = participants.findAll {
                it.dateOfBirth <= LocalDate.parse(queryParams.olderOrEqualThan)
                        .minusYears(queryParams.youngerOrEqualThan)
            }
        }
        if (queryParams.civilStatus != null) {
            participants = participants.findAll {it.civilStatus.toString() == queryParams.civilStatus}
        }
        if (queryParams.countryOfBirth != null) {
            participants = participants.findAll {it.countryOfBirth.toString() == queryParams.countryOfBirth}
        }
        if (queryParams.nationality != null) {
            participants = participants.findAll {it.nationality.toString() == queryParams.nationality}
        }
        if (queryParams.currentCountry != null) {
            participants = participants.findAll {it.currentCountry.toString() == queryParams.currentCountry}
        }
        if (queryParams.firstLanguage != null) {
            participants = participants.findAll {it.firstLanguage.toString() == queryParams.firstLanguage}
        }
        if (queryParams.highestEducationLevelAchieved != null) {
            participants = participants.findAll {
                it.highestEducationLevelAchieved.toString() == queryParams.highestEducationLevelAchieved
            }
        }
        if (queryParams.isStudent() != null) {
            participants = participants.findAll {it.isStudent() == queryParams.isStudent()}
        }
        if (queryParams.monthlyIncomeHigherOrEqualThan != null) {
            participants = participants.findAll {it.monthlyIncome >= queryParams.monthlyIncomeHigherOrEqualThan}
        }
        if (queryParams.monthlyIncomeLesserOrEqualThan != null) {
            participants = participants.findAll {it.monthlyIncome <= queryParams.monthlyIncomeLesserOrEqualThan}
        }
        if (queryParams.employmentStatus != null) {
            participants = participants.findAll {it.employmentStatus.toString() == queryParams.employmentStatus}
        }
        if (queryParams.formOfEmployment != null) {
            participants = participants.findAll {it.formOfEmployment.toString() == queryParams.formOfEmployment}
        }
        if (queryParams.industry != null) {
            participants = participants.findAll {it.industry.toString() == queryParams.industry}
        }
        if (queryParams.politicalSide != null) {
            participants = participants.findAll {it.politicalSide.toString() == queryParams.politicalSide}
        }

        return participants.collect {it.participantId.raw}
    }

    void clear() {
        personalProfiles.clear()
    }

    Set<PersonalProfile> findAll() {
        return personalProfiles
    }

    boolean containsProfileWithId(String id) {
        return personalProfiles.findAll {it.participantId.raw == id}.size() == 1
    }
}