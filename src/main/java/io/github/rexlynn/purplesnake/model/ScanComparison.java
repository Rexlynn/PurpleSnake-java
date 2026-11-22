package io.github.rexlynn.purplesnake.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** What changed between two scans of the same target. Immutable. */
public class ScanComparison {

    private final List<Integer> newlyOpened;
    private final List<Integer> noLongerOpen;

    public ScanComparison(List<Integer> newlyOpened, List<Integer> noLongerOpen) {
        this.newlyOpened = new ArrayList<>(newlyOpened);
        this.noLongerOpen = new ArrayList<>(noLongerOpen);
    }

    /** Ports that are open now but were not open in the previous scan. */
    public List<Integer> getNewlyOpened() {
        return Collections.unmodifiableList(newlyOpened);
    }

    /** Ports that were open in the previous scan but are not open now. */
    public List<Integer> getNoLongerOpen() {
        return Collections.unmodifiableList(noLongerOpen);
    }

    public boolean hasChanges() {
        return !newlyOpened.isEmpty() || !noLongerOpen.isEmpty();
    }
}
