package io.github.rexlynn.purplesnake.persistence;

import io.github.rexlynn.purplesnake.model.ScanReport;

import java.util.ArrayList;
import java.util.List;

/** Keeps reports in memory only. Used in tests (fast, no files needed). */
public class InMemoryReportRepository implements ReportRepository {

    private final List<ScanReport> reports = new ArrayList<>();

    @Override
    public void save(ScanReport report) {
        reports.add(report);
    }

    @Override
    public List<ScanReport> findAll() {
        return new ArrayList<>(reports);
    }
}
