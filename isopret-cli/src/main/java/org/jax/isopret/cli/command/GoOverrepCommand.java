package org.jax.isopret.cli.command;

import org.jax.isopret.core.GoAnalysisResults;
import org.jax.isopret.core.IsopretGoAnalysisRunner;
import org.jax.isopret.core.IsopretProvider;
import org.jax.isopret.core.analysis.IsopretStats;
import org.jax.isopret.except.IsopretRuntimeException;
import org.jax.isopret.model.*;
import org.jax.isopret.core.impl.rnaseqdata.RnaSeqResultsParser;
import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.analysis.stats.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * This command creates a list of significantly overrepresented GO terms for differential expression and splicing.
 * @author Peter N Robinson
 */
@CommandLine.Command(name = "GO",
        mixinStandardHelpOptions = true,
        description = "Gene Ontology Overrepresentation")
public class GoOverrepCommand extends AbstractRnaseqAnalysisCommand
        implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoOverrepCommand.class);
    /**
     * The desired significance threshold for GO results. Note that this is different from the
     * FDR threshold for differential genes/isoforms in RNA-seq
     */
    private static final double GO_PVAL_THRESHOLD = 0.05;

    @CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
    ExclusiveInputFile exclusive;

    static class ExclusiveInputFile {
        @CommandLine.Option(names={"-b","--hbadeals"},
                description ="HBA-DEALS file" , required = true)
        private String hbadealsFile = null;
        @CommandLine.Option(names={"-e","--edger"},
                description ="edgeR file" , required = true)
        private String edgeRFile = null;
    }

    @CommandLine.Option(names={"-c","--calculation"},
            description ="Ontologizer calculation (Term-for-Term [default], PC-Union, PC-Intersection)" )
    private String ontologizerCalculation = "Term-for-Term";
    @CommandLine.Option(names={"--mtc"},
            description="Multiple-Testing-Correction for GO analysis (${DEFAULT-VALUE} [default], Benjamini-Hochberg, " +
                    " Benjamini-Yekutieli," +
                    " Bonferroni-Holm," +
                    " Sidak," +
                    " None)")
    private String mtc = "Bonferroni";
    @CommandLine.Option(names={"-v", "--verbose"}, description = "Show stats on commandline")
    private boolean verbose = true;
    @CommandLine.Option(names={"--outfile"}, description = "Name of output file to write stats (default: ${DEFAULT-VALUE})")
    private String outfile = "isopret-go-overrep.txt";
    /** Set to true if we are using HbaDeals, otherwise set to false (indicates edgeR). */
    private boolean isHbaDeals;
    private File rnaseqDataFile;



    @Override
    public Integer call() {
        // validate input file and determine if it is HBA-DEALS or edgeR
        if (exclusive.edgeRFile == null && exclusive.hbadealsFile != null) {
            isHbaDeals = true;
            rnaseqDataFile = new File(exclusive.hbadealsFile);
        } else if (exclusive.edgeRFile != null && exclusive.hbadealsFile == null) {
            isHbaDeals = false;
            rnaseqDataFile = new File(exclusive.edgeRFile);
        }
        if (! rnaseqDataFile.isFile()) {
            throw new IsopretRuntimeException("Could not find RNA-seq data file at " + rnaseqDataFile.getAbsoluteFile());
        }

        IsopretProvider provider = IsopretProvider.provider(Paths.get(this.downloadDirectory));
        Ontology geneOntology = provider.geneOntology();
        geneSymbolAccessionListMap = provider.geneSymbolToTranscriptListMap();
        Map<AccessionNumber, GeneModel> hgncMap  = provider.ensemblGeneModelMap();
        Map<GeneSymbolAccession, List<Transcript>> geneSymbolAccessionToTranscriptMap = provider.geneSymbolToTranscriptListMap();
        Map<TermId, Set<TermId>> transcriptIdToGoTermsMap = provider.transcriptIdToGoTermsMap();
        AssociationContainer<TermId> transcriptContainer = provider.transcriptContainer();
        AssociationContainer<TermId> geneContainer = provider.geneContainer();

        // ----------  HBA-DEALS input file  ----------------
        LOGGER.info("About to create thresholder");
        Map<AccessionNumber, GeneResult> hbaDealsResults;
        if (isHbaDeals) {
            hbaDealsResults = RnaSeqResultsParser.fromHbaDeals(rnaseqDataFile, hgncMap);
        } else {
            hbaDealsResults = RnaSeqResultsParser.fromEdgeR(rnaseqDataFile, hgncMap);
        }
        LOGGER.trace("Analyzing {} genes.", hbaDealsResults.size());
        MtcMethod mtcMethod = MtcMethod.fromString(mtc);
        /* ---------- 7. Set up HbaDeal GO analysis ------------------------- */
        GoMethod goMethod = GoMethod.fromString(this.ontologizerCalculation);
        LOGGER.info("Using Gene Ontology approach {}", goMethod.name());
        LOGGER.info("About to create HbaDealsGoContainer");
        IsopretGoAnalysisRunner runner;
        if (isHbaDeals) {
            runner = IsopretGoAnalysisRunner.hbadeals(provider, rnaseqDataFile, mtcMethod, goMethod);
        } else {
            runner = IsopretGoAnalysisRunner.edgeR(provider, rnaseqDataFile, mtcMethod, goMethod);
        }

        GoAnalysisResults results = runner.run();


        writeGoResultsToFile(results, geneOntology);

        if (verbose) {
            IsopretStats.Builder builder = new IsopretStats.Builder();
            String goVersion = geneOntology.getMetaInfo().getOrDefault("data-version", "n/a/");
            builder.geneOntologyVersion(goVersion)
                    .hgncCount(hgncMap.size())
                    .goAssociationsGenes(geneContainer.getTotalAnnotationCount())
                    .annotatedGeneCount(geneContainer.getAnnotatedDomainItemCount())
                    .annotatingGoTermCountGenes(geneContainer.getAnnotatingTermCount())
                    .geneSymbolCount(geneSymbolAccessionToTranscriptMap.size())
                    .transcriptsCount(transcriptIdToGoTermsMap.size());

            IsopretStats stats = builder.build();
            stats.display();
        }

        return 0;
    }

    private void writeGoResultsToFile(GoAnalysisResults results,
                                      Ontology geneOntology) {


        List<GoTerm2PValAndCounts> dasGoTerms = results.dasGoTerms();
        List<GoTerm2PValAndCounts> dgeGoTerms = results.dgeGoTerms();
        if (outfile == null) {
            outfile = getDefaultOutfileName("gene-ontology", exclusive.hbadealsFile);
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outfile))) {
            for (var cts : dasGoTerms) {
                if (cts.passesThreshold(GO_PVAL_THRESHOLD))
                    try {
                        bw.write("DAS\t" + cts.getRow(geneOntology) + "\n");
                    } catch (Exception e) {
                        // some issue with getting terms, probably ontology is not in sync
                        LOGGER.error("Could not get data for {}: {}", cts, e.getLocalizedMessage());
                    }
            }
            for (var cts : dgeGoTerms) {
                if (cts.passesThreshold(GO_PVAL_THRESHOLD))
                    try {
                        bw.write("DGE\t" + cts.getRow(geneOntology) + "\n");
                    } catch (Exception e) {
                        // some issue with getting terms, probably ontology is not in sync
                        LOGGER.error("Could not get data for {}: {}", cts, e.getLocalizedMessage());
                    }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
