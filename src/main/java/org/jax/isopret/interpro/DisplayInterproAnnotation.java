package org.jax.isopret.interpro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisplayInterproAnnotation extends InterproAnnotation implements Comparable<DisplayInterproAnnotation> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DisplayInterproAnnotation.class);

    private final InterproEntry interproEntry;

    public DisplayInterproAnnotation(InterproAnnotation annot, InterproEntry entry) {
        super(annot);
        this.interproEntry = entry;
    }

    public InterproEntry getInterproEntry() {
        return this.interproEntry;
    }

    @Override
    public int compareTo(DisplayInterproAnnotation that) {
        return this.interproEntry.getDescription().compareTo(that.interproEntry.getDescription());
    }
}
