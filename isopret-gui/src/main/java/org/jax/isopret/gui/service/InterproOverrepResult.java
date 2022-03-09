package org.jax.isopret.gui.service;

import java.util.Formatter;

public record InterproOverrepResult(String interproAccession,
                                    String interproDescription,
                                    int populationTotal,
                                    int populationAnnotated,
                                    int studyTotal,
                                    int studyAnnotated,
                                    double rawPval,
                                    double bonferroniPval) implements Comparable<InterproOverrepResult> {



    public String getStudyCounts() {
        return String.format("%d/%d", studyAnnotated, studyTotal);
    }

    public String getStudyPercentage() {
        return getPerecentage( studyAnnotated, studyTotal);
    }

    public String getPopulationCounts() {
        return String.format("%d/%d", populationAnnotated, populationTotal);
    }

    public String getPopulationPercentage() {
        return getPerecentage( populationAnnotated, populationTotal);
    }

    private String getPerecentage(int numerator, int denominator) {
        if (denominator == 0.0) {
            return "0%";
        } else {
            double p = numerator/(double) denominator;
            return String.format("%.2f%%", 100.0*p);
        }
    }

    public String getRawP() {
        return scientificNotation(rawPval);
    }

    public String getBonferroniP() {
        return scientificNotation(bonferroniPval);
    }

    public String scientificNotation(double pval) {
        Formatter fmt = new Formatter();
        if (pval > 0.01) return String.format("%.3f", pval);
        else if (pval > 0.001) return String.format("%.4f", pval);
        else if (pval == 0.0) return "0";
        else return fmt.format("%16.2e",pval).toString();
    }



    @Override
    public int compareTo(InterproOverrepResult that) {
        return Double.compare(this.rawPval, that.rawPval);
    }
}
