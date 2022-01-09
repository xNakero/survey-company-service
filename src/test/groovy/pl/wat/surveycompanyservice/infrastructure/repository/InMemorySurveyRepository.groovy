package pl.wat.surveycompanyservice.infrastructure.repository

import org.jetbrains.annotations.NotNull
import pl.wat.surveycompanyservice.domain.survey.Survey
import pl.wat.surveycompanyservice.domain.survey.SurveyRepository
import pl.wat.surveycompanyservice.domain.survey.SurveyStatus
import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.ResearcherId
import pl.wat.surveycompanyservice.shared.SurveyId
import pl.wat.surveycompanyservice.shared.SurveyParticipationId

import java.util.concurrent.CopyOnWriteArraySet

import static pl.wat.surveycompanyservice.domain.survey.SurveyStatus.SCHEDULED_TO_FINISH

class InMemorySurveyRepository implements SurveyRepository {

    Set<Survey> surveys = new CopyOnWriteArraySet<>()

    @Override
    void saveSurvey(Survey survey) {
        surveys.remove(
                surveys.find {
                    it.id.raw == survey.id.raw
                }
        )
        surveys.add(survey)
    }

    @Override
    Survey find(SurveyId surveyId) {
        return surveys.find { surveyId.raw == it.id.raw }
    }

    @Override
    void saveParticipationToSurvey(
            SurveyId surveyId,
            int spotsToUpdate,
            SurveyParticipationId surveyParticipationId
    ) {
        Survey survey = surveys.find { it.id.raw == surveyId.raw }
        List participationIds = survey.participationIds
        participationIds.add(surveyParticipationId.raw)
        Survey updatedSurvey = new Survey(
                survey.id,
                survey.researcherId,
                survey.participationIds,
                participationIds,
                survey.title,
                survey.url,
                survey.timeToCompleteInSeconds,
                survey.description,
                survey.spotsTotal,
                survey.spotsTaken + 1,
                survey.completionCode,
                survey.status,
                survey.startedAt
        )
        saveSurvey(updatedSurvey)
    }

    @Override
    List<Survey> findSurveysEligibleToFinish() {
        return null
    }

    @Override
    List<Survey> findBySurveyIds(@NotNull List<SurveyId> surveyIds) {
        return surveys.findAll { surveyIds.raw.contains(it.id.raw) }.toList()
    }

    @Override
    void removeByIds(@NotNull List<SurveyId> surveyIds) {
        for (surveyId in surveyIds) {
            surveys.remove(
                    surveys.find {
                        it.id.raw == surveyId.raw
                    }
            )
        }
    }

    @Override
    List<Survey> findAllActiveByResearcherId(@NotNull ResearcherId researcherId) {
        return surveys.findAll { it.researcherId.raw == researcherId.raw }.toList()
    }

    @Override
    List<Survey> findEligibleToParticipate(@NotNull ParticipantId participantId) {
        return surveys.findAll { it.eligibleParticipantsIds.contains(participantId.raw) }.toList()
    }

    @Override
    void scheduleToFinish(@NotNull SurveyId surveyId, @NotNull ResearcherId researcherId) {
        Survey survey = surveys.find { it.id.raw == surveyId.raw }
        Survey updatedSurvey = new Survey(
                survey.id,
                survey.researcherId,
                survey.participationIds,
                survey.participationIds,
                survey.title,
                survey.url,
                survey.timeToCompleteInSeconds,
                survey.description,
                survey.spotsTotal,
                survey.spotsTaken,
                survey.completionCode,
                SCHEDULED_TO_FINISH,
                survey.startedAt
        )
        saveSurvey(updatedSurvey)
    }

    void clear() {
        surveys.clear()
    }

    Set findAll() {
        return surveys
    }
}
