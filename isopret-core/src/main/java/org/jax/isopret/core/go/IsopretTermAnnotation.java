package org.jax.isopret.core.go;


import com.google.common.collect.ComparisonChain;
import org.monarchinitiative.phenol.ontology.data.TermAnnotation;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.Objects;
import java.util.Optional;

/**
 * Simple class to represent that a given GO Term {@link #goTermId} annotates a given gene or isoform {@link #accessionNumber}.
 * @author Peter N Robinson
 */
public class IsopretTermAnnotation implements TermAnnotation {
    private final TermId goTermId;
    private final TermId accessionNumber;

    public IsopretTermAnnotation(TermId accession, TermId goTermId) {
        this.accessionNumber = accession;
        this.goTermId = goTermId;
    }


    public TermId getTermId() {
        return goTermId;
    }

    @Override
    public TermId getItemId() {
        return accessionNumber;
    }

    @Override
    public Optional<String> getEvidenceCode() {
        return Optional.empty();
    }

    @Override
    public Optional<Float> getFrequency() {
        return Optional.empty();
    }

    @Override
    public int compareTo(TermAnnotation that) {
        return ComparisonChain.start()
                .compare(this.getTermId(), that.getTermId())
                .compare(this.getItemId(), that.getItemId())
                .result();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.goTermId, this.accessionNumber);
    }

    @Override
    public String toString() {
        return "IsopretTermAnnotation [" + accessionNumber.getValue()+ ": " + goTermId.getValue() + "]";
    }
}
