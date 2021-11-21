package pl.wat.surveycompanyservice.infrastructure.repository

import org.elasticsearch.action.DocWriteResponse.Result
import org.springframework.beans.factory.annotation.Autowired
import pl.wat.surveycompanyservice.BaseIntegrationTest
import pl.wat.surveycompanyservice.domain.profile.EducationLevel
import pl.wat.surveycompanyservice.domain.profile.ElasticPersonalProfile
import pl.wat.surveycompanyservice.domain.profile.PersonalProfile
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileQueryParams
import pl.wat.surveycompanyservice.shared.ParticipantId

import java.time.LocalDate

import static org.elasticsearch.action.DocWriteResponse.Result.NOOP
import static org.elasticsearch.action.DocWriteResponse.Result.UPDATED
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.*
import static pl.wat.surveycompanyservice.domain.profile.CivilStatus.MARRIED
import static pl.wat.surveycompanyservice.domain.profile.Country.GERMANY
import static pl.wat.surveycompanyservice.domain.profile.EmploymentStatus.PART_TIME
import static pl.wat.surveycompanyservice.domain.profile.FormOfEmployment.BUSINESS_TO_BUSINESS
import static pl.wat.surveycompanyservice.domain.profile.Industry.IT
import static pl.wat.surveycompanyservice.domain.profile.Language.ENGLISH
import static pl.wat.surveycompanyservice.domain.profile.PoliticalSide.RIGHT

class ElasticPersonalProfileRepositoryIntTest extends BaseIntegrationTest {

    @Autowired
    ElasticPersonalProfileRepository elasticPersonalProfileRepository

    def 'should save personalProfile'() {
        given:
            PersonalProfile personalProfile = personalProfile()
        when:
            elasticPersonalProfileRepository.save(personalProfile)
        then:
            with(elasticsearchRestTemplate.get(PARTICIPANT_ID, ElasticPersonalProfile.class).toPersonalProfile()) {
                it.dateOfBirth == personalProfile.dateOfBirth
                it.civilStatus == personalProfile.civilStatus
                it.countryOfBirth == personalProfile.countryOfBirth
                it.nationality == personalProfile.nationality
                it.currentCountry == personalProfile.currentCountry
                it.firstLanguage == personalProfile.firstLanguage
                it.highestEducationLevelAchieved == personalProfile.highestEducationLevelAchieved
                it.isStudent() == personalProfile.isStudent()
                it.monthlyIncome == personalProfile.monthlyIncome
                it.employmentStatus == personalProfile.employmentStatus
                it.formOfEmployment == personalProfile.formOfEmployment
                it.industry == personalProfile.industry
                it.politicalSide == personalProfile.politicalSide
            }
    }

    def 'should update profile'() {
        given:
            PersonalProfile defaultProfile = personalProfile()
            elasticsearchRestTemplate.save(defaultProfile.toElasticPersonalProfile())
            Map updateRequestMap = [
                    dateOfBirth                  : '2001-12-12',
                    civilStatus                  : 'MARRIED',
                    countryOfBirth               : 'GERMANY',
                    nationality                  : 'GERMANY',
                    currentCountry               : 'GERMANY',
                    firstLanguage                : 'ENGLISH',
                    highestEducationLevelAchieved: 'DOCTORATE_OR_HIGHER',
                    isStudent                    : true,
                    monthlyIncome                : 23456,
                    employmentStatus             : 'PART_TIME',
                    formOfEmployment             : 'BUSINESS_TO_BUSINESS',
                    industry                     : 'IT',
                    politicalSide                : 'RIGHT'
            ]
            PersonalProfile updateRequest = personalProfile(updateRequestMap)
        when:
            Result noChangeResult = elasticPersonalProfileRepository.updateProfile(defaultProfile)
        then:
            noChangeResult == NOOP
        when:
            Result updatedResult = elasticPersonalProfileRepository.updateProfile(updateRequest)
        then:
            updatedResult == UPDATED
            with(elasticsearchRestTemplate.get(PARTICIPANT_ID, ElasticPersonalProfile.class).toPersonalProfile()) {
                it.dateOfBirth == LocalDate.parse('2001-12-12')
                it.civilStatus == MARRIED
                it.countryOfBirth == GERMANY
                it.nationality == GERMANY
                it.currentCountry == GERMANY
                it.firstLanguage == ENGLISH
                it.highestEducationLevelAchieved == EducationLevel.valueOf('DOCTORATE_OR_HIGHER')
                it.isStudent()
                it.monthlyIncome == 23456
                it.employmentStatus == PART_TIME
                it.formOfEmployment == BUSINESS_TO_BUSINESS
                it.industry == IT
                it.politicalSide == RIGHT
        }
    }

    def 'should find profile by participantId'() {
        given:
            String participantId = '123'
            PersonalProfile defaultProfile = personalProfile([participantId: participantId])
            elasticsearchRestTemplate.save(defaultProfile.toElasticPersonalProfile())
        and:
            Thread.sleep(1000)
        when:
            PersonalProfile profile = elasticPersonalProfileRepository.findProfile(new ParticipantId(participantId))
        then:
            with(profile) {
                it.participantId == defaultProfile.participantId
            }
    }

    def 'should find eligible participants'() {
        given:
            PersonalProfile personalProfile1 = personalProfile([
                    'participantId'                : '123',
                    'dateOfBirth'                  : '2001-12-12',
                    'civilStatus'                  : 'MARRIED',
                    'countryOfBirth'               : 'GERMANY',
                    'nationality'                  : 'GERMANY',
                    'currentCountry'               : 'GERMANY',
                    'firstLanguage'                : 'ENGLISH',
                    'highestEducationLevelAchieved': 'DOCTORATE_OR_HIGHER',
                    'isStudent'                    : true,
                    'monthlyIncome'                : 23456,
                    'employmentStatus'             : 'PART_TIME',
                    'formOfEmployment'             : 'BUSINESS_TO_BUSINESS',
                    'industry'                     : 'IT',
                    'politicalSide'                : 'RIGHT'
            ])
            PersonalProfile personalProfile2 = personalProfile([
                    'participantId'                : '456',
                    'dateOfBirth'                  : '1998-12-18',
                    'civilStatus'                  : 'SINGLE',
                    'countryOfBirth'               : null,
                    'nationality'                  : null,
                    'currentCountry'               : 'POLAND',
                    'firstLanguage'                : null,
                    'highestEducationLevelAchieved': null,
                    'isStudent'                    : true,
                    'monthlyIncome'                : 10000,
                    'employmentStatus'             : 'PART_TIME',
                    'formOfEmployment'             : 'CONTRACT_OF_EMPLOYMENT',
                    'industry'                     : 'IT',
                    'politicalSide'                : 'CENTRE'
            ])
            PersonalProfile personalProfile3 = personalProfile([
                    'participantId'                : '789',
                    'dateOfBirth'                  : '2006-10-10',
                    'civilStatus'                  : 'SINGLE',
                    'countryOfBirth'               : null,
                    'nationality'                  : null,
                    'currentCountry'               : 'GERMANY',
                    'firstLanguage'                : null,
                    'highestEducationLevelAchieved': 'HIGH_SCHOOL',
                    'isStudent'                    : true,
                    'monthlyIncome'                : null,
                    'employmentStatus'             : null,
                    'formOfEmployment'             : null,
                    'industry'                     : null,
                    'politicalSide'                : 'LEFT'
            ])
            PersonalProfile personalProfile4 = personalProfile([
                    'participantId'                : '012',
                    'dateOfBirth'                  : null,
                    'civilStatus'                  : null,
                    'countryOfBirth'               : null,
                    'nationality'                  : null,
                    'currentCountry'               : null,
                    'firstLanguage'                : null,
                    'highestEducationLevelAchieved': null,
                    'isStudent'                    : null,
                    'monthlyIncome'                : null,
                    'employmentStatus'             : null,
                    'formOfEmployment'             : null,
                    'industry'                     : null,
                    'politicalSide'                : null
            ])
        and:
            elasticsearchRestTemplate.save(personalProfile1.toElasticPersonalProfile())
            elasticsearchRestTemplate.save(personalProfile2.toElasticPersonalProfile())
            elasticsearchRestTemplate.save(personalProfile3.toElasticPersonalProfile())
            elasticsearchRestTemplate.save(personalProfile4.toElasticPersonalProfile())
        and:
            Thread.sleep(1000)
        and:
            PersonalProfileQueryParams queryParams1 = personalProfileQueryParams([
                    youngerOrEqualThan: 20,
                    olderOrEqualThan: 20
            ])
            PersonalProfileQueryParams queryParams2 = personalProfileQueryParams([
                    isStudent: true
            ])
            PersonalProfileQueryParams queryParams3 = personalProfileQueryParams([
                    monthlyIncomeHigherOrEqualThan: 9000,
                    monthlyIncomeLesserOrEqualThan: 11000
            ])
            PersonalProfileQueryParams queryParams4 = personalProfileQueryParams([
                    youngerOrEqualThan: 18
            ])
            PersonalProfileQueryParams queryParams5 = personalProfileQueryParams([
                    civilStatus: 'SINGLE',
                    industry: 'IT'
            ])
        when:
            List eligibleParticipantIds1 = elasticPersonalProfileRepository.findEligibleParticipantIds(queryParams1)
            List eligibleParticipantIds2 = elasticPersonalProfileRepository.findEligibleParticipantIds(queryParams2)
            List eligibleParticipantIds3 = elasticPersonalProfileRepository.findEligibleParticipantIds(queryParams3)
            List eligibleParticipantIds4 = elasticPersonalProfileRepository.findEligibleParticipantIds(queryParams4)
            List eligibleParticipantIds5 = elasticPersonalProfileRepository.findEligibleParticipantIds(queryParams5)
        then:
            eligibleParticipantIds1.contains(personalProfile1.participantId.raw)
            eligibleParticipantIds2.containsAll([
                    personalProfile1.participantId.raw,
                    personalProfile2.participantId.raw,
                    personalProfile3.participantId.raw
            ])
            eligibleParticipantIds3.contains(personalProfile2.participantId.raw)
            eligibleParticipantIds4.contains(personalProfile3.participantId.raw)
            eligibleParticipantIds5.contains(personalProfile2.participantId.raw)

    }
}