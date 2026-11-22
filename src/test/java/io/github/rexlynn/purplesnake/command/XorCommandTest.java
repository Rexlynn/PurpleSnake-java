package io.github.rexlynn.purplesnake.command;

import io.github.rexlynn.purplesnake.ui.ConsoleIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XorCommandTest {

    @TempDir
    Path tempDir;

    private final ByteArrayOutputStream captured = new ByteArrayOutputStream();

    private XorCommand commandWithInput(String typedText) {
        ConsoleIO io = new ConsoleIO(
                new ByteArrayInputStream(typedText.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(captured));
        return new XorCommand(io);
    }

    @Test
    void encryptThenDecryptThroughTheMenuRestoresTheFile() throws IOException {
        Path original = tempDir.resolve("a.txt");
        Path encrypted = tempDir.resolve("b.bin");
        Path restored = tempDir.resolve("c.txt");
        Files.writeString(original, "secret message");

        commandWithInput(original + "\n" + encrypted + "\nkey\n").execute();
        commandWithInput(encrypted + "\n" + restored + "\nkey\n").execute();

        assertEquals("secret message", Files.readString(restored));
    }

    @Test
    void sameFileIsRejectedAndTheFileSurvives() throws IOException {
        Path file = tempDir.resolve("a.txt");
        Files.writeString(file, "keep me");
        // The output file exists already, so the command asks about overwriting: answer "y"
        commandWithInput(file + "\n" + file + "\nkey\ny\n").execute();

        assertTrue(captured.toString().contains("Input and output files must be different."));
        assertEquals("keep me", Files.readString(file));
    }

    @Test
    void existingOutputFileIsNotOverwrittenWithoutPermission() throws IOException {
        Path input = tempDir.resolve("in.txt");
        Path output = tempDir.resolve("out.txt");
        Files.writeString(input, "new");
        Files.writeString(output, "old");

        commandWithInput(input + "\n" + output + "\nkey\nn\n").execute();

        assertTrue(captured.toString().contains("Cancelled."));
        assertEquals("old", Files.readString(output));
    }

    @Test
    void missingInputFileGivesAClearMessage() {
        commandWithInput(tempDir.resolve("missing.txt") + "\n" + tempDir.resolve("o.txt") + "\nkey\n").execute();
        assertTrue(captured.toString().contains("File not found"));
    }

    @Test
    void emptyKeyIsRejected() {
        commandWithInput("a.txt\nb.txt\n\n").execute();
        assertTrue(captured.toString().contains("Key cannot be empty."));
    }

    @Test
    void emptyPathIsRejected() {
        commandWithInput("\nb.txt\nkey\n").execute();
        assertTrue(captured.toString().contains("File paths cannot be empty."));
    }
}
