package io.github.rexlynn.purplesnake.command;

import io.github.rexlynn.purplesnake.service.NetworkInfoProvider;
import io.github.rexlynn.purplesnake.ui.ConsoleIO;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NetworkInfoCommandTest {

    private final ByteArrayOutputStream captured = new ByteArrayOutputStream();

    /** A provider with fixed answers, so the test does not depend on the real computer. */
    private static class FakeProvider extends NetworkInfoProvider {
        @Override
        public List<String> describeInterfaces() throws SocketException {
            return List.of("Interface: fake0 [UP]", "  IP: 10.0.0.5");
        }

        @Override
        public List<String> listConnections() throws IOException {
            throw new IOException("tool missing");
        }
    }

    private NetworkInfoCommand commandWithInput(String typedText) {
        ConsoleIO io = new ConsoleIO(
                new ByteArrayInputStream(typedText.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(captured));
        return new NetworkInfoCommand(io, new FakeProvider());
    }

    @Test
    void interfacesAreShown() {
        commandWithInput("1\n0\n").execute();
        assertTrue(captured.toString().contains("IP: 10.0.0.5"));
    }

    @Test
    void failureOfTheSystemToolIsReportedNotThrown() {
        commandWithInput("2\n0\n").execute();
        String output = captured.toString();
        assertTrue(output.contains("REAL connections"));
        assertTrue(output.contains("Could not get network info: tool missing"));
    }

    @Test
    void invalidChoiceShowsMenuAgain() {
        commandWithInput("7\n0\n").execute();
        assertTrue(captured.toString().contains("Invalid choice, try again."));
    }
}
