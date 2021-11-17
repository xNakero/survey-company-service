package pl.wat.surveycompanyservice.infrastructure.elastic

import org.springframework.stereotype.Component
import java.time.Duration

@Component
class ElasticsearchProperties(
    val hostAndPort: String = "localhost:9200",
    val connectTimeout: Duration = Duration.ofSeconds(1),
    val socketTimeout: Duration = Duration.ofMinutes(1)
)