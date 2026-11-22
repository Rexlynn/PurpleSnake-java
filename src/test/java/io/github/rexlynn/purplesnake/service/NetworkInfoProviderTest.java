package io.github.rexlynn.purplesnake.service;

import org.junit.jupiter.api.Test;

import java.net.SocketException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class NetworkInfoProviderTest {

    @Test
    void windowsUsesNetstat() {
        assertEquals(List.of("netstat", "-ano"), NetworkInfoProvider.commandFor("Windows 11"));
    }

    @Test
    void linuxUsesSs() {
        assertEquals(List.of("ss", "-tun"), NetworkInfoProvider.commandFor("Linux"));
    }

    @Test
    void unknownOsFallsBackToLinuxStyleCommand() {
        assertEquals(List.of("ss", "-tun"), NetworkInfoProvider.commandFor(null));
    }

    @Test
    void interfaceDescriptionIsNotEmpty() throws SocketException {
        // Every computer has at least the loopback interface
        assertFalse(new NetworkInfoProvider().describeInterfaces().isEmpty());
    }
}
