package io.github.rexlynn.purplesnake.command;

import io.github.rexlynn.purplesnake.service.NetworkInfoProvider;
import io.github.rexlynn.purplesnake.ui.ConsoleIO;

import java.io.IOException;
import java.util.List;

/** Shows local network interfaces and active connections (a small sub-menu). */
public class NetworkInfoCommand extends BaseCommand {

    private final NetworkInfoProvider provider;

    public NetworkInfoCommand(ConsoleIO io, NetworkInfoProvider provider) {
        super("Network Info", io);
        this.provider = provider;
    }

    @Override
    public void execute() {
        while (true) {
            io.println("\n=== Network Info ===");
            io.println("1) Network interfaces");
            io.println("2) Active connections");
            io.println("0) Back");

            int choice = io.readInt("Your choice: ");
            if (choice == 0) {
                return;
            }
            try {
                if (choice == 1) {
                    print(provider.describeInterfaces());
                } else if (choice == 2) {
                    io.println("Note: this shows the REAL connections of this computer.");
                    io.println("Do not show it on a shared screen if that is a problem.\n");
                    print(provider.listConnections());
                } else {
                    io.println("Invalid choice, try again.");
                }
            } catch (IOException e) {
                io.println("Could not get network info: " + e.getMessage());
            }
        }
    }

    private void print(List<String> lines) {
        io.println();
        for (String line : lines) {
            io.println(line);
        }
    }
}
