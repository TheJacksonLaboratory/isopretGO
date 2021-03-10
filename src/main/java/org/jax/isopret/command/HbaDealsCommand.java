package org.jax.isopret.command;

import org.jax.isopret.analysis.Partition;
import org.jax.isopret.except.IsopretRuntimeException;
import org.jax.isopret.go.*;
import org.jax.isopret.hbadeals.HbaDealsParser;
import org.jax.isopret.hbadeals.HbaDealsResult;
import org.jax.isopret.hbadeals.HbaDealsThresholder;
import org.jax.isopret.hgnc.HgncItem;
import org.jax.isopret.hgnc.HgncParser;
import org.jax.isopret.html.HtmlTemplate;
import org.jax.isopret.html.TsvWriter;
import org.jax.isopret.interpro.*;
import org.jax.isopret.prosite.PrositeHit;
import org.jax.isopret.prosite.PrositeMapParser;
import org.jax.isopret.prosite.PrositeMapping;
import org.jax.isopret.transcript.AccessionNumber;
import org.jax.isopret.transcript.AnnotatedGene;
import org.jax.isopret.transcript.JannovarReader;
import org.jax.isopret.transcript.Transcript;
import org.jax.isopret.visualization.*;
import org.monarchinitiative.phenol.analysis.GoAssociationContainer;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.GoTerm2PValAndCounts;
import org.monarchinitiative.svart.*;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandLine.Command(name = "hbadeals", aliases = {"H"},
        mixinStandardHelpOptions = true,
        description = "Analyze HBA-DEALS files")
public class HbaDealsCommand implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HbaDealsCommand.class);

    @CommandLine.Option(names={"-b","--hbadeals"}, description ="HBA-DEALS output file" , required = true)
    private String hbadealsFile;
    @CommandLine.Option(names={"-c","--calculation"}, description ="Ontologizer calculation (Term-for-Term, PC-Union, PC-Intersection)" )
    public String ontologizerCalculation = "Term-for-Term";
    @CommandLine.Option(names={"--mtc"}, description="Multiple-Testing-Correction for GO analysis")
    public String mtc = "Bonferroni";
    private String fastaFile = "data/Homo_sapiens.GRCh38.cdna.all.fa.gz";
    @CommandLine.Option(names={"-p","--prosite"}, description ="prosite.dat file")
    private String prositeDataFile = "data/prosite.dat";
    @CommandLine.Option(names={"--prositemap"}, description = "prosite map file", required = true)
    private String prositeMapFile;
    @CommandLine.Option(names={"--desc"}, description ="interpro_domain_desc.txt file", required = true)
    private String interproDescriptionFile;
    @CommandLine.Option(names={"--domains"}, description ="interpro_domains.txt", required = true)
    private String interproDomainsFile;
    @CommandLine.Option(names={"-g","--go"}, description ="go.obo file")
    private String goOboFile = "data/go.obo";
    @CommandLine.Option(names={"-a","--gaf"}, description ="goa_human.gaf.gz file")
    private String goGafFile = "data/goa_human.gaf";
    @CommandLine.Option(names = {"-j", "--jannovar"}, description = "Path to Jannovar transcript file")
    private String jannovarPath = "data/hg38_ensembl.ser";
    @CommandLine.Option(names={"--prefix"}, description = "Name of output file (without .html ending)")
    private String outprefix = "isopret";
    @CommandLine.Option(names={"--tsv"}, description = "Output TSV files with ontology results and study sets")
    private boolean outputTsv = false;
    @CommandLine.Option(names={"-n", "--namespace"}, required = true, description = "Namespace of gene identifiers (ENSG, ucsc, RefSeq)")
    private String namespace = "ensembl";
    @CommandLine.Option(names={"--chunk"}, description = "Chunk size (how many results to show per HTML file; default: ${DEFAULT-VALUE}")
    int chunkSize = 500;


    private final static Map<String, PrositeMapping> EMPTY_PROSITE_MAP = Map.of();

    public HbaDealsCommand() {

    }


    @Override
    public Integer call() {
        int hbadeals = 0;
        int hbadealSig = 0;
        int foundTranscripts = 0;
        int foundProsite = 0;
        Map<String, Object> data = new HashMap<>(); // for the HTML template engine
        // ----------  1. Gene Ontology -------------------
        GoParser goParser = new GoParser(goOboFile, goGafFile);
        final Ontology ontology = goParser.getOntology();
        final GoAssociationContainer goAssociationContainer = goParser.getAssociationContainer();

        // ----------  3. HGNC Mapping from accession numbers to gene symbols -------------
        Map<AccessionNumber, HgncItem> hgncMap = initializeHgncMapper();


        MtcMethod mtc = MtcMethod.fromString(this.mtc);
        GoMethod goMethod = GoMethod.fromString(this.ontologizerCalculation);

        Map<String, List<Transcript>> geneSymbolToTranscriptMap = getTranscriptMap(GenomicAssemblies.GRCh38p13());
        PrositeMapParser pmparser = new PrositeMapParser(prositeMapFile, prositeDataFile);
        Map<String, PrositeMapping> prositeMappingMap = pmparser.getPrositeMappingMap();
        Map<String, String> prositeIdToName = pmparser.getPrositeNameMap();

        InterproMapper interproMapper = new InterproMapper(this.interproDescriptionFile, this.interproDomainsFile);

        HbaDealsThresholder thresholder = initializeHbaDealsThresholder(hgncMap, this.hbadealsFile);

        double expressionThreshold = thresholder.getExpressionThreshold();
        double splicingThreshold = thresholder.getSplicingThreshold();
        /* ----------  Set up HbaDeal GO analysis ------------------------- */
        HbaDealsGoAnalysis hbago =  getHbaDealsGoAnalysis(goMethod,
                thresholder,
                 ontology,
                 goAssociationContainer,
                 mtc);
        // ----------   2. Add some data for the output template.
        addOntologyDataToTemplate(data, ontology,goAssociationContainer,thresholder, hbago);

        List<GoTerm2PValAndCounts> dasGoTerms = hbago.dasOverrepresetationAnalysis();
        List<GoTerm2PValAndCounts> dgeGoTerms = hbago.dgeOverrepresetationAnalysis();
        dasGoTerms.sort(new SortByPvalue());
        dgeGoTerms.sort(new SortByPvalue());

        if (outputTsv) {
            LOGGER.trace("TSV output");
            TsvWriter tsvWriter = new TsvWriter.Builder()
                    .thresholder(thresholder)
                    .dasGoTerms(dasGoTerms)
                    .dgeGoTerms(dgeGoTerms)
                    .prefix(outprefix)
                    .ontologizerCalculation(this.ontologizerCalculation)
                    .mtc(this.mtc)
                    .ontology(ontology)
                    .build();
            tsvWriter.write();
            return 0;
        }
        // Add differentially expressed genes/GO analysis
        String dgeTable = getGoHtmlTable(dgeGoTerms, ontology, "dgego-table");
        data.put("dgeTable", dgeTable);
        // Same for DAS (note -- in this application, DAS may overlap with DAS/DGE)
        String dasTable = getGoHtmlTable(dasGoTerms, ontology, "dasgo-table");
        data.put("dasTable", dasTable);
        // genes symbols differential  for significant expression OR splicing
        Set<String> significantGeneSymbols = thresholder.dasGeneSymbols();
        significantGeneSymbols.addAll(thresholder.dgeGeneSymbols());
        // Set of all enriched GO Terms Ids for significant expression OR splicing
        Set<TermId> einrichedGoTermIdSet = Stream.concat(dasGoTerms.stream(), dgeGoTerms.stream())
                .map(GoTerm2PValAndCounts::getItem)
                .collect(Collectors.toSet());
        Map<String, Set<GoTermIdPlusLabel>> enrichedGeneAnnots = hbago.getEnrichedSymbolToEnrichedGoMap(einrichedGoTermIdSet,significantGeneSymbols);


        List<String> unidentifiedSymbols = new ArrayList<>();
        List<String> geneVisualizations = new ArrayList<>();

        HtmlVisualizer visualizer = new HtmlVisualizer(prositeIdToName);
        List<AnnotatedGene> annotatedGeneList = new ArrayList<>();
        int foundInterpor = 0;
        int missed = 0;
        for (var entry : thresholder.getRawResults().entrySet()) {
            String geneSymbol = entry.getKey();
            hbadeals++;
            if (!geneSymbolToTranscriptMap.containsKey(geneSymbol)) {
                System.err.printf("[WARN] Could not identify transcripts for %s.\n", geneSymbol);
                continue;
            }
            foundTranscripts++;
            HbaDealsResult result = entry.getValue();

            if (! result.hasDifferentialSplicingOrExpressionResult(splicingThreshold, expressionThreshold)) {
                continue;
            }
            hbadealSig++;
            List<Transcript> transcripts = geneSymbolToTranscriptMap.get(geneSymbol);

            final Map<String, List<PrositeHit>> EMPTY_PROSITE_HIT_MAP = Map.of();

            Map<String, List<PrositeHit>> prositeHitsForCurrentGene;
            //Map<String, List<InterproAnnotation>> interproHitsForCurrentGene;
            if (! prositeMappingMap.containsKey(result.getGeneAccession().getAccessionString())) {
                LOGGER.trace("Could not identify prosite Mapping for {}.\n", geneSymbol);
                prositeHitsForCurrentGene = EMPTY_PROSITE_HIT_MAP;
            } else {
                PrositeMapping pmapping = prositeMappingMap.get(result.getGeneAccession().getAccessionString());
                prositeHitsForCurrentGene = pmapping.getTranscriptToPrositeListMap();
                foundProsite++;
            }

            Map<AccessionNumber, List<DisplayInterproAnnotation>> transcriptToInterproHitMap = interproMapper.transcriptToInterproHitMap(result.getGeneAccession());
            if (transcriptToInterproHitMap.isEmpty())
                missed++;
            else
                foundInterpor++;

            if (result.hasDifferentialSplicingOrExpressionResult(splicingThreshold, expressionThreshold)) {
                AnnotatedGene agene = new AnnotatedGene(transcripts,
                        prositeHitsForCurrentGene,
                        transcriptToInterproHitMap,
                        result,
                        expressionThreshold,
                        splicingThreshold);
                annotatedGeneList.add(agene);
            }
        }
        Collections.sort(annotatedGeneList);
        int i = 0;
        for (AnnotatedGene annotatedGene : annotatedGeneList) {
            i++;
            Set<GoTermIdPlusLabel> goTerms = enrichedGeneAnnots.getOrDefault(annotatedGene.getSymbol(), new HashSet<>());
            geneVisualizations.add(visualizer.getHtml(new EnsemblVisualizable(annotatedGene, goTerms, i)));
        }
        // record source of analysis
        File f = new File(hbadealsFile);
        data.put("hbadealsFile", f.getAbsolutePath());
        if (outprefix!=null && ! outprefix.isEmpty()) {
            data.put("prefix", outprefix);
        } else {
            data.put("prefix", f.getName());
        }
        if (geneVisualizations.size() > chunkSize+100) {
            Partition<String> partition = Partition.ofSize(geneVisualizations, chunkSize);
            int n_partitions = partition.size();
            for (int j=0;j<n_partitions;j++) {
                LOGGER.trace("Output of part {} of HTML file", (j+1));
                List<String> geneVisualizationSublist = partition.get(j);
                data.put("genelist", geneVisualizations);
                String outFileName = "isopret-" + this.outprefix + "-part-" + (j+1);
                data.put("genelist", geneVisualizationSublist);
                data.put("parts_info", String.format("Part %d of %d", (j+1), n_partitions));
                HtmlTemplate template = new HtmlTemplate(data, outFileName);
                template.outputFile();
                LOGGER.trace("Output HTML file: {}", outFileName);
            }
        } else {
            data.put("genelist", geneVisualizations);
            String outFileName = "isopret-" + this.outprefix;
            HtmlTemplate template = new HtmlTemplate(data, outFileName);
            template.outputFile();
        }

        LOGGER.trace("Total unidentified genes:"+ unidentifiedSymbols.size());
        LOGGER.info("Total HBADEALS results: {}, found transcripts {}, also significant {}, also prosite: {}",
                hbadeals, foundTranscripts, hbadealSig, foundProsite);
        System.out.printf("FOUND %d MISSED %d\n",foundInterpor, missed);
        return 0;
    }


    /**
     *
     * @param goTerms List of Pvals & counts for an enriched GO Term
     * @param ontology reference to Gene Ontology object
     * @param title -- title as it will be used in the JavaScript, e.g., "dgego-table"
     * @return an HTML table representing the results of GO analysis
     */
    private String getGoHtmlTable(List<GoTerm2PValAndCounts> goTerms, Ontology ontology, String title) {
        List<GoVisualizable> govis = new ArrayList<>();
        for (var v : goTerms) {
            govis.add(new HtmlGoVisualizable(v, ontology));
        }
        HtmlGoVisualizer htmlGoVisualizer = new HtmlGoVisualizer(govis, title);
        return htmlGoVisualizer.getHtml();
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

    /**
     * Convenience method to add data for output to the HTML template.
     * @param data map with data for template
     * @param ontology Reference to Gene Ontology object
     * @param goAssociationContainer GO associations.
     */
    private void addOntologyDataToTemplate(Map<String, Object> data,
                                           Ontology ontology,
                                           GoAssociationContainer goAssociationContainer,
                                           HbaDealsThresholder thresholder,
                                           HbaDealsGoAnalysis hbago) {
        data.put("go_version", ontology.getMetaInfo().getOrDefault("data-version", "unknown"));
        data.put("n_go_terms", ontology.getNonObsoleteTermIds().size());
        data.put("annotation_term_count", goAssociationContainer.getOntologyTermCount());
        data.put("annotation_count", goAssociationContainer.getRawAssociations().size());
        data.put("annotated_genes", goAssociationContainer.getTotalNumberOfAnnotatedItems());
        LOGGER.trace("We got {} GO terms.", ontology.countNonObsoleteTerms());
        LOGGER.trace("We got {} term to annotation list mappings.",goAssociationContainer.getRawAssociations().size());
        data.put("n_population", hbago.populationCount());
        data.put("n_das",thresholder.getDasGeneCount());
        data.put("n_das_unmapped", hbago.unmappedDasCount());
        data.put("unmappable_das_list", VisualizationUtil.fromList(hbago.unmappedDasSymbols(), "Unmappable DAS Gene Symbols"));
        data.put("n_dge", thresholder.getDgeGeneCount());
        data.put("n_dge_unmapped", hbago.unmappedDgeCount());
        data.put("unmappable_dge_list", VisualizationUtil.fromList(hbago.unmappedDgeSymbols(), "Unmappable DGE Gene Symbols"));
        data.put("probability_threshold", thresholder.getFdrThreshold());
        data.put("expression_threshold", thresholder.getExpressionThreshold());
        data.put("splicing_threshold", thresholder.getSplicingThreshold());
    }


    private Map<AccessionNumber, HgncItem> initializeHgncMapper() {
        HgncParser hgncParser = new HgncParser();
        if (this.namespace.equalsIgnoreCase("ensembl")) {
            return hgncParser.ensemblMap();
//        }
//        else if (this.namespace.equalsIgnoreCase("ucsc")) {
//            //return hgncParser.ucscMap();
//        } else if (this.namespace.equalsIgnoreCase("refseq")) {
//            // return hgncParser.refseqMap();
        } else {
            throw new IsopretRuntimeException("Name space was " + namespace + " but must be one of ensembl, UCSC, refseq");
        }
    }

    /**
     * Get a map of transcripts. The key is a gene symbol, the value is a list of Svart transcript obejcts
     * @param assembly The genome assembly (Only hg38 is supperted for now)
     * @return  map of transcripts
     */
    private Map<String, List<Transcript>> getTranscriptMap(GenomicAssembly assembly) {
        JannovarReader jreader = new JannovarReader(this.jannovarPath, assembly);
        return jreader.getSymbolToTranscriptListMap();
    }

    private HbaDealsGoAnalysis getHbaDealsGoAnalysis(GoMethod goMethod,
                                             HbaDealsThresholder thresholder,
                                             Ontology ontology,
                                             GoAssociationContainer goAssociationContainer,
                                             MtcMethod mtc) {
        if (goMethod == GoMethod.PCunion) {
            return HbaDealsGoAnalysis.parentChildUnion(thresholder, ontology, goAssociationContainer, mtc);
        } else if (goMethod == GoMethod.PCintersect) {
            return HbaDealsGoAnalysis.parentChildIntersect(thresholder, ontology, goAssociationContainer, mtc);
        } else {
            return HbaDealsGoAnalysis.termForTerm(thresholder, ontology, goAssociationContainer, mtc);
        }
    }


    private HbaDealsThresholder initializeHbaDealsThresholder(Map<AccessionNumber, HgncItem> hgncMap, String hbadealsPath) {
        HbaDealsParser hbaParser = new HbaDealsParser(hbadealsPath, hgncMap);
        Map<String, HbaDealsResult> hbaDealsResults = hbaParser.getHbaDealsResultMap();
        LOGGER.trace("Analyzing {} genes.", hbaDealsResults.size());
        return new HbaDealsThresholder(hbaDealsResults);
    }

}
