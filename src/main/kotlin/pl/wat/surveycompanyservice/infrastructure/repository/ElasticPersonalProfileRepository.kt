package pl.wat.surveycompanyservice.infrastructure.repository

import com.fasterxml.jackson.databind.ObjectMapper
import org.elasticsearch.action.DocWriteResponse.Result.NOOP
import org.elasticsearch.action.DocWriteResponse.Result.UPDATED
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy.IMMEDIATE
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.client.RequestOptions.DEFAULT
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType.JSON
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.rest.RestStatus.CREATED
import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.stereotype.Component
import pl.wat.surveycompanyservice.domain.profile.CivilStatus
import pl.wat.surveycompanyservice.domain.profile.Country
import pl.wat.surveycompanyservice.domain.profile.EducationLevel
import pl.wat.surveycompanyservice.domain.profile.ElasticPersonalProfile
import pl.wat.surveycompanyservice.domain.profile.EmploymentStatus
import pl.wat.surveycompanyservice.domain.profile.FormOfEmployment
import pl.wat.surveycompanyservice.domain.profile.Industry
import pl.wat.surveycompanyservice.domain.profile.Language
import pl.wat.surveycompanyservice.domain.profile.PersonalProfile
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileQueryParams
import pl.wat.surveycompanyservice.domain.profile.PersonalProfileRepository
import pl.wat.surveycompanyservice.domain.profile.PoliticalSide
import pl.wat.surveycompanyservice.domain.profile.Sex
import pl.wat.surveycompanyservice.shared.ParticipantId
import java.time.Clock
import java.time.LocalDate

@Component
class ElasticPersonalProfileRepository(
    private val client: RestHighLevelClient,
    private val objectMapper: ObjectMapper,
    private val clock: Clock
) : PersonalProfileRepository {

    override fun save(personalProfile: PersonalProfile) {
        val profileAsMap = personalProfile.toMap()
        val indexRequest = IndexRequest(INDEX)
            .id(personalProfile.participantId.raw)
            .source(profileAsMap)
            .setRefreshPolicy(IMMEDIATE)

        val indexResponse = client.index(indexRequest, DEFAULT)
        if (indexResponse.status() != CREATED) {
            throw IndexingErrorException("Personal profile with id: ${personalProfile.participantId.raw} was not saved.")
        }
    }

    override fun updateProfile(personalProfile: PersonalProfile) {
        val updateJson = personalProfile.toJson()
        val updateRequest = UpdateRequest(INDEX, personalProfile.participantId.raw)
            .doc(updateJson, JSON)
            .setRefreshPolicy(IMMEDIATE)
        val updateResponse = client.update(updateRequest, DEFAULT)
        if (updateResponse.result !in setOf(UPDATED, NOOP)) {
            throw UpdatingErrorException("Personal profile with id: ${personalProfile.participantId.raw} was not updated.")
        }
    }

    override fun findProfile(participantId: ParticipantId): PersonalProfile {
        val searchRequest = SearchRequest(INDEX)
        val searchSourceBuilder = SearchSourceBuilder()
        val boolQuery = QueryBuilders.boolQuery()

        boolQuery.must(QueryBuilders.matchQuery(ID, participantId.raw))

        searchSourceBuilder.query(boolQuery)
        searchRequest.source(searchSourceBuilder)
        val result = client.search(searchRequest, DEFAULT).internalResponse.hits().hits
        return result.map { it.toElasticPersonalProfile() }.first().toPersonalProfile(participantId)
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
        queryParams.sex?.let { boolQuery.must(QueryBuilders.matchQuery(SEX, it)) }
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
        return result.map { it.id }
    }

    private fun PersonalProfile.toJson(): String {
        val updates: MutableMap<String, Any?> = this.toMap()
        return objectMapper.writeValueAsString(updates)
    }

    private fun PersonalProfile.toMap(): MutableMap<String, Any?> =
        mutableMapOf(
            DATE_OF_BIRTH to dateOfBirth,
            CIVIL_STATUS to civilStatus,
            SEX to sex,
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

    private fun SearchHit.toElasticPersonalProfile(): PersonalProfileSearchResult =
        objectMapper.readValue(this.sourceAsString, PersonalProfileSearchResult::class.java)

    companion object {
        const val ID = "_id"
        const val DATE_OF_BIRTH = "dateOfBirth"
        const val CIVIL_STATUS = "civilStatus"
        const val SEX = "sex"
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

data class PersonalProfileSearchResult(
    val dateOfBirth: LocalDate?,
    val civilStatus: String?,
    val sex: String?,
    val countryOfBirth: String?,
    val nationality: String?,
    val currentCountry: String?,
    val firstLanguage: String?,
    val highestEducationLevelAchieved: String?,
    val isStudent: Boolean?,
    val monthlyIncome: Int?,
    val employmentStatus: String?,
    val formOfEmployment: String?,
    val industry: String?,
    val politicalSide: String?
) {
    fun toPersonalProfile(participantId: ParticipantId): PersonalProfile = PersonalProfile(
        participantId = participantId,
        dateOfBirth = dateOfBirth,
        civilStatus = civilStatus?.let { CivilStatus.valueOf(it) },
        sex = sex?.let { Sex.valueOf(it) },
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

class IndexingErrorException(message: String?) : RuntimeException(message)
class UpdatingErrorException(message: String?) : RuntimeException(message)