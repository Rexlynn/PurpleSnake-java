package io.github.rexlynn.purplesnake.command;

/**
 * COMMAND pattern: one menu action = one object.
 * The menu only knows this interface; it does not know what the actions really do.
 */
public interface Command {

    /** The text shown in the menu. */
    String getLabel();

    /** Runs the action (asks the user for input, does the work, prints the result). */
    void execute();
}
