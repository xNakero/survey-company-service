package pl.wat.surveycompanyservice.domain.profile

import org.springframework.stereotype.Service
import pl.wat.surveycompanyservice.api.BasicInformation
import pl.wat.surveycompanyservice.api.Demographics
import pl.wat.surveycompanyservice.api.Education
import pl.wat.surveycompanyservice.api.PersonalProfileDto
import pl.wat.surveycompanyservice.api.PoliticalViews
import pl.wat.surveycompanyservice.api.Work
import pl.wat.surveycompanyservice.shared.ParticipantId

@Service
class PersonalProfileService(
    private val personalProfileRepository: PersonalProfileRepository
) {
    fun createEmptyProfile(participantId: ParticipantId) =
        getEmptyPersonalProfile(participantId).let { personalProfileRepository.save(it) }

    fun updateProfile(personalProfile: PersonalProfile): PersonalProfileDto {
        personalProfileRepository.updateProfile(personalProfile)
        return personalProfileRepository.findProfile(personalProfile.participantId).toPersonalProfileDto()
    }

    fun getProfileData(participantId: ParticipantId): PersonalProfileDto =
        personalProfileRepository.findProfile(participantId).toPersonalProfileDto()

    fun findEligibleParticipantIds(query: PersonalProfileQueryParams): List<String> =
        personalProfileRepository.findEligibleParticipantIds(query)

    private fun getEmptyPersonalProfile(participantId: ParticipantId): PersonalProfile = PersonalProfile(
        participantId = participantId,
        dateOfBirth = null,
        civilStatus = null,
        countryOfBirth = null,
        nationality = null,
        currentCountry = null,
        firstLanguage = null,
        highestEducationLevelAchieved = null,
        isStudent = null,
        monthlyIncome = null,
        employmentStatus = null,
        formOfEmployment = null,
        industry = null,
        politicalSide = null
    )
}

fun PersonalProfile.toPersonalProfileDto(): PersonalProfileDto = PersonalProfileDto(
    basicInformation = BasicInformation(
        dateOfBirth = dateOfBirth,
        civilStatus = civilStatus?.toString()
    ),
    demographics = Demographics(
        countryOfBirth = countryOfBirth?.toString(),
        nationality = nationality?.toString(),
        currentCountry = currentCountry?.toString(),
        firstLanguage = firstLanguage?.toString()
    ),
    education = Education(
        highestEducationLevelAchieved = highestEducationLevelAchieved?.toString(),
        isStudent = isStudent
    ),
    work = Work(
        monthlyIncome = monthlyIncome,
        employmentStatus = employmentStatus?.toString(),
        formOfEmployment = formOfEmployment?.toString(),
        industry = industry?.toString()
    ),
    politicalViews = PoliticalViews(
        politicalSide = politicalSide?.toString()
    )
)