package org.jax.isopret.visualization;

import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.List;

public class GoAnnotationRow implements Comparable<GoAnnotationRow> {
    private final TermId goId;
    private final String goLabel;
    private final boolean goTermSignificant;
    private final List<Boolean> transcriptAnnotated;


    public GoAnnotationRow(TermId goId, String label, boolean significant, List<Boolean> transcriptAnnotated) {
        this.goId = goId;
        this.goLabel = label;
        this.goTermSignificant = significant;
        this.transcriptAnnotated = transcriptAnnotated;
    }

    public TermId getGoId() {
        return goId;
    }

    public String getGoLabel() {
        return goLabel;
    }

    public boolean isGoTermSignificant() {
        return goTermSignificant;
    }

    public List<Boolean> getTranscriptAnnotated() {
        return transcriptAnnotated;
    }


    /**
     * Sort first according to significance and then alphabetically.
    */
    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(GoAnnotationRow that) {
        return this.goTermSignificant && ! that.goTermSignificant ? -1 :
                that.goTermSignificant && ! this.goTermSignificant ? 1 :
                        this.goLabel.compareTo(that.goLabel);
    }
}
