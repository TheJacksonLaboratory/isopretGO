package org.jax.isopret.go;

import org.monarchinitiative.phenol.ontology.data.TermId;

/**
 * A convenience class to hold the GO id and the corresponding label to organize data for output.
 */
public class GoTermIdPlusLabel {
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
}
