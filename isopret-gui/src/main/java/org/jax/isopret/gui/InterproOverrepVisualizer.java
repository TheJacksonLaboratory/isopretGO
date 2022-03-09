package org.jax.isopret.gui;

import org.jax.isopret.gui.service.InterproOverrepResult;

import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

public class InterproOverrepVisualizer {

    private final List<InterproOverrepResult> results;


    public InterproOverrepVisualizer(List<InterproOverrepResult> results) {
        Collections.sort(results);
        this.results = results;
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
        for (InterproOverrepResult res: results) {
            double raw_p = res.rawPval();
            double corrected_p = Math.min(1.0, N*raw_p);
            writer.write(String.format("%s\t%s\t%d\t%d\t%d\t%d\t%e\t%e\n",
                    res.interproAccession(),
                    res.interproDescription(),
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
