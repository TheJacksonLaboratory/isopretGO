package org.jax.isopret.html;

import org.jax.isopret.hbadeals.HbaDealsResult;
import org.jax.isopret.hbadeals.HbaDealsThresholder;
import org.jax.isopret.hbadeals.HbaDealsTranscriptResult;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.stats.GoTerm2PValAndCounts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Output a series of TSV files with "significant" differential HBA-DEALS results and the corresponding Study sets and
 * Gene Ontology analysis results.
 */
public class TsvWriter extends AbstractWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(TsvWriter.class);



    TsvWriter(String prefix,
              HbaDealsThresholder thresholder,
              List<GoTerm2PValAndCounts> das,
              List<GoTerm2PValAndCounts> dge,
              String calc,
              String mtc,
              Ontology ontology) {
        super(prefix, thresholder, das, dge, calc, mtc, ontology);
    }

    @Override
    public void write() {
        outputDifferentialGenes();
        outputGoResultsTable(this.dgeGoTerms, "dge", this.geneOntology);
        outputGoResultsTable(this.dasGoTerms, "das", this.geneOntology);
        outputStudySet(thresholder.dasGeneSymbols(), "das");
        outputStudySet(thresholder.dgeGeneSymbols(), "dge");
        outputStudySet(thresholder.population(), "population");
    }


    private String header() {
        String [] fields = {"gene", "accession", "result.type", "probability", "probability.threshold", "fold.change"};
        return String.join("\t", fields);
    }


    private void outputDifferentialGenes() {
        double expressionThreshold = this.thresholder.getExpressionThreshold();
        double splicingThreshold = this.thresholder.getSplicingThreshold();
        String outname = String.format("%s-hbadeals-differential.tsv", this.outprefix);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outname))) {
            writer.write(header() + "\n");
            for (HbaDealsResult result : this.hbaDealsResults.values()) {
                if (result.hasDifferentialSplicingOrExpressionResult(splicingThreshold, expressionThreshold)) {
                    String symbol = result.getSymbol();
                    String geneAccession = result.getGeneAccession().getAccessionString();
                    double expressionP = result.getExpressionP();
                    double expressionFc = result.getExpressionFoldChange();
                    String line = String.format("%s\t%s\texpression\t%f\t%f\t%f\n", symbol, geneAccession, expressionP, expressionThreshold, expressionFc);
                    writer.write(line);
                    for (HbaDealsTranscriptResult tresult : result.getTranscriptMap().values()) {
                        String transcriptAccession = tresult.getTranscript();
                        double transcriptP = tresult.getP();
                        double transcriptFc = tresult.getFoldChange();
                        line = String.format("%s\t%s\tsplicing\t%f\t%f\t%f\n", symbol, transcriptAccession, transcriptP, splicingThreshold,transcriptFc);
                        writer.write(line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("Could not write to {}: {}", outname, e.getMessage());
            return;
        }
        LOGGER.trace("Wrote HBA-DEALS results to TSV file {}", outname);
    }


    /**
     * Out put an Ontologizer-like output file with a name such as
     * table-mason_latest_de-Term-For-Term-Bonferroni.txt
     * @param goTerms List of {@link GoTerm2PValAndCounts} objects, assumed to be presorted
     * @param nameComponent one of DAS, DGE, DASDGE
     */
    private void outputGoResultsTable(List<GoTerm2PValAndCounts> goTerms, String nameComponent, Ontology ontology) {
        String fname = String.format("%s-table-%s-%s-%s.txt", outprefix, nameComponent, this.ontologizerCalculation, this.mtc);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fname))) {
            writer.write(GoTerm2PValAndCounts.header() + "\n");
            for (GoTerm2PValAndCounts pval : goTerms) {
                writer.write(pval.getRow(ontology) + "\n");
            }
        } catch (IOException e ) {
            e.printStackTrace();
        }
    }

    private void outputStudySet(Set<String> geneSymbols, String nameComponent) {
        String fname = String.format("%s_%s-symbols.txt", outprefix, nameComponent);
        List<String> geneList = new ArrayList<>(geneSymbols);
        Collections.sort(geneList);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fname))) {
            writer.write(GoTerm2PValAndCounts.header() + "\n");
            for (String symbol: geneList) {
                writer.write(symbol + "\n");
            }
        } catch (IOException e ) {
            e.printStackTrace();
        }
    }

}
