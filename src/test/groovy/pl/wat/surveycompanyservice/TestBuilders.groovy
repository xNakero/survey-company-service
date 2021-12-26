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
import pl.wat.surveycompanyservice.domain.survey.Survey
import pl.wat.surveycompanyservice.domain.survey.SurveyStatus
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipation
import pl.wat.surveycompanyservice.domain.surveyparticipation.ParticipationStatus
import pl.wat.surveycompanyservice.domain.user.AppUser
import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.ResearcherId
import pl.wat.surveycompanyservice.shared.SurveyId
import pl.wat.surveycompanyservice.shared.SurveyParticipationId

import java.time.Instant
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
    public static final int SPOTS_TOTAL = 12
    public static final int SPOTS_TAKEN = 0
    public static final String COMPLETION_CODE = 'SQVQ3RBFSKSJ0X9UTWXJSPP306QO5C2L'
    public static final String SURVEY_ID = '61cb2fbf-e83a-4fd2-9d7b-879686653699'
    public static final String RESEARCHER_ID = '2'
    public static final String SURVEY_PARTICIPATION_ID = '1-1'
    public static final String PARTICIPATION_STATUS = 'IN_PROGRESS'
    public static final Instant STARTED_AT = Instant.parse('2021-11-11T10:00:00.000Z')
    public static final Instant HAS_TO_FINISH_UNTIL = Instant.parse('2021-11-11T10:20:00.000Z')
    public static final String SURVEY_STATUS = 'ACTIVE'
    public static final Instant SURVEY_STARTED_AT = Instant.parse('2021-11-11T10:30:00.000Z')
    public static final Instant PARTICIPATION_FINISHED_AT = Instant.parse('2021-11-11T10:15:00.000Z')

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

    static Survey survey(Map params = [:]) {
        SurveyId surveyId = params.surveyId != null ? new SurveyId(params.surveyId) : new SurveyId(SURVEY_ID)
        ResearcherId researcherId = params.researcherId != null ? new ResearcherId(params.researcherId) : new ResearcherId(RESEARCHER_ID)
        List participationIds = params.participationIds as List ?: []
        List eligibleParticipantIds = params.eligibleParticipantIds as List ?: []
        String title = params.title as String ?: TITLE
        String url = params.url as String ?: URL
        Integer timeToCompleteInSeconds = params.timeToCompleteInSeconds as Integer ?: TIME_TO_COMPLETE_IN_SECONDS
        String description = params.description as String ?: DESCRIPTION
        Integer spotsTotal = params.spotsTotal as Integer ?: SPOTS_TOTAL
        Integer spotsTaken = params.spotsTaken as Integer ?: SPOTS_TAKEN
        String completionCode = params.completionCode as String ?: COMPLETION_CODE
        SurveyStatus status = params.status as SurveyStatus ?: SURVEY_STATUS as SurveyStatus
        Instant startedAt = params.startedAt ? Instant.parse(params.startedAt) : SURVEY_STARTED_AT


        return new Survey(
                surveyId,
                researcherId,
                participationIds,
                eligibleParticipantIds,
                title,
                url,
                timeToCompleteInSeconds,
                description,
                spotsTotal,
                spotsTaken,
                completionCode,
                status,
                startedAt
        )
    }

    static SurveyParticipation surveyParticipation(Map params = [:]) {
        SurveyParticipationId surveyParticipationId = params.surveyParticipationId != null?
                new SurveyParticipationId(params.surveyParticipationId) : new SurveyParticipationId(SURVEY_PARTICIPATION_ID)
        ParticipantId participantId = params.participantId != null ? new ParticipantId(params.participantId) : new ParticipantId(PARTICIPANT_ID)
        SurveyId surveyId = params.surveyId != null ? new SurveyId(params.surveyId) : new SurveyId(SURVEY_ID)
        ParticipationStatus status = params.status as ParticipationStatus ?: PARTICIPATION_STATUS as ParticipationStatus
        Instant startedAt =  params.startedAt ? Instant.parse(params.startedAt) : STARTED_AT
        Instant hasToFinishUntil = params.hasToFinishUntil ? Instant.parse(params.hasToFinishUntil) : HAS_TO_FINISH_UNTIL
        String completionCode = params.completionCode
        Instant finishedAt = params.finishedAt ? Instant.parse(params.finishedAt) : PARTICIPATION_FINISHED_AT

        return new SurveyParticipation(
                surveyParticipationId,
                participantId,
                surveyId,
                status,
                startedAt,
                hasToFinishUntil,
                completionCode,
                finishedAt
        )
    }
}