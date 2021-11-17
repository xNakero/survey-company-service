package pl.wat.surveycompanyservice.domain.profile

import pl.wat.surveycompanyservice.infrastructure.validator.Enum
import javax.validation.constraints.Max
import javax.validation.constraints.Min

data class PersonalProfileQueryParams(
    @field:Min(value = 16, message = "Age has to be at least 16.")
    @field:Max(value = 99, message = "Age has to be maximum 99.")
    val olderOrEqualThan: Int?,
    @field:Min(value = 16, message = "Age has to be at least 16.")
    @field:Max(value = 99, message = "Age has to be maximum 99.")
    val youngerOrEqualThan: Int?,
    @field:Enum(CivilStatus::class, message = "There is no such civil status.") val civilStatus: String?,
    @field:Enum(Country::class, message = "There is no such country.") val countryOfBirth: String?,
    @field:Enum(Country::class, message = "There is no such country.")val nationality: String?,
    @field:Enum(Country::class, message = "There is no such country.")val currentCountry: String?,
    @field:Enum(Language::class, message = "There is no such language.")val firstLanguage: String?,
    @field:Enum(EducationLevel::class, message = "There is no such education level.")val highestEducationLevelAchieved: String?,
    val isStudent: Boolean?,
    @field:Min(value = 0, message = "Income can't be negative.") val monthlyIncomeHigherOrEqualThan: Int?,
    val monthlyIncomeLesserOrEqualThan: Int?,
    @field:Enum(EmploymentStatus::class, message = "There is no such employment status.")val employmentStatus: String?,
    @field:Enum(FormOfEmployment::class, message = "There is no such form of employment.")val formOfEmployment: String?,
    @field:Enum(Industry::class, message = "There is no such industry.") val industry: String?,
    @field:Enum(PoliticalSide::class, message = "There is no such political side.")val politicalSide: String?
)