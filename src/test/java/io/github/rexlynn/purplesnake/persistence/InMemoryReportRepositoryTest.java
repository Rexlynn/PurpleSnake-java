package io.github.rexlynn.purplesnake.persistence;

import io.github.rexlynn.purplesnake.exception.ReportStorageException;
import io.github.rexlynn.purplesnake.model.PortState;
import io.github.rexlynn.purplesnake.model.ScanReport;
import io.github.rexlynn.purplesnake.model.ScanResult;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests the in-memory implementation AND the default method of the interface. */
class InMemoryReportRepositoryTest {

    private ScanReport report(String target, int day) {
        return new ScanReport(target, LocalDateTime.of(2026, 11, day, 9, 0),
                List.of(new ScanResult(80, PortState.OPEN)));
    }

    @Test
    void savedReportsAreFound() {
        InMemoryReportRepository repository = new InMemoryReportRepository();
        repository.save(report("a", 1));
        repository.save(report("b", 2));
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void changingTheReturnedListDoesNotChangeTheRepository() {
        InMemoryReportRepository repository = new InMemoryReportRepository();
        repository.save(report("a", 1));
        repository.findAll().clear();
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void defaultMethodFindsTheLatestReport() throws ReportStorageException {
        // The variable has the INTERFACE type: the default method is inherited
        ReportRepository repository = new InMemoryReportRepository();
        repository.save(report("a", 1));
        repository.save(report("a", 5));
        repository.save(report("a", 3));
        assertEquals(5, repository.findLatestFor("a").get().getScannedAt().getDayOfMonth());
        assertTrue(repository.findLatestFor("zzz").isEmpty());
    }
}
