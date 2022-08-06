package org.jax.isopret.core.impl.go;

import org.monarchinitiative.phenol.analysis.ItemAnnotations;
import org.monarchinitiative.phenol.ontology.data.TermAnnotation;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.ArrayList;
import java.util.List;

public class IsopretAnnotations implements ItemAnnotations<TermId> {
    /** TermId of the item (e.g., gene or transcript) for which this object stores 0 - n Associations (e.g., GO associations). */
    private final TermId annotatedItem;

    /** List of annotations (associations of the annotatedItem with Ontology Terms). */
    private final List<TermAnnotation> annotations;

    public IsopretAnnotations(TermId itemAccessionId, List<TermAnnotation> termAnnots) {
        this.annotatedItem = itemAccessionId;
        this.annotations = termAnnots;
    }


    @Override
    public TermId annotatedItem() {
        return this.annotatedItem;
    }

    @Override
    public List<TermAnnotation> getAnnotations() {
        return this.annotations;
    }

    // TODO  -- cast needed with new phenol API, but will be refactored!
    @Override
    public List<TermId> getAnnotatingTermIds() {
        List<TermId> goTermIds = new ArrayList<>();
        for (TermAnnotation tannot : this.annotations) {
            IsopretTermAnnotation ita = (IsopretTermAnnotation) tannot;
            goTermIds.add(ita.getTermId());
        }
        return goTermIds;
    }

    @Override
    public boolean containsAnnotation(TermId tid) {
        return annotations.stream().anyMatch(a -> a.getItemId().equals(tid));
    }

    @Override
    public int getAnnotationCount() {
        return annotations.size();
    }
}
