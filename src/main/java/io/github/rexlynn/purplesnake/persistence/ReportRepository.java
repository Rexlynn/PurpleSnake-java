package io.github.rexlynn.purplesnake.persistence;

import io.github.rexlynn.purplesnake.exception.ReportStorageException;
import io.github.rexlynn.purplesnake.model.ScanReport;

import java.util.List;
import java.util.Optional;

/**
 * ABSTRACTION: a stable contract for "where scan reports are stored".
 * The rest of the program only knows this interface, so the storage can change
 * (files today, a database tomorrow, memory in tests) without touching the callers.
 */
public interface ReportRepository {

    void save(ScanReport report) throws ReportStorageException;

    /** Returns a NEW list with all saved reports, oldest first. */
    List<ScanReport> findAll() throws ReportStorageException;

    /**
     * Default method: shared behaviour that every implementation gets for free.
     * Finds the most recent report of the given target (ignoring upper/lower case).
     */
    default Optional<ScanReport> findLatestFor(String target) throws ReportStorageException {
        ScanReport latest = null;
        for (ScanReport report : findAll()) {
            if (report.getTarget().equalsIgnoreCase(target)) {
                if (latest == null || report.getScannedAt().isAfter(latest.getScannedAt())) {
                    latest = report;
                }
            }
        }
        return Optional.ofNullable(latest);
    }
}
