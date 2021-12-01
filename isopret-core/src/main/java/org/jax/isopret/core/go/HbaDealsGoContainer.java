package org.jax.isopret.core.go;

import org.jax.isopret.core.except.IsopretRuntimeException;
import org.jax.isopret.core.hbadeals.HbaDealsThresholder;
import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.analysis.GoAssociationContainer;
import org.monarchinitiative.phenol.analysis.StudySet;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.stats.GoTerm2PValAndCounts;
import org.monarchinitiative.phenol.stats.ParentChildIntersectionPValueCalculation;
import org.monarchinitiative.phenol.stats.ParentChildUnionPValueCalculation;
import org.monarchinitiative.phenol.stats.TermForTermPValueCalculation;
import org.monarchinitiative.phenol.stats.mtc.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class HbaDealsGoContainer {

    private final static double ALPHA = 0.05;

    private final Ontology ontology;
    private final AssociationContainer associationContainer;
    private final GoMethod goMethod;
    private final MultipleTestingCorrection mtc;


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

        Set<String> dasGeneSymbols = thresholder.dasGeneSymbols();
    }


//    public List<GoTerm2PValAndCounts> termForTermDge() {
//        StudySet study = new
//    }



}
