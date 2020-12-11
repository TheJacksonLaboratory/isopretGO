package org.jax.isopret.command;

import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.jax.isopret.ensembl.EnsemblCdnaParser;
import org.jax.isopret.ensembl.EnsemblGene;
import org.jax.isopret.go.GoParser;
import org.jax.isopret.hbadeals.HbaDealsParser;
import org.jax.isopret.hbadeals.HbaDealsResult;
import org.jax.isopret.html.HtmlGene;
import org.jax.isopret.html.HtmlTemplate;
import org.jax.isopret.prosite.PrositeComparator;
import org.jax.isopret.prosite.PrositeHit;
import org.jax.isopret.prosite.PrositeMapParser;
import org.jax.isopret.prosite.PrositeMapping;
import org.jax.isopret.transcript.AnnotatedGene;
import org.jax.isopret.transcript.GenomicAssemblyProvider;
import org.jax.isopret.transcript.JannovarReader;
import org.jax.isopret.transcript.Transcript;
import org.jax.isopret.visualization.HrmlVisualizable;
import org.jax.isopret.visualization.HtmlVisualizer;
import org.jax.isopret.visualization.Visualizer;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.variant.api.GenomicAssembly;
import picocli.CommandLine;

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
    @CommandLine.Option(names = {"-j", "--jannovar"}, required = true, description = "Path to Jannovar transcript file")
    private String jannovarPath;


    private final static Map<String, PrositeMapping> EMPTY_PROSITE_MAP = Map.of();

    public HbaDealsCommand() {

    }

    private void initializeGO() {
        GoParser goParser = new GoParser(goOboFile, goGafFile);
        final Ontology ontology = goParser.getOntology();
        final Map<String, List<TermId>> annotationMap = goParser.getAnnotationMap();
        System.out.printf("[INFO] We got %d GO terms.\n", ontology.countNonObsoleteTerms());
        System.out.printf("[INFO] We got %d term to annotation list mappings\n",annotationMap.size());
    }

    @Override
    public Integer call() {
        int hbadeals = 0;
        int hbadealSig = 0;
        int foundTranscripts = 0;
        int foundProsite = 0;

        GenomicAssembly hg38 =  GenomicAssemblyProvider.hg38();
        JannovarReader jreader = new JannovarReader(this.jannovarPath, hg38);
        Map<String, List<Transcript>> geneSymbolToTranscriptMap = jreader.getSymbolToTranscriptListMap();
        PrositeMapParser pmparser = new PrositeMapParser(prositeMapFile, prositeDataFile);
        Map<String, PrositeMapping> prositeMappingMap = pmparser.getPrositeMappingMap();
        HbaDealsParser hbaParser = new HbaDealsParser(hbadealsFile);
        Map<String, HbaDealsResult> hbaDealsResults = hbaParser.getHbaDealsResultMap();
        System.out.printf("[INFO] Analyzing %d genes.\n", hbaDealsResults.size());
        List<String> unidentifiedSymbols = new ArrayList<>();
        List<String> visualizations = new ArrayList<>();
        HtmlVisualizer visualizer = new HtmlVisualizer();
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
           // String geneID = transcripts.stream().map(TranscriptModel)

            Map<String, PrositeMapping> transcriptToHitMap = EMPTY_PROSITE_MAP;
            if (! prositeMappingMap.containsKey(geneSymbol)) {
                System.err.printf("[WARN] Could not identify prosite Mapping for %s.\n", geneSymbol);
                continue;
            }
            foundProsite++;
            PrositeMapping pmapping = prositeMappingMap.get(result.);
            AnnotatedGene agene = new AnnotatedGene(transcripts, pmapping.getTranscriptToPrositeListMap(), result);
            System.out.printf("[INFO] processing %s: ", geneSymbol);
            visualizations.add(visualizer.getHtml(new HrmlVisualizable(agene)));
        }
        Map<String, Object> data = new HashMap<>();
        data.put("genelist", visualizations);


        HtmlTemplate template = new HtmlTemplate(data);
        template.outputFile();
        System.out.println("[INFO] Total unidentified genes:"+ unidentifiedSymbols.size());
        System.out.printf("[INFO] Total HBADEALS results: %d, found transcripts %d, also significant %d, also prosite: %d\n",
                hbadeals, foundTranscripts, hbadealSig, foundProsite);
        return 0;
    }
}
