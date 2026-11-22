package io.github.rexlynn.purplesnake.command;

import io.github.rexlynn.purplesnake.service.XorCipher;
import io.github.rexlynn.purplesnake.ui.ConsoleIO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

/** XOR-obfuscates or restores a file with a text key. */
public class XorCommand extends BaseCommand {

    public XorCommand(ConsoleIO io) {
        super("XOR Encrypt / Decrypt", io);
    }

    @Override
    public void execute() {
        io.println("\n=== XOR Encrypt / Decrypt ===");
        io.println("Note: XOR with a repeating key is obfuscation, not secure encryption.");

        String inputPath = io.readLine("Input file: ");
        String outputPath = io.readLine("Output file: ");
        String key = io.readLine("Key (text): ");

        if (inputPath.isEmpty() || outputPath.isEmpty()) {
            io.println("File paths cannot be empty.");
            return;
        }
        if (key.isEmpty()) {
            io.println("Key cannot be empty.");
            return;
        }

        Path input = Paths.get(inputPath);
        Path output = Paths.get(outputPath);

        // Never overwrite an existing file without asking
        if (Files.exists(output) && !io.confirm("Output file already exists. Overwrite?")) {
            io.println("Cancelled.");
            return;
        }

        try {
            new XorCipher(key).processFile(input, output);
            io.println("Done! Output saved to " + outputPath);
        } catch (NoSuchFileException e) {
            io.println("File not found (or its folder does not exist): " + e.getMessage());
        } catch (IOException e) {
            io.println("I/O error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            io.println("Error: " + e.getMessage());
        }
    }
}
