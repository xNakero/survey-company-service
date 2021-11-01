package pl.wat.surveycompanyservice.domain.profile

import java.time.LocalDate

data class PersonalProfile(
    val basicInformation: BasicInformation,
    val demographics: DemographicsData,
    val finance: Finance,
    val education: Education,
    val work: Work
)

data class BasicInformation(
    val dateOfBirth: LocalDate?,
    val civilStatus: CivilStatus?
)

data class DemographicsData(
    val countryOfBirth: Country?,
    val nationality: Country?,
    val currentCountry: Country?,
    val firstLanguage: Language?
)

data class Finance(
    val yearlyIncomeInEuro: Int?,
    val employmentStatus: EmploymentStatus?
)

data class Education(
    val highestEducationLevelAchieved: EducationLevel?,
    val isStudent: Boolean?
)

data class Work(
    val monthlyIncome: Int?,
    val employmentStatus: EmploymentStatus?,
    val formOfEmployment: FormOfEmployment?,
    val industry: Industry?
)

enum class CivilStatus {
    SINGLE, MARRIED, DIVORCED, WIDOW
}

enum class Country {
    POLAND, GERMANY, ENGLAND
}

enum class Language {
    POLISH, GERMAN, ENGLISH_UK
}

enum class EmploymentStatus {
    FULL_TIME, PART_TIME, UNEMPLOYED, RETIRED
}

enum class EducationLevel {
    PRIMARY_SCHOOL, HIGH_SCHOOL, UNDERGRADUATE, GRADUATE, DOCTORATE_OR_HIGHER
}

enum class FormOfEmployment {
    CONTRACT_OF_EMPLOYMENT, CONTRACT_OF_MANDATE, BUSINESS_TO_BUSINESS
}

enum class Industry {
    IT, FINANCE, TRADE
}
