package org.jax.isopret.core.go;

import org.monarchinitiative.phenol.analysis.stats.GoTerm2PValAndCounts;

import java.util.Comparator;

public class SortByPvalue implements Comparator<GoTerm2PValAndCounts> {
    // Used for sorting in ascending order of
    // roll number
    public int compare(GoTerm2PValAndCounts a, GoTerm2PValAndCounts b)
    {
        double diff = a.getRawPValue() - b.getRawPValue();
        if (diff > 0) {
            return 1;
        } else if (diff < 0) {
            return -1;
        } else  {
            return 0;
        }
    }
}

