package io.github.rexlynn.purplesnake.command;

import io.github.rexlynn.purplesnake.exception.ReportStorageException;
import io.github.rexlynn.purplesnake.model.PortState;
import io.github.rexlynn.purplesnake.persistence.InMemoryReportRepository;
import io.github.rexlynn.purplesnake.service.PortScanner;
import io.github.rexlynn.purplesnake.ui.ConsoleIO;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** End-to-end tests: scripted keyboard input -> real scan of this computer -> printed output. */
class PortScanCommandTest {

    private final ByteArrayOutputStream captured = new ByteArrayOutputStream();
    private final InMemoryReportRepository repository = new InMemoryReportRepository();

    private PortScanCommand commandWithInput(String typedText) {
        ConsoleIO io = new ConsoleIO(
                new ByteArrayInputStream(typedText.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(captured));
        return new PortScanCommand(io, new PortScanner(500), repository);
    }

    @Test
    void scanFindsOpenPortAndSavesReport() throws IOException, ReportStorageException {
        try (ServerSocket server = new ServerSocket(0)) {
            int port = server.getLocalPort();
            commandWithInput("127.0.0.1\n" + port + "\n" + port + "\n").execute();

            String output = captured.toString();
            assertTrue(output.contains("Port " + String.format("%5d", port) + " OPEN"));
            assertTrue(output.contains("Open: 1"));
            assertTrue(output.contains("Report saved."));
            assertEquals(1, repository.findAll().size());
            assertEquals(PortState.OPEN, repository.findAll().get(0).getResults().get(0).getState());
        }
    }

    @Test
    void secondScanShowsChangesSinceTheFirst() throws IOException {
        int port;
        try (ServerSocket server = new ServerSocket(0)) {
            port = server.getLocalPort();
            commandWithInput("127.0.0.1\n" + port + "\n" + port + "\n").execute(); // open
        }
        captured.reset();
        // The server is closed now, so the same scan finds the port closed
        commandWithInput("127.0.0.1\n" + port + "\n" + port + "\n").execute();

        String output = captured.toString();
        assertTrue(output.contains("Changes since the scan of"));
        assertTrue(output.contains("NO LONGER OPEN: port " + port));
    }

    @Test
    void invalidRangeIsReportedAndNothingIsSaved() throws ReportStorageException {
        commandWithInput("127.0.0.1\n100\n50\n").execute();
        assertTrue(captured.toString().contains("Start port must not be greater than end port."));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void unknownHostIsReportedAndNothingIsSaved() throws ReportStorageException {
        commandWithInput("nohost.invalid\n80\n80\n").execute();
        assertTrue(captured.toString().contains("Unknown host: nohost.invalid"));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void emptyTargetIsRejected() throws ReportStorageException {
        commandWithInput("\n").execute();
        assertTrue(captured.toString().contains("Target cannot be empty."));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void largeRangeCanBeCancelled() throws ReportStorageException {
        // 1-1000 is a large range; answering "n" must cancel before any scanning
        commandWithInput("127.0.0.1\n1\n1000\nn\n").execute();
        String output = captured.toString();
        assertTrue(output.contains("may take up to"));
        assertTrue(output.contains("Scan cancelled."));
        assertTrue(repository.findAll().isEmpty());
    }
}
