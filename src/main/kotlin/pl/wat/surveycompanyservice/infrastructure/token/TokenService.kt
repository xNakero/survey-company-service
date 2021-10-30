package pl.wat.surveycompanyservice.infrastructure.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Service
import java.lang.System.currentTimeMillis
import java.util.*

@Service
class TokenService(
    private val tokenProperties: TokenProperties,
    private val objectMapper: ObjectMapper
) {

    fun getToken(authentication: Authentication): String =
        JWT.create()
            .withSubject(authentication.name)
            .withIssuer(tokenProperties.issuer)
            .withExpiresAt(Date(currentTimeMillis() + tokenProperties.tokenDurationInMillis))
            .withIssuedAt(Date(currentTimeMillis()))
            .withClaim("roles", authentication.authorities.joinToString(","))
            .sign(getSignAlgorithm())

    fun getRefreshToken(authentication: Authentication): String =
        JWT.create()
            .withSubject(authentication.name)
            .withIssuer(tokenProperties.issuer)
            .withExpiresAt(Date(currentTimeMillis() + tokenProperties.refreshTokenDurationInMillis))
            .withIssuedAt(Date(currentTimeMillis()))
            .withClaim("roles", authentication.authorities.joinToString(","))
            .sign(getSignAlgorithm())

    fun authenticate(token: String): UsernamePasswordAuthenticationToken {
        val verifier = JWT.require(getSignAlgorithm())
            .withIssuer(tokenProperties.issuer)
            .build()
        val jwt = verifier.verify(token.trim())
        val subject = jwt.subject
        val authorities = getAuthoritiesFromPayload(jwt.payload)
        return UsernamePasswordAuthenticationToken(subject, null, authorities)
    }

    private fun getSignAlgorithm(): Algorithm =
        Algorithm.HMAC512(tokenProperties.secret)

    private fun getAuthoritiesFromPayload(payload: String): Set<SimpleGrantedAuthority> {
        val decodedPayload = encodePayload(payload)
        val payloadMap = objectMapper.readValue(decodedPayload, HashMap::class.java)
        return payloadMap["roles"].toString().split(",").map { SimpleGrantedAuthority(it) }.toSet()
    }

    private fun encodePayload(payload: String): String {
        val decoder = Base64.getDecoder()
        return String(decoder.decode(payload))
    }
}