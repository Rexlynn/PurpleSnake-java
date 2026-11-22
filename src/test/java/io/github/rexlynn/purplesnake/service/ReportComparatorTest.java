package io.github.rexlynn.purplesnake.service;

import io.github.rexlynn.purplesnake.model.PortState;
import io.github.rexlynn.purplesnake.model.ScanComparison;
import io.github.rexlynn.purplesnake.model.ScanReport;
import io.github.rexlynn.purplesnake.model.ScanResult;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportComparatorTest {

    private final ReportComparator comparator = new ReportComparator();

    private ScanReport report(int day, ScanResult... results) {
        return new ScanReport("lab-pc", LocalDateTime.of(2026, 11, day, 12, 0), List.of(results));
    }

    @Test
    void detectsNewlyOpenedPort() {
        ScanReport before = report(1, new ScanResult(22, PortState.OPEN), new ScanResult(80, PortState.CLOSED));
        ScanReport after = report(2, new ScanResult(22, PortState.OPEN), new ScanResult(80, PortState.OPEN));
        ScanComparison changes = comparator.compare(before, after);
        assertEquals(List.of(80), changes.getNewlyOpened());
        assertTrue(changes.getNoLongerOpen().isEmpty());
        assertTrue(changes.hasChanges());
    }

    @Test
    void detectsPortThatIsNoLongerOpen() {
        ScanReport before = report(1, new ScanResult(22, PortState.OPEN), new ScanResult(80, PortState.OPEN));
        ScanReport after = report(2, new ScanResult(22, PortState.OPEN), new ScanResult(80, PortState.FILTERED));
        ScanComparison changes = comparator.compare(before, after);
        assertEquals(List.of(80), changes.getNoLongerOpen());
        assertTrue(changes.getNewlyOpened().isEmpty());
    }

    @Test
    void identicalScansHaveNoChanges() {
        ScanReport before = report(1, new ScanResult(22, PortState.OPEN));
        ScanReport after = report(2, new ScanResult(22, PortState.OPEN));
        assertFalse(comparator.compare(before, after).hasChanges());
    }

    @Test
    void changeBetweenClosedAndFilteredIsNotInteresting() {
        ScanReport before = report(1, new ScanResult(80, PortState.CLOSED));
        ScanReport after = report(2, new ScanResult(80, PortState.FILTERED));
        assertFalse(comparator.compare(before, after).hasChanges());
    }

    @Test
    void portsScannedOnlyInOneReportAreIgnored() {
        ScanReport before = report(1, new ScanResult(22, PortState.OPEN));
        ScanReport after = report(2, new ScanResult(22, PortState.OPEN), new ScanResult(80, PortState.OPEN));
        // Port 80 was not scanned before, so it is unknown, not "newly opened"
        assertFalse(comparator.compare(before, after).hasChanges());
    }

    @Test
    void resultListsAreSortedByPort() {
        ScanReport before = report(1, new ScanResult(80, PortState.CLOSED), new ScanResult(22, PortState.CLOSED));
        ScanReport after = report(2, new ScanResult(80, PortState.OPEN), new ScanResult(22, PortState.OPEN));
        assertEquals(List.of(22, 80), comparator.compare(before, after).getNewlyOpened());
    }
}
