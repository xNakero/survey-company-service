package pl.wat.surveycompanyservice.scheduler

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipationRepository
import java.time.Clock

@Component
class SurveyParticipationScheduler(
    private val repository: SurveyParticipationRepository,
    private val clock: Clock
) {

    @Scheduled(fixedDelay = 10000, initialDelay = 0)
    fun finishUnfinishedSurveyParticipations() {
        val now = clock.instant()
        repository.finishAllUnfinishedInTime(now)
    }
}