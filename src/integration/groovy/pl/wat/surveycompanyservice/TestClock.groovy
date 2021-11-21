package pl.wat.surveycompanyservice

import groovy.transform.CompileStatic

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@CompileStatic
class TestClock extends Clock {

    static final Instant DEFAULT = Instant.parse('2021-11-11T10:00:00.000Z')

    Instant now = DEFAULT

    @Override
    ZoneId getZone() {
        return ZoneId.systemDefault()
    }

    @Override
    Clock withZone(ZoneId zone) {
        return this
    }

    @Override
    Instant instant() {
        return now
    }

    void advanceTimeTo(Instant time) {
        now = time
    }

    void reset() {
        now = DEFAULT
    }
}