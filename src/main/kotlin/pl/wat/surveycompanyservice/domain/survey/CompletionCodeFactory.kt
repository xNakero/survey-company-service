package pl.wat.surveycompanyservice.domain.survey

import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class CompletionCodeFactory {

    fun generateCode(length: Int): String {
        if (length <= 0) throw IllegalCodeLengthException("Code cannot be negative")
        val chars = ('A'..'Z') + ('0'..'9')
        return (0..length)
            .map { Random.nextInt(0, chars.size) }
            .map(chars::get)
            .joinToString("")
    }

}

class IllegalCodeLengthException(message: String?) : RuntimeException(message)