package pl.wat.surveycompanyservice.infrastructure.repository

import pl.wat.surveycompanyservice.BaseIntegrationTest
import pl.wat.surveycompanyservice.domain.surveyhistory.HistoryEntry
import pl.wat.surveycompanyservice.domain.surveyhistory.HistoryParticipation
import pl.wat.surveycompanyservice.domain.surveyhistory.MongoHistoryEntry
import pl.wat.surveycompanyservice.shared.ParticipantId
import pl.wat.surveycompanyservice.shared.ResearcherId

import static pl.wat.surveycompanyservice.IntegrationTestBuilders.HISTORY_ENTRY_ID
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.PARTICIPANT_ID
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.RESEARCHER_ID
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.historyEntry
import static pl.wat.surveycompanyservice.IntegrationTestBuilders.historyParticipation

class MongoHistoryEntryRepositoryIntTest extends BaseIntegrationTest{

    def 'should save all historyEntries'() {
        given:
            HistoryEntry historyEntry1 = historyEntry()
            HistoryEntry historyEntry2 = historyEntry([historyEntryId: '123'])
        when:
            historyEntryRepository.saveAll([historyEntry1, historyEntry2])
        then:
            mongoOperations.findAll(MongoHistoryEntry.class).size() == 2
    }

    def 'should find historyEntries by participantId'() {
        given:
            HistoryParticipation historyParticipation1 = historyParticipation()
            HistoryEntry historyEntry1 = historyEntry()
            historyEntry1.historyParticipations.add(historyParticipation1)
            mongoOperations.save(historyEntry1.toMongoHistoryEntry())
        and:
            HistoryParticipation historyParticipation2 = historyParticipation([participantId: '123'])
            HistoryEntry historyEntry2 = historyEntry([historyEntryId: '123'])
            historyEntry2.historyParticipations.add(historyParticipation2)
            mongoOperations.save(historyEntry2.toMongoHistoryEntry())
        and:
            HistoryEntry historyEntry3 = historyEntry([historyEntryId: '456'])
            mongoOperations.save(historyEntry3.toMongoHistoryEntry())
        when:
            List result = historyEntryRepository.findByParticipantId(new ParticipantId(PARTICIPANT_ID))
        then:
            result.size() == 1
            result.first().id.raw == HISTORY_ENTRY_ID
    }

    def 'should find historyEntries by researcherId'() {
        given:
            HistoryEntry historyEntry1 = historyEntry()
            HistoryEntry historyEntry2 = historyEntry([
                    historyEntryId: '123',
                    researcherId: '123'
            ])
        and:
            mongoOperations.save(historyEntry1.toMongoHistoryEntry())
            mongoOperations.save(historyEntry2.toMongoHistoryEntry())
        when:
            List result = historyEntryRepository.findByResearcherId(new ResearcherId(RESEARCHER_ID))
        then:
            result.size() == 1
            result.first().id.raw == HISTORY_ENTRY_ID
    }
}
