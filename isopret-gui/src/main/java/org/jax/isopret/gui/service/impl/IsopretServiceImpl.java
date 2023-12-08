package org.jax.isopret.gui.service.impl;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.jax.isopret.core.analysis.IsopretStats;
import org.jax.isopret.core.impl.go.*;
import org.jax.isopret.core.impl.rnaseqdata.IsoformSpecificThresholder;
import org.jax.isopret.core.InterproMapper;
import org.jax.isopret.core.impl.rnaseqdata.RnaSeqAnalysisMethod;
import org.jax.isopret.data.*;
import org.jax.isopret.gui.service.IsopretDataLoadTask;
import org.jax.isopret.model.*;
import org.jax.isopret.visualization.DasDgeGoVisualizer;
import org.jax.isopret.visualization.EnsemblVisualizable;
import org.jax.isopret.visualization.GoAnnotationMatrix;
import org.jax.isopret.visualization.Visualizable;
import org.jax.isopret.gui.service.IsopretService;
import org.jax.isopret.gui.service.model.GeneOntologyComparisonMode;
import org.jax.isopret.gui.service.model.GoComparison;
import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermAnnotation;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.analysis.stats.GoTerm2PValAndCounts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class IsopretServiceImpl implements IsopretService  {
    private static final Logger LOGGER = LoggerFactory.getLogger(IsopretServiceImpl.class);

    /** File for reading/writing settings. */
    @Autowired
    File isopretSettingsFile;
    private final Properties pgProperties;
    private final StringProperty downloadDirProp;
    private final StringProperty rnaSeqResultsFileProperty;

    private final DoubleProperty downloadCompletenessProp;
    /** The HBA-DEALS or edgeR file. */
    private File rnaSeqResultsFile = null;
    private GoMethod goMethod = GoMethod.TFT;
    private MtcMethod mtcMethod = MtcMethod.NONE;
    private Ontology geneOntology = null;
    private InterproMapper interproMapper = null;
    private IsoformSpecificThresholder thresholder = null;
    private Map<AccessionNumber, GeneModel> geneAccessionToModelMap = Map.of();
    private List<GoTerm2PValAndCounts> dasGoTerms = List.of();
    private List<GoTerm2PValAndCounts> dgeGoTerms = List.of();
    private IsopretAssociationContainer transcriptContainer = null;
    private IsopretAssociationContainer geneContainer = null;
    //private Map<AccessionNumber, List<Transcript>> geneIdToTranscriptMap;
    /** Key: transcript id; value: set of Annotating GO Terms. */
    private Map<TermId, Set<TermId>> transcript2GoMap = Map.of();
    private IsopretStats isopretStats = null;
    private RnaSeqAnalysisMethod rnaSeqAnalysisMethod;

    public IsopretServiceImpl(Properties pgProperties) {
        this.pgProperties = pgProperties;
        if (this.pgProperties.containsKey("downloaddir")) {
            this.downloadDirProp = new SimpleStringProperty(this.pgProperties.getProperty("downloaddir"));
        } else {
            this.downloadDirProp = new SimpleStringProperty("");
        }
        this.rnaSeqResultsFileProperty = new SimpleStringProperty("");
        this.downloadCompletenessProp = new SimpleDoubleProperty(calculateDownloadCompleteness());
    }

    private double calculateDownloadCompleteness() {
        return sourcesDownloaded();
    }


    @Override
    public double sourcesDownloaded() {
        if (! pgProperties.containsKey("downloaddir")) {
            LOGGER.warn("Download directory not initialized");
            return 0d;
        }
        String downloadPath = pgProperties.getProperty("downloaddir");
        File downloadDir = new File(downloadPath);
        if (! downloadDir.isDirectory()) {
            LOGGER.warn("Download directory not initialized");
            return 0d;
        }
        Set<String> expectedDownloadedFiles = getExpectedDownloadedFiles();
        int expected = expectedDownloadedFiles.size();
        File [] downloadedFiles = downloadDir.listFiles();
        if (downloadedFiles == null) {
            LOGGER.warn("No downloaded files");
            return 0d;
        }
        Set<String> basenames = Arrays.stream(downloadedFiles).map(File::getName).collect(Collectors.toSet());
        int found = 0;
        for (String name : expectedDownloadedFiles) {
            if (basenames.contains(name)) {
                found++;
            } else {
                LOGGER.error("Did not find {} in download directory at {}.", name, downloadDir);
            }
        }
        return (double) found/expected;
    }

    /**
     * This method gets called when user chooses to close Gui. The contents of the pgProperties object are
     * written to a file in the user's isopretfx directory
     */
    @Override
    public void saveSettings() {
        try {
            pgProperties.store(new FileWriter(isopretSettingsFile), "store to properties file");
        } catch (IOException e) {
            LOGGER.error("Could not write settings: {}", e.getMessage());
        }
    }

    @Override
    public Set<String> getExpectedDownloadedFiles() {
        return Set.of("go.json",
                "interpro_domain_desc.txt",
                "isoform_function_list_cc.txt",
                "hg38_ensembl.ser",
                "interpro_domains.txt",
                "isoform_function_list_mf.txt",
                "hgnc_complete_set.txt",
                "isoform_function_list_bp.txt");
    }

    @Override
    public void setDownloadDir(File file){
        pgProperties.setProperty("downloaddir", file.getAbsolutePath());
        this.downloadDirProp.setValue(file.getAbsolutePath());
    }

    @Override
    public void setRnaSeqFile(File file, RnaSeqAnalysisMethod method) {
        this.rnaSeqResultsFile = file;
        this.rnaSeqAnalysisMethod = method;
        this.rnaSeqResultsFileProperty.setValue(file.getAbsolutePath());
        LOGGER.info("Set HBA-DEALS file to {}", this.rnaSeqResultsFile);
    }

    @Override
    public StringProperty downloadDirProperty() {
        File f = new File(downloadDirProp.get());
        if (! f.exists()) {
            this.downloadDirProp.setValue("Download directory not set");
        } else if (! f.isDirectory()) {
            this.downloadDirProp.setValue("Error, download property set to file");
        }
        return this.downloadDirProp;
    }

    @Override
    public StringProperty rnaSeqResultsFileProperty() {
        return rnaSeqResultsFileProperty;
    }


    @Override
    public DoubleProperty downloadCompletenessProperty() {
        return this.downloadCompletenessProp;
    }

    @Override
    public void setGoMethod(String method) {
        this.goMethod = GoMethod.fromString(method);
    }

    @Override
    public void setMtcMethod(String method) {
        this.mtcMethod = MtcMethod.fromString(method);
    }

    @Override
    public RnaSeqAnalysisMethod getRnaSeqMethod() {
        return this.rnaSeqAnalysisMethod;
    }

    @Override
    public Optional<File> getDownloadDir() {
        String ddir = downloadDirProp.get();
        if (ddir == null) return Optional.empty();
        File f = new File(ddir);
        if (f.isDirectory()) {
            return Optional.of(f);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<File> getRnaSeqResultsFileOpt() {
        if (this.rnaSeqResultsFile == null || ! this.rnaSeqResultsFile.isFile()) {
            return Optional.empty();
        } else  {
            return Optional.of(this.rnaSeqResultsFile);
        }
    }

    @Override
    public void setData(IsopretDataLoadTask task) {
        this.geneOntology = task.getGeneOntology();
        this.interproMapper = task.getInterproMapper();
        this.geneContainer = task.getGeneContainer();
        this.transcriptContainer = task.getTranscriptContainer();
        this.dgeGoTerms = task.getDgeResults();
        this.dasGoTerms = task.getDasResults();
        this.thresholder = task.getIsoformSpecificThresholder();
        this.geneAccessionToModelMap = task.getGeneSymbolToModelMap();
        this.transcript2GoMap = task.getTranscript2GoMap();
        this.isopretStats = task.getIsopretStats();
        this.goMethod = task.getOverrepMethod();
        this.mtcMethod = task.getMultipleTestingMethod();
        LOGGER.info("Finished setting data. ");
        if (this.transcript2GoMap == null) {
            LOGGER.error("transcript2GoMap == null");
        } else {
            LOGGER.info("transcript2GoMap n = {} entries", transcript2GoMap.size());
        }
    }

    @Override
    public List<GoTerm2PValAndCounts> getDasGoTerms() {
        return dasGoTerms;
    }
    @Override
    public List<GoTerm2PValAndCounts> getDgeGoTerms() {
        return dgeGoTerms;
    }


    @Override
    public  Map<String, String> getResultsSummaryMap(){
        Map<String, String> resultsMap = new HashMap<>();
        if (thresholder == null) {
            resultsMap.put("n/a", "not initialized");
        } else {
            resultsMap.put("Observed genes", String.valueOf(thresholder.getTotalGeneCount()));
            resultsMap.put("Differentially expressed genes", String.valueOf(thresholder.getDgeGeneCount()));
            resultsMap.put("Differentially spliced genes", String.valueOf(thresholder.getDasIsoformCount()));
            resultsMap.put("FDR threshold", String.valueOf(thresholder.getFdrThreshold()));
            resultsMap.put("Significant DGE GO Terms", String.valueOf(this.dgeGoTerms.size()));
            resultsMap.put("Significant DAS GO Terms", String.valueOf(this.dasGoTerms.size()));

        }
        return resultsMap;
    }

    @Override
    public Visualizable getVisualizableForGene(AccessionNumber ensgAccession) {
        if (! this.thresholder.getRawResults().containsKey(ensgAccession)) {
            LOGGER.error("Could not find HBADEALS results for {}.", ensgAccession);
            return null;
        }
        GeneModel geneModel = this.geneAccessionToModelMap.get(ensgAccession);
        List<Transcript> transcripts = geneModel == null ? List.of() : geneModel.transcriptList();
        GeneResult result = thresholder.getRawResults().get(ensgAccession);
        double splicingThreshold = thresholder.getSplicingPepThreshold();
        double expressionThreshold = thresholder.getExpressionPepThreshold();
        Map<AccessionNumber, List<DisplayInterproAnnotation>> transcriptToInterproHitMap =
                interproMapper.transcriptToInterproHitMap(result.getGeneAccession());
        AnnotatedGene agene = new AnnotatedGene(transcripts,
                transcriptToInterproHitMap,
                result,
                expressionThreshold,
                splicingThreshold);
        GoAnnotationMatrix annotationMatrix = getGoAnnotationMatrixForGene(result);
        return new EnsemblVisualizable(agene, annotationMatrix);
    }



    @Override
    public List<Visualizable> getGeneVisualizables(Set<AccessionNumber> includedEnsgAccessionSet) {
        List<Visualizable> visualizables = new ArrayList<>();
        // sort the raw results according to minimum p-values
        List<GeneResult> results = thresholder.getRawResults().values()
                .stream()
                .sorted()
                .toList();
        for (GeneResult result : results) {
            AccessionNumber ensgAccession = result.getGeneAccession();
            // if this gene is in our included set.. (it should always be in geneAccessionToModelMap, but check here to avoid NPE
            if ( includedEnsgAccessionSet.contains(ensgAccession) &&
                    this.geneAccessionToModelMap.containsKey(ensgAccession)) {
                GeneModel model = this.geneAccessionToModelMap.get(ensgAccession);
                List<Transcript> transcripts = model == null ? List.of() : model.transcriptList();
                double splicingThreshold = thresholder.getSplicingPepThreshold();
                double expressionThreshold = thresholder.getExpressionPepThreshold();
                Map<AccessionNumber, List<DisplayInterproAnnotation>> transcriptToInterproHitMap =
                        interproMapper.transcriptToInterproHitMap(result.getGeneAccession());
                AnnotatedGene agene = new AnnotatedGene(transcripts,
                        transcriptToInterproHitMap,
                        result,
                        expressionThreshold,
                        splicingThreshold);
                GoAnnotationMatrix annotationMatrix = getGoAnnotationMatrixForGene(result);
                EnsemblVisualizable viz = new EnsemblVisualizable(agene, annotationMatrix);
                visualizables.add(viz);
            }
        }
        return visualizables;
    }

    @Override
    public List<Visualizable> getGeneVisualizables() {
        List<Visualizable> visualizables = new ArrayList<>();
        // sort the raw results according to minimum p-values
        List<GeneResult> results = thresholder.getRawResults().values()
                .stream()
                .sorted()
                .toList();
        for (GeneResult result : results) {
            List<Transcript> transcripts = result.getGeneModel().transcriptList();
            double splicingThreshold = thresholder.getSplicingPepThreshold();
            double expressionThreshold = thresholder.getExpressionPepThreshold();
            Map<AccessionNumber, List<DisplayInterproAnnotation>> transcriptToInterproHitMap =
                    interproMapper.transcriptToInterproHitMap(result.getGeneAccession());
            AnnotatedGene agene = new AnnotatedGene(transcripts,
                    transcriptToInterproHitMap,
                    result,
                    expressionThreshold,
                    splicingThreshold);
            GoAnnotationMatrix annotationMatrix = getGoAnnotationMatrixForGene(result);
            EnsemblVisualizable viz = new EnsemblVisualizable(agene, annotationMatrix);
            visualizables.add(viz);
        }
        return visualizables;
    }

    @Override
    public List<AnnotatedGene> getAnnotatedGeneList() {
        List<AnnotatedGene> annotatedGenes = new ArrayList<>();
        // sort the raw results according to minimum p-values
        List<GeneResult> results = thresholder.getRawResults().values()
                .stream()
                .sorted()
                .toList();
        for (GeneResult result : results) {
            List<Transcript> transcripts = result.getGeneModel().transcriptList();
            double splicingThreshold = thresholder.getSplicingPepThreshold();
            double expressionThreshold = thresholder.getExpressionPepThreshold();
            Map<AccessionNumber, List<DisplayInterproAnnotation>> transcriptToInterproHitMap =
                    interproMapper.transcriptToInterproHitMap(result.getGeneAccession());
            AnnotatedGene agene = new AnnotatedGene(transcripts,
                    transcriptToInterproHitMap,
                    result,
                    expressionThreshold,
                    splicingThreshold);
            annotatedGenes.add(agene);
        }
        return annotatedGenes;
    }



    public Ontology getGeneOntology() {
        return this.geneOntology;
    }

    @Override
    public String getGoLabel(GeneOntologyComparisonMode mode) {
        switch (mode) {
            case DAS -> {
                int n = this.dasGoTerms.size();
                return "GO Overrepresentation analysis of differentially spliced isoforms: " + n + " significantly overrepresented terms.";
            }
            case DGE -> {
                int n = this.dgeGoTerms.size();
                return "GO Overrepresentation analysis of differentially expressed genes: " + n + " significantly overrepresented terms.";
            }
        }
        return ""; // should never happen but needed for compiler
    }


    @Override
    public String getGoMethods() {
        StringBuilder sb = new StringBuilder();
        switch (this.goMethod) {
            case TFT -> sb.append("Term-for-term analysis");
            case PCintersect -> sb.append("Parent-child intersection");
            case PCunion -> sb.append("Parent-child union");
        }
        sb.append(" (");
        switch (this.mtcMethod) {
            case BONFERRONI -> sb.append("Bonferroni");
            case SIDAK -> sb.append("Sidak");
            case NONE -> sb.append("No MTC");
            case BONFERRONI_HOLM -> sb.append("Bonferroni-Holm");
            case BENJAMINI_HOCHBERG -> sb.append("Benjamini-Hochberg");
            case BENJAMINI_YEKUTIELI -> sb.append("Benjamini-Yekutieli");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String getGoSummary() {
        String version = geneOntology.getMetaInfo().getOrDefault("data-version", "no version found");
        String nterms = String.valueOf(geneOntology.nonObsoleteTermIdCount());
        return "Gene Ontology (version: " + version +"), " + nterms + " terms.";
    }

    /**
     *
     * @param goIds All GO ids that annotate some gene
     * @return count of GO terms found to be significant and total count
     */
    @Override
    public int totalSignificantGoTermsAnnotatingGene(Set<TermId> goIds) {
        // total significant terms in DGE/DAS
        return Stream.concat(dgeGoTerms.stream().map(GoTerm2PValAndCounts::getGoTermId).
                        filter(goIds::contains),
                        dasGoTerms.stream().map(GoTerm2PValAndCounts::getGoTermId).
                                filter(goIds::contains))
                .collect(Collectors.toSet()).size();
    }



    public AssociationContainer<TermId> getTranscriptContainer() {
        return transcriptContainer;
    }

    public AssociationContainer<TermId> getGeneContainer() {
        return geneContainer;
    }


    public GoAnnotationMatrix getGoAnnotationMatrixForGene(GeneResult result) {
        AccessionNumber accession = result.getGeneAccession();
        Set<TermId> expressedTranscriptSet = result.getTranscriptMap().keySet().stream()
                .map(AccessionNumber::toTermId)
                .collect(Collectors.toSet());
        Set<TermId> significantGoSet = dgeGoTerms.stream()
                .map(GoTerm2PValAndCounts::getGoTermId)
                .collect(Collectors.toSet());
        return new GoAnnotationMatrix(this.geneOntology,
                this.geneAccessionToModelMap,
                this.transcript2GoMap,
                significantGoSet,
                accession,
                expressedTranscriptSet);
    }

    @Override
    public GoComparison getGoComparison() {
        // note that if we can access the button, then we have cnstructed the GO tab
        // and the following three variables are not null
        return new GoComparison(this.dgeGoTerms, this.dasGoTerms, this.geneOntology,
                this.goMethod, this.mtcMethod);
    }

    @Override
    public IsopretStats getIsopretStats() {
        return isopretStats;
    }

    @Override
    public String getGoReport() {
        DasDgeGoVisualizer visualizer = new DasDgeGoVisualizer(geneOntology, dasGoTerms, dgeGoTerms);
        return visualizer.getTsv();
    }

    @Override
    public Optional<String> getGoReportDefaultFilename() {
        if (rnaSeqResultsFile == null) return Optional.empty();
        String name = rnaSeqResultsFile.getName() + "-" + mtcMethod.name() + "-" + goMethod.name() + ".tsv";
        return Optional.of(name);
    }

    @Override
    public List<Visualizable> getDgeForGoTerm(TermId goId) {
        double expThres = this.thresholder.getExpressionPepThreshold();
        List<GeneResult> dge = this.thresholder.getRawResults().values()
                .stream()
                .filter(h -> h.hasDifferentialExpressionResult(expThres))
                .toList();
        // now figure out which of these genes are annotated to goId
        Set<TermId> domainIdSet = this.geneContainer.getDomainItemsAnnotatedByGoTerm(goId);
        List<AccessionNumber> ensgAccessionList = dge.stream().filter(d -> domainIdSet.contains(d.getGeneAccession().toTermId()))
                .map(GeneResult::getGeneModel)
                .map(GeneModel::ensemblGeneId)
                .toList();
        // transform to visualizable
        List<Visualizable> visualizables = new ArrayList<>();
        for (AccessionNumber ensg : ensgAccessionList) {
            visualizables.add(getVisualizableForGene(ensg));
        }
        return visualizables;
    }

    @Override
    public List<Visualizable> getDasForGoTerm(TermId goId) {
        double splicingPepThreshold = this.thresholder.getSplicingPepThreshold();
        LOGGER.info("getDasForGoTerm, splicingPepThreshold={}", splicingPepThreshold);
        List<GeneResult> das = this.thresholder.getRawResults().values()
                .stream()
                .filter(h -> h.hasDifferentialSplicingResult(splicingPepThreshold))
                .toList();
        LOGGER.info("getDasForGoTerm, das.size={}", das.size());
        // now figure out which of these genes are annotated to goId
        Set<TermId> domainIdSet  = this.transcriptContainer.getDomainItemsAnnotatedByGoTerm(goId);
        // when we get here, the domain ids are transcript ids.
        LOGGER.info("getDasForGoTerm, domainIdSet.size={} for go={}", domainIdSet.size(), goId.getValue());
        Set<AccessionNumber> ensgAccessionSet = new HashSet<>();
        for (GeneResult result : das) {
            for (AccessionNumber transcriptAccession : result.getTranscriptMap().keySet()) {
                if (domainIdSet.contains(transcriptAccession.toTermId())) {
                    ensgAccessionSet.add(result.getGeneModel().ensemblGeneId());
                    break; // done with this gene
                }
            }
        }
        LOGGER.info("getDasForGoTerm, symbols.size={} ", ensgAccessionSet.size());
        // transform to visualizable
        List<Visualizable> visualizables = new ArrayList<>();
        for (AccessionNumber ensg : ensgAccessionSet) {
            visualizables.add(getVisualizableForGene(ensg));
        }
        return visualizables;
    }

    @Override
    public double getSplicingPepThreshold() {
        return thresholder.getSplicingPepThreshold();
    }
    @Override
    public Map<GoTermIdPlusLabel, Integer> getGoAnnotationsForTranscript(Set<TermId> annotatedItemTermIds) {
        Map<GoTermIdPlusLabel, Integer> countMap = new HashMap<>();
        Map<TermId, IsopretAnnotations> annotMap =   transcriptContainer.getAssociationMap();

        for (IsopretAnnotations annots : annotMap.values()) {
            for (TermAnnotation a : annots.getAnnotations()) {
                IsopretTermAnnotation itanot = (IsopretTermAnnotation) a;
                if (annotatedItemTermIds.contains(itanot.getItemId())) {
                    TermId goId = itanot.getTermId();
                    String label = geneOntology.getTermLabel(goId).orElse("n/a");
                    GoTermIdPlusLabel gtlab = new GoTermIdPlusLabel(goId, label);
                    countMap.merge(gtlab, 1, Integer::sum);
                }
            }
        }
        return countMap;
    }

}
