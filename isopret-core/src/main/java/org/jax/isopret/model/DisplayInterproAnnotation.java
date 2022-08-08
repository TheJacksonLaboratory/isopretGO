package org.jax.isopret.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;

public class DisplayInterproAnnotation extends InterproAnnotation implements Comparable<DisplayInterproAnnotation> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DisplayInterproAnnotation.class);

    private final Set<InterproEntryType> sites = Set.of(InterproEntryType.ACTIVE_SITE,
            InterproEntryType.BINDING_SITE,
            InterproEntryType.CONSERVED_SITE,
            InterproEntryType.PTM);


    private final InterproEntry interproEntry;

    public DisplayInterproAnnotation(InterproAnnotation annot, InterproEntry entry) {
        super(annot);
        this.interproEntry = entry;
    }

    public InterproEntry getInterproEntry() {
        return this.interproEntry;
    }

    public boolean isDomain() {
        return this.interproEntry.getEntryType() == InterproEntryType.DOMAIN;
    }

    public boolean isFamily() {
        return this.interproEntry.getEntryType() == InterproEntryType.FAMILY;
    }

    public boolean isSuperFamily() {
        return this.interproEntry.getEntryType() == InterproEntryType.HOMOLOGOUS_SUPERFAMILY;
    }

    public boolean isFamilyOrSuperfamily() {
        return (this.interproEntry.getEntryType() == InterproEntryType.FAMILY ||
                this.interproEntry.getEntryType() == InterproEntryType.HOMOLOGOUS_SUPERFAMILY);
    }

    public boolean isRepeat() {
        return this.interproEntry.getEntryType() == InterproEntryType.REPEAT;
    }

    public boolean isSite() {
        return this.sites.contains(this.interproEntry.getEntryType());
    }


    @Override
    public int hashCode() {
        return Objects.hash(this.interproEntry,this.enst, this.ensg, this.interpro, this.start, this.end);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (! (obj instanceof DisplayInterproAnnotation that)) return false;
        return this.interproEntry.equals(that.interproEntry) &&
                this.ensg.equals(that.ensg) &&
                this.enst.equals(that.enst) &&
                this.interpro == that.interpro &&
                this.start == that.start &&
                this.end == that.end;
    }

    /**
     * Check if the other DisplayInterproAnnotation overlaps with this.
     * The expected use case is that other will start at a position that is equal to or more than
     * the start position of this, because we will be checking a sorted list.
     * If this is not the case, then check whether there is at least 90% overlap.
     * @param other another DisplayInterproAnnotation object that might overlap with this one
     * @return true if there is at least 75% reciprocal overlap.
     */
    public boolean overlapsBy(DisplayInterproAnnotation other) {
         final double THRESHOLD = 0.75d;
        if (other.getStart() >= getStart() && other.getEnd() <= getEnd()) {
            return true; // other is completely contained in "this"
        }
        int maxLen = Math.max(getLength(), other.getLength());
        int start = Math.max(getStart(), other.getStart());
        int end = Math.min(getEnd(), other.getEnd());
        double overlap =  (double)(end-start)/maxLen;
        return overlap >= THRESHOLD;
    }


    @Override
    public int compareTo(DisplayInterproAnnotation that) {
        return this.interproEntry.getDescription().compareTo(that.interproEntry.getDescription());
    }

    /** This method is used to merge two overlapping annotations */
    public DisplayInterproAnnotation merge(DisplayInterproAnnotation other) {
        int start = Math.min(getStart(), other.getStart());
        int end = Math.max(getEnd(), other.getEnd());
        InterproAnnotation merged = new InterproAnnotation(getEnst(), getEnsg(), getInterpro(), start, end);
        return new DisplayInterproAnnotation(merged, getInterproEntry());
    }
}
