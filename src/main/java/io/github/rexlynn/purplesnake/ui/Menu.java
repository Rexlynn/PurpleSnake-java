package io.github.rexlynn.purplesnake.ui;

import io.github.rexlynn.purplesnake.command.Command;
import io.github.rexlynn.purplesnake.exception.InputClosedException;

import java.util.ArrayList;
import java.util.List;

/**
 * The INVOKER of the Command pattern: it shows the registered commands as a numbered menu
 * and runs the chosen one. It never knows which concrete command it runs (POLYMORPHISM).
 *
 * Relationship: the menu holds 0..* commands (aggregation: commands can exist without it).
 */
public class Menu {

    private final ConsoleIO io;
    private final List<Command> commands = new ArrayList<>();

    public Menu(ConsoleIO io) {
        this.io = io;
    }

    public void register(Command command) {
        commands.add(command);
    }

    public void run() {
        while (true) {
            io.println("\n=== PurpleSnake Security Toolkit ===");
            for (int i = 0; i < commands.size(); i++) {
                io.println((i + 1) + ") " + commands.get(i).getLabel());
            }
            io.println("0) Exit");

            int choice = io.readInt("Your choice: ");

            if (choice == 0) {
                io.println("Goodbye!");
                return; // leaves run(), which ends the loop
            }
            if (choice < 1 || choice > commands.size()) {
                io.println("Invalid choice, try again.");
                continue; // back to the top of the loop
            }

            try {
                // Polymorphism: the same call runs different code for different command objects
                commands.get(choice - 1).execute();
            } catch (InputClosedException e) {
                throw e; // the user closed the input: let Main end the program
            } catch (RuntimeException e) {
                // A bug in one command must not kill the whole program
                io.println("Unexpected error: " + e.getMessage());
            }
        }
    }
}
