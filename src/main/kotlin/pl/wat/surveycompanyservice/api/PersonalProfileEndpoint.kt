package pl.wat.surveycompanyservice.api

import org.springframework.http.HttpStatus.OK
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pl.wat.surveycompanyservice.domain.profile.CivilStatus
import pl.wat.surveycompanyservice.domain.profile.Country
import pl.wat.surveycompanyservice.domain.profile.EducationLevel
import pl.wat.surveycompanyservice.domain.profile.EmploymentStatus
import pl.wat.surveycompanyservice.domain.profile.FormOfEmployment
import pl.wat.surveycompanyservice.domain.profile.Industry
import pl.wat.surveycompanyservice.domain.profile.Language
import pl.wat.surveycompanyservice.domain.profile.PersonalProfile
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileFacade
import pl.wat.surveycompanyservice.domain.profile.PoliticalSide
import pl.wat.surveycompanyservice.domain.user.AppUser
import pl.wat.surveycompanyservice.infrastructure.validator.Enum
import pl.wat.surveycompanyservice.shared.ParticipantId
import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.Min

@RestController
@RequestMapping("/personal-profile")
class PersonalProfileEndpoint(
    private val personalProfileFacade: PersonalProfileFacade
) {
    @PutMapping
    @ResponseStatus(OK)
    fun update(
        @AuthenticationPrincipal user: AppUser,
        @Valid @RequestBody request: PersonalProfileDto
    ): PersonalProfileDto =
        personalProfileFacade.updateProfile(request.toPersonalProfile(user.userId.toString()))

    @GetMapping
    @ResponseStatus(OK)
    fun get(@AuthenticationPrincipal user: AppUser): PersonalProfileDto =
        personalProfileFacade.getProfileData(ParticipantId(user.userId.toString()))
}

@Validated
data class PersonalProfileDto(
    @field:Valid val basicInformation: BasicInformation,
    @field:Valid val demographics: Demographics,
    @field:Valid val education: Education,
    @field:Valid val work: Work,
    @field:Valid val politicalViews: PoliticalViews
) {
    fun toPersonalProfile(userId: String): PersonalProfile = PersonalProfile(
        participantId = ParticipantId(userId),
        dateOfBirth = basicInformation.dateOfBirth,
        civilStatus = basicInformation.civilStatus?.let { CivilStatus.valueOf(it) },
        countryOfBirth = demographics.countryOfBirth?.let { Country.valueOf(it) },
        nationality = demographics.nationality?.let { Country.valueOf(it) },
        currentCountry = demographics.currentCountry?.let { Country.valueOf(it) },
        firstLanguage = demographics.firstLanguage?.let { Language.valueOf(it) },
        highestEducationLevelAchieved = education.highestEducationLevelAchieved?.let { EducationLevel.valueOf(it) },
        isStudent = education.isStudent,
        monthlyIncome = work.monthlyIncome,
        employmentStatus = work.employmentStatus?.let { EmploymentStatus.valueOf(it) },
        formOfEmployment = work.formOfEmployment?.let { FormOfEmployment.valueOf(it) },
        industry = work.industry?.let { Industry.valueOf(it) },
        politicalSide = politicalViews.politicalSide?.let { PoliticalSide.valueOf(it) }
    )
}

data class BasicInformation(
    val dateOfBirth: LocalDate?,
    @field:Enum(CivilStatus::class, message = "There is no such civil status.") val civilStatus: String?
)

data class Demographics(
    @field:Enum(Country::class, message = "There is no such country for countryOBirth.") val countryOfBirth: String?,
    @field:Enum(Country::class, message = "There is no such country for nationality.") val nationality: String?,
    @field:Enum(Country::class, message = "There is no such country for currentCountry.") val currentCountry: String?,
    @field:Enum(Language::class, message = "There is no such language.") val firstLanguage: String?
)

data class Education(
    @field:Enum(EducationLevel::class, message = "There is no such education level.") val highestEducationLevelAchieved: String?,
    val isStudent: Boolean?
)

data class Work(
    @field:Min(value = 0, message = "Minimum income has to be 0.") val monthlyIncome: Int?,
    @field:Enum(EmploymentStatus::class, message = "There is no such employment status.") val employmentStatus: String?,
    @field:Enum(FormOfEmployment::class, message = "There is no such form of employment.") val formOfEmployment: String?,
    @field:Enum(Industry::class, message = "There is no such industry.") val industry: String?
)

data class PoliticalViews(
    @field:Enum(PoliticalSide::class, message = "There is no such political side.") val politicalSide: String?
)