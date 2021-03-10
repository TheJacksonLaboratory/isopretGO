package org.jax.isopret.interpro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

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
    public int hashCode() {
        return Objects.hash(this.interproEntry,this.enst, this.ensg, this.interpro, this.start, this.end);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (! (obj instanceof DisplayInterproAnnotation)) return false;
        DisplayInterproAnnotation that = (DisplayInterproAnnotation) obj;
        return this.interproEntry.equals(that.interproEntry) &&
                this.ensg.equals(that.ensg) &&
                this.enst.equals(that.enst) &&
                this.interpro == that.interpro &&
                this.start == that.start &&
                this.end == that.end;
    }

    @Override
    public int compareTo(DisplayInterproAnnotation that) {
        return this.interproEntry.getDescription().compareTo(that.interproEntry.getDescription());
    }
}
