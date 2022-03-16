package org.jax.isopret.core.analysis;

import org.jax.isopret.core.hbadeals.HbaDealsTranscriptResult;
import org.jax.isopret.core.interpro.InterproEntry;
import org.jax.isopret.core.model.AccessionNumber;
import org.jax.isopret.core.model.AnnotatedGene;
import org.monarchinitiative.phenol.analysis.stats.Hypergeometric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;

public class InterproFisherExact {
    private static final Logger LOGGER = LoggerFactory.getLogger(InterproFisherExact.class);

    private final Map<InterproEntry, Integer> studySetInterproCounts;
    private final Map<InterproEntry, Integer> populationSetInterproCounts;
    private final int populationSize;
    private final int studySize;
    private final Hypergeometric hypergeometric;
    private final static int MINIMUM_TERM_COUNT_TO_TEST = 2;
    private final Predicate<Integer> hasAtLeastMinCount = num -> num >= MINIMUM_TERM_COUNT_TO_TEST;

    /**
     *
     * @param annotatedGeneList list of all genes with at least one read in the experiment
     * @param splicingPepThreshold posterior error probability (PEP) threshold calculated for this experiment for splicing
     */
    public InterproFisherExact(List<AnnotatedGene> annotatedGeneList, double splicingPepThreshold) {
        populationSize = calculatePopulationSize(annotatedGeneList);
        studySize = calculateStudysetSize(annotatedGeneList, splicingPepThreshold);
        studySetInterproCounts = new HashMap<>();
        populationSetInterproCounts = new HashMap<>();
        hypergeometric = new Hypergeometric();
        int n_isoforms_with_interpro = 0;
        int n_isoforms_without_interpro = 0;

        for (var agene : annotatedGeneList) {
            //Map<AccessionNumber, List<DisplayInterproAnnotation>> m = agene.getTranscriptToInterproHitMap();
            Map<AccessionNumber, Set<InterproEntry>> uniqIntproSetMap = agene.getTranscriptToUniqueInterproMap();
            Set<HbaDealsTranscriptResult> results = agene.getHbaDealsResult().getTranscriptResults();
            for (var res : results) {
                AccessionNumber accession = res.getTranscriptId();
                if (! uniqIntproSetMap.containsKey(accession)) {
                    // not all isoforms have an interpro accession, so this is not an error.
                    n_isoforms_without_interpro++;
                    continue;
                }
                n_isoforms_with_interpro++;
                for (InterproEntry interproEntry : uniqIntproSetMap.get(accession)) {
                    populationSetInterproCounts.merge(interproEntry, 1, Integer::sum);
                    if (res.isSignificant(splicingPepThreshold)) {
                        studySetInterproCounts.merge(interproEntry, 1, Integer::sum);
                    }
                }
            }
        }
        LOGGER.info("Isoforms with interpro annotation: {}; without: {}\n",
                n_isoforms_with_interpro, n_isoforms_without_interpro);

    }

    private int calculateStudysetSize(List<AnnotatedGene> annotatedGeneList, double splicingPepThreshold) {
        return annotatedGeneList.stream()
                .mapToInt(agene -> agene.getHbaDealsResult().getSignificantTranscriptCount(splicingPepThreshold))
                .sum();
    }

    private int calculatePopulationSize(List<AnnotatedGene> annotatedGeneList) {
        return annotatedGeneList.stream()
                .mapToInt(agene -> agene.getHbaDealsResult().getExpressedTranscriptCount())
                .sum();
    }

    private int getNumberOfEffectiveTests() {
       return (int)this.studySetInterproCounts.values().stream().filter(hasAtLeastMinCount).count();
    }


    public List<InterproOverrepResult> calculateInterproOverrepresentation() {
        List<InterproOverrepResult> results = new ArrayList<>();
        int n_tests = getNumberOfEffectiveTests();
        for (InterproEntry interproEntry : this.studySetInterproCounts.keySet()) {
            int populationAnnotated = this.populationSetInterproCounts.get(interproEntry);
            int studyAnnotated = this.studySetInterproCounts.getOrDefault(interproEntry, 0);
            if (studyAnnotated < MINIMUM_TERM_COUNT_TO_TEST) {
                continue;
            }
            double raw_pval = hypergeometric.phypergeometric(populationSize,
                   populationAnnotated,
                    studySize,
                    studyAnnotated);
            double bonferroni_pval = Math.min(1.0,raw_pval * n_tests);
            InterproOverrepResult ipresult = new InterproOverrepResult(interproEntry, populationSize, populationAnnotated,
                    studySize, studyAnnotated, raw_pval, bonferroni_pval);
            results.add(ipresult);
        }
        return results;
    }


}
