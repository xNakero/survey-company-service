package pl.wat.surveycompanyservice.domain.survey

import org.springframework.stereotype.Component

@Component
data class SurveyProperties(
    val codeLength: Int = 32
)
