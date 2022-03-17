package org.jax.isopret.cli.command;

import org.jax.isopret.core.analysis.IsopretStats;
import org.jax.isopret.core.except.IsopretException;
import org.jax.isopret.core.except.IsopretRuntimeException;
import org.jax.isopret.core.go.*;
import org.jax.isopret.core.hbadeals.HbaDealsIsoformSpecificThresholder;
import org.jax.isopret.core.hbadeals.HbaDealsParser;
import org.jax.isopret.core.hbadeals.HbaDealsResult;
import org.jax.isopret.core.model.GeneModel;
import org.jax.isopret.core.io.TranscriptFunctionFileParser;
import org.jax.isopret.core.model.AccessionNumber;
import org.jax.isopret.core.model.GeneSymbolAccession;
import org.jax.isopret.core.model.Transcript;
import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.analysis.StudySet;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.analysis.stats.*;
import org.monarchinitiative.phenol.analysis.stats.mtc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Our inference procedure generates a file called {@code isoform_function_list.txt} that
 * contains a list of isoforms with their inferred functions,
 * ENST00000380173	GO:2001303
 * ENST00000251535	GO:2001304
 * ENST00000609196	GO:2001311
 * (...)
 * This command compares the function with the original GO genewise annotations
 */
@CommandLine.Command(name = "GO",
        mixinStandardHelpOptions = true,
        description = "Gene Ontology Overrepresentation")
public class GoOverrepCommand extends IsopretCommand implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoOverrepCommand.class);
    @CommandLine.Option(names={"-b","--hbadeals"},
            description ="HBA-DEALS output file" , required = true)
    private String hbadealsFile;
    @CommandLine.Option(names={"-c","--calculation"},
            description ="Ontologizer calculation (Term-for-Term, PC-Union, PC-Intersection)" )
    private String ontologizerCalculation = "Term-for-Term";
    @CommandLine.Option(names={"--mtc"},
            description="Multiple-Testing-Correction for GO analysis")
    private String mtc = "Bonferroni";
    @CommandLine.Option(names={"-v", "--verbose"}, description = "Show stats on commandline")
    private boolean verbose = true;
    @CommandLine.Option(names={"--outfile"}, description = "Name of output file to write stats")
    private String outfile = null;



    @Override
    public Integer call() throws IsopretException {
        Ontology geneOntology = loadGeneOntology();
        int n_go_terms = geneOntology.countNonObsoleteTerms();
        LOGGER.info("Loaded Gene Ontology with {} terms", n_go_terms);
        Map<AccessionNumber, GeneModel> hgncMap = loadHgncMap();
        LOGGER.info("Loaded HGNC map with {} genes", hgncMap.size());
        Map<GeneSymbolAccession, List<Transcript>> geneSymbolAccessionToTranscriptMap = loadJannovarSymbolToTranscriptMap();
        LOGGER.info("Loaded geneId-to-transcript map with {} genes", geneSymbolAccessionToTranscriptMap.size());
        Map<TermId, Set<TermId>> transcriptIdToGoTermsMap = loadTranscriptIdToGoTermsMap();
        LOGGER.info("Loaded transcriptIdToGoTermsMap with {} entries", transcriptIdToGoTermsMap.size());
        Map<TermId, TermId> transcriptToGeneIdMap = createTranscriptToGeneIdMap(geneSymbolAccessionToTranscriptMap);
        LOGGER.info("Loaded transcriptToGeneIdMap with {} entries", transcriptIdToGoTermsMap.size());
        // create and check the annotation containers for the inferred data
        LOGGER.info("Loading TranscriptFunctionFileParser");
        TranscriptFunctionFileParser fxnparser = new TranscriptFunctionFileParser(new File(downloadDirectory), geneOntology);
        Map<TermId, Set<TermId>> transcript2GoMap = fxnparser.getTranscriptIdToGoTermsMap();
        LOGGER.info("Loaded transcript2GoMap with {} entries", transcript2GoMap.size());
        Map<TermId, Set<TermId>> gene2GoMap = fxnparser.getGeneIdToGoTermsMap(transcriptToGeneIdMap);
        LOGGER.info("Loaded gene2GoMap with {} entries", gene2GoMap.size());
        LOGGER.info("About to create transcript container");
        IsopretContainerFactory isoContainerFac = new IsopretContainerFactory(geneOntology,transcriptIdToGoTermsMap, gene2GoMap);
        AssociationContainer<TermId> transcriptContainer = isoContainerFac.transcriptContainer();
        LOGGER.info("transcriptContainer terms n={}", transcriptContainer.getAnnotatingTermCount());
        LOGGER.info("transcriptContainer items n={}", transcriptContainer.getAnnotatedDomainItemCount());
        AssociationContainer<TermId> geneContainer = isoContainerFac.geneContainer();
        LOGGER.info("geneContainer terms n={}", geneContainer.getAnnotatingTermCount());
        LOGGER.info("geneContainer items n={}", geneContainer.getAnnotatedDomainItemCount());

        // ----------  6. HBA-DEALS input file  ----------------
        LOGGER.info("About to create thresholder");
        HbaDealsParser hbaParser = new HbaDealsParser(this.hbadealsFile, hgncMap);
        Map<AccessionNumber, HbaDealsResult> hbaDealsResults = hbaParser.getEnsgAcc2hbaDealsMap();
        LOGGER.trace("Analyzing {} genes.", hbaDealsResults.size());
       // HbaDealsThresholder thresholder = initializeHbaDealsThresholder(hgncMap, this.hbadealsFile);
        MtcMethod mtcMethod = MtcMethod.fromString(mtc);
        HbaDealsIsoformSpecificThresholder isoThresholder = new HbaDealsIsoformSpecificThresholder(hbaDealsResults,
                0.05,
                geneContainer,
                transcriptContainer);
        LOGGER.info("Initialized HBADealsThresholder");
        LOGGER.info("isoThresholder.getDgePopulation().getAnnotatedItemCount()={}"
                ,isoThresholder.getDgePopulation().getAnnotatedItemCount());
        /* ---------- 7. Set up HbaDeal GO analysis ------------------------- */
        GoMethod goMethod = GoMethod.fromString(this.ontologizerCalculation);
        LOGGER.info("Using Gene Ontology approach {}", goMethod.name());
        LOGGER.info("About to create HbaDealsGoContainer");
        List<GoTerm2PValAndCounts> dgeGoTerms = doGoAnalysis(goMethod,
                mtcMethod,
                geneOntology,
                isoThresholder.getDgeStudy(),
                isoThresholder.getDgePopulation());
        System.out.println("Go enrichments, DGE");
        for (var cts : dgeGoTerms) {
            if (cts.passesThreshold(0.05))
                try {
                    System.out.println(cts.getRow(geneOntology));
                } catch (Exception e) {
                    // some issue with getting terms, probably ontology is not in sync
                    LOGGER.error("Could not get data for {}: {}", cts, e.getLocalizedMessage());
                }
        }

        List<GoTerm2PValAndCounts> dasGoTerms = doGoAnalysis(goMethod,
                mtcMethod,
                geneOntology,
                isoThresholder.getDasStudy(),
                isoThresholder.getDasPopulation());
        System.out.println("Go enrichments, DAS");
        for (var cts : dasGoTerms) {
            if (cts.passesThreshold(0.05))
                try {
                    System.out.println(cts.getRow(geneOntology));
                } catch (Exception e) {
                    // some issue with getting terms, probably ontology is not in sync
                    LOGGER.error("Could not get data for {}: {}", cts, e.getLocalizedMessage());
                }
        }
        writeGoResultsToFile(dasGoTerms, dgeGoTerms, geneOntology);

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

    private List<GoTerm2PValAndCounts> doGoAnalysis(GoMethod goMethod, MtcMethod mtcMethod, Ontology geneOntology, StudySet dgeStudy, StudySet dgePopulation) {
        final double ALPHA = 0.05;
        PValueCalculation pvalcal;
        MultipleTestingCorrection mtc;
        switch (mtcMethod) {
            case BONFERRONI -> mtc = new Bonferroni();
            case BONFERRONI_HOLM -> mtc = new BonferroniHolm();
            case BENJAMINI_HOCHBERG -> mtc = new BenjaminiHochberg();
            case BENJAMINI_YEKUTIELI -> mtc = new BenjaminiYekutieli();
            case SIDAK -> mtc = new Sidak();
            case NONE -> mtc = new NoMultipleTestingCorrection();
            default -> {
                // should never happen
                System.err.println("[WARNING] Did not recognize MTC");
                mtc = new Bonferroni();
            }
        }
        if (goMethod.equals(GoMethod.TFT)) {
            pvalcal = new TermForTermPValueCalculation(geneOntology,
                    dgePopulation,
                    dgeStudy,
                    mtc);
        } else if (goMethod.equals(GoMethod.PCunion)) {
            pvalcal = new ParentChildUnionPValueCalculation(geneOntology,
                    dgePopulation,
                    dgeStudy,
                    mtc);
        } else if (goMethod.equals(GoMethod.PCintersect)) {
            pvalcal = new ParentChildIntersectionPValueCalculation(geneOntology,
                    dgePopulation,
                    dgeStudy,
                    mtc);
        } else {
            throw new IsopretRuntimeException("Did not recognise GO Method");
        }
        return pvalcal.calculatePVals()
                .stream()
                .filter(item -> item.passesThreshold(ALPHA))
                .sorted()
                .collect(Collectors.toList());

    }

    private void writeGoResultsToFile(List<GoTerm2PValAndCounts> dasGoTerms,
                                      List<GoTerm2PValAndCounts> dgeGoTerms,
                                      Ontology geneOntology) {

        if (outfile == null) {
            outfile = getDefaultOutfileName("gene-ontology", hbadealsFile);
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outfile))) {
            for (var cts : dasGoTerms) {
                if (cts.passesThreshold(0.05))
                    try {
                        bw.write("DAS\t" + cts.getRow(geneOntology) + "\n");
                    } catch (Exception e) {
                        // some issue with getting terms, probably ontology is not in sync
                        LOGGER.error("Could not get data for {}: {}", cts, e.getLocalizedMessage());
                    }
            }
            for (var cts : dgeGoTerms) {
                if (cts.passesThreshold(0.05))
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




    Map<TermId, TermId> createTranscriptToGeneIdMap(Map<GeneSymbolAccession, List<Transcript>> gene2transcript) {
        Map<TermId, TermId> accessionNumberMap = new HashMap<>();
        for (var entry : gene2transcript.entrySet()) {
            var geneAcc = entry.getKey();
            var geneTermId = geneAcc.accession().toTermId();
            var transcriptList = entry.getValue();
            for (var transcript: transcriptList) {
                var transcriptAcc = transcript.accessionId();
                var transcriptTermId = transcriptAcc.toTermId();
                //System.out.println(transcriptAcc.getAccessionString() +": " + geneAcc.getAccessionString());
                accessionNumberMap.put(transcriptTermId, geneTermId);
            }
        }
        return Map.copyOf(accessionNumberMap); // immutable copy
    }
}
