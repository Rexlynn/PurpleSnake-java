package io.github.rexlynn.purplesnake.command;

import io.github.rexlynn.purplesnake.exception.ReportStorageException;
import io.github.rexlynn.purplesnake.model.PortState;
import io.github.rexlynn.purplesnake.model.ScanReport;
import io.github.rexlynn.purplesnake.model.ScanResult;
import io.github.rexlynn.purplesnake.persistence.ReportRepository;
import io.github.rexlynn.purplesnake.ui.ConsoleIO;

import java.util.ArrayList;
import java.util.List;

/** Lists the saved scan reports (newest first) and shows the details of one. */
public class HistoryCommand extends BaseCommand {

    private final ReportRepository repository;

    public HistoryCommand(ConsoleIO io, ReportRepository repository) {
        super("Scan history", io);
        this.repository = repository;
    }

    @Override
    public void execute() {
        io.println("\n=== Scan History ===");

        List<ScanReport> reports;
        try {
            reports = new ArrayList<>(repository.findAll());
        } catch (ReportStorageException e) {
            io.println("Could not read the history: " + e.getMessage());
            return;
        }
        if (reports.isEmpty()) {
            io.println("No saved scans yet. Run a port scan first.");
            return;
        }

        // Lambda Comparator: newest report first
        reports.sort((a, b) -> b.getScannedAt().compareTo(a.getScannedAt()));

        for (int i = 0; i < reports.size(); i++) {
            ScanReport report = reports.get(i);
            io.println((i + 1) + ") " + report.getScannedAt().format(DATE_FORMAT)
                    + "  " + report.getTarget()
                    + "  open=" + report.count(PortState.OPEN)
                    + " closed=" + report.count(PortState.CLOSED)
                    + " filtered=" + report.count(PortState.FILTERED));
        }

        int choice = io.readInt("Report number for details (0 = back): ");
        if (choice == 0) {
            return;
        }
        if (choice < 1 || choice > reports.size()) {
            io.println("Invalid report number.");
            return;
        }
        showDetails(reports.get(choice - 1));
    }

    private void showDetails(ScanReport report) {
        io.println("\nOpen ports of " + report.getTarget() + " at "
                + report.getScannedAt().format(DATE_FORMAT) + ":");
        List<ScanResult> open = report.getResultsByState(PortState.OPEN);
        if (open.isEmpty()) {
            io.println("  (none)");
        }
        for (ScanResult result : open) {
            io.println("  " + result);
        }
    }
}
