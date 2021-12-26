package pl.wat.surveycompanyservice.infrastructure.repository

import pl.wat.surveycompanyservice.BaseIntegrationTest
import pl.wat.surveycompanyservice.IntegrationTestBuilders
import pl.wat.surveycompanyservice.domain.surveyhistory.HistoryEntry
import pl.wat.surveycompanyservice.domain.surveyhistory.MongoHistoryEntry

import static pl.wat.surveycompanyservice.IntegrationTestBuilders.historyEntry

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
}
