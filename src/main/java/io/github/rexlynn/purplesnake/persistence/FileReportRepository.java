package io.github.rexlynn.purplesnake.persistence;

import io.github.rexlynn.purplesnake.exception.ReportStorageException;
import io.github.rexlynn.purplesnake.model.PortState;
import io.github.rexlynn.purplesnake.model.ScanReport;
import io.github.rexlynn.purplesnake.model.ScanResult;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores every report as one small text file in a directory.
 *
 * File format:
 *   PURPLESNAKE-REPORT 1
 *   target=127.0.0.1
 *   scannedAt=2026-11-22T16:20:00.123
 *   22,OPEN
 *   23,CLOSED
 */
public class FileReportRepository implements ReportRepository {

    private static final String HEADER = "PURPLESNAKE-REPORT 1";
    private static final String EXTENSION = ".report";
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmssSSS");

    private final Path directory;

    public FileReportRepository(Path directory) {
        if (directory == null) {
            throw new IllegalArgumentException("Directory must not be null.");
        }
        this.directory = directory;
    }

    @Override
    public void save(ScanReport report) throws ReportStorageException {
        try {
            Files.createDirectories(directory);
            Path file = directory.resolve(fileNameFor(report));

            // try-with-resources closes (and flushes) the writer even if an error happens.
            // CREATE_NEW: never silently overwrite an existing report.
            try (BufferedWriter writer = Files.newBufferedWriter(
                    file, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                writer.write(HEADER);
                writer.newLine();
                writer.write("target=" + report.getTarget());
                writer.newLine();
                writer.write("scannedAt=" + report.getScannedAt());
                writer.newLine();
                for (ScanResult result : report.getResults()) {
                    writer.write(result.getPort() + "," + result.getState());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new ReportStorageException("Could not save report: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ScanReport> findAll() throws ReportStorageException {
        List<ScanReport> reports = new ArrayList<>();
        if (!Files.isDirectory(directory)) {
            return reports; // nothing saved yet
        }
        try (DirectoryStream<Path> files = Files.newDirectoryStream(directory, "*" + EXTENSION)) {
            for (Path file : files) {
                reports.add(read(file));
            }
        } catch (IOException e) {
            throw new ReportStorageException("Could not read reports: " + e.getMessage(), e);
        }
        // Sort oldest first with a lambda Comparator
        reports.sort((a, b) -> a.getScannedAt().compareTo(b.getScannedAt()));
        return reports;
    }

    /**
     * Builds a safe file name. The target comes from the user, so every character that is
     * not a letter, digit, dot or dash is replaced. This blocks tricks like "../../x".
     */
    private String fileNameFor(ScanReport report) {
        String safeTarget = report.getTarget().replaceAll("[^A-Za-z0-9.-]", "_");
        if (safeTarget.length() > 60) {
            safeTarget = safeTarget.substring(0, 60);
        }
        return FILE_TIME.format(report.getScannedAt()) + "_" + safeTarget + EXTENSION;
    }

    private ScanReport read(Path file) throws ReportStorageException {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            if (!HEADER.equals(reader.readLine())) {
                throw corrupt(file, "missing or wrong header");
            }
            String target = readValue(reader, "target=", file);
            String time = readValue(reader, "scannedAt=", file);

            List<ScanResult> results = new ArrayList<>();
            int lineNumber = 3;
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length != 2) {
                    throw corrupt(file, "line " + lineNumber + ": expected 'port,STATE'");
                }
                results.add(new ScanResult(
                        Integer.parseInt(parts[0].trim()), PortState.valueOf(parts[1].trim())));
            }
            return new ScanReport(target, LocalDateTime.parse(time), results);

        } catch (IOException e) {
            throw new ReportStorageException("Could not read " + file.getFileName() + ": " + e.getMessage(), e);
        } catch (IllegalArgumentException | DateTimeParseException e) {
            // NumberFormatException (bad port), unknown state, invalid data or bad date
            throw corrupt(file, e.getMessage());
        }
    }

    private String readValue(BufferedReader reader, String prefix, Path file)
            throws IOException, ReportStorageException {
        String line = reader.readLine();
        if (line == null || !line.startsWith(prefix)) {
            throw corrupt(file, "expected a line starting with '" + prefix + "'");
        }
        return line.substring(prefix.length());
    }

    private ReportStorageException corrupt(Path file, String reason) {
        return new ReportStorageException("Corrupt report file " + file.getFileName() + ": " + reason);
    }
}
