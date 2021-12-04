package pl.wat.surveycompanyservice

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import pl.wat.surveycompanyservice.api.BasicInformation
import pl.wat.surveycompanyservice.api.Demographics
import pl.wat.surveycompanyservice.api.Education
import pl.wat.surveycompanyservice.api.PersonalProfileDto
import pl.wat.surveycompanyservice.api.PoliticalViews
import pl.wat.surveycompanyservice.api.SurveyParamsDto
import pl.wat.surveycompanyservice.api.SurveyToPostDto
import pl.wat.surveycompanyservice.api.Work
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
import pl.wat.surveycompanyservice.domain.survey.Survey
import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.ResearcherId
import pl.wat.surveycompanyservice.shared.SurveyId

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

    public static final String SURVEY_ID = '61cb2fbf-e83a-4fd2-9d7b-879686653699'
    public static final String RESEARCHER_ID = '2'
    public static final String TITLE = 'Title'
    public static final String URL = 'http://survey.com'
    public static final int TIME_TO_COMPLETE_IN_SECONDS = 3600
    public static final String DESCRIPTION = 'Lorem Ipsum dolor sit amet, consectetur adipiscing elit, ' +
            'sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, ' +
            'quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. ' +
            'Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. ' +
            'Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.'
    public static final int SPOTS_TOTAL = 12
    public static final int SPOTS_TAKEN = 0
    public static final String COMPLETION_CODE = 'SQVQ3RBFSKSJ0X9UTWXJSPP306QO5C2L'

    @Autowired
    ObjectMapper objectMapper

    static Map researcherRegistrationRequest(Map params = [:]) {
        return [
                username: params.username ?: RESEARCHER_USERNAME,
                password: params.password ?: PASSWORD,
                role    : "RESEARCHER"
        ]
    }

    static Map participantRegistrationRequest(Map params = [:]) {
        return [
                username: params.username ?: PARTICIPANT_USERNAME,
                password: params.password ?: PASSWORD,
                role    : "PARTICIPANT"
        ]
    }

    static Map loginRequest(Map params = [:]) {
        return [
                username: params.username ?: RESEARCHER_USERNAME,
                password: params.password ?: PASSWORD
        ]
    }

    static PersonalProfileDto personalProfileDto(Map params = [:]) {
        return new PersonalProfileDto(
                new BasicInformation(
                        params.dateOfBirth ? LocalDate.parse(params.dateOfBirth) : DATE_OF_BIRTH,
                        params.civilStatus as String ?: CIVIL_STATUS
                ),
                new Demographics(
                        params.countryOfBirth as String ?: COUNTRY,
                        params.nationality as String ?: COUNTRY,
                        params.currentCountry as String ?: COUNTRY,
                        params.firstLanguage as String ?: LANGUAGE
                ),
                new Education(
                        params.highestEducationLevelAchieved as String ?: EDUCATION_LEVEL,
                        params.isStudent as Boolean ?: IS_STUDENT
                ),
                new Work(
                        params.monthlyIncome as Integer ?: MONTHLY_INCOME,
                        params.employmentStatus as String ?: EMPLOYMENT_STATUS,
                        params.formOfEmployment as String ?: FORM_OF_EMPLOYMENT,
                        params.industry as String ?: INDUSTRY
                ),
                new PoliticalViews(
                        params.politicalSide as String ?: POLITICAL_SIDE
                )
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

    static Survey survey(Map params = [:]) {
        SurveyId surveyId = params.surveyId != null ? new SurveyId(params.surveyId) : new SurveyId(SURVEY_ID)
        ResearcherId researcherId = params.researcherId != null ? new ResearcherId(params.researcherId) : new ResearcherId(RESEARCHER_ID)
        List participantIds = params.participantsIds as List ?: []
        List eligibleParticipantIds = params.eligibleParticipantIds as List ?: []
        String title = params.title as String ?: TITLE
        String url = params.url as String ?: URL
        Integer timeToCompleteInSeconds = params.timeToCompleteInSeconds as Integer ?: TIME_TO_COMPLETE_IN_SECONDS
        String description = params.description as String ?: DESCRIPTION
        Integer spotsTotal = params.spotsTotal as Integer ?: SPOTS_TOTAL
        Integer spotsTaken = params.spotsTaken as Integer ?: SPOTS_TAKEN
        String completionCode = params.completionCode as String ?: COMPLETION_CODE

        return new Survey(
                surveyId,
                researcherId,
                participantIds,
                eligibleParticipantIds,
                title,
                url,
                timeToCompleteInSeconds,
                description,
                spotsTotal,
                spotsTaken,
                completionCode
        )
    }

    static SurveyParamsDto surveyParamsDto(Map params = [:]) {
        String title = params.title as String ?: TITLE
        String url = params.url as String ?: URL
        Integer timeToCompleteInSeconds = params.timeToCompleteInSeconds as Integer ?: TIME_TO_COMPLETE_IN_SECONDS
        String description = params.description as String ?: DESCRIPTION
        Integer spots = params.spots as Integer ?: SPOTS_TOTAL

        return new SurveyParamsDto(
                title,
                url,
                timeToCompleteInSeconds,
                description,
                spots
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
