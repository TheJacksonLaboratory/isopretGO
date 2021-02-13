package org.jax.isopret.command;

import org.jax.isopret.except.IsopretRuntimeException;
import org.jax.isopret.go.*;
import org.jax.isopret.hbadeals.HbaDealsParser;
import org.jax.isopret.hbadeals.HbaDealsResult;
import org.jax.isopret.hbadeals.HbaDealsThresholder;
import org.jax.isopret.hgnc.HgncItem;
import org.jax.isopret.hgnc.HgncParser;
import org.jax.isopret.html.HtmlTemplate;
import org.jax.isopret.prosite.PrositeHit;
import org.jax.isopret.prosite.PrositeMapParser;
import org.jax.isopret.prosite.PrositeMapping;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
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
        data.put("go_version", ontology.getMetaInfo().getOrDefault("data-version", "unknown"));
        data.put("n_go_terms", ontology.getNonObsoleteTermIds().size());
        data.put("annotation_term_count", goAssociationContainer.getOntologyTermCount());
        data.put("annotation_count", goAssociationContainer.getRawAssociations().size());
        data.put("annotated_genes", goAssociationContainer.getTotalNumberOfAnnotatedItems());
        LOGGER.trace("We got {} GO terms.", ontology.countNonObsoleteTerms());
        LOGGER.trace("We got {} term to annotation list mappings.",goAssociationContainer.getRawAssociations().size());
        MtcMethod mtc = MtcMethod.fromString(this.mtc);
        GoMethod goMethod = GoMethod.fromString(this.ontologizerCalculation);
        // ----------  2. HGNC Mapping from accession numbers to gene symbols -------------
        Map<String, HgncItem> hgncMap;
        HgncParser hgncParser = new HgncParser();
        if (this.namespace.equalsIgnoreCase("ensembl")) {
            hgncMap = hgncParser.ensemblMap();
        } else if (this.namespace.equalsIgnoreCase("ucsc")) {
            hgncMap = hgncParser.ucscMap();
        } else if (this.namespace.equalsIgnoreCase("refseq")) {
            hgncMap = hgncParser.refseqMap();
        } else {
            throw new IsopretRuntimeException("Name space was " + namespace + " but must be one of ensembl, UCSC, refseq");
        }
        GenomicAssembly hg38 =  GenomicAssemblies.GRCh38p13();
        JannovarReader jreader = new JannovarReader(this.jannovarPath, hg38);
        Map<String, List<Transcript>> geneSymbolToTranscriptMap = jreader.getSymbolToTranscriptListMap();
        PrositeMapParser pmparser = new PrositeMapParser(prositeMapFile, prositeDataFile);
        Map<String, PrositeMapping> prositeMappingMap = pmparser.getPrositeMappingMap();
        Map<String, String> prositeIdToName = pmparser.getPrositeNameMap();
        HbaDealsParser hbaParser = new HbaDealsParser(hbadealsFile, hgncMap);
        Map<String, HbaDealsResult> hbaDealsResults = hbaParser.getHbaDealsResultMap();
        HbaDealsThresholder thresholder = new HbaDealsThresholder(hbaDealsResults);

        HbaDealsGoAnalysis hbago;
        if (goMethod == GoMethod.PCunion) {
            hbago = HbaDealsGoAnalysis.parentChildUnion(thresholder, ontology, goAssociationContainer, mtc);
        } else if (goMethod == GoMethod.PCintersect) {
            hbago = HbaDealsGoAnalysis.parentChildIntersect(thresholder, ontology, goAssociationContainer, mtc);
        } else {
            hbago = HbaDealsGoAnalysis.termForTerm(thresholder, ontology, goAssociationContainer, mtc);
        }

        int populationSize = hbago.populationCount();
        data.put("n_population", populationSize);
        data.put("n_das", hbago.dasCount());
        data.put("n_das_unmapped", hbago.unmappedDasCount());
        data.put("unmappable_das_list", Util.fromList(hbago.unmappedDasSymbols(), "Unmappable DAS Gene Symbols"));
        data.put("n_dge", hbago.dgeCount());
        data.put("n_dge_unmapped", hbago.unmappedDgeCount());
        data.put("unmappable_dge_list", Util.fromList(hbago.unmappedDgeSymbols(), "Unmappable DGE Gene Symbols"));
        data.put("n_dasdge", hbago.dasDgeCount());
        data.put("n_dasdge_unmapped", hbago.unmappedDasDgeCount());
        data.put("unmappable_dasdge_list", Util.fromList(hbago.unmappedDasDgeSymbols(), "Unmappable DAS/DGE Gene Symbols"));
        data.put("probability_threshold", thresholder.getFdrThreshold());
        data.put("expression_threshold", thresholder.getExpressionThreshold());
        data.put("splicing_threshold", thresholder.getSplicingThreshold());
        List<GoTerm2PValAndCounts> dasGoTerms = hbago.dasOverrepresetationAnalysis();
        List<GoTerm2PValAndCounts> dgeGoTerms = hbago.dgeOverrepresetationAnalysis();
        List<GoTerm2PValAndCounts> dasDgeGoTerms = hbago.dasDgeOverrepresetationAnalysis();
        dasGoTerms.sort(new SortByPvalue());
        dgeGoTerms.sort(new SortByPvalue());
        dasDgeGoTerms.sort(new SortByPvalue());
        if (outputTsv) {
            outputGoResultsTable(dasGoTerms, "das", ontology);
            outputGoResultsTable(dgeGoTerms, "dge", ontology);
            outputGoResultsTable(dasDgeGoTerms, "dasdge", ontology);
            outputStudySet(thresholder.dasGeneSymbols(), "das");
            outputStudySet(thresholder.dgeGeneSymbols(), "dge");
            outputStudySet(thresholder.dasDgeGeneSymbols(), "dasdge");
            outputStudySet(thresholder.population(), "population");
        }
        // Add differentially expressed genes/GO analysis
        String dgeTable = getGoHtmlTable(dgeGoTerms, ontology, "dgego-table");
        data.put("dgeTable", dgeTable);
        // Same for DAS (note -- in this application, DAS may overlap with DAS/DGE)
        String dasTable = getGoHtmlTable(dasGoTerms, ontology, "dasgo-table");
        data.put("dasTable", dasTable);
        // Same for DAS+DGE
        String dasDgeTable = getGoHtmlTable(dasDgeGoTerms, ontology, "dasdgego-table");
        data.put("dasDgeTable", dasDgeTable);
        // The following sets are used for the HTML output to mark genes
        // that are annotated to a significant GO term.
        Predicate<HbaDealsResult> diffExpressed = HbaDealsResult::hasSignificantExpressionResult;
        Predicate<HbaDealsResult> diffSpliced = HbaDealsResult::hasaSignificantSplicingResult;
        // Set of all gene symbols for gene with significant expression OR splicing
        Set<String> significantGeneSymbols = hbaDealsResults
                .values()
                .stream()
                .filter(diffExpressed.or(diffSpliced))
                .map(HbaDealsResult::getSymbol)
                .collect(Collectors.toSet());
        // Set of all enriched GO Terms Ids for significant expression OR splicing
        Set<TermId> einrichedGoTermIdSet = Stream.concat(dasDgeGoTerms.stream(), dgeGoTerms.stream())
                .map(GoTerm2PValAndCounts::getItem)
                .collect(Collectors.toSet());
        Map<String, Set<GoTermIdPlusLabel>> enrichedGeneAnnots = hbago.getEnrichedSymbolToEnrichedGoMap(einrichedGoTermIdSet,significantGeneSymbols);

        LOGGER.trace("Analyzing {} genes.", hbaDealsResults.size());
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

            final Map<String, List<PrositeHit>> EMPTY_PROSITE_HIT_MAP = Map.of();
            Map<String, List<PrositeHit>> prositeHitsForCurrentGene;
            if (! prositeMappingMap.containsKey(result.getGeneAccession())) {
                LOGGER.trace("Could not identify prosite Mapping for {}.\n", geneSymbol);
                prositeHitsForCurrentGene = EMPTY_PROSITE_HIT_MAP;
            } else {
                PrositeMapping pmapping = prositeMappingMap.get(result.getGeneAccession());
                prositeHitsForCurrentGene = pmapping.getTranscriptToPrositeListMap();
                foundProsite++;
            }

            AnnotatedGene agene = new AnnotatedGene(transcripts, prositeHitsForCurrentGene, result);
            if (result.isDASandDGE()) {
                Set<GoTermIdPlusLabel> goTerms = enrichedGeneAnnots.getOrDefault(result.getSymbol(), new HashSet<>());
                dasAndDgeVisualizations.add(visualizer.getHtml(new EnsemblVisualizable(agene, goTerms)));
            } else if (result.isDAS()) {
                Set<GoTermIdPlusLabel> goTerms = enrichedGeneAnnots.getOrDefault(result.getSymbol(), new HashSet<>());
                dasVisualizations.add(visualizer.getHtml(new EnsemblVisualizable(agene, goTerms)));
            } else if (result.isDGE()) {
                Set<GoTermIdPlusLabel> goTerms = enrichedGeneAnnots.getOrDefault(result.getSymbol(), new HashSet<>());
                dgeVisualizations.add(visualizer.getHtml(new EnsemblVisualizable(agene, goTerms)));
            } else {
                // should never get here, sanity check
                throw new IsopretRuntimeException("Neither DAS, nor DGE, not DAS/DGE, nor non-significant");
            }
        }

        data.put("dgedaslist", dasAndDgeVisualizations);
        data.put("daslist", dasVisualizations);
        data.put("dgelist", dgeVisualizations);
        data.put("populationCount", populationSize);
        List<GoVisualizable> govis = new ArrayList<>();
        for (var v : dgeGoTerms) {
            govis.add(new HtmlGoVisualizable(v, ontology));
        }

        // record source of analysis
        File f = new File(hbadealsFile);
        data.put("hbadealsFile", f.getName());


        HtmlTemplate template = new HtmlTemplate(data, this.outprefix);
        template.outputFile();
        LOGGER.trace("Total unidentified genes:"+ unidentifiedSymbols.size());
        LOGGER.info("Total HBADEALS results: {}, found transcripts {}, also significant {}, also prosite: {}",
                hbadeals, foundTranscripts, hbadealSig, foundProsite);
        return 0;
    }

    /**
     * Out put an Ontologizer-like output file with a name such as
     * table-mason_latest_de-Term-For-Term-Bonferroni.txt
     * @param goTerms List of {@link GoTerm2PValAndCounts} objects, assumed to be presorted
     * @param nameComponent one of DAS, DGE, DASDGE
     */
    private void outputGoResultsTable(List<GoTerm2PValAndCounts> goTerms, String nameComponent, Ontology ontology) {
        String fname = String.format("table-%s_%s-%s-%s.txt", outprefix, nameComponent, this.ontologizerCalculation, this.mtc);
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
        String fname = String.format("symbols-%s_%s.txt", outprefix, nameComponent);
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

}
