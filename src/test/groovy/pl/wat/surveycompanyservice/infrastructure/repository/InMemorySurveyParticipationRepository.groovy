package pl.wat.surveycompanyservice.infrastructure.repository

import org.codehaus.groovy.runtime.InvokerHelper
import org.jetbrains.annotations.NotNull
import pl.wat.surveycompanyservice.domain.profile.PersonalProfile
import pl.wat.surveycompanyservice.domain.survey.Survey
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipation
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipationRepository
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyStatus
import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.SurveyParticipationId

import java.time.Clock
import java.time.Instant
import java.util.concurrent.CopyOnWriteArraySet

import static pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyStatus.IN_PROGRESS
import static pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyStatus.TIMEOUT

class InMemorySurveyParticipationRepository implements SurveyParticipationRepository {

    private Set<SurveyParticipation> surveyParticipations = new CopyOnWriteArraySet<>()
    private Clock clock

    InMemorySurveyParticipationRepository(Clock clock) {
        this.clock = clock
    }

    @Override
    void insert(SurveyParticipation surveyParticipation) {
        surveyParticipations.remove(
                surveyParticipations.find {
                    it.id.raw == surveyParticipation.id.raw
                }
        )
        surveyParticipations.add(surveyParticipation)
    }

    @Override
    SurveyParticipation find(SurveyParticipationId surveyParticipationId) {
        return surveyParticipations.find { it.id.raw == surveyParticipationId.raw }
    }

    @Override
    void update(SurveyParticipationId surveyParticipationId, SurveyStatus surveyStatus, String completionCode) {
        SurveyParticipation participation = surveyParticipations.find { it.id.raw == surveyParticipationId.raw }
        SurveyParticipation updatedParticipation = new SurveyParticipation(
                participation.id,
                participation.participantId,
                participation.surveyId,
                surveyStatus,
                participation.startedAt,
                participation.hasToFinishUntil,
                completionCode
        )
        insert(updatedParticipation)
    }


    @Override
    List<SurveyParticipation> findByParticipantId(@NotNull ParticipantId participantId) {
        return surveyParticipations.findAll { it.participantId.raw == participantId.raw }.toList()
    }

    @Override
    void finishAllUnfinishedInTime(@NotNull Instant timestamp) {
        List<SurveyParticipation> toUpdate = surveyParticipations.findAll { it.status == IN_PROGRESS && it.hasToFinishUntil <= clock.instant() }.collect()
        for (SurveyParticipation participation: toUpdate) {
            SurveyParticipation newParticipation = new SurveyParticipation(
                    participation.id,
                    participation.participantId,
                    participation.surveyId,
                    TIMEOUT,
                    participation.startedAt,
                    participation.hasToFinishUntil,
                    participation.completionCode
            )
            insert(newParticipation)
        }
    }

    void clear() {
        surveyParticipations.clear()
    }

    Set findAll() {
        return surveyParticipations
    }
}
