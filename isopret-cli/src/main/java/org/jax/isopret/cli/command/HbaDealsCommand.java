package org.jax.isopret.cli.command;

import org.jax.isopret.core.go.GoMethod;
import org.jax.isopret.core.go.MtcMethod;
import org.jax.isopret.core.interpro.InterproMapper;
import org.jax.isopret.core.hbadeals.HbaDealsThresholder;
import org.jax.isopret.core.transcript.AccessionNumber;
import org.jax.isopret.core.transcript.Transcript;
import org.jax.isopret.core.hgnc.HgncItem;
import org.jax.isopret.core.html.AbstractWriter;
import org.jax.isopret.core.html.HtmlWriter;
import org.jax.isopret.core.html.TsvWriter;
import org.monarchinitiative.phenol.analysis.GoAssociationContainer;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.stats.GoTerm2PValAndCounts;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "test/resources/hbadeals", aliases = {"H"},
        mixinStandardHelpOptions = true,
        description = "Analyze HBA-DEALS files")
public class HbaDealsCommand extends IsopretCommand implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HbaDealsCommand.class);
    @CommandLine.Option(names={"-b","--hbadeals"}, description ="HBA-DEALS output file" , required = true)
    private String hbadealsFile;
    @CommandLine.Option(names={"-c","--calculation"}, description ="Ontologizer calculation (Term-for-Term, PC-Union, PC-Intersection)" )
    public String ontologizerCalculation = "Term-for-Term";
    @CommandLine.Option(names={"--mtc"}, description="Multiple-Testing-Correction for GO analysis")
    public String mtc = "Bonferroni";
    @CommandLine.Option(names={"--prefix"}, description = "Name of output file (without .html ending)")
    private String outprefix = "isopret";
    @CommandLine.Option(names={"--tsv"}, description = "Output TSV files with ontology results and study sets")
    private boolean outputTsv = false;
    @CommandLine.Option(names={"--chunk"}, description = "Chunk size (how many results to show per HTML file; default: ${DEFAULT-VALUE}")
    int chunkSize = 250;

    public HbaDealsCommand() {
    }

    @Override
    public Integer call() {

        // ----------  1. Gene Ontology -------------------
        final Ontology ontology = loadGeneOntology();
        final GoAssociationContainer goAssociationContainer = loadGoAssociationContainer();
        // ----------  2. HGNC Mapping from accession numbers to gene symbols -------------
        Map<AccessionNumber, HgncItem> hgncMap = loadHgncMap();
        // ----------  3. Transcript map from Jannovar ----------------
        Map<String, List<Transcript>> geneSymbolToTranscriptMap = loadJannovarTranscriptMap();
        // ----------  4. Interpro domain data ----------------
        InterproMapper interproMapper = loadInterproMapper();
        // ----------  5. GO overrepresentation method  ----------------
        MtcMethod mtc = MtcMethod.fromString(this.mtc);
        GoMethod goMethod = GoMethod.fromString(this.ontologizerCalculation);
        // ----------  6. HBA-DEALS input file  ----------------
        HbaDealsThresholder thresholder = initializeHbaDealsThresholder(hgncMap, this.hbadealsFile);

        /* ---------- 7. Set up HbaDeal GO analysis ------------------------- */
//        HbaDealsGoAnalysis hbago =  new HbaDealsGoAnalysis(ontology,
//                thresholder.
//                ,
//                thresholder,
//                 ,
//                 goAssociationContainer,
//                 mtc);
//TODO --bring up to date

        List<GoTerm2PValAndCounts> dasGoTerms = List.of();// hbago.overrepresetationAnalysis();
        List<GoTerm2PValAndCounts> dgeGoTerms =List.of();// hbago.overrepresetationAnalysis();

        if (outputTsv) {
            LOGGER.trace("TSV output");
            TsvWriter tsvWriter = new AbstractWriter.Builder()
                    .thresholder(thresholder)
                    .dasGoTerms(dasGoTerms)
                    .dgeGoTerms(dgeGoTerms)
                    .prefix(outprefix)
                    .ontologizerCalculation(this.ontologizerCalculation)
                    .mtc(this.mtc)
                    .ontology(ontology)
                    .buildTsvWriter();
            tsvWriter.write();
        } else {
            // output HTML
            HtmlWriter htmlWriter = new AbstractWriter.Builder()
                    .thresholder(thresholder)
                    .dasGoTerms(dasGoTerms)
                    .dgeGoTerms(dgeGoTerms)
                    .prefix(outprefix)
                    .ontologizerCalculation(this.ontologizerCalculation)
                    .mtc(this.mtc)
                    .ontology(ontology)
                    .goAssociationContainer(goAssociationContainer)
                    .hbago(null)
                    .genesymbolToTranscriptMap(geneSymbolToTranscriptMap)
                    .chunkSize(chunkSize)
                    .interproMapper(interproMapper)
                    .hbadealsFile(new File(hbadealsFile))
                    .buildHtmlWriter();
           htmlWriter.write();
        }

        return 0;
    }


    static class SortByPvalue implements Comparator<GoTerm2PValAndCounts>
    {
        // Used for sorting in ascending order of
        // roll number
        public int compare(GoTerm2PValAndCounts a, GoTerm2PValAndCounts b)
        {
            double diff = a.getRawPValue() - b.getRawPValue();
            if (diff > 0) {
                return 1;
            } else if (diff < 0) {
                return -1;
            } else  {
                return 0;
            }
        }
    }







}
