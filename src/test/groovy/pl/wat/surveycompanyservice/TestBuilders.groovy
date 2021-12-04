package pl.wat.surveycompanyservice

import pl.wat.surveycompanyservice.api.SurveyToPostDto
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileQueryParams
import pl.wat.surveycompanyservice.api.SurveyParamsDto
import pl.wat.surveycompanyservice.domain.profile.CivilStatus
import pl.wat.surveycompanyservice.domain.profile.Country
import pl.wat.surveycompanyservice.domain.profile.EducationLevel
import pl.wat.surveycompanyservice.domain.profile.EmploymentStatus
import pl.wat.surveycompanyservice.domain.profile.FormOfEmployment
import pl.wat.surveycompanyservice.domain.profile.Industry
import pl.wat.surveycompanyservice.domain.profile.Language
import pl.wat.surveycompanyservice.domain.profile.PersonalProfile
import pl.wat.surveycompanyservice.domain.profile.PoliticalSide
import pl.wat.surveycompanyservice.domain.role.Role
import pl.wat.surveycompanyservice.domain.user.AppUser
import pl.wat.surveycompanyservice.shared.ParticipantId

import java.time.LocalDate

import static pl.wat.surveycompanyservice.domain.role.AppRole.PARTICIPANT

class TestBuilders {

    public static final String USERNAME = "username"
    public static final String PASSWORD = "password"
    public static final Long USER_ID = 1
    public static final LocalDate DATE_OF_BIRTH = LocalDate.parse('2000-01-01')
    public static final String CIVIL_STATUS = 'SINGLE'
    public static final String COUNTRY = 'POLAND'
    public static final String LANGUAGE = 'POLISH'
    public static final String EDUCATION_LEVEL = 'GRADUATE'
    public static final boolean IS_STUDENT = false
    public static final int MONTHLY_INCOME = 12345
    public static final String EMPLOYMENT_STATUS = 'FULL_TIME'
    public static final String FORM_OF_EMPLOYMENT = 'CONTRACT_OF_EMPLOYMENT'
    public static final String INDUSTRY = 'FINANCE'
    public static final String POLITICAL_SIDE = 'CENTRE'
    public static final String PARTICIPANT_ID = '1'
    public static final String TITLE = 'title'
    public static final String URL = 'http://url.com'
    public static final int TIME_TO_COMPLETE_IN_SECONDS = 600
    public static final String DESCRIPTION = 'description'
    public static final int SPOTS = 10

    static AppUser appUser(Map params = [:]) {
        return new AppUser(
                params.userId as Long ?: USER_ID,
                params.username as String ?: USERNAME,
                params.password as String ?:PASSWORD,
                [new Role(1L, PARTICIPANT.toString(), [] as Set)] as Set
        )
    }

    static PersonalProfile personalProfile(Map params = [:]) {
        ParticipantId participantId = params.participantId != null ? new ParticipantId(params.participantId) : new ParticipantId(PARTICIPANT_ID)
        LocalDate dateOfBirth = params.dateOfBirth ? LocalDate.parse(params.dateOfBirth) : DATE_OF_BIRTH
        CivilStatus civilStatus = params.civilStatus as CivilStatus ?: CIVIL_STATUS as CivilStatus
        Country countryOfBirth = params.countryOfBirth as Country ?: COUNTRY as Country
        Country nationality = params.nationality as Country ?: COUNTRY as Country
        Country currentCountry = params.currentCountry as Country ?: COUNTRY as Country
        Language firstLanguage = params.firstLanguage as Language ?: LANGUAGE as Language
        EducationLevel highestEducationLevelAchieved = params.highestEducationLevelAchieved as EducationLevel ?: EDUCATION_LEVEL as EducationLevel
        Boolean isStudent = params.isStudent as Boolean ?: IS_STUDENT
        int monthlyIncome = params.monthlyIncome as Integer ?: MONTHLY_INCOME
        EmploymentStatus employmentStatus = params.employmentStatus as EmploymentStatus ?: EMPLOYMENT_STATUS as EmploymentStatus
        FormOfEmployment formOfEmployment = params.formOfEmployment as FormOfEmployment ?: FORM_OF_EMPLOYMENT as FormOfEmployment
        Industry industry = params.industry as Industry ?: INDUSTRY as Industry
        PoliticalSide politicalSide = params.politicalSide as PoliticalSide ?: POLITICAL_SIDE as PoliticalSide

        return new PersonalProfile(
                participantId,
                dateOfBirth,
                civilStatus,
                countryOfBirth,
                nationality,
                currentCountry,
                firstLanguage,
                highestEducationLevelAchieved,
                isStudent,
                monthlyIncome,
                employmentStatus,
                formOfEmployment,
                industry,
                politicalSide
        )
    }

    static SurveyParamsDto surveyParamsDto(Map params = [:]) {
        String title = params.title as String ?: TITLE
        String url = params.url as String ?: URL
        Integer timeToCompleteInSeconds = params.timeToCompleteInSeconds as Integer ?: TIME_TO_COMPLETE_IN_SECONDS
        String description = params.description as String ?: DESCRIPTION
        Integer spots = params.spots as Integer ?: SPOTS

        return new SurveyParamsDto(
                title,
                url,
                timeToCompleteInSeconds,
                description,
                spots
        )
    }

    static PersonalProfileQueryParams personalProfileQueryParams(Map params = [:]) {
        Integer olderOrEqualThan = params.olderOrEqualThan as Integer
        Integer youngerOrEqualThan = params.youngerOrEqualThan as Integer
        String civilStatus = params.civilStatus as String
        String countryOfBirth = params.countryOfBirth as String
        String nationality = params.nationality as String
        String currentCountry = params.currentCountry as String
        String firstLanguage = params.firstLanguage as String
        String highestEducationLevelAchieved = params.highestEducationLEvelAchieved as String
        Boolean isStudent = params.isStudent as Boolean
        Integer monthlyIncomeHigherOrEqualThan = params.monthlyIncomeHigherOrEqualThan as Integer
        Integer monthlyIncomeLesserOrEqualThan = params.monthlyIncomeLesserOrEqualThan as Integer
        String employmentStatus = params.employmentStatus as String
        String formOfEmployment = params.formOfEmployment as String
        String industry = params.industry as String
        String politicalSide = params.politicalSide as String

        return new PersonalProfileQueryParams(
                olderOrEqualThan,
                youngerOrEqualThan,
                civilStatus,
                countryOfBirth,
                nationality,
                currentCountry,
                firstLanguage,
                highestEducationLevelAchieved,
                isStudent,
                monthlyIncomeHigherOrEqualThan,
                monthlyIncomeLesserOrEqualThan,
                employmentStatus,
                formOfEmployment,
                industry,
                politicalSide
        )
    }

    static SurveyToPostDto surveyToPostDto(Map params = [:]) {
        SurveyParamsDto surveyParamsDto = params.surveyParamsDto ? surveyParamsDto(params.surveyParamsDto as Map) : surveyParamsDto()
        PersonalProfileQueryParams personalProfileQueryParams = params.personalProfileQueryParams ?
                personalProfileQueryParams(params.personalProfileQueryParams as Map) :
                personalProfileQueryParams()

        return new SurveyToPostDto(
                surveyParamsDto,
                personalProfileQueryParams
        )
    }
}