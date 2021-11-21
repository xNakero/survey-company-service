package pl.wat.surveycompanyservice.shared

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class Beans {

    @Bean
    fun clock(): Clock = Clock.systemDefaultZone()
}
