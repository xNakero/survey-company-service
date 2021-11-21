package pl.wat.surveycompanyservice.infrastructure.repository

import com.fasterxml.jackson.databind.ObjectMapper
import org.elasticsearch.action.DocWriteResponse.Result
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.client.RequestOptions.DEFAULT
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType.JSON
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.query.Criteria
import org.springframework.data.elasticsearch.core.query.CriteriaQuery
import org.springframework.stereotype.Component
import pl.wat.surveycompanyservice.domain.profile.ElasticPersonalProfile
import pl.wat.surveycompanyservice.domain.profile.PersonalProfile
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileQueryParams
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileRepository
import pl.wat.surveycompanyservice.shared.ParticipantId
import java.time.Clock
import java.time.LocalDate

@Component
class ElasticPersonalProfileRepository(
    private val elasticsearchRestTemplate: ElasticsearchRestTemplate,
    private val client: RestHighLevelClient,
    private val objectMapper: ObjectMapper,
    private val clock: Clock
) : PersonalProfileRepository {

    override fun save(personalProfile: PersonalProfile) {
        elasticsearchRestTemplate.save(personalProfile.toElasticPersonalProfile())
    }

    override fun updateProfile(personalProfile: PersonalProfile): Result {
        val updateJson = personalProfile.mapToUpdates()
        val updateRequest = UpdateRequest(INDEX, personalProfile.participantId.raw)
            .doc(updateJson, JSON)
        val updateResponse = client.update(updateRequest, DEFAULT)
        return updateResponse.result
    }

    override fun findProfile(participantId: ParticipantId): PersonalProfile {
        val query = CriteriaQuery(Criteria(ID).`is`(participantId.raw))
        val hits = elasticsearchRestTemplate.search(query, ElasticPersonalProfile::class.java, IndexCoordinates.of(INDEX))
        return hits.getSearchHit(0).content.toPersonalProfile()
    }

    override fun findEligibleParticipantIds(queryParams: PersonalProfileQueryParams): List<String> {
        val searchRequest = SearchRequest(INDEX)
        val searchSourceBuilder = SearchSourceBuilder()
        val boolQuery = QueryBuilders.boolQuery()

        if (queryParams.olderOrEqualThan != null && queryParams.youngerOrEqualThan != null) {
            boolQuery.must(QueryBuilders.rangeQuery(DATE_OF_BIRTH)
                .lte(LocalDate.now(clock).minusYears(queryParams.olderOrEqualThan.toLong() - 1).minusDays(1))
                .gte(LocalDate.now(clock).minusYears(queryParams.youngerOrEqualThan.toLong())))
        } else {
            queryParams.olderOrEqualThan
                ?.let {
                    boolQuery.must(QueryBuilders.rangeQuery(DATE_OF_BIRTH)
                            .lte(LocalDate.now(clock).minusYears(it.toLong() - 1).minusDays(1)))
                }
            queryParams.youngerOrEqualThan
                ?.let {
                    boolQuery.must(QueryBuilders.rangeQuery(DATE_OF_BIRTH)
                            .gte(LocalDate.now(clock).minusYears(it.toLong())))
                }
        }
        queryParams.civilStatus?.let { boolQuery.must(QueryBuilders.matchQuery(CIVIL_STATUS, it)) }
        queryParams.countryOfBirth?.let { boolQuery.must(QueryBuilders.matchQuery(COUNTRY_OF_BIRTH, it)) }
        queryParams.nationality?.let { boolQuery.must(QueryBuilders.matchQuery(NATIONALITY, it)) }
        queryParams.currentCountry?.let { boolQuery.must(QueryBuilders.matchQuery(CURRENT_COUNTRY, it)) }
        queryParams.firstLanguage?.let { boolQuery.must(QueryBuilders.matchQuery(FIRST_LANGUAGE, it)) }
        queryParams.highestEducationLevelAchieved
            ?.let { boolQuery.must(QueryBuilders.matchQuery(HIGHEST_EDUCATION_LEVEL_ACHIEVED, it)) }
        queryParams.isStudent?.let { boolQuery.must(QueryBuilders.matchQuery(IS_STUDENT, it)) }
        if (queryParams.monthlyIncomeHigherOrEqualThan != null && queryParams.monthlyIncomeLesserOrEqualThan != null) {
            boolQuery.must(QueryBuilders.rangeQuery(MONTHLY_INCOME)
                .lte(queryParams.monthlyIncomeLesserOrEqualThan)
                .gte(queryParams.monthlyIncomeHigherOrEqualThan))
        } else {
            queryParams.monthlyIncomeHigherOrEqualThan
                ?.let { boolQuery.must(QueryBuilders.rangeQuery(MONTHLY_INCOME).gte(it)) }
            queryParams.monthlyIncomeLesserOrEqualThan
                ?.let { boolQuery.must(QueryBuilders.rangeQuery(MONTHLY_INCOME).lte(it)) }
        }
        queryParams.employmentStatus?.let { boolQuery.must(QueryBuilders.matchQuery(EMPLOYMENT_STATUS, it)) }
        queryParams.formOfEmployment?.let { boolQuery.must(QueryBuilders.matchQuery(FORM_OF_EMPLOYMENT, it)) }
        queryParams.industry?.let { boolQuery.must(QueryBuilders.matchQuery(INDUSTRY, it)) }
        queryParams.politicalSide?.let { boolQuery.must(QueryBuilders.matchQuery(POLITICAL_SIDE, it)) }

        searchSourceBuilder.query(boolQuery)

        searchRequest.source(searchSourceBuilder)

        val result = client.search(searchRequest, DEFAULT).hits
        return result.map { it.toParticipantId() }
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

    private fun SearchHit.toParticipantId(): String =
        objectMapper.readValue(this.sourceAsString, ElasticPersonalProfile::class.java).participantId


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
        const val INDEX = "personal_profile"
    }
}