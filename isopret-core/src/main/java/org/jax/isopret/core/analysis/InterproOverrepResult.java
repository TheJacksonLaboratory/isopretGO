package org.jax.isopret.core.analysis;

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
        String perc = getPercentage( studyAnnotated, studyTotal);
        return String.format("%d/%d (%s)", studyAnnotated, studyTotal, perc);
    }


    public String getPopulationCounts() {
        String perc = getPercentage( populationAnnotated, populationTotal);
        return String.format("%d/%d (%s)", populationAnnotated, populationTotal, perc);
    }


    private String getPercentage(int numerator, int denominator) {
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
