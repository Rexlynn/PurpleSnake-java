package io.github.rexlynn.purplesnake.command;

import io.github.rexlynn.purplesnake.model.PortState;
import io.github.rexlynn.purplesnake.model.ScanReport;
import io.github.rexlynn.purplesnake.model.ScanResult;
import io.github.rexlynn.purplesnake.persistence.InMemoryReportRepository;
import io.github.rexlynn.purplesnake.ui.ConsoleIO;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HistoryCommandTest {

    private final ByteArrayOutputStream captured = new ByteArrayOutputStream();
    private final InMemoryReportRepository repository = new InMemoryReportRepository();

    private HistoryCommand commandWithInput(String typedText) {
        ConsoleIO io = new ConsoleIO(
                new ByteArrayInputStream(typedText.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(captured));
        return new HistoryCommand(io, repository);
    }

    private void addReport(String target, int day, PortState stateOfPort80) {
        repository.save(new ScanReport(target, LocalDateTime.of(2026, 11, day, 8, 0),
                List.of(new ScanResult(22, PortState.CLOSED), new ScanResult(80, stateOfPort80))));
    }

    @Test
    void emptyHistoryShowsAHint() {
        commandWithInput("").execute();
        assertTrue(captured.toString().contains("No saved scans yet."));
    }

    @Test
    void newestReportIsListedFirst() {
        addReport("old-host", 1, PortState.OPEN);
        addReport("new-host", 9, PortState.CLOSED);
        commandWithInput("0\n").execute();

        String output = captured.toString();
        assertTrue(output.indexOf("new-host") < output.indexOf("old-host"));
        assertTrue(output.contains("1) 2026-11-09 08:00:00  new-host  open=0 closed=2 filtered=0"));
    }

    @Test
    void detailsShowTheOpenPorts() {
        addReport("host", 2, PortState.OPEN);
        commandWithInput("1\n").execute();
        assertTrue(captured.toString().contains("Port    80 OPEN (HTTP)"));
    }

    @Test
    void invalidReportNumberIsRejected() {
        addReport("host", 2, PortState.OPEN);
        commandWithInput("5\n").execute();
        assertTrue(captured.toString().contains("Invalid report number."));
    }

    @Test
    void reportWithoutOpenPortsSaysNone() {
        addReport("host", 2, PortState.CLOSED);
        commandWithInput("1\n").execute();
        assertEquals(1, captured.toString().split("\\(none\\)", -1).length - 1);
    }
}
