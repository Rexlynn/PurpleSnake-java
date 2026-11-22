package io.github.rexlynn.purplesnake.ui;

import io.github.rexlynn.purplesnake.exception.InputClosedException;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * All reading from the keyboard and writing to the screen goes through this class.
 * The streams are given in the constructor, so tests can use fake input and capture the output.
 */
public class ConsoleIO {

    // One Scanner for the whole program (several Scanners on System.in can lose input)
    private final Scanner scanner;
    private final PrintStream out;

    public ConsoleIO(InputStream in, PrintStream out) {
        this.scanner = new Scanner(in);
        this.out = out;
    }

    public void println(String text) {
        out.println(text);
    }

    public void println() {
        out.println();
    }

    /** Shows the prompt and reads one line (without spaces at both ends). */
    public String readLine(String prompt) {
        out.print(prompt);
        if (!scanner.hasNextLine()) {
            throw new InputClosedException();
        }
        return scanner.nextLine().trim();
    }

    /**
     * Keeps asking until the user types a valid whole number.
     * Reading the whole line and parsing it avoids the nextInt() problem
     * where the Enter key stays in the buffer.
     */
    public int readInt(String prompt) {
        while (true) {
            String line = readLine(prompt);
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                println("Please enter a valid number.");
            }
        }
    }

    /** Asks a yes/no question. Only "y" or "Y" means yes. */
    public boolean confirm(String question) {
        return readLine(question + " (y/n): ").equalsIgnoreCase("y");
    }
}
