package pl.wat.surveycompanyservice.infrastructure.repository

import com.fasterxml.jackson.databind.ObjectMapper
import org.elasticsearch.action.DocWriteResponse.Result
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.client.RequestOptions.DEFAULT
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType.JSON
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.query.Criteria
import org.springframework.data.elasticsearch.core.query.CriteriaQuery
import org.springframework.stereotype.Component
import pl.wat.surveycompanyservice.domain.profile.ElasticPersonalProfile
import pl.wat.surveycompanyservice.domain.profile.PersonalProfile
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileRepository
import pl.wat.surveycompanyservice.shared.UserId

@Component
class ElasticPersonalProfileRepository(
    private val elasticsearchRestTemplate: ElasticsearchRestTemplate,
    private val client: RestHighLevelClient,
    private val objectMapper: ObjectMapper
) : PersonalProfileRepository {

    override fun createEmptyProfile(personalProfile: PersonalProfile) {
        elasticsearchRestTemplate.save(personalProfile.toMongoPersonalProfile())
    }

    override fun updateProfile(personalProfile: PersonalProfile): Result {
        val updateJson = personalProfile.mapToUpdates()
        val updateRequest = UpdateRequest("personal_profile", personalProfile.userId.raw)
            .doc(updateJson, JSON)
        val updateResponse = client.update(updateRequest, DEFAULT)
        return updateResponse.result
    }

    override fun getProfile(userId: UserId): PersonalProfile {
        val query = CriteriaQuery(Criteria(ID).`is`(userId.raw))
        val hits = elasticsearchRestTemplate.search(query, ElasticPersonalProfile::class.java, IndexCoordinates.of("personal_profile"))
        return hits.getSearchHit(0).content.toPersonalProfile()
    }

    private fun PersonalProfile.mapToUpdates(): String {
        val updates: MutableMap<String, Any?> = mutableMapOf(
            DATE_OF_BIRTH to dateOfBirth,
            CIVIL_STATUS to civilStatus,
            COUNTRY_OF_BIRTH to countryOfBirth,
            NATIONALITY to nationality,
            CURRENT_COUNTRY to currentCountry,
            FIRST_LANGUAGE to firstLanguage,
            HIGHEST_EDUCATION_LEVEL_ACHIEVED to highestEducationLevelAchieved,
            IS_STUDENT to isStudent,
            MONTHLY_INCOME to monthlyIncome,
            EMPLOYMENT_STATUS to employmentStatus,
            FORM_OF_EMPLOYMENT to formOfEmployment,
            INDUSTRY to industry,
            POLITICAL_SIDE to politicalSide
        )
        return objectMapper.writeValueAsString(updates)
    }

    companion object {
        const val ID = "_id"
        const val DATE_OF_BIRTH = "dateOfBirth"
        const val CIVIL_STATUS = "civilStatus"
        const val COUNTRY_OF_BIRTH = "countryOfBirth"
        const val NATIONALITY = "nationality"
        const val CURRENT_COUNTRY = "currentCountry"
        const val FIRST_LANGUAGE = "firstLanguage"
        const val HIGHEST_EDUCATION_LEVEL_ACHIEVED = "highestEducationLevelAchieved"
        const val IS_STUDENT = "isStudent"
        const val MONTHLY_INCOME = "monthlyIncome"
        const val EMPLOYMENT_STATUS = "employmentStatus"
        const val FORM_OF_EMPLOYMENT = "formOfEmployment"
        const val INDUSTRY = "industry"
        const val POLITICAL_SIDE = "politicalSide"
    }
}