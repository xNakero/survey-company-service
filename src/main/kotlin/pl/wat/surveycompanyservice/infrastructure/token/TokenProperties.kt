package pl.wat.surveycompanyservice.infrastructure.token

import org.springframework.stereotype.Component

@Component
class TokenProperties(
    val tokenDurationInMillis: Long = 86_400_000,
    val refreshTokenDurationInMillis: Long = 259_200_000,
    val secret: String = "secret",
    val issuer: String = "survey-app"
)