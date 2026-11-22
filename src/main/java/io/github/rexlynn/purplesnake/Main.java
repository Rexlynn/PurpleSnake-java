package io.github.rexlynn.purplesnake;

import io.github.rexlynn.purplesnake.command.HistoryCommand;
import io.github.rexlynn.purplesnake.command.NetworkInfoCommand;
import io.github.rexlynn.purplesnake.command.PortScanCommand;
import io.github.rexlynn.purplesnake.command.XorCommand;
import io.github.rexlynn.purplesnake.exception.InputClosedException;
import io.github.rexlynn.purplesnake.persistence.FileReportRepository;
import io.github.rexlynn.purplesnake.persistence.ReportRepository;
import io.github.rexlynn.purplesnake.service.NetworkInfoProvider;
import io.github.rexlynn.purplesnake.service.PortScanner;
import io.github.rexlynn.purplesnake.ui.ConsoleIO;
import io.github.rexlynn.purplesnake.ui.Menu;

import java.nio.file.Paths;

/** Program entry point: creates the objects, connects them and starts the menu. */
public class Main {

    public static void main(String[] args) {
        ConsoleIO io = new ConsoleIO(System.in, System.out);

        // The variable has the INTERFACE type; only this line knows the concrete class.
        ReportRepository repository = new FileReportRepository(Paths.get("data", "reports"));

        Menu menu = new Menu(io);
        menu.register(new PortScanCommand(io, new PortScanner(), repository));
        menu.register(new HistoryCommand(io, repository));
        menu.register(new XorCommand(io));
        menu.register(new NetworkInfoCommand(io, new NetworkInfoProvider()));

        try {
            menu.run();
        } catch (InputClosedException e) {
            io.println("\nInput closed. Exiting.");
        }
    }
}
