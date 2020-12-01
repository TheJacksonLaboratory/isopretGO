package org.jax.isopret.hbadeals;

import org.jax.isopret.except.PrositometryRuntimeException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Parse the HBA-DEALS output file, e.g.,
 * Gene	Isoform	ExplogFC/FC	P
 * AC016831.6	ENST00000659385	1.04449971508812	0.61019
 * POLR2J3	Expression	0.301337111501297	0.10708
 * POLR2J3	ENST00000504157	1.01818551579155	0.73892
 * POLR2J3	ENST00000511313	0.998158250064647	0.74954
 */
public class HbaDealsParser {
    /**
     * Path to an output file from HBA-DEALS
     */
    private final String hbadealsFile;

    private final static double DEFAULT_PROB_THRESHOLD = 0.9;

    private final double threshold;

    private final Map<String, HbaDealsResult> hbaDealsResultMap;

    private final List<Double> uncorrectedPvals;

    public HbaDealsParser(String fname) {
        this(fname, DEFAULT_PROB_THRESHOLD);
    }

    /**
     * The following is not an efficient way of doing things, but it works for now
     */
    public Map<Double, Double> calculateBenjaminiHochbergMTC() {
        Map<Double, Double> p2correctedP = new HashMap<>();
        Collections.sort(this.uncorrectedPvals, Collections.reverseOrder());
        int N=this.uncorrectedPvals.size();
        for (int r = 0;r<N;r++) {
            double raw_p = this.uncorrectedPvals.get(r);
            double corrected = raw_p * N/(r+1);
            p2correctedP.put(raw_p, corrected);
        }
        return p2correctedP;
    }



    public HbaDealsParser(String fname, double thresh) {
        hbadealsFile = fname;
        threshold = thresh;
        this.hbaDealsResultMap = new HashMap<>();
        this.uncorrectedPvals = new ArrayList<>();
        int n_lines = 0;
        List<HbaLine> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(this.hbadealsFile))) {
            String line;
            br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                //System.out.println(line);
                HbaLine hline = new HbaLine(line);
                lines.add(hline);
                n_lines++;
            }
        } catch (IOException e) {
            throw new PrositometryRuntimeException("Could not read HBA-DEALS file: " + e.getMessage());
        }

        Collections.sort(lines);
        // Benjamini Hochberg
        int N=lines.size();
        for (int r = 0;r<N;r++) {
            HbaLine hline = lines.get(r);
            double raw_p = hline.raw_p;
            hline.corrected_p = Math.min(1.0, raw_p * N/(r+1));
            this.hbaDealsResultMap.putIfAbsent(hline.symbol, new HbaDealsResult(hline.symbol));
            HbaDealsResult hbaresult = this.hbaDealsResultMap.get(hline.symbol);
            if (hline.isIsoform) {
                hbaresult.addTranscriptResult(hline.isoform, hline.expFC, hline.raw_p, hline.corrected_p);
            } else {
                hbaresult.addExpressionResult(hline.expFC, hline.raw_p, hline.corrected_p);
            }
        }



        System.out.printf("[INFO] We parsed %d lines from %s.\n", n_lines, this.hbadealsFile);
        System.out.printf("[INFO] We got %d genes with HBA DEALS results\n", hbaDealsResultMap.size());
    }

    public Map<String, HbaDealsResult> getHbaDealsResultMap() {
        return hbaDealsResultMap;
    }

    /**
     * A convenience class that will let us calculate the Benjamini Hochberg p values
     * To do so, we sort these lines by raw p value
     */
    static class HbaLine implements Comparable<HbaLine> {
        final static String UNINITIALIZED = "";
        final String symbol;
        final boolean isIsoform;
        final String isoform;
        final double expFC;
        final double raw_p;
        double corrected_p;

        public HbaLine(String line) {
            String [] fields = line.split("\t");
            if (fields.length != 4) {
                String msg = String.format("[ERROR] Malformed line with %d fields: %s\n", fields.length, line);
                throw new PrositometryRuntimeException(msg);
            }
            symbol = fields[0];
            if (fields[1].equalsIgnoreCase("Expression")) {
                isIsoform = false;
                isoform = UNINITIALIZED; // gene expression entry
            } else {
                isIsoform = true;
                isoform = fields[1];
            }
            this.expFC = Double.parseDouble(fields[2]);
            double q = Double.parseDouble(fields[3]);
            this.raw_p = q;
        }

        /** sort in reverse order, i.e., with lowest values first */
        @Override
        public int compareTo(HbaLine that) {
            return Double.compare(this.raw_p, that.raw_p);
        }
    }

}
