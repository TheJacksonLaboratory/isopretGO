package org.jax.isopret.hbadeals;

import org.jax.isopret.except.IsopretRuntimeException;
import org.jax.isopret.hgnc.HgncItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Parse the HBA-DEALS output file, e.g.,
 * Gene	Isoform	ExplogFC/FC	P
 * ENSG00000160710	Expression	1.54770825394965	0
 * ENSG00000160710	ENST00000368471	0.563281823470453	1e-05
 * ENSG00000160710	ENST00000368474	1.45668870537677	0.00192
 * ENSG00000160710	ENST00000463920	0.84541220998081	0.7134
 * ENSG00000160710	ENST00000529168	1.05034162415497	0.9602
 * ENSG00000160710	ENST00000649021	0.833370141719852	0.66569
 */
public class HbaDealsParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(HbaDealsParser.class);
    /**
     * Path to an output file from HBA-DEALS
     */
    private final String hbadealsFile;

    /** Key -- a gene symbol, value -- an {@link HbaDealsResult} object with results for gene expression and splicing.*/
    private final Map<String, HbaDealsResult> hbaDealsResultMap;


    /**
     * Sanity check that the header is correct.
     * @param header Header of the HBA-DEALS file
     */
    public void checkHeader(String header) {
        String [] headerFields = {"Gene",  "Isoform", "ExplogFC/FC","P"};
        String [] fields = header.split("\t");
        if (headerFields.length != fields.length) {
            throw new IsopretRuntimeException("Malformed HBADEALS header line (Should have 4 fields but had "
                    + fields.length + "): " + header);
        }
        for (int i=0; i<headerFields.length; i++) {
            if (! headerFields[i].equals(fields[i])) {
                throw new IsopretRuntimeException("HBADEALS header field " +i+ " malformed. We expected \"" +
                        headerFields[i] + "\" but got \"" + fields[i] + "\" (" + header +")");
            }
        }
    }

    /**
     *
     * @param fname Path to an HBA-DEALS output file
     * @param hgncMap Map from
     */
    public HbaDealsParser(String fname, Map<String, HgncItem> hgncMap) {
        hbadealsFile = fname;
        this.hbaDealsResultMap = new HashMap<>();
        int n_lines = 0;
        List<HbaLine> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(this.hbadealsFile))) {
            String line;
            checkHeader(br.readLine()); // skip header
            while ((line = br.readLine()) != null) {
                //System.out.println(line);
                HbaLine hline = new HbaLine(line);
                lines.add(hline);
                n_lines++;
            }
        } catch (IOException e) {
            throw new IsopretRuntimeException("Could not read HBA-DEALS file: " + e.getMessage());
        }
        int found_symbol = 0;
        int missed_symbol = 0;
        for (HbaLine hline : lines) {
            String symbol = hline.geneAccession; // if we cannot find symbol, just show the accession
            if (hgncMap.containsKey(hline.geneAccession)) {
                symbol = hgncMap.get(hline.geneAccession).getGeneSymbol();
                found_symbol++;
            } else {
                LOGGER.warn("Could not find symbol for " + hline.geneAccession);
                missed_symbol++;
            }
            this.hbaDealsResultMap.putIfAbsent(symbol, new HbaDealsResult(hline.geneAccession, symbol));
            HbaDealsResult hbaresult = this.hbaDealsResultMap.get(symbol);
            if (hline.isIsoform) {
                hbaresult.addTranscriptResult(hline.isoform, hline.expFC, hline.raw_p);
            } else {
                hbaresult.addExpressionResult(hline.expFC, hline.raw_p);
            }
        }
        if (missed_symbol > found_symbol) {
            LOGGER.error("We could not map {} accession numbers and could map {} accession numbers", missed_symbol, found_symbol);
            throw new IsopretRuntimeException("Could not map most accession numbers/identifiers. Terminating program because this will invalidate downstream analysis.");
        }
        LOGGER.trace("We found gene symbols {} times and missed it {} times.\n", found_symbol, missed_symbol);
        LOGGER.trace("We parsed {} lines from {}.\n", n_lines, this.hbadealsFile);
        LOGGER.trace("We got {} genes with HBA DEALS results\n", hbaDealsResultMap.size());
    }

    public Map<String, HbaDealsResult> getHbaDealsResultMap() {
        return hbaDealsResultMap;
    }

    /**
     * A convenience class that will let us calculate the Benjamini Hochberg p values
     * To do so, we sort these lines by raw p value
     */
    static class HbaLine {
        final static String UNINITIALIZED = "";
        final String geneAccession;
        final boolean isIsoform;
        final String isoform;
        final double expFC;
        final double raw_p;

        public HbaLine(String line) {
            String [] fields = line.split("\t");
            if (fields.length != 4) {
                String msg = String.format("[ERROR] Malformed line with %d fields: %s\n", fields.length, line);
                throw new IsopretRuntimeException(msg);
            }
            geneAccession = fields[0];
            if (fields[1].equalsIgnoreCase("Expression")) {
                isIsoform = false;
                isoform = UNINITIALIZED; // gene expression entry
            } else {
                isIsoform = true;
                isoform = fields[1];
            }
            this.expFC = Double.parseDouble(fields[2]);
            this.raw_p = Double.parseDouble(fields[3]);
        }
    }

}
