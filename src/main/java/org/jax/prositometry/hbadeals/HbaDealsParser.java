package org.jax.prositometry.hbadeals;

import org.jax.prositometry.except.PrositometryRuntimeException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

    public HbaDealsParser(String fname) {
        this(fname, DEFAULT_PROB_THRESHOLD);
    }

    public HbaDealsParser(String fname, double thresh) {
        hbadealsFile = fname;
        threshold = thresh;
        this.hbaDealsResultMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(this.hbadealsFile))) {
            String line;
            br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                //System.out.println(line);
                String [] fields = line.split("\t");
                if (fields.length != 4) {
                    System.err.printf("[ERROR] Malformed line with %d fields: %s\n", fields.length, line);
                    continue;
                }
                String sym = fields[0];
                String isoform = fields[1];
                double expFC = Double.parseDouble(fields[2]);
                double P = Double.parseDouble(fields[3]);
                this.hbaDealsResultMap.putIfAbsent(sym, new HbaDealsResult(sym));
                HbaDealsResult hbaresult = this.hbaDealsResultMap.get(sym);
                if (isoform.equalsIgnoreCase("Expression")) {
                    hbaresult.addExpressionResult(expFC, P);
                } else {
                    hbaresult.addTranscriptResult(isoform, expFC, P);
                }
            }
        } catch (IOException e) {
            throw new PrositometryRuntimeException("Could not read HBA-DEALS file: " + e.getMessage());
        }
        System.out.printf("[INFO] We got %d genes with HBA DEALS results\n", hbaDealsResultMap.size());
    }

    public Map<String, HbaDealsResult> getHbaDealsResultMap() {
        return hbaDealsResultMap;
    }
}
