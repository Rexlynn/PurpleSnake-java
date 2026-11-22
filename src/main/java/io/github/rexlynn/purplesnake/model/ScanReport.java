package io.github.rexlynn.purplesnake.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * One finished scan: which target, when, and the result of every scanned port.
 *
 * Relationship: a ScanReport OWNS its ScanResults (composition, 1 report to 0..* results).
 * The results do not make sense without the report, and the report keeps its own private copy.
 */
public class ScanReport {

    private final String target;
    private final LocalDateTime scannedAt;
    private final List<ScanResult> results;

    public ScanReport(String target, LocalDateTime scannedAt, List<ScanResult> results) {
        if (target == null || target.isBlank()) {
            throw new IllegalArgumentException("Target must not be empty.");
        }
        if (scannedAt == null) {
            throw new IllegalArgumentException("Scan time must not be null.");
        }
        if (results == null) {
            throw new IllegalArgumentException("Results must not be null.");
        }
        for (ScanResult result : results) {
            if (result == null) {
                throw new IllegalArgumentException("Results must not contain null.");
            }
        }
        this.target = target.trim();
        this.scannedAt = scannedAt;
        // Defensive copy: if the caller changes its own list later, this report is not affected.
        this.results = new ArrayList<>(results);
        // Sort by the natural order of ScanResult (compareTo = port number).
        Collections.sort(this.results);
    }

    public String getTarget() {
        return target;
    }

    public LocalDateTime getScannedAt() {
        return scannedAt;
    }

    /** Read-only view: the caller can read the list but cannot change it. */
    public List<ScanResult> getResults() {
        return Collections.unmodifiableList(results);
    }

    public List<ScanResult> getResultsByState(PortState state) {
        List<ScanResult> matching = new ArrayList<>();
        for (ScanResult result : results) {
            if (result.getState() == state) {
                matching.add(result);
            }
        }
        return matching;
    }

    public int count(PortState state) {
        return getResultsByState(state).size();
    }

    /** Returns a NEW sorted list using any rule given as a Comparator (often a lambda). */
    public List<ScanResult> getSortedBy(Comparator<ScanResult> comparator) {
        List<ScanResult> copy = new ArrayList<>(results);
        copy.sort(comparator);
        return copy;
    }
}
