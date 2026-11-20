import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class PurpleSnake {

    // One shared Scanner for the whole program. Creating several Scanners on
    // System.in can lose input, so every method uses this one.
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        NetworkMonitor netMonitor = new NetworkMonitor();

        while (true) {
            System.out.println("\n=== PurpleSnake Security Toolkit ===");
            System.out.println("1) Port Scanner");
            System.out.println("2) XOR Encrypt / Decrypt");
            System.out.println("3) Network Monitor (Interfaces & Active Connections)");
            System.out.println("0) Exit");

            int choice = readInt("Your Choice: ");

            switch (choice) {
                case 1:
                    runPortScanner();
                    break;
                case 2:
                    runXorTool();
                    break;
                case 3:
                    try {
                        System.out.println("\n--- Network Monitor Running ---");
                        netMonitor.printInterfaces();
                        System.out.println("\n--- Active Connections ---");
                        netMonitor.printActiveConnections();
                    } catch (IOException e) {
                        System.out.println("Network Monitor Error: " + e.getMessage());
                    }
                    break;
                case 0:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice, try again.");
            }
        }
    }

    // ---------- input helpers ----------

    // Reads one line from the user. If the input stream is closed
    // (Ctrl+D on Linux/WSL, Ctrl+Z then Enter on Windows)
    // the program exits cleanly instead of crashing.
    private static String readLine(String prompt) {
        System.out.print(prompt);

        if (!scanner.hasNextLine()) {
            System.out.println("\nInput closed. Exiting.");
            System.exit(0);
        }
        return scanner.nextLine().trim();
    }

    // Keeps asking until the user types a valid whole number.
    // Reading the whole line and parsing it also avoids the nextInt() problem
    // where the Enter key stays in the buffer.
    private static int readInt(String prompt) {

        while (true) {
            String line = readLine(prompt);
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    // ---------- service names ----------

    public static String getServiceName(int port) {
        switch (port) {
            case 20: return "FTP Data";
            case 21: return "FTP";
            case 22: return "SSH";
            case 23: return "Telnet";
            case 25: return "SMTP";
            case 53: return "DNS";
            case 67: case 68: return "DHCP";
            case 69: return "TFTP";
            case 80: return "HTTP";
            case 110: return "POP3";
            case 143: return "IMAP";
            case 443: return "HTTPS";
            case 445: return "SMB";
            case 3306: return "MySQL DB";
            case 3389: return "RDP";
            default: return "Unknown/Custom Service";
        }
    }

    // ---------- 1) port scanner ----------

    public static void runPortScanner() {
        System.out.println("\n=== Port Scanner ===");
        System.out.println("Only scan hosts you own or have permission to scan.");

        String target = readLine("Enter target IP or hostname: ");

        // An empty text would silently mean "this computer" (localhost),
        // so we reject it explicitly.
        if (target.isEmpty()) {
            System.out.println("Target cannot be empty.");
            return;
        }

        // Resolve the name once, before the loop. A wrong host is detected
        // immediately instead of printing one error line per port.
        InetAddress address;
        try {
            address = InetAddress.getByName(target);
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + target);
            return;
        }

        int startPort = readInt("Enter starting port: ");
        int endPort = readInt("Enter ending port: ");

        if (startPort < 1 || endPort > 65535 || startPort > endPort) {
            System.out.println("Invalid port range, ports must be between 1 and 65535 and start <= end.");
            return;
        }

        // The scan is serial: if the host does not answer, every port costs
        // up to 1 second. For big ranges we warn the user first.
        int portCount = endPort - startPort + 1;
        if (portCount > 100) {
            System.out.println("This scan covers " + portCount + " ports and may take up to "
                    + portCount + " seconds if the host does not respond.");
            String answer = readLine("Continue? (y/n): ");
            if (!answer.equalsIgnoreCase("y")) {
                System.out.println("Scan cancelled.");
                return;
            }
        }

        System.out.println("\nScanning " + address.getHostAddress()
                + " from port " + startPort + " to " + endPort + "...\n");

        for (int port = startPort; port <= endPort; port++) {
            // try-with-resources: the socket is closed automatically after the block
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(address, port), 1000);
                System.out.printf("Port %5d OPEN (%s)\n", port, getServiceName(port));
            } catch (SocketTimeoutException e) {
                System.out.printf("Port %5d FILTERED (no response)\n", port);
            } catch (ConnectException e) {
                System.out.printf("Port %5d CLOSED.\n", port);
            } catch (IOException e) {
                System.out.printf("Port %5d FILTERED/ERROR (%s)\n", port, e.getMessage());
            }
        }
        System.out.println("Scan Complete.");
    }

    // ---------- 2) XOR tool ----------

    public static void runXorTool() {
        System.out.println("\n=== XOR Encrypt/Decrypt Tool ===");
        System.out.println("Note: XOR with a repeating key is obfuscation, not secure encryption.");

        String inputPath = readLine("Enter input file path: ");
        String outputPath = readLine("Enter output file path: ");
        String key = readLine("Enter key (string): ");

        if (inputPath.isEmpty() || outputPath.isEmpty()) {
            System.out.println("File paths cannot be empty!");
            return;
        }
        if (key.isEmpty()) {
            System.out.println("Key cannot be empty!");
            return;
        }

        // Compare real, cleaned-up paths. Comparing plain text would miss that
        // "a.txt" and "./a.txt" are the same file, and the output stream would
        // empty the input file before we read it.
        Path in = Paths.get(inputPath).toAbsolutePath().normalize();
        Path out = Paths.get(outputPath).toAbsolutePath().normalize();
        if (in.equals(out)) {
            System.out.println("Error: Input and output file paths cannot be the same!");
            return;
        }

        // Never overwrite an existing file without asking
        if (out.toFile().exists()) {
            String answer = readLine("Output file already exists. Overwrite? (y/n): ");
            if (!answer.equalsIgnoreCase("y")) {
                System.out.println("Cancelled.");
                return;
            }
        }

        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);

        // Buffered streams read/write in blocks internally, which is much faster
        // than a system call for every single byte.
        try (BufferedInputStream fin = new BufferedInputStream(new FileInputStream(inputPath));
             BufferedOutputStream fout = new BufferedOutputStream(new FileOutputStream(outputPath))) {

            int ch;
            long i = 0;
            while ((ch = fin.read()) != -1) {
                // "& 0xFF" turns the signed Java byte (-128..127) into 0..255
                int enc = ch ^ (keyBytes[(int) (i % keyBytes.length)] & 0xFF);
                fout.write(enc);
                i++;
            }
            System.out.println("Done! Output saved to " + outputPath);

        } catch (FileNotFoundException e) {
            System.out.println("Could not open file: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("I/O Error during XOR operation: " + e.getMessage());
        }
    }
}

/**
 * 3) Network monitor: lists interfaces (pure Java) and active connections
 * (by running the operating system's own tool).
 */
class NetworkMonitor {

    public void printInterfaces() throws SocketException {
        // getNetworkInterfaces() returns an old-style Enumeration;
        // Collections.list turns it into a List we can loop over.
        List<NetworkInterface> interfaces =
                Collections.list(NetworkInterface.getNetworkInterfaces());

        for (NetworkInterface ni : interfaces) {
            System.out.println("Interface: " + ni.getDisplayName()
                    + " (" + ni.getName() + ") "
                    + (ni.isUp() ? "[UP]" : "[DOWN]"));

            for (InetAddress addr : Collections.list(ni.getInetAddresses())) {
                System.out.println("  IP: " + addr.getHostAddress());
            }
            System.out.println("--------------------------------------------------");
        }
    }

    public void printActiveConnections() throws IOException {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            runCommand(List.of("netstat", "-ano"));
        } else {
            try {
                runCommand(List.of("ss", "-tun"));
            } catch (IOException e) {
                runCommand(List.of("netstat", "-tun")); // older systems
            }
        }
    }

    private void runCommand(List<String> command) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true); // merge error output into normal output

        Process process = builder.start();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }
}
