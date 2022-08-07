package org.jax.isopret.visualization;

import org.jax.isopret.core.InterproAnalysisResults;
import org.jax.isopret.core.analysis.InterproOverrepResult;

import java.io.StringWriter;

public class InterproOverrepVisualizer {

    private final InterproAnalysisResults results;


    public InterproOverrepVisualizer(InterproAnalysisResults overrepresults) {
        this.results = overrepresults;
    }


    public String header() {
        String [] fields = {"Interpro.id", "Interpro.description", "population.annotated", "population.total",
                "study.annotated", "study.total", "p.raw", "p.bonferroni"};

        return String.join("\t", fields);
    }

    public String getTsv() {
        int N = results.size();
        StringWriter writer = new StringWriter();
        writer.write(header() + "\n");
        for (InterproOverrepResult res: results.results()) {
            double raw_p = res.rawPval();
            double corrected_p = Math.min(1.0, N*raw_p);
            writer.write(String.format("%s\t%s\t%d\t%d\t%d\t%d\t%e\t%e\n",
                    res.interproEntry().getIntroproAccession(),
                    res.interproEntry().getDescription(),
                    res.populationAnnotated(),
                    res.populationTotal(),
                    res.studyAnnotated(),
                    res.studyTotal(),
                    raw_p,
                    corrected_p));
        }
        return writer.toString();
    }
}
