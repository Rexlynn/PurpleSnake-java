package io.github.rexlynn.purplesnake.service;

import io.github.rexlynn.purplesnake.model.PortState;
import io.github.rexlynn.purplesnake.model.ScanComparison;
import io.github.rexlynn.purplesnake.model.ScanReport;
import io.github.rexlynn.purplesnake.model.ScanResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Compares two scans of the same target and reports what changed.
 * Only ports that were scanned in BOTH reports are compared (a port that was not
 * scanned before is unknown, not "newly opened").
 */
public class ReportComparator {

    public ScanComparison compare(ScanReport previous, ScanReport current) {
        // Map: port number -> state in the previous scan, for fast lookup
        Map<Integer, PortState> before = new HashMap<>();
        for (ScanResult result : previous.getResults()) {
            before.put(result.getPort(), result.getState());
        }

        List<Integer> newlyOpened = new ArrayList<>();
        List<Integer> noLongerOpen = new ArrayList<>();

        for (ScanResult now : current.getResults()) {
            PortState was = before.get(now.getPort());
            if (was == null) {
                continue; // not scanned before, nothing to compare
            }
            if (now.getState() == PortState.OPEN && was != PortState.OPEN) {
                newlyOpened.add(now.getPort());
            } else if (now.getState() != PortState.OPEN && was == PortState.OPEN) {
                noLongerOpen.add(now.getPort());
            }
        }
        return new ScanComparison(newlyOpened, noLongerOpen);
    }
}
