package io.github.rexlynn.purplesnake.model;

import java.util.HashMap;
import java.util.Map;

/** Lookup table: well-known port number -> service name. */
public final class ServiceCatalog {

    public static final String UNKNOWN = "Unknown service";

    private static final Map<Integer, String> SERVICES = new HashMap<>();

    // Static initializer: runs once when the class is loaded and fills the table.
    static {
        SERVICES.put(20, "FTP Data");
        SERVICES.put(21, "FTP");
        SERVICES.put(22, "SSH");
        SERVICES.put(23, "Telnet");
        SERVICES.put(25, "SMTP");
        SERVICES.put(53, "DNS");
        SERVICES.put(67, "DHCP");
        SERVICES.put(68, "DHCP");
        SERVICES.put(69, "TFTP");
        SERVICES.put(80, "HTTP");
        SERVICES.put(110, "POP3");
        SERVICES.put(143, "IMAP");
        SERVICES.put(443, "HTTPS");
        SERVICES.put(445, "SMB");
        SERVICES.put(3306, "MySQL");
        SERVICES.put(3389, "RDP");
    }

    // Private constructor: this is a utility class, nobody should create objects of it.
    private ServiceCatalog() {
    }

    public static String nameFor(int port) {
        // getOrDefault returns UNKNOWN when the port is not in the table
        return SERVICES.getOrDefault(port, UNKNOWN);
    }
}
