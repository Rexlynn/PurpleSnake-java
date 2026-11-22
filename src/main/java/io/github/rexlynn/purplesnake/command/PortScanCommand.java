package io.github.rexlynn.purplesnake.command;

import io.github.rexlynn.purplesnake.exception.InvalidPortRangeException;
import io.github.rexlynn.purplesnake.exception.ReportStorageException;
import io.github.rexlynn.purplesnake.model.PortRange;
import io.github.rexlynn.purplesnake.model.PortState;
import io.github.rexlynn.purplesnake.model.ScanComparison;
import io.github.rexlynn.purplesnake.model.ScanReport;
import io.github.rexlynn.purplesnake.model.ScanResult;
import io.github.rexlynn.purplesnake.persistence.ReportRepository;
import io.github.rexlynn.purplesnake.service.PortScanner;
import io.github.rexlynn.purplesnake.service.ReportComparator;
import io.github.rexlynn.purplesnake.ui.ConsoleIO;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;

/** Scans a host, saves the report and shows what changed since the previous scan. */
public class PortScanCommand extends BaseCommand {

    private static final int LARGE_RANGE = 100;

    private final PortScanner scanner;
    private final ReportRepository repository;
    private final ReportComparator comparator = new ReportComparator();

    public PortScanCommand(ConsoleIO io, PortScanner scanner, ReportRepository repository) {
        super("Port Scanner (saves a report and shows changes)", io);
        this.scanner = scanner;
        this.repository = repository;
    }

    @Override
    public void execute() {
        io.println("\n=== Port Scanner ===");
        io.println("Only scan hosts you own or have permission to scan.");

        String target = io.readLine("Target IP or hostname: ");
        if (target.isEmpty()) {
            io.println("Target cannot be empty.");
            return;
        }

        int start = io.readInt("Start port: ");
        int end = io.readInt("End port: ");

        PortRange range;
        try {
            range = new PortRange(start, end);
        } catch (InvalidPortRangeException e) {
            io.println(e.getMessage());
            return;
        }

        if (range.size() > LARGE_RANGE) {
            // Serial scan: every unanswered port can cost up to one timeout
            long seconds = (long) range.size() * scanner.getTimeoutMillis() / 1000;
            io.println("This scan covers " + range.size() + " ports and may take up to "
                    + seconds + " seconds if the host does not respond.");
            if (!io.confirm("Continue?")) {
                io.println("Scan cancelled.");
                return;
            }
        }

        // Load the previous report BEFORE saving the new one
        Optional<ScanReport> previous = loadPrevious(target);

        io.println("\nScanning " + target + ", ports " + range + " ...\n");
        ScanReport report;
        try {
            report = scanner.scan(target, range);
        } catch (UnknownHostException e) {
            io.println("Unknown host: " + target);
            return;
        }

        printReport(report);
        save(report);
        previous.ifPresent(old -> printComparison(old, report));
    }

    private Optional<ScanReport> loadPrevious(String target) {
        try {
            return repository.findLatestFor(target);
        } catch (ReportStorageException e) {
            io.println("Warning: could not read previous scans (" + e.getMessage() + ")");
            return Optional.empty();
        }
    }

    private void printReport(ScanReport report) {
        List<ScanResult> open = report.getResultsByState(PortState.OPEN);
        for (ScanResult result : open) {
            io.println(result.toString());
        }
        io.println("\nScan complete. Open: " + open.size()
                + ", Closed: " + report.count(PortState.CLOSED)
                + ", Filtered: " + report.count(PortState.FILTERED));
    }

    private void save(ScanReport report) {
        try {
            repository.save(report);
            io.println("Report saved.");
        } catch (ReportStorageException e) {
            io.println("Warning: " + e.getMessage());
        }
    }

    private void printComparison(ScanReport previous, ScanReport current) {
        ScanComparison changes = comparator.compare(previous, current);
        io.println("\nChanges since the scan of " + previous.getScannedAt().format(DATE_FORMAT) + ":");
        if (!changes.hasChanges()) {
            io.println("  No changes.");
            return;
        }
        for (int port : changes.getNewlyOpened()) {
            io.println("  NEW OPEN:   port " + port);
        }
        for (int port : changes.getNoLongerOpen()) {
            io.println("  NO LONGER OPEN: port " + port);
        }
    }
}
