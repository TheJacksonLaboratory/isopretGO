package org.jax.isopret.core.go;

import org.jax.isopret.core.hbadeals.HbaDealsThresholder;
import org.jax.isopret.core.transcript.AccessionNumber;
import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.analysis.DirectAndIndirectTermAnnotations;
import org.monarchinitiative.phenol.analysis.StudySet;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.GoTerm2PValAndCounts;
import org.monarchinitiative.phenol.stats.TermForTermPValueCalculation;
import org.monarchinitiative.phenol.stats.mtc.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class HbaDealsGoContainer {

    private final static double ALPHA = 0.05;

    private final Ontology ontology;
    private final AssociationContainer associationContainer;
    private final GoMethod goMethod;
    private final MultipleTestingCorrection mtc;
    private final HbaDealsThresholder thresholder;


    public HbaDealsGoContainer(Ontology ontology,
                               HbaDealsThresholder thresholder,
                               org.monarchinitiative.phenol.analysis.AssociationContainer acontainer,
                               GoMethod goMethod,
                               MtcMethod mtcMethod) {
        this.ontology = ontology;
        this.associationContainer = acontainer;
        this.goMethod = goMethod;
        switch (mtcMethod) {
            case BONFERRONI -> this.mtc = new Bonferroni();
            case BONFERRONI_HOLM -> this.mtc = new BonferroniHolm();
            case BENJAMINI_HOCHBERG -> this.mtc = new BenjaminiHochberg();
            case BENJAMINI_YEKUTIELI -> this.mtc = new BenjaminiYekutieli();
            case SIDAK -> this.mtc = new Sidak();
            case NONE -> this.mtc = new NoMultipleTestingCorrection();
            default -> {
                // should never happen
                System.err.println("[WARNING] Did not recognize MTC");
                this.mtc = new Bonferroni();
            }
        }
        this.thresholder = thresholder;
    }


    public List<GoTerm2PValAndCounts> termForTermDge() {
        Set<TermId> studyIds = this.thresholder.dgeGeneTermIds();
        Map<TermId, DirectAndIndirectTermAnnotations> annMap = associationContainer.getAssociationMap(studyIds);
        StudySet study = new StudySet(studyIds, "dge-study", annMap);
        Set<TermId> popIds = this.thresholder.getAllGeneTermIds();
        Map<TermId, DirectAndIndirectTermAnnotations> popMap = associationContainer.getAssociationMap(popIds);
        StudySet population = new StudySet(popIds, "population", popMap);

        TermForTermPValueCalculation tftpvalcal = new TermForTermPValueCalculation(this.ontology,
                population,
                study,
                new Bonferroni());
        return tftpvalcal.calculatePVals()
                .stream()
                .filter(item -> item.passesThreshold(ALPHA))
                .collect(Collectors.toList());
    }


    public List<GoTerm2PValAndCounts> termForTermDas() {
        Set<TermId> studyIds = this.thresholder.dasIsoformTermIds();
        Map<TermId, DirectAndIndirectTermAnnotations> annMap = associationContainer.getAssociationMap(studyIds);
        StudySet study = new StudySet(studyIds, "das-study", annMap);
        Set<TermId> popIds = this.thresholder.getAllTranscriptTermIds();
        Map<TermId, DirectAndIndirectTermAnnotations> popMap = associationContainer.getAssociationMap(popIds);
        StudySet population = new StudySet(popIds, "population", popMap);

        TermForTermPValueCalculation tftpvalcal = new TermForTermPValueCalculation(this.ontology,
                population,
                study,
                new Bonferroni());
        return tftpvalcal.calculatePVals()
                .stream()
                .filter(item -> item.passesThreshold(ALPHA))
                .collect(Collectors.toList());
    }
}
