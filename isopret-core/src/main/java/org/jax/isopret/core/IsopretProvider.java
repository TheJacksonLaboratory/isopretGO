package org.jax.isopret.core;

import org.jax.isopret.core.impl.DefaultIsopretProvider;
import org.jax.isopret.model.AccessionNumber;
import org.jax.isopret.model.GeneModel;
import org.jax.isopret.model.GeneSymbolAccession;
import org.jax.isopret.model.Transcript;
import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * a class to centralize the building and provision of data objects
 * required for the analysis.
 */
public interface IsopretProvider {

    Ontology geneOntology();

    Map<GeneSymbolAccession, List<Transcript>> geneSymbolToTranscriptListMap();

    Map<AccessionNumber, GeneModel> ensemblGeneModelMap();

    Map<TermId, Set<TermId>> transcriptIdToGoTermsMap();

    Map<TermId, Set<TermId>> gene2GoMap();

    Map<TermId, TermId> transcriptToGeneIdMap();

    AssociationContainer<TermId> transcriptContainer();

    AssociationContainer<TermId> geneContainer();

    static IsopretProvider provider(Path directory ) {
        return new DefaultIsopretProvider(directory);
    }


}
