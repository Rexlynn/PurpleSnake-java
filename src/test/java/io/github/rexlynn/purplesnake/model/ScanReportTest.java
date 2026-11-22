package io.github.rexlynn.purplesnake.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScanReportTest {

    private static final LocalDateTime TIME = LocalDateTime.of(2026, 11, 22, 16, 0, 0);

    private List<ScanResult> unsortedResults() {
        List<ScanResult> list = new ArrayList<>();
        list.add(new ScanResult(443, PortState.OPEN));
        list.add(new ScanResult(22, PortState.CLOSED));
        list.add(new ScanResult(80, PortState.OPEN));
        list.add(new ScanResult(23, PortState.FILTERED));
        return list;
    }

    @Test
    void resultsAreSortedByPortAutomatically() {
        ScanReport report = new ScanReport("lab-pc", TIME, unsortedResults());
        List<ScanResult> sorted = report.getResults();
        assertEquals(22, sorted.get(0).getPort());
        assertEquals(23, sorted.get(1).getPort());
        assertEquals(80, sorted.get(2).getPort());
        assertEquals(443, sorted.get(3).getPort());
    }

    @Test
    void reportKeepsItsOwnCopyOfTheList() {
        List<ScanResult> source = unsortedResults();
        ScanReport report = new ScanReport("lab-pc", TIME, source);
        source.clear(); // changing the caller's list must not change the report
        assertEquals(4, report.getResults().size());
    }

    @Test
    void returnedListCannotBeModified() {
        ScanReport report = new ScanReport("lab-pc", TIME, unsortedResults());
        List<ScanResult> view = report.getResults();
        assertThrows(UnsupportedOperationException.class, () -> view.add(new ScanResult(1, PortState.OPEN)));
    }

    @Test
    void countsAndFiltersByState() {
        ScanReport report = new ScanReport("lab-pc", TIME, unsortedResults());
        assertEquals(2, report.count(PortState.OPEN));
        assertEquals(1, report.count(PortState.CLOSED));
        assertEquals(1, report.count(PortState.FILTERED));
        assertEquals(80, report.getResultsByState(PortState.OPEN).get(0).getPort());
    }

    @Test
    void canBeSortedWithALambdaComparator() {
        ScanReport report = new ScanReport("lab-pc", TIME, unsortedResults());
        // Highest port first
        List<ScanResult> descending = report.getSortedBy((a, b) -> Integer.compare(b.getPort(), a.getPort()));
        assertEquals(443, descending.get(0).getPort());
        assertEquals(22, descending.get(3).getPort());

        // By state first, then by port (Comparator.comparing with a method reference)
        List<ScanResult> byState = report.getSortedBy(
                Comparator.comparing(ScanResult::getState).thenComparingInt(ScanResult::getPort));
        assertEquals(PortState.OPEN, byState.get(0).getState());
        assertEquals(80, byState.get(0).getPort());
    }

    @Test
    void sortingWithAComparatorDoesNotChangeTheReport() {
        ScanReport report = new ScanReport("lab-pc", TIME, unsortedResults());
        report.getSortedBy((a, b) -> Integer.compare(b.getPort(), a.getPort()));
        assertEquals(22, report.getResults().get(0).getPort());
    }

    @Test
    void blankTargetIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ScanReport("  ", TIME, unsortedResults()));
        assertThrows(IllegalArgumentException.class, () -> new ScanReport(null, TIME, unsortedResults()));
    }

    @Test
    void nullTimeOrResultsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ScanReport("x", null, unsortedResults()));
        assertThrows(IllegalArgumentException.class, () -> new ScanReport("x", TIME, null));
    }

    @Test
    void targetIsTrimmed() {
        assertEquals("lab-pc", new ScanReport("  lab-pc  ", TIME, unsortedResults()).getTarget());
    }
}
