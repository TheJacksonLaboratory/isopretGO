package org.jax.isopret.core.analysis;

import org.jax.isopret.core.hbadeals.HbaDealsTranscriptResult;
import org.jax.isopret.core.interpro.DisplayInterproAnnotation;
import org.jax.isopret.core.interpro.InterproEntry;
import org.jax.isopret.core.transcript.AccessionNumber;
import org.jax.isopret.core.transcript.AnnotatedGene;
import org.monarchinitiative.phenol.analysis.stats.Hypergeometric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;

public class InterproFisherExact {
    private static final Logger LOGGER = LoggerFactory.getLogger(InterproFisherExact.class);

    private final Map<Integer, Integer> studySetInterproCounts;
    private final Map<Integer, Integer> populationSetInterproCounts;
    private final Map<Integer, DisplayInterproAnnotation> interproIdToDisplay;
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
        interproIdToDisplay = new HashMap<>();
        hypergeometric = new Hypergeometric();
        for (var agene : annotatedGeneList) {
            Map<AccessionNumber, List<DisplayInterproAnnotation>> m = agene.getTranscriptToInterproHitMap();
            for (var dialist : m.values()) {
                for (var dia : dialist) {
                    int interpoId = dia.getInterproEntry().getId();
                    populationSetInterproCounts.merge(interpoId, 1, Integer::sum); // increment, start at 1 if was absent
                    interproIdToDisplay.putIfAbsent(interpoId, dia);
                }
            }
            Set<HbaDealsTranscriptResult> results = agene.getHbaDealsResult().getTranscriptResults();
            for (var res : results) {
                if (res.isSignificant(splicingPepThreshold)) {
                    AccessionNumber accession = res.getTranscriptId();
                    List<DisplayInterproAnnotation> dialist = m.get(accession);
                    if (dialist == null) {
                        LOGGER.warn("Could not find annotations for {}", accession);
                        continue;
                    }
                    for (var dia : dialist) {
                        int interpoId = dia.getInterproEntry().getId();
                        studySetInterproCounts.merge(interpoId, 1, Integer::sum); // increment, start at 1 if was absent
                    }
                }
            }
        }
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
        for (int interproId : this.interproIdToDisplay.keySet()) {
            DisplayInterproAnnotation display = this.interproIdToDisplay.get(interproId);
            int populationAnnotated = this.populationSetInterproCounts.get(interproId);
            int studyAnnotated = this.studySetInterproCounts.getOrDefault(interproId, 0);
            if (studyAnnotated < MINIMUM_TERM_COUNT_TO_TEST) {
                continue;
            }
            double raw_pval = hypergeometric.phypergeometric(populationSize,
                   populationAnnotated,
                    studySize,
                    studyAnnotated);
            double bonferroni_pval = Math.min(1.0,raw_pval * n_tests);

            InterproEntry interproEntry = display.getInterproEntry();
            InterproOverrepResult ipresult = new InterproOverrepResult(interproEntry, populationSize, populationAnnotated,
                    studySize, studyAnnotated, raw_pval, bonferroni_pval);
            results.add(ipresult);
        }
        return results;
    }


}
