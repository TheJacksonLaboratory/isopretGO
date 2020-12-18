package org.jax.isopret.command;

import org.jax.isopret.except.IsopretRuntimeException;
import org.jax.isopret.go.GoParser;
import org.jax.isopret.go.HbaDealsGoAnalysis;
import org.jax.isopret.hbadeals.HbaDealsParser;
import org.jax.isopret.hbadeals.HbaDealsResult;
import org.jax.isopret.html.HtmlTemplate;
import org.jax.isopret.prosite.PrositeHit;
import org.jax.isopret.prosite.PrositeMapParser;
import org.jax.isopret.prosite.PrositeMapping;
import org.jax.isopret.transcript.AnnotatedGene;
import org.jax.isopret.transcript.GenomicAssemblyProvider;
import org.jax.isopret.transcript.JannovarReader;
import org.jax.isopret.transcript.Transcript;
import org.jax.isopret.visualization.*;
import org.monarchinitiative.phenol.analysis.GoAssociationContainer;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.stats.GoTerm2PValAndCounts;
import org.monarchinitiative.variant.api.GenomicAssembly;
import picocli.CommandLine;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "hbadeals", aliases = {"H"},
        mixinStandardHelpOptions = true,
        description = "Analyze HBA-DEALS files")
public class HbaDealsCommand implements Callable<Integer> {

    @CommandLine.Option(names={"-b","--hbadeals"}, description ="HBA-DEALS output file" , required = true)
    private String hbadealsFile;
    @CommandLine.Option(names={"-f","--fasta"}, description ="FASTA file" )
    private String fastaFile = "data/Homo_sapiens.GRCh38.cdna.all.fa.gz";
    @CommandLine.Option(names={"-p","--prosite"}, description ="prosite.dat file")
    private String prositeDataFile = "data/prosite.dat";
    @CommandLine.Option(names={"--prositemap"}, description = "prosite mape file", required = true)
    private String prositeMapFile;
    @CommandLine.Option(names={"-g","--go"}, description ="go.obo file")
    private String goOboFile = "data/go.obo";
    @CommandLine.Option(names={"-a","--gaf"}, description ="goa_human.gaf.gz file")
    private String goGafFile = "data/goa_human.gaf";
    @CommandLine.Option(names = {"-j", "--jannovar"}, description = "Path to Jannovar transcript file")
    private String jannovarPath = "data/hg38_ensembl.ser";


    private final static Map<String, PrositeMapping> EMPTY_PROSITE_MAP = Map.of();

    public HbaDealsCommand() {

    }


    @Override
    public Integer call() {
        int hbadeals = 0;
        int hbadealSig = 0;
        int foundTranscripts = 0;
        int foundProsite = 0;

        GoParser goParser = new GoParser(goOboFile, goGafFile);
        final Ontology ontology = goParser.getOntology();
        final GoAssociationContainer goAssociationContainer = goParser.getAssociationContainer();
        System.out.printf("[INFO] We got %d GO terms.\n", ontology.countNonObsoleteTerms());
        System.out.printf("[INFO] We got %d term to annotation list mappings\n",goAssociationContainer.getRawAssociations().size());


        GenomicAssembly hg38 =  GenomicAssemblyProvider.hg38();
        JannovarReader jreader = new JannovarReader(this.jannovarPath, hg38);
        Map<String, List<Transcript>> geneSymbolToTranscriptMap = jreader.getSymbolToTranscriptListMap();
        PrositeMapParser pmparser = new PrositeMapParser(prositeMapFile, prositeDataFile);
        Map<String, PrositeMapping> prositeMappingMap = pmparser.getPrositeMappingMap();
        Map<String, String> prositeIdToName = pmparser.getPrositeNameMap();
        HbaDealsParser hbaParser = new HbaDealsParser(hbadealsFile);
        Map<String, HbaDealsResult> hbaDealsResults = hbaParser.getHbaDealsResultMap();

        HbaDealsGoAnalysis hbago = new HbaDealsGoAnalysis(hbaDealsResults, ontology, goAssociationContainer);
        int populationSize = hbago.populationCount();
        List<GoTerm2PValAndCounts> dasGoTerms = hbago.dasOverrepresetationAnalysis();
        List<GoTerm2PValAndCounts> dgeGoTerms = hbago.dasOverrepresetationAnalysis();

        System.out.printf("[INFO] Analyzing %d genes.\n", hbaDealsResults.size());
        List<String> unidentifiedSymbols = new ArrayList<>();
        List<String> dasAndDgeVisualizations = new ArrayList<>();
        List<String> dasVisualizations = new ArrayList<>();
        List<String> dgeVisualizations = new ArrayList<>();
        HtmlVisualizer visualizer = new HtmlVisualizer(prositeIdToName);
        for (var entry : hbaDealsResults.entrySet()) {
            String geneSymbol = entry.getKey();
            hbadeals++;
            if (!geneSymbolToTranscriptMap.containsKey(geneSymbol)) {
                System.err.printf("[WARN] Could not identify transcripts for %s.\n", geneSymbol);
                continue;
            }
            foundTranscripts++;
            HbaDealsResult result = entry.getValue();
            if (! result.hasSignificantResult()) {
                continue;
            }
            hbadealSig++;
            List<Transcript> transcripts = geneSymbolToTranscriptMap.get(geneSymbol);


            Map<String, PrositeMapping> transcriptToHitMap = EMPTY_PROSITE_MAP;
            //Map<String, List<PrositeHit>>
            final Map<String, List<PrositeHit>> EMPTY_PROSITE_HIT_MAP = Map.of();
            Map<String, List<PrositeHit>> prositeHitsForCurrentGene;
            if (! prositeMappingMap.containsKey(result.getGeneAccession())) {
                System.err.printf("[WARN] Could not identify prosite Mapping for %s.\n", geneSymbol);
                prositeHitsForCurrentGene = EMPTY_PROSITE_HIT_MAP;
            } else {
                PrositeMapping pmapping = prositeMappingMap.get(result.getGeneAccession());
                prositeHitsForCurrentGene = pmapping.getTranscriptToPrositeListMap();
                foundProsite++;
            }

            AnnotatedGene agene = new AnnotatedGene(transcripts, prositeHitsForCurrentGene, result);
            System.out.printf("[INFO] processing %s: ", geneSymbol);
            if (result.isDASandDGE()) {
                dasAndDgeVisualizations.add(visualizer.getHtml(new EnsemblVisualizable(agene)));
            } else if (result.isDAS()) {
                dasVisualizations.add(visualizer.getHtml(new EnsemblVisualizable(agene)));
            } else if (result.isDGE()) {
                dgeVisualizations.add(visualizer.getHtml(new EnsemblVisualizable(agene)));
            } else {
                // should never get here, sanity check
                throw new IsopretRuntimeException("Neither DAS, nor DGE, not DAS/DGE, nor non-significant");
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("dgedaslist", dasAndDgeVisualizations);
        data.put("n_dgedas", dasAndDgeVisualizations.size());
        data.put("daslist", dasVisualizations);
        data.put("n_das", dasVisualizations.size());
        data.put("dgelist", dgeVisualizations);
        data.put("n_dge", dgeVisualizations.size());
        data.put("populationCount", populationSize);
        List<GoVisualizable> govis = new ArrayList<>();
        for (var v : dgeGoTerms) {
            govis.add(new HtmlGoVisualizable(v, ontology));
        }
        HtmlGoVisualizer htmlGoVisualizer = new HtmlGoVisualizer(govis, "dgego-table");
        String dgeTable = htmlGoVisualizer.getHtml();
        govis.clear();
        for (var v : dasGoTerms) {
            govis.add(new HtmlGoVisualizable(v, ontology));
        }
        htmlGoVisualizer = new HtmlGoVisualizer(govis, "dasgo-table");
        String dasTable = htmlGoVisualizer.getHtml();
        data.put("dgeTable", dgeTable);

        data.put("dasTable", dasTable);

        File f = new File(hbadealsFile);
        data.put("hbadealsFile", f.getName());


        HtmlTemplate template = new HtmlTemplate(data);
        template.outputFile();
        System.out.println("[INFO] Total unidentified genes:"+ unidentifiedSymbols.size());
        System.out.printf("[INFO] Total HBADEALS results: %d, found transcripts %d, also significant %d, also prosite: %d\n",
                hbadeals, foundTranscripts, hbadealSig, foundProsite);
        return 0;
    }
}
