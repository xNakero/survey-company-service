package pl.wat.surveycompanyservice.domain.profile

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.annotations.FieldType.Date
import org.springframework.data.elasticsearch.annotations.FieldType.Integer
import org.springframework.data.elasticsearch.annotations.FieldType.Text
import pl.wat.surveycompanyservice.shared.UserId
import java.time.Instant
import java.time.LocalDate


data class PersonalProfile(
    val userId: UserId,
    val dateOfBirth: LocalDate?,
    val civilStatus: CivilStatus?,
    val countryOfBirth: Country?,
    val nationality: Country?,
    val currentCountry: Country?,
    val firstLanguage: Language?,
    val highestEducationLevelAchieved: EducationLevel?,
    val isStudent: Boolean?,
    val monthlyIncome: Int?,
    val employmentStatus: EmploymentStatus?,
    val formOfEmployment: FormOfEmployment?,
    val industry: Industry?,
    val politicalSide: PoliticalSide?
) {

    fun toMongoPersonalProfile(): ElasticPersonalProfile = ElasticPersonalProfile(
        userId = userId.raw,
        dateOfBirth = dateOfBirth,
        civilStatus = civilStatus.toString(),
        countryOfBirth = countryOfBirth.toString(),
        nationality = nationality.toString(),
        currentCountry = currentCountry.toString(),
        firstLanguage = firstLanguage.toString(),
        highestEducationLevelAchieved = highestEducationLevelAchieved.toString(),
        isStudent = isStudent,
        monthlyIncome = monthlyIncome,
        employmentStatus = employmentStatus.toString(),
        formOfEmployment = formOfEmployment.toString(),
        industry = industry.toString(),
        politicalSide = politicalSide.toString()
    )
}

@Document(indexName = "personal_profile")
data class ElasticPersonalProfile(
    @Id val userId: String,
    @field:Field(type = Text) val dateOfBirth: LocalDate?,
    @field:Field(type = Date) val civilStatus: String?,
    @field:Field(type = Text) val countryOfBirth: String?,
    @field:Field(type = Text) val nationality: String?,
    @field:Field(type = Text) val currentCountry: String?,
    @field:Field(type = Text) val firstLanguage: String?,
    @field:Field(type = Text) val highestEducationLevelAchieved: String?,
    @field:Field(type = FieldType.Boolean) val isStudent: Boolean?,
    @field:Field(type = Integer) val monthlyIncome: Int?,
    @field:Field(type = Text) val employmentStatus: String?,
    @field:Field(type = Text) val formOfEmployment: String?,
    @field:Field(type = Text) val industry: String?,
    @field:Field(type = Text) val politicalSide: String?
) {
    fun toPersonalProfile(): PersonalProfile = PersonalProfile(
        userId = UserId(userId),
        dateOfBirth = dateOfBirth,
        civilStatus = civilStatus?.let { CivilStatus.valueOf(it) },
        countryOfBirth = countryOfBirth?.let { Country.valueOf(it) },
        nationality = nationality?.let { Country.valueOf(it) },
        currentCountry = currentCountry?.let { Country.valueOf(it) },
        firstLanguage = firstLanguage?.let { Language.valueOf(it) },
        highestEducationLevelAchieved = highestEducationLevelAchieved?.let { EducationLevel.valueOf(it) },
        isStudent = isStudent,
        monthlyIncome = monthlyIncome,
        employmentStatus = employmentStatus?.let { EmploymentStatus.valueOf(it) },
        formOfEmployment = formOfEmployment?.let { FormOfEmployment.valueOf(it) },
        industry = industry?.let { Industry.valueOf(it) },
        politicalSide = politicalSide?.let { PoliticalSide.valueOf(it) }
    )
}

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

enum class PoliticalSide {
    LEFT, CENTRE, RIGHT
}