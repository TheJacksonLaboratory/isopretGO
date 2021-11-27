package org.jax.isopret.core.analysis;


import com.google.common.collect.ComparisonChain;
import org.jax.isopret.core.transcript.AccessionNumber;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.ontology.data.TermAnnotation;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.Objects;

public class IsopretTermAnnotation implements TermAnnotation {

    private static final long serialVersionUID = 1L;

    private final TermId goTermId;
    private final AccessionNumber accessionNumber;

    public IsopretTermAnnotation(AccessionNumber accession, TermId goTermId) {
        this.accessionNumber = accession;
        this.goTermId = goTermId;
    }

    @Override
    public TermId getTermId() {
        return goTermId;
    }

    @Override
    public TermId getLabel() {
        return TermId.of(String.format("ENST:%011d", accessionNumber.getAccessionNumber()));
    }

    @Override
    public int compareTo(TermAnnotation o) {
        if (!(o instanceof IsopretTermAnnotation that)) {
            throw new PhenolRuntimeException("Cannot compare " + o + " to " + this);
        }
        return ComparisonChain.start()
                .compare(this.goTermId, that.goTermId)
                .compare(this.accessionNumber.getAccessionNumber(), that.accessionNumber.getAccessionNumber())
                .result();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.goTermId, this.accessionNumber);
    }

    @Override
    public String toString() {
        return "IsopretTermAnnotation [termId=" + goTermId + ": " + accessionNumber.getAccessionString() + "]";
    }
}
