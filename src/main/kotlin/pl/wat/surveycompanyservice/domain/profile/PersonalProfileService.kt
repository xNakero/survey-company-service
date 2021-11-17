package pl.wat.surveycompanyservice.domain.profile

import org.elasticsearch.action.DocWriteResponse.Result
import org.elasticsearch.action.DocWriteResponse.Result.NOOP
import org.elasticsearch.action.DocWriteResponse.Result.UPDATED
import org.springframework.stereotype.Service
import pl.wat.surveycompanyservice.api.BasicInformation
import pl.wat.surveycompanyservice.api.Demographics
import pl.wat.surveycompanyservice.api.Education
import pl.wat.surveycompanyservice.api.PersonalProfileDto
import pl.wat.surveycompanyservice.api.PoliticalViews
import pl.wat.surveycompanyservice.api.Work
import pl.wat.surveycompanyservice.shared.UserId

@Service
class PersonalProfileService(
    private val personalProfileRepository: PersonalProfileRepository
) {
    fun createEmptyProfile(userId: UserId) =
        getEmptyPersonalProfile(userId).let { personalProfileRepository.createEmptyProfile(it) }

    fun updateProfile(personalProfile: PersonalProfile) =
        personalProfileRepository.updateProfile(personalProfile)
            .handleUpdate(personalProfile)

    fun clearProfileData(userId: UserId): PersonalProfileDto {
        val emptyPersonalProfile = getEmptyPersonalProfile(userId)
        val result = personalProfileRepository.updateProfile(emptyPersonalProfile)
        if (result in listOf(UPDATED, NOOP) )
            return emptyPersonalProfile.toPersonalProfileDto()
        else
            throw UnknownOperationException("There was an error when clearing profile of user with id: ${userId.raw}")
    }

    fun getProfileData(userId: UserId): PersonalProfileDto =
        personalProfileRepository.getProfile(userId).toPersonalProfileDto()

    private fun getEmptyPersonalProfile(userId: UserId): PersonalProfile = PersonalProfile(
        userId = userId,
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
            Result.NOT_FOUND -> throw NoPersonalProfileFoundException("There is no personal profile for user with id: ${personalProfile.userId.raw}.")
            NOOP -> personalProfile.toPersonalProfileDto()
            UPDATED -> personalProfileRepository.getProfile(personalProfile.userId).toPersonalProfileDto()
            else -> throw UnknownOperationException("There was an error when updating profile of user with id: ${personalProfile.userId.raw}")
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