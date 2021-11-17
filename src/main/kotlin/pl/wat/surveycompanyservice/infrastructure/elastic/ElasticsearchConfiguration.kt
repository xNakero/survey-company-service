package pl.wat.surveycompanyservice.infrastructure.elastic

import org.elasticsearch.client.RestHighLevelClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.RestClients
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories


@Configuration
@EnableElasticsearchRepositories(basePackages = ["pl.wat.surveycompanyservice"])
class ElasticsearchConfiguration(
    private val elasticsearchProperties: ElasticsearchProperties
) : AbstractElasticsearchConfiguration() {

    @Bean
    override fun elasticsearchClient(): RestHighLevelClient {
        val clientConfiguration = ClientConfiguration.builder()
            .connectedTo(elasticsearchProperties.hostAndPort)
            .withConnectTimeout(elasticsearchProperties.connectTimeout)
            .withSocketTimeout(elasticsearchProperties.socketTimeout)
            .build()
        return RestClients.create(clientConfiguration).rest()
    }

    @Bean
    fun elasticSearchRestTemplate(): ElasticsearchRestTemplate =
        ElasticsearchRestTemplate(elasticsearchClient())
}