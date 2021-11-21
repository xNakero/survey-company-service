package pl.wat.surveycompanyservice.domain.profile

import org.elasticsearch.action.DocWriteResponse.Result
import org.elasticsearch.action.DocWriteResponse.Result.NOOP
import org.elasticsearch.action.DocWriteResponse.Result.NOT_FOUND
import org.elasticsearch.action.DocWriteResponse.Result.UPDATED
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

    fun updateProfile(personalProfile: PersonalProfile) =
        personalProfileRepository.updateProfile(personalProfile)
            .handleUpdate(personalProfile)

    fun clearProfileData(participantId: ParticipantId): PersonalProfileDto {
        val emptyPersonalProfile = getEmptyPersonalProfile(participantId)
        val result = personalProfileRepository.updateProfile(emptyPersonalProfile)
        if (result in listOf(UPDATED, NOOP) )
            return emptyPersonalProfile.toPersonalProfileDto()
        else
            throw UnknownOperationException("There was an error when clearing profile of user with id: ${participantId.raw}")
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

    private fun Result.handleUpdate(personalProfile: PersonalProfile): PersonalProfileDto {
        return when (this) {
            NOT_FOUND -> throw NoPersonalProfileFoundException("There is no personal profile for user with id: ${personalProfile.participantId.raw}.")
            NOOP -> personalProfile.toPersonalProfileDto()
            UPDATED -> personalProfileRepository.findProfile(personalProfile.participantId).toPersonalProfileDto()
            else -> throw UnknownOperationException("There was an error when updating profile of user with id: ${personalProfile.participantId.raw}")
        }
    }
}

fun PersonalProfile.toPersonalProfileDto(): PersonalProfileDto = PersonalProfileDto(
    basicInformation = BasicInformation(
        dateOfBirth = dateOfBirth,
        civilStatus = civilStatus.toString()
    ),
    demographics = Demographics(
        countryOfBirth = countryOfBirth.toString(),
        nationality = nationality.toString(),
        currentCountry = currentCountry.toString(),
        firstLanguage = firstLanguage.toString()
    ),
    education = Education(
        highestEducationLevelAchieved = highestEducationLevelAchieved.toString(),
        isStudent = isStudent
    ),
    work = Work(
        monthlyIncome = monthlyIncome,
        employmentStatus = employmentStatus.toString(),
        formOfEmployment = formOfEmployment.toString(),
        industry = industry.toString()
    ),
    politicalViews = PoliticalViews(
        politicalSide = politicalSide.toString()
    )
)

class NoPersonalProfileFoundException(message: String?) : RuntimeException(message)
class UnknownOperationException(message: String?) : RuntimeException(message)