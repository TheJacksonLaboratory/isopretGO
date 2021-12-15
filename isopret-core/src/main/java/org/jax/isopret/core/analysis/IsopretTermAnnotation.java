package org.jax.isopret.core.analysis;


import com.google.common.collect.ComparisonChain;
import org.jax.isopret.core.transcript.AccessionNumber;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.ontology.data.TermAnnotation;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.Objects;

public class IsopretTermAnnotation implements TermAnnotation {
    private final TermId goTermId;
    private final TermId accessionNumber;

    public IsopretTermAnnotation(TermId accession, TermId goTermId) {
        this.accessionNumber = accession;
        this.goTermId = goTermId;
    }

    @Override
    public TermId getTermId() {
        return goTermId;
    }

    @Override
    public TermId getLabel() {
        return accessionNumber;
    }

    @Override
    public int compareTo(TermAnnotation other) {
        if (!(other instanceof IsopretTermAnnotation that)) {
            throw new PhenolRuntimeException("Cannot compare " + other + " to " + this);
        }
        return ComparisonChain.start()
                .compare(this.goTermId, that.goTermId)
                .compare(this.accessionNumber, that.accessionNumber)
                .result();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.goTermId, this.accessionNumber);
    }

    @Override
    public String toString() {
        return "IsopretTermAnnotation [termId=" + goTermId + ": " + accessionNumber.getValue() + "]";
    }
}
