package org.jax.isopret.cli.command;

import org.jax.isopret.core.analysis.AssociationContainerStats;
import org.jax.isopret.core.analysis.IsopretAssociationContainer;
import org.jax.isopret.core.analysis.TranscriptToGeneStats;
import org.jax.isopret.core.go.GoMethod;
import org.jax.isopret.core.go.HbaDealsGoAnalysis;
import org.jax.isopret.core.go.HbaDealsGoContainer;
import org.jax.isopret.core.go.MtcMethod;
import org.jax.isopret.core.hbadeals.HbaDealsThresholder;
import org.jax.isopret.core.hgnc.HgncItem;
import org.jax.isopret.core.io.TranscriptFunctionFileParser;
import org.jax.isopret.core.transcript.AccessionNumber;
import org.jax.isopret.core.transcript.Transcript;
import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.analysis.GoAssociationContainer;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.GoTerm2PValAndCounts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Our inference procedure generates a file called {@code isoform_function_list.txt} that
 * contains a list of isoforms with their inferred functions,
 * ENST00000380173	GO:2001303
 * ENST00000251535	GO:2001304
 * ENST00000609196	GO:2001311
 * (...)
 * This command compares the function with the original GO genewise annotations
 */
@CommandLine.Command(name = "transcriptqc", aliases = {"T"},
        mixinStandardHelpOptions = true,
        description = "Q/C the transcript annotations")
public class TranscriptAnnotQcCommand extends IsopretCommand implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TranscriptAnnotQcCommand.class);
    @CommandLine.Option(names={"--trfx"},
            required = true,
            description = "transcript function file")
    private String transcriptFx;
    @CommandLine.Option(names={"-b","--hbadeals"},
            scope = CommandLine.ScopeType.INHERIT,
            description ="HBA-DEALS output file" , required = true)
    private String hbadealsFile;
    @CommandLine.Option(names={"-c","--calculation"},
            scope = CommandLine.ScopeType.INHERIT,
            description ="Ontologizer calculation (Term-for-Term, PC-Union, PC-Intersection)" )
    private String ontologizerCalculation = "Term-for-Term";
    @CommandLine.Option(names={"--mtc"},
            scope = CommandLine.ScopeType.INHERIT,
            description="Multiple-Testing-Correction for GO analysis")
    private String mtc = "Bonferroni";


    @Override
    public Integer call() {
        Ontology geneOntology = loadGeneOntology();
        LOGGER.info("Loaded Gene Ontology with {} terms", geneOntology.countNonObsoleteTerms());
        //GoAssociationContainer container = loadGoAssociationContainer();
        Map<AccessionNumber, HgncItem> hgncMap = loadHgncMap();
        LOGGER.info("Loaded HGNC map with {} genes", hgncMap.size());
        Map<AccessionNumber, List<Transcript>> geneIdToTranscriptMap = loadJannovarGeneIdToTranscriptMap();
        LOGGER.info("Loaded geneId-to-transcript map with {} genes", geneIdToTranscriptMap.size());
        Map<TermId, Set<TermId>> transcriptIdToGoTermsMap = loadTranscriptIdToGoTermsMap();
        LOGGER.info("Loaded transcriptIdToGoTermsMap with {} entries", transcriptIdToGoTermsMap.size());
        int c = 0;
        for (var e : transcriptIdToGoTermsMap.entrySet()) {
            if (c>5) break;
            LOGGER.info("{}) {} -> {}", ++c, e.getKey(), e.getValue());
        }
        Map<TermId, TermId> transcriptToGeneIdMap = createTranscriptToGeneIdMap(geneIdToTranscriptMap);
        LOGGER.info("Loaded transcriptToGeneIdMap with {} entries", transcriptIdToGoTermsMap.size());
        c=0;
        for (var e : transcriptToGeneIdMap.entrySet()) {
            if (c>5) break;
            LOGGER.info("{}) {} -> {}", ++c, e.getKey(), e.getValue());
        }
        System.out.println();
        //TranscriptToGeneStats stats = new TranscriptToGeneStats(geneOntology, transcriptIdToGoTermsMap, transcriptToGeneIdMap);
        LOGGER.info("Displaying stats");
        //stats.display();
        // create and check the annotation containers for the inferred data
        LOGGER.info("Loading TranscriptFunctionFileParser");
        TranscriptFunctionFileParser fxnparser = new TranscriptFunctionFileParser(new File(transcriptFx), geneOntology);
        Map<TermId, Set<TermId>> transcript2GoMap = fxnparser.getTranscriptIdToGoTermsMap();
        LOGGER.info("Loaded transcript2GoMap with {} entries", transcript2GoMap.size());
        c=0;
        for (var e : transcript2GoMap.entrySet()) {
            if (c>5) break;
            LOGGER.info("{}) {} -> {}", ++c, e.getKey(), e.getValue());
        }
        Map<TermId, Set<TermId>> gene2GoMap = fxnparser.getGeneIdToGoTermsMap(transcriptToGeneIdMap);
        LOGGER.info("Loaded gene2GoMap with {} entries", gene2GoMap.size());
        c=0;
        for (var e : gene2GoMap.entrySet()) {
            if (c>5) break;
            LOGGER.info("{}) {} -> {}", ++c, e.getKey(), e.getValue());
        }
        //if (true) return 0;
        LOGGER.info("About to create transcrpt container");
        AssociationContainer transcriptContainer = new IsopretAssociationContainer(geneOntology, transcript2GoMap);
        AssociationContainer geneContainer = new IsopretAssociationContainer(geneOntology, gene2GoMap);
//        var containerStats = new AssociationContainerStats(geneOntology, transcriptContainer, "Transcripts");
//        containerStats.display();
//        containerStats = new AssociationContainerStats(geneOntology, geneContainer, "Genes");
//        containerStats.display();

        // ----------  6. HBA-DEALS input file  ----------------
        LOGGER.info("About to create thresholder");
        HbaDealsThresholder thresholder = initializeHbaDealsThresholder(hgncMap, this.hbadealsFile);
        /* ---------- 7. Set up HbaDeal GO analysis ------------------------- */
        GoMethod goMethod = GoMethod.fromString(this.ontologizerCalculation);
        LOGGER.info("About to create HbaDealsGoContainer");
        HbaDealsGoContainer hbaDealsGoContainer = new HbaDealsGoContainer(geneOntology,
                thresholder,
                geneContainer,
                goMethod,
                MtcMethod.fromString(mtc)
                );
        List<GoTerm2PValAndCounts> dgeGoTerms = hbaDealsGoContainer.termForTermDge();
        System.out.println("Go enrichments, DGE");
        for (var cts : dgeGoTerms) {
            System.out.println(cts.getRow(geneOntology));
        }
        HbaDealsGoContainer hbaDealsGoContainerT = new HbaDealsGoContainer(geneOntology,
                thresholder,
                transcriptContainer,
                goMethod,
                MtcMethod.fromString(mtc)
        );
        List<GoTerm2PValAndCounts> dasGoTerms = hbaDealsGoContainerT.termForTermDas();
        System.out.println("Go enrichments, DAS");
        for (var cts : dasGoTerms) {
            System.out.println(cts.getRow(geneOntology));
        }

//        HbaDealsGoAnalysis hbago =  getHbaDealsGoAnalysis(goMethod,
//                thresholder,
//                geneOntology,
//                transcriptContainer,
//                mtc);
//
//
//        List<GoTerm2PValAndCounts> dasGoTerms = hbago.dasOverrepresetationAnalysis();
//        List<GoTerm2PValAndCounts> dgeGoTerms = hbago.dgeOverrepresetationAnalysis();
//        dasGoTerms.sort(new HbaDealsCommand.SortByPvalue());
//        dgeGoTerms.sort(new HbaDealsCommand.SortByPvalue());
//
//        for (var x : dasGoTerms.listIterator()) {
//            System.out.println(x.toString());
//        }

        return 0;
    }


    Map<TermId, TermId> createTranscriptToGeneIdMap(Map<AccessionNumber, List<Transcript>> gene2transcript) {
        Map<TermId, TermId> accessionNumberMap = new HashMap<>();
        for (var entry : gene2transcript.entrySet()) {
            var geneAcc = entry.getKey();
            var geneTermId = geneAcc.toTermId();
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
