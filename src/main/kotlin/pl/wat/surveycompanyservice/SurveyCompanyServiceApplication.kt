package pl.wat.surveycompanyservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class SurveyCompanyServiceApplication

fun main(args: Array<String>) {
    runApplication<SurveyCompanyServiceApplication>(*args)
}

@RestController
class PingEndpoint() {

    @GetMapping("/ping")
    fun ping(): Map<String, String> = mapOf("value" to "OK")
}
