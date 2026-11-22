package io.github.rexlynn.purplesnake.ui;

import io.github.rexlynn.purplesnake.exception.InputClosedException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleIOTest {

    private final ByteArrayOutputStream captured = new ByteArrayOutputStream();

    /** Creates a ConsoleIO that "types" the given text and records everything printed. */
    private ConsoleIO consoleWithInput(String typedText) {
        return new ConsoleIO(
                new ByteArrayInputStream(typedText.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(captured));
    }

    @Test
    void readLineTrimsSpaces() {
        assertEquals("hello", consoleWithInput("   hello  \n").readLine("> "));
    }

    @Test
    void readIntAcceptsANumberWithSpaces() {
        assertEquals(42, consoleWithInput(" 42 \n").readInt("> "));
    }

    @Test
    void readIntAsksAgainAfterInvalidInput() {
        ConsoleIO io = consoleWithInput("abc\n\n4x\n7\n");
        assertEquals(7, io.readInt("> "));
        // The warning was printed three times (for "abc", the empty line and "4x")
        String output = captured.toString();
        assertEquals(3, output.split("Please enter a valid number.", -1).length - 1);
    }

    @Test
    void closedInputThrowsInputClosedException() {
        ConsoleIO io = consoleWithInput(""); // nothing to read = like Ctrl+D
        assertThrows(InputClosedException.class, () -> io.readLine("> "));
    }

    @Test
    void readIntAlsoStopsWhenInputCloses() {
        ConsoleIO io = consoleWithInput("abc\n"); // invalid, then the input ends
        assertThrows(InputClosedException.class, () -> io.readInt("> "));
    }

    @Test
    void confirmAcceptsYesInAnyCaseAndNothingElse() {
        assertTrue(consoleWithInput("y\n").confirm("Sure?"));
        assertTrue(consoleWithInput("Y\n").confirm("Sure?"));
        assertFalse(consoleWithInput("n\n").confirm("Sure?"));
        assertFalse(consoleWithInput("yes please\n").confirm("Sure?"));
        assertFalse(consoleWithInput("\n").confirm("Sure?"));
    }

    @Test
    void promptIsPrintedBeforeReading() {
        consoleWithInput("x\n").readLine("Name: ");
        assertTrue(captured.toString().startsWith("Name: "));
    }
}
