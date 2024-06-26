package org.jax.isopret.core.impl.rnaseqdata;

import org.jax.isopret.exception.IsopretRuntimeException;
import org.jax.isopret.model.GeneModel;
import org.jax.isopret.data.AccessionNumber;
import org.jax.isopret.model.GeneResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * Parse the HBA-DEALS output file.
 * The HBA-DEALS output file contains 4 columns. The first column is the gene name, the second is the transcript name, the third is the fold change,
 * and the fourth is 1-probability of differential expression or proportion(splicing), which is the posterior error probability (PEP).
 * Entries that refer to expression have ‘Expression’ in their second column. If isoform.level is FALSE, entries that refer to differential
 * splicing of the gene will have ‘Splicing’ in their second column entry.
 * The fold change for expression is given as log2 fold change, and for splicing as fold change.
 * <p>
 * Gene	Isoform	ExplogFC/FC	P
 * ENSG00000160710	Expression	1.54770825394965	0
 * ENSG00000160710	ENST00000368471	0.563281823470453	1e-05
 * (...)
 * </p>
 * or alternatively from the edgeR results file.
 * <p>
 * Gene	Isoform	ExplogFC/FC	BH
 * ENSG00000000971	ENST00000367429	1.52835998738455	0.0640936190681988
 * ENSG00000000971	ENST00000630130	2.66993070863875	0.326994012740595
 * ENSG00000000971	Expression	-0.379897497979925	1    
 * (...)
 * </p>
 * The meaning of the fourth column is different but the format of the files is otherwise
 * identical.
 */
public class RnaSeqResultsParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(RnaSeqResultsParser.class);
    /** The expected header line for HBA-DEALS results. */
    private static final String HBADEALS_HEADER = 
            String.join("\t", List.of("Gene",  "Isoform", "ExplogFC/FC","P"));
    /** The expected header line for edgeR results. */
    private static final String EDGER_HEADER = 
            String.join("\t", List.of("Gene",  "Isoform", "ExplogFC/FC","BH"));
    /**
     * Path to an output file from HBA-DEALS
     */
    private final File hbadealsFile;

    /** Key -- an ENSG {@link AccessionNumber}, value -- an {@link GeneResultImpl} object with results for gene expression and splicing.*/
    private final Map<AccessionNumber, GeneResult> ensgAcc2geneResultMap;

    private final  Map<AccessionNumber, GeneModel> hgncMap;

    
    private RnaSeqResultsParser(File fname, Map<AccessionNumber, GeneModel> hgncMap, boolean isHbaDeals) {
        hbadealsFile = fname;
        this.hgncMap = hgncMap;

        try (BufferedReader br = new BufferedReader(new FileReader(this.hbadealsFile))) {
            String header = br.readLine();
            if (isHbaDeals) {
                if (! header.equals(HBADEALS_HEADER)) {
                    throw new IsopretRuntimeException("HBADEALS header field malformed. We expected \"" +
                            HBADEALS_HEADER+ "\" but got \"" +  header +")");
                }
            } else {
                if (! header.equals(EDGER_HEADER)) {
                    throw new IsopretRuntimeException("edgeR header field malformed. We expected \"" +
                            EDGER_HEADER+ "\" but got \"" +  header +")");
                }
            }
           this.ensgAcc2geneResultMap = parseResults(br);

        } catch (IOException e) {
            throw new IsopretRuntimeException("Error reading RNA-seq results data: " + e.getMessage());
        }

    }

    private Map<AccessionNumber, GeneResult> parseResults(BufferedReader br) throws IOException {
        List<RnaSeqResultLine> lines = new ArrayList<>();
        Map<AccessionNumber, GeneResult> resultsMap = new HashMap<>();
        int n_lines = 0;
        Set<String> unfound = new HashSet<>();
        String line;
        while ((line = br.readLine()) != null) {
                //Note -- for now we only support Ensembl accession numbers!
                RnaSeqResultLine hline = RnaSeqResultLine.fromEnsembl(line);
                lines.add(hline);
                n_lines++;
            }

        int found_symbol = 0;
        int invalid_lines = 0;
        for (RnaSeqResultLine hline : lines) {
            if (! hline.isValid()) {
                invalid_lines++;
                continue;
            }
            AccessionNumber ensgAccession = hline.geneAccession(); // if we cannot find symbol, just show the accession
            if (hgncMap.containsKey(ensgAccession)) {
                GeneModel model = hgncMap.get(ensgAccession);
                resultsMap.putIfAbsent(ensgAccession, new GeneResultImpl(hline.geneAccession(), model));
                GeneResult hbaresult = resultsMap.get(ensgAccession);
                if (hline.isIsoform()) {
                    hbaresult.addTranscriptResult(hline.isoform(), hline.expFC(), hline.raw_p());
                } else {
                    hbaresult.addExpressionResult(hline.expFC(), hline.raw_p());
                }
                found_symbol++;
            } else {
                unfound.add(hline.geneAccession().getAccessionString());
            }

        }
        if (unfound.size() > found_symbol) {
            LOGGER.error("We could not map {} accession numbers and could map {} accession numbers", unfound.size(), found_symbol);
            throw new IsopretRuntimeException("Could not map most accession numbers/identifiers. Terminating program because this will invalidate downstream analysis.");
        }
        LOGGER.trace("We found gene symbols {} times and missed it {} times.\n", found_symbol, unfound.size());
        LOGGER.trace("We parsed {} lines from {}.\n", n_lines, this.hbadealsFile);
        LOGGER.trace("We got {} genes with HBA DEALS results\n", resultsMap.size());
        if (! unfound.isEmpty()) {
            LOGGER.info("Could not find symbols for {} accessions.", unfound.size());
        }
        if (invalid_lines > 0) {
            String errmsg = String.format("%d invalid lines (splicing fold change negative-but only expr values should be log2). Fix before continuing.",
                    invalid_lines);
            throw new IsopretRuntimeException(errmsg);
        }
        return resultsMap;
    }


    /**
     * This parser expects ENSG and ENST nomenclature
     * @param file HBA-DEALS output file
     * @param hgncMap Map from
     */

    public static Map<AccessionNumber, GeneResult> fromHbaDeals(File file, Map<AccessionNumber, GeneModel> hgncMap) {
        RnaSeqResultsParser parser = new RnaSeqResultsParser(file, hgncMap, true);
        return parser.ensgAcc2geneResultMap;
    }

    public static Map<AccessionNumber, GeneResult> fromEdgeR(File file, Map<AccessionNumber, GeneModel> hgncMap) {
        RnaSeqResultsParser parser = new RnaSeqResultsParser(file, hgncMap, false);
        return parser.ensgAcc2geneResultMap;
    }

    public static Map<AccessionNumber, GeneResult> parse(File file,
                                                         Map<AccessionNumber, GeneModel> hgncMap,
                                                         RnaSeqAnalysisMethod method) {
        RnaSeqResultsParser parser;
        if (method == RnaSeqAnalysisMethod.HBADEALS) {
            parser = new RnaSeqResultsParser(file, hgncMap, true);
        } else {
            parser = new RnaSeqResultsParser(file, hgncMap, false);
        }
        return parser.ensgAcc2geneResultMap;
    }

}
