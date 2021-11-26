package org.jax.isopret.html;

import org.jax.isopret.analysis.Partition;
import org.jax.isopret.go.GoTermIdPlusLabel;
import org.jax.isopret.go.HbaDealsGoAnalysis;
import org.jax.isopret.hbadeals.HbaDealsResult;
import org.jax.isopret.hbadeals.HbaDealsThresholder;
import org.jax.isopret.interpro.DisplayInterproAnnotation;
import org.jax.isopret.interpro.InterproMapper;
import org.jax.isopret.transcript.AccessionNumber;
import org.jax.isopret.transcript.AnnotatedGene;
import org.jax.isopret.transcript.Transcript;
import org.jax.isopret.visualization.*;
import org.monarchinitiative.phenol.analysis.GoAssociationContainer;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.GoTerm2PValAndCounts;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Class to organize writing to HTML files.
 * TODO this is messy and needs to be cleaned up.
 */
public class HtmlWriter extends AbstractWriter {
    int hbadeals = 0;
    int hbadealSig = 0;
    int foundTranscripts = 0;
    private final int chunkSize;
    private final List<String> geneVisualizations;
    /**
     * A map for the freemarker HTML template engine.
     */
    private final Map<String, Object> data = new HashMap<>(); // for the HTML template engine

    HtmlWriter(String prefix,
               HbaDealsThresholder thresholder,
               List<GoTerm2PValAndCounts> das,
               List<GoTerm2PValAndCounts> dge,
               String calc,
               String mtc,
               Ontology ontology,
               GoAssociationContainer goAssociationContainer,
               HbaDealsGoAnalysis hbago,
               Map<String, List<Transcript>> geneSymbolToTranscriptMap,
               int chunkSize,
               InterproMapper interproMapper,
               File hbadealsFile) {
        super(prefix, thresholder, das, dge, calc, mtc, ontology);
        double expressionThreshold = thresholder.getExpressionThreshold();
        double splicingThreshold = thresholder.getSplicingThreshold();
        this.chunkSize = chunkSize;
        // ----------   2. Add some data for the output template.
        addOntologyDataToTemplate(data, ontology,goAssociationContainer,thresholder, hbago);
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
        this.geneVisualizations = new ArrayList<>();

        HtmlVisualizer visualizer = new HtmlVisualizer();
        List<AnnotatedGene> annotatedGeneList = new ArrayList<>();
        int foundInterPro = 0;
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

            Map<AccessionNumber, List<DisplayInterproAnnotation>> transcriptToInterproHitMap = interproMapper.transcriptToInterproHitMap(result.getGeneAccession());
            if (transcriptToInterproHitMap.isEmpty())
                missed++;
            else
                foundInterPro++;

            if (result.hasDifferentialSplicingOrExpressionResult(splicingThreshold, expressionThreshold)) {
                AnnotatedGene agene = new AnnotatedGene(transcripts,
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
        data.put("hbadealsFile", hbadealsFile.getAbsolutePath());
        if (outprefix!=null && ! outprefix.isEmpty()) {
            data.put("prefix", outprefix);
        } else {
            data.put("prefix", hbadealsFile.getName());
        }
        LOGGER.trace("Total unidentified genes:"+ unidentifiedSymbols.size());
        LOGGER.info("Total HBADEALS results: {}, found transcripts {}, also significant {}, also interpro: {}",
                hbadeals, foundTranscripts, hbadealSig, foundInterPro);
    }

    @Override
    public void write() {
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

}
