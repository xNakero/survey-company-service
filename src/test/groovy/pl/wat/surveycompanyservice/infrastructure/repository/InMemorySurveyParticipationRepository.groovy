package pl.wat.surveycompanyservice.infrastructure.repository

import org.jetbrains.annotations.NotNull
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipation
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyParticipationRepository
import pl.wat.surveycompanyservice.domain.surveyparticipation.SurveyStatus
import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.SurveyParticipationId

import java.util.concurrent.CopyOnWriteArraySet

class InMemorySurveyParticipationRepository implements SurveyParticipationRepository {

    Set<SurveyParticipation> surveyParticipations = new CopyOnWriteArraySet<>()

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
        return surveyParticipations.find {it.id.raw == surveyParticipationId.raw}
    }

    @Override
    void update(SurveyParticipationId surveyParticipationId, SurveyStatus surveyStatus, String completionCode) {
        SurveyParticipation participation = surveyParticipations.find {it.id.raw == surveyParticipationId.raw}
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
        return surveyParticipations.findAll {it.participantId.raw == participantId.raw}.toList()
    }

    void clear() {
        surveyParticipations.clear()
    }

    Set findAll() {
        return surveyParticipations
    }
}
