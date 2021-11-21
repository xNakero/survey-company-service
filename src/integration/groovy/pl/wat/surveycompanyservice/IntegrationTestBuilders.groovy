package pl.wat.surveycompanyservice

import pl.wat.surveycompanyservice.domain.profile.CivilStatus
import pl.wat.surveycompanyservice.domain.profile.Country
import pl.wat.surveycompanyservice.domain.profile.EducationLevel
import pl.wat.surveycompanyservice.domain.profile.EmploymentStatus
import pl.wat.surveycompanyservice.domain.profile.FormOfEmployment
import pl.wat.surveycompanyservice.domain.profile.Industry
import pl.wat.surveycompanyservice.domain.profile.Language
import pl.wat.surveycompanyservice.domain.profile.PersonalProfile
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileQueryParams
import pl.wat.surveycompanyservice.domain.profile.PoliticalSide
import pl.wat.surveycompanyservice.shared.ParticipantId

import java.time.LocalDate

class IntegrationTestBuilders {

    public static final String RESEARCHER_USERNAME = "researcher@gmail.com"
    public static final String PARTICIPANT_USERNAME = "participant@gmail.com"
    public static final String PASSWORD = "password"
    public static final String JWT_REGEX = '^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$'
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

    static Map researcherRegistrationRequest(Map params = [:]) {
        return [
                username    : params.username ?: RESEARCHER_USERNAME,
                password    : params.password ?: PASSWORD,
                role        : "RESEARCHER"
        ]
    }

    static Map participantRegistrationRequest(Map params = [:]) {
        return [
                username    : params.username ?: PARTICIPANT_USERNAME,
                password    : params.password ?: PASSWORD,
                role        : "PARTICIPANT"
        ]
    }

    static Map loginRequest(Map params = [:]) {
        return [
                username    : params.username ?: RESEARCHER_USERNAME,
                password    : params.password ?: PASSWORD
        ]
    }

    static Map personalProfileDto(Map params = [:]) {
        return [
                basicInformation: [
                        dateOfBirth: params.dateOfBirth ?: DATE_OF_BIRTH,
                        civilStatus: params.civilStatus ?: CIVIL_STATUS
                ],
                demographics    : [
                        countryOfBirth: params.countryOfBirth ?: COUNTRY,
                        nationality   : params.nationality ?: COUNTRY,
                        currentCountry: params.currentCountry ?: COUNTRY,
                        firstLanguage : params.firstLanguage ?: LANGUAGE
                ],
                education       : [
                        highestEducationLevelAchieved: params.highestEducationLevelAchieved ?: EDUCATION_LEVEL,
                        isStudent                    : params.isStudent as Boolean ?: IS_STUDENT
                ],
                work            : [
                        monthlyIncome   : params.monthlyIncome as Integer ?: MONTHLY_INCOME,
                        employmentStatus: params.employmentStatus ?: EMPLOYMENT_STATUS,
                        formOfEmployment: params.formOfEmployment ?: FORM_OF_EMPLOYMENT,
                        industry        : params.industry ?: INDUSTRY
                ],
                politicalViews  : [
                        politicalSide: params.politicalSide ?: POLITICAL_SIDE
                ]
        ]
    }

    static PersonalProfile personalProfile(Map params = [:]) {
        ParticipantId participantId = params.participantId != null ? new ParticipantId(params.participantId) : new ParticipantId(PARTICIPANT_ID)
        LocalDate dateOfBirth = params.dateOfBirth ? LocalDate.parse(params.dateOfBirth) : DATE_OF_BIRTH
        CivilStatus civilStatus = params.civilStatus as CivilStatus?: CivilStatus.valueOf(CIVIL_STATUS)
        Country countryOfBirth = params.countryOfBirth as Country ?: COUNTRY as Country
        Country nationality = params.nationality as Country ?: COUNTRY as Country
        Country currentCountry = params.currentCountry as Country ?: COUNTRY as Country
        Language firstLanguage = params.firstLanguage as Language ?: LANGUAGE as Language
        EducationLevel highestEducationLevelAchieved = params.highestEducationLevelAchieved as EducationLevel?: EDUCATION_LEVEL as EducationLevel
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
}
