package io.github.rexlynn.purplesnake.ui;

import io.github.rexlynn.purplesnake.command.Command;
import io.github.rexlynn.purplesnake.exception.InputClosedException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuTest {

    /** A fake command that only counts how many times it was executed. */
    private static class CountingCommand implements Command {
        private final String label;
        int executions = 0;

        CountingCommand(String label) {
            this.label = label;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public void execute() {
            executions++;
        }
    }

    /** A fake command that always crashes. */
    private static class CrashingCommand implements Command {
        @Override
        public String getLabel() {
            return "Crash";
        }

        @Override
        public void execute() {
            throw new IllegalStateException("boom");
        }
    }

    private final ByteArrayOutputStream captured = new ByteArrayOutputStream();

    private ConsoleIO consoleWithInput(String typedText) {
        return new ConsoleIO(
                new ByteArrayInputStream(typedText.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(captured));
    }

    @Test
    void choosingANumberRunsTheMatchingCommand() {
        CountingCommand first = new CountingCommand("First");
        CountingCommand second = new CountingCommand("Second");
        Menu menu = new Menu(consoleWithInput("2\n2\n1\n0\n"));
        menu.register(first);
        menu.register(second);

        menu.run();

        assertEquals(1, first.executions);
        assertEquals(2, second.executions); // polymorphism: the menu ran both through "Command"
    }

    @Test
    void menuListsEveryCommandLabel() {
        Menu menu = new Menu(consoleWithInput("0\n"));
        menu.register(new CountingCommand("Alpha"));
        menu.register(new CountingCommand("Beta"));
        menu.run();

        String output = captured.toString();
        assertTrue(output.contains("1) Alpha"));
        assertTrue(output.contains("2) Beta"));
        assertTrue(output.contains("0) Exit"));
    }

    @Test
    void invalidChoicesAreRejectedAndTheMenuContinues() {
        CountingCommand command = new CountingCommand("Only");
        Menu menu = new Menu(consoleWithInput("9\n-1\nabc\n1\n0\n"));
        menu.register(command);

        menu.run();

        assertEquals(1, command.executions);
        assertTrue(captured.toString().contains("Invalid choice, try again."));
        assertTrue(captured.toString().contains("Please enter a valid number."));
    }

    @Test
    void crashingCommandDoesNotKillTheMenu() {
        CountingCommand command = new CountingCommand("Fine");
        Menu menu = new Menu(consoleWithInput("1\n2\n0\n"));
        menu.register(new CrashingCommand());
        menu.register(command);

        menu.run();

        assertTrue(captured.toString().contains("Unexpected error: boom"));
        assertEquals(1, command.executions);
    }

    @Test
    void closedInputEndsTheMenuWithAnException() {
        Menu menu = new Menu(consoleWithInput("")); // no input at all
        menu.register(new CountingCommand("X"));
        assertThrows(InputClosedException.class, menu::run);
    }
}
