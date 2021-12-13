package org.jax.isopret.core.go;

import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.Objects;

/**
 * A convenience class to hold the GO id and the corresponding label to organize data for output.
 */
public class GoTermIdPlusLabel implements Comparable<GoTermIdPlusLabel> {
    private final String id;
    private final String label;

    public GoTermIdPlusLabel(TermId tid, String label) {
        this.id = tid.getValue();
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (! (obj instanceof GoTermIdPlusLabel that)) return false;
        return this.id.equals(that.id) && this.label.equals(that.label);
    }

    @Override
    public int compareTo(GoTermIdPlusLabel that) {
        return this.label.compareTo(that.label);
    }
}
