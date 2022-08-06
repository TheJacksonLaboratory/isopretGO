package org.jax.isopret.visualization;

import org.jax.isopret.model.InterproEntry;

public class InterproVisualizable implements Comparable<InterproVisualizable> {

    private final String interproAccession;
    private final String description;
    private final String entryType;
    private final int id;

    public InterproVisualizable(InterproEntry entry) {
        this.interproAccession = entry.getIntroproAccession();
        this.description = entry.getDescription();
        this.entryType = entry.getEntryType().name();
        this.id = entry.getId();
    }

    public String getInterproAccession() {
        return interproAccession;
    }

    public String getDescription() {
        return description;
    }

    public String getEntryType() {
        return entryType;
    }

    @Override
    public int compareTo(InterproVisualizable that) {
        return Integer.compare(this.id, that.id);
    }
}
