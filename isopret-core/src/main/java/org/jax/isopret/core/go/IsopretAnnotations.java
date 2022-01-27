package org.jax.isopret.core.go;

import org.monarchinitiative.phenol.analysis.ItemAnnotations;
import org.monarchinitiative.phenol.ontology.data.TermAnnotation;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public List<TermId> getAnnotatingTermIds() {
        return this.annotations.stream().map(TermAnnotation::getItemId).collect(Collectors.toList());
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
