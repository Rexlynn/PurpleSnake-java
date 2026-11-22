package io.github.rexlynn.purplesnake.command;

import io.github.rexlynn.purplesnake.ui.ConsoleIO;

import java.time.format.DateTimeFormatter;

/**
 * INHERITANCE: an abstract class holds what all commands share (label and console).
 * It is "abstract" because it cannot be used alone: execute() is left for the subclasses.
 */
public abstract class BaseCommand implements Command {

    protected static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String label;
    protected final ConsoleIO io; // protected: visible to subclasses

    protected BaseCommand(String label, ConsoleIO io) {
        this.label = label;
        this.io = io;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
