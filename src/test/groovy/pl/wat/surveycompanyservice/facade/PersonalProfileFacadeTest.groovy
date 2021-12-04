package pl.wat.surveycompanyservice.facade

import pl.wat.surveycompanyservice.BaseUnitTest
import pl.wat.surveycompanyservice.api.PersonalProfileDto
import pl.wat.surveycompanyservice.domain.profile.PersonalProfile
import pl.wat.surveycompanyservice.shared.ParticipantId

import static pl.wat.surveycompanyservice.TestBuilders.CIVIL_STATUS
import static pl.wat.surveycompanyservice.TestBuilders.COUNTRY
import static pl.wat.surveycompanyservice.TestBuilders.DATE_OF_BIRTH
import static pl.wat.surveycompanyservice.TestBuilders.EDUCATION_LEVEL
import static pl.wat.surveycompanyservice.TestBuilders.EMPLOYMENT_STATUS
import static pl.wat.surveycompanyservice.TestBuilders.FORM_OF_EMPLOYMENT
import static pl.wat.surveycompanyservice.TestBuilders.INDUSTRY
import static pl.wat.surveycompanyservice.TestBuilders.IS_STUDENT
import static pl.wat.surveycompanyservice.TestBuilders.LANGUAGE
import static pl.wat.surveycompanyservice.TestBuilders.MONTHLY_INCOME
import static pl.wat.surveycompanyservice.TestBuilders.PARTICIPANT_ID
import static pl.wat.surveycompanyservice.TestBuilders.POLITICAL_SIDE
import static pl.wat.surveycompanyservice.TestBuilders.personalProfile

class PersonalProfileFacadeTest extends BaseUnitTest{

    def 'should save empty personal profile'() {
        given:
            ParticipantId participantId = new ParticipantId('1')
        when:
            personalProfileFacade.createEmptyProfile(participantId)
        then:
            inMemoryPersonalProfileRepository.containsProfileWithId(participantId.raw)
    }

    def 'should update personal profile and return it'() {
        given:
            inMemoryPersonalProfileRepository.save(personalProfile())
            PersonalProfile profileUpdates = personalProfile([
                    nationality: 'ENGLAND',
                    isStudent: true
            ])
        when:
            PersonalProfileDto updatedProfile = personalProfileFacade.updateProfile(profileUpdates)
        then:
            updatedProfile.demographics.nationality == profileUpdates.nationality.toString()
            updatedProfile.education.student == profileUpdates.student
        and:
            PersonalProfile savedProfile = inMemoryPersonalProfileRepository.findAll()
                    .find {it.participantId.raw == profileUpdates.participantId.raw}
            savedProfile.student == profileUpdates.student
            savedProfile.nationality == profileUpdates.nationality
    }

    def 'should get profile data'() {
        given:
            inMemoryPersonalProfileRepository.save(personalProfile())
        when:
            PersonalProfileDto profile = personalProfileFacade.getProfileData(new ParticipantId(PARTICIPANT_ID))
        then:
            with(profile) {
                it.basicInformation.dateOfBirth == DATE_OF_BIRTH
                it.basicInformation.civilStatus.toString() == CIVIL_STATUS
                it.demographics.countryOfBirth.toString() == COUNTRY
                it.demographics.nationality.toString() == COUNTRY
                it.demographics.currentCountry.toString() == COUNTRY
                it.demographics.firstLanguage.toString() == LANGUAGE
                it.education.highestEducationLevelAchieved.toString() == EDUCATION_LEVEL
                it.education.isStudent() == IS_STUDENT
                it.work.monthlyIncome == MONTHLY_INCOME
                it.work.employmentStatus.toString() == EMPLOYMENT_STATUS
                it.work.formOfEmployment.toString() == FORM_OF_EMPLOYMENT
                it.work.industry.toString() == INDUSTRY
                it.politicalViews.politicalSide.toString() == POLITICAL_SIDE
            }
    }
}
