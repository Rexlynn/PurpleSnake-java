package io.github.rexlynn.purplesnake.persistence;

import io.github.rexlynn.purplesnake.exception.ReportStorageException;
import io.github.rexlynn.purplesnake.model.PortState;
import io.github.rexlynn.purplesnake.model.ScanReport;
import io.github.rexlynn.purplesnake.model.ScanResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileReportRepositoryTest {

    @TempDir
    Path tempDir;

    private FileReportRepository repository;

    @BeforeEach
    void createRepository() {
        // A folder that does not exist yet: the repository must create it itself
        repository = new FileReportRepository(tempDir.resolve("reports"));
    }

    private ScanReport report(String target, int day) {
        return new ScanReport(target, LocalDateTime.of(2026, 11, day, 10, 30, 15, 123_000_000),
                List.of(new ScanResult(22, PortState.OPEN),
                        new ScanResult(23, PortState.CLOSED),
                        new ScanResult(80, PortState.FILTERED)));
    }

    @Test
    void emptyRepositoryReturnsEmptyList() throws ReportStorageException {
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void savedReportCanBeLoadedAgainUnchanged() throws ReportStorageException {
        ScanReport original = report("192.168.1.10", 5);
        repository.save(original);

        List<ScanReport> loaded = repository.findAll();
        assertEquals(1, loaded.size());
        ScanReport copy = loaded.get(0);
        assertEquals("192.168.1.10", copy.getTarget());
        assertEquals(original.getScannedAt(), copy.getScannedAt());
        assertEquals(3, copy.getResults().size());
        assertEquals(22, copy.getResults().get(0).getPort());
        assertEquals(PortState.OPEN, copy.getResults().get(0).getState());
        assertEquals(PortState.FILTERED, copy.getResults().get(2).getState());
    }

    @Test
    void reportsAreReturnedOldestFirst() throws ReportStorageException {
        repository.save(report("a", 9));
        repository.save(report("a", 3));
        repository.save(report("a", 6));

        List<ScanReport> loaded = repository.findAll();
        assertEquals(3, loaded.get(0).getScannedAt().getDayOfMonth());
        assertEquals(6, loaded.get(1).getScannedAt().getDayOfMonth());
        assertEquals(9, loaded.get(2).getScannedAt().getDayOfMonth());
    }

    @Test
    void findLatestForReturnsTheNewestReportOfThatTarget() throws ReportStorageException {
        repository.save(report("lab-pc", 3));
        repository.save(report("lab-pc", 8));
        repository.save(report("other-pc", 20));

        Optional<ScanReport> latest = repository.findLatestFor("LAB-PC"); // case does not matter
        assertTrue(latest.isPresent());
        assertEquals(8, latest.get().getScannedAt().getDayOfMonth());
    }

    @Test
    void findLatestForUnknownTargetIsEmpty() throws ReportStorageException {
        repository.save(report("lab-pc", 3));
        assertFalse(repository.findLatestFor("nobody").isPresent());
    }

    @Test
    void dangerousTargetNamesCannotEscapeTheFolder() throws ReportStorageException, IOException {
        repository.save(report("../../evil target", 4));

        // The file must be inside the reports folder, with a harmless name
        try (Stream<Path> files = Files.list(tempDir.resolve("reports"))) {
            List<Path> list = files.toList();
            assertEquals(1, list.size());
            assertFalse(list.get(0).getFileName().toString().contains("/"));
        }
        assertFalse(Files.exists(tempDir.resolve("evil target")));
        // The original target text is still stored inside the file
        assertEquals("../../evil target", repository.findAll().get(0).getTarget());
    }

    @Test
    void corruptFileIsReportedWithItsName() throws IOException {
        Path folder = tempDir.resolve("reports");
        Files.createDirectories(folder);
        Files.writeString(folder.resolve("broken.report"), "this is not a report");

        ReportStorageException error = assertThrows(ReportStorageException.class, () -> repository.findAll());
        assertTrue(error.getMessage().contains("broken.report"));
    }

    @Test
    void badPortLineIsReportedAsCorrupt() throws IOException {
        Path folder = tempDir.resolve("reports");
        Files.createDirectories(folder);
        Files.writeString(folder.resolve("bad.report"),
                "PURPLESNAKE-REPORT 1\ntarget=x\nscannedAt=2026-11-05T10:00:00\nabc,OPEN\n");

        assertThrows(ReportStorageException.class, () -> repository.findAll());
    }

    @Test
    void otherFilesInTheFolderAreIgnored() throws IOException, ReportStorageException {
        Path folder = tempDir.resolve("reports");
        Files.createDirectories(folder);
        Files.writeString(folder.resolve("notes.txt"), "not a report");
        assertTrue(repository.findAll().isEmpty());
    }
}
