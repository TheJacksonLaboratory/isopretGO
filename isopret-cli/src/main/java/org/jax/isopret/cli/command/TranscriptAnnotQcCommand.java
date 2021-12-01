package org.jax.isopret.cli.command;

import org.jax.isopret.core.analysis.AssociationContainerStats;
import org.jax.isopret.core.analysis.IsopretAssociationContainer;
import org.jax.isopret.core.analysis.TranscriptToGeneStats;
import org.jax.isopret.core.go.GoMethod;
import org.jax.isopret.core.go.HbaDealsGoAnalysis;
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
        GoAssociationContainer container = loadGoAssociationContainer();
        Map<AccessionNumber, HgncItem> hgncMap = loadHgncMap();
        Map<AccessionNumber, List<Transcript>> geneIdToTranscriptMap = loadJannovarGeneIdToTranscriptMap();
        Map<AccessionNumber, Set<TermId>> transcriptIdToGoTermsMap = loadTranscriptIdToGoTermsMap();
        Map<AccessionNumber, AccessionNumber> transcriptToGeneIdMap = createTranscriptToGeneIdMap(geneIdToTranscriptMap);
        System.out.println();
        TranscriptToGeneStats stats = new TranscriptToGeneStats(geneOntology, transcriptIdToGoTermsMap, transcriptToGeneIdMap);
        stats.display();
        // create and check the annotation containers for the inferred data
        TranscriptFunctionFileParser fxnparser = new TranscriptFunctionFileParser(new File(transcriptFx), geneOntology);
        Map<AccessionNumber, Set<TermId>> transcript2GoMap = fxnparser.getTranscriptIdToGoTermsMap();
        Map<AccessionNumber, Set<TermId>> gene2GoMap = fxnparser.getGeneIdToGoTermsMap(transcriptToGeneIdMap);
        AssociationContainer transcriptContainer = new IsopretAssociationContainer(geneOntology, transcript2GoMap);
        AssociationContainer geneContainer = new IsopretAssociationContainer(geneOntology, gene2GoMap);
        var containerStats = new AssociationContainerStats(geneOntology, transcriptContainer, "GoGAF");
        containerStats.display();
        containerStats = new AssociationContainerStats(geneOntology, transcriptContainer, "Transcripts");
        containerStats.display();
        containerStats = new AssociationContainerStats(geneOntology, geneContainer, "Genes");
        containerStats.display();

        // ----------  6. HBA-DEALS input file  ----------------
        HbaDealsThresholder thresholder = initializeHbaDealsThresholder(hgncMap, this.hbadealsFile);
        /* ---------- 7. Set up HbaDeal GO analysis ------------------------- */
        GoMethod goMethod = GoMethod.fromString(this.ontologizerCalculation);
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


    Map<AccessionNumber, AccessionNumber> createTranscriptToGeneIdMap(Map<AccessionNumber, List<Transcript>> gene2transcript) {
        Map<AccessionNumber, AccessionNumber> accessionNumberMap = new HashMap<>();
        for (var entry : gene2transcript.entrySet()) {
            var geneAcc = entry.getKey();
            var transcriptList = entry.getValue();
            for (var transcript: transcriptList) {
                var transcriptAcc = transcript.accessionId();
                //System.out.println(transcriptAcc.getAccessionString() +": " + geneAcc.getAccessionString());
                accessionNumberMap.put(transcriptAcc, geneAcc);
            }
        }
        return Map.copyOf(accessionNumberMap); // immutable copy
    }
}
