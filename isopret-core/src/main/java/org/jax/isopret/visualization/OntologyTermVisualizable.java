package org.jax.isopret.visualization;

import org.monarchinitiative.phenol.ontology.data.Term;

public class OntologyTermVisualizable {

    private final String termId;
    private final String termLabel;

    public OntologyTermVisualizable(Term term) {
        this.termId = term.id().getValue();
        this.termLabel = term.getName();
    }

    public String getTermId() {
        return termId;
    }

    public String getTermLabel() {
        return termLabel;
    }
}
