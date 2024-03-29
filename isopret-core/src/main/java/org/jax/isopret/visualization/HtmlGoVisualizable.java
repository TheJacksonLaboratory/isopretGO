package org.jax.isopret.visualization;

import org.jax.isopret.exception.IsopretRuntimeException;
import org.monarchinitiative.phenol.analysis.stats.GoTerm2PValAndCounts;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.Optional;

public class HtmlGoVisualizable implements GoVisualizable {

    private final String termId;
    private final String termLabel;
    private final int studyCount;
    private final int studyTotal;
    private final int populationCount;
    private final int populationTotal;
    private final double pvalue;
    private final double adjpvalue;

    public HtmlGoVisualizable(GoTerm2PValAndCounts gt2pc, Ontology ontology) {
        TermId goId = gt2pc.getItem();
        Optional<String> opt = ontology.getTermLabel(goId);
        if (opt.isPresent()) {
            this.termLabel = opt.get();
        } else {
            throw new IsopretRuntimeException("Could not find label for " + goId.getValue());
        }
        this.termId = goId.getValue();
        this.studyCount = gt2pc.getAnnotatedStudyGenes();
        this.studyTotal = gt2pc.getTotalStudyGenes();
        this.populationCount = gt2pc.getAnnotatedPopulationGenes();
        this.populationTotal = gt2pc.getTotalPopulationGenes();
        this.pvalue = gt2pc.getRawPValue();
        this.adjpvalue = gt2pc.getAdjustedPValue();
    }


    @Override
    public String termLabel() {
        return this.termLabel;
    }

    @Override
    public String termId() {
        return this.termId;
    }

    @Override
    public int studyCount() {
        return this.studyCount;
    }

    @Override
    public int studyTotal() {
        return this.studyTotal;
    }

    @Override
    public int populationCount() {
        return this.populationCount;
    }

    @Override
    public int populationTotal() {
        return this.populationTotal;
    }

    @Override
    public double pvalue() {
        return this.pvalue;
    }

    @Override
    public double adjustedPvalue() {
        return this.adjpvalue;
    }
}
