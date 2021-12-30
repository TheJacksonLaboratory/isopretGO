package org.jax.isopret.core.visualization;


import org.jax.isopret.core.interpro.DisplayInterproAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This class arranges output items to prepare for display in the JavaFx GUI
 */
public class FxVisualizer {
    Logger LOGGER = LoggerFactory.getLogger(FxVisualizer.class);


    private final String geneSymbol;
    private final String geneAccession;
    private final String geneUrl;
    private final String isoformSvg;
    private final String proteinSvg;


    /** Number of all annotated transcripts of some gene */
    private final int totalTranscriptCount;


    private final String chromosome;



    private final List<IsoformVisualizable> isoformList;

    private final List<DisplayInterproAnnotation> interproData;

    private final List<OntologyTermVisualizable> goterms;

    private final boolean differentiallyExpressed;

    private final boolean differentiallySpliced;



    public FxVisualizer(Visualizable vis) {
        this.geneSymbol = vis.getGeneSymbol();
        this.geneAccession = vis.getGeneAccession();
        this.geneUrl = vis.getGeneUrl();
        this.chromosome = vis.getChromosome();
        this.isoformSvg = vis.getIsoformSvg();
        this.proteinSvg = vis.getProteinSvg();
        this.goterms = vis.getGoTerms();
        this.differentiallyExpressed = vis.isDifferentiallyExpressed();
        this.differentiallySpliced = vis.isDifferentiallySpliced();
        this.totalTranscriptCount = vis.getTotalTranscriptCount();
        this.interproData = vis.getInterproForExpressedTranscripts();

        isoformList = List.of(); // TODO
    }


}
