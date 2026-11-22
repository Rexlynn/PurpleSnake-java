package io.github.rexlynn.purplesnake.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Collects information about the local network:
 *  - interfaces and their IP addresses (pure Java API)
 *  - active connections (by running the operating system's own tool)
 * Both methods RETURN lines instead of printing, so the user interface decides how to show them.
 */
public class NetworkInfoProvider {

    public List<String> describeInterfaces() throws SocketException {
        List<String> lines = new ArrayList<>();
        NetworkInterface[] interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
                .toArray(new NetworkInterface[0]);

        for (NetworkInterface ni : interfaces) {
            lines.add("Interface: " + ni.getDisplayName() + " (" + ni.getName() + ") "
                    + (ni.isUp() ? "[UP]" : "[DOWN]"));
            for (InetAddress address : Collections.list(ni.getInetAddresses())) {
                lines.add("  IP: " + address.getHostAddress());
            }
        }
        return lines;
    }

    public List<String> listConnections() throws IOException {
        String os = System.getProperty("os.name");
        try {
            return runCommand(commandFor(os));
        } catch (IOException e) {
            if (isWindows(os)) {
                throw e;
            }
            // Older Linux systems have "netstat" but not "ss"
            return runCommand(List.of("netstat", "-tun"));
        }
    }

    /** Chooses the right command for the operating system. */
    public static List<String> commandFor(String osName) {
        if (isWindows(osName)) {
            return List.of("netstat", "-ano");
        }
        return List.of("ss", "-tun");
    }

    private static boolean isWindows(String osName) {
        return osName != null && osName.toLowerCase().contains("win");
    }

    private List<String> runCommand(List<String> command) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true); // merge error output into normal output

        Process process = builder.start();
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }
}
