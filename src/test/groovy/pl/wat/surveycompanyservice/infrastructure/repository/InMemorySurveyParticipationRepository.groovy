package pl.wat.surveycompanyservice.infrastructure.repository


import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipation
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipationRepository
import pl.wat.surveycompanyservice.domain.surveyparticipation.ParticipationStatus
import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.SurveyId
import pl.wat.surveycompanyservice.shared.SurveyParticipationId

import java.time.Clock
import java.time.Instant
import java.util.concurrent.CopyOnWriteArraySet

import static pl.wat.surveycompanyservice.domain.surveyparticipation.ParticipationStatus.IN_PROGRESS
import static pl.wat.surveycompanyservice.domain.surveyparticipation.ParticipationStatus.IN_PROGRESS
import static pl.wat.surveycompanyservice.domain.surveyparticipation.ParticipationStatus.TIMEOUT

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
    void update(SurveyParticipationId surveyParticipationId, ParticipationStatus surveyStatus, String completionCode, Instant timestamp) {
        SurveyParticipation participation = surveyParticipations.find { it.id.raw == surveyParticipationId.raw }
        SurveyParticipation updatedParticipation = new SurveyParticipation(
                participation.id,
                participation.participantId,
                participation.surveyId,
                surveyStatus,
                participation.startedAt,
                participation.hasToFinishUntil,
                completionCode,
                timestamp
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
                    participation.completionCode,
                    participation.finishedAt
            )
            insert(newParticipation)
        }
    }

    @Override
    List<SurveyParticipation> findInProgressBySurveyIds(@NotNull List<SurveyId> surveyIds) {
        return surveyParticipations.findAll {it.id.raw in surveyIds.raw && it.status == IN_PROGRESS}
    }

    @Override
    List<SurveyParticipation> findBySurveyIds(@NotNull List<SurveyId> surveyIds) {
        return surveyParticipations.findAll {it.id.raw in surveyIds.raw }
    }

    @Override
    void removeBySurveyIds(@NotNull List<SurveyId> surveyIds) {
        for (surveyId in surveyIds) {
            surveyParticipations.remove(
                    surveyParticipations.find {
                        it.surveyId.raw == surveyId.raw
                    }
            )
        }
    }

    void clear() {
        surveyParticipations.clear()
    }

    Set findAll() {
        return surveyParticipations
    }
}
