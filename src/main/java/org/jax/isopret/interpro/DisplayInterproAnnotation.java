package org.jax.isopret.interpro;

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
