package org.jax.isopret.core.go;

import org.monarchinitiative.phenol.analysis.ItemAnnotations;
import org.monarchinitiative.phenol.ontology.data.TermAnnotation;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IsopretGeneAnnotations implements ItemAnnotations<TermId> {


    /** TermId of the item (e.g., gene) for which this object stores 0 - n Associations (e.g., GO associations). */
    private final TermId annotatedGene;

    /** List of annotations (associations of the annotatedItem with Ontology Terms. */
    private final List<TermAnnotation> annotations;

    IsopretGeneAnnotations(TermId geneId) {
        this.annotatedGene = geneId;
        annotations = new ArrayList<>();
    }


    public void addAnnotation(TermAnnotation a) {
        this.annotations.add(a);
    }

    @Override
    public TermId annotatedItem() {
        return this.annotatedGene;
    }

    @Override
    public List<TermId> getAnnotatingTermIds() {
        return this.annotations.stream().map(TermAnnotation::getTermId).collect(Collectors.toList());
    }

    @Override
    public List<TermAnnotation> getAnnotations() {
        return this.annotations;
    }

    @Override
    public boolean containsAnnotation(TermId tid) {
        return annotations.stream().anyMatch(a -> a.getTermId().equals(tid));
    }
}
