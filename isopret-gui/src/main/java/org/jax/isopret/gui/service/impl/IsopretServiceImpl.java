package org.jax.isopret.gui.service.impl;

import javafx.application.HostServices;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.jax.isopret.core.go.GoMethod;
import org.jax.isopret.core.go.MtcMethod;
import org.jax.isopret.core.hbadeals.HbaDealsIsoformSpecificThresholder;
import org.jax.isopret.core.hbadeals.HbaDealsResult;
import org.jax.isopret.core.interpro.DisplayInterproAnnotation;
import org.jax.isopret.core.interpro.InterproMapper;
import org.jax.isopret.core.io.IsopretDownloader;
import org.jax.isopret.core.transcript.AccessionNumber;
import org.jax.isopret.core.transcript.AnnotatedGene;
import org.jax.isopret.core.transcript.Transcript;
import org.jax.isopret.core.visualization.EnsemblVisualizable;
import org.jax.isopret.core.visualization.GoAnnotationMatrix;
import org.jax.isopret.core.visualization.Visualizable;
import org.jax.isopret.gui.configuration.IsopretDataLoadTask;
import org.jax.isopret.gui.service.IsopretService;
import org.jax.isopret.gui.service.model.GoComparison;
import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.ontology.data.Ontology;
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

@Service
public class IsopretServiceImpl implements IsopretService  {
    private static final Logger LOGGER = LoggerFactory.getLogger(IsopretServiceImpl.class);

    /** File for reading/writing settings. */
    @Autowired
    File isopretSettingsFile;

    @Autowired
    private HostServices hostServices;

    private final Properties pgProperties;
    private final StringProperty downloadDirProp;
    private final StringProperty hbaDealsFileProperty;
    private final DoubleProperty downloadCompletenessProp;
    private File hbaDealsFile = null;
    private GoMethod goMethod = GoMethod.TFT;
    private MtcMethod mtcMethod = MtcMethod.NONE;

    private List<String> analysisErrors = null;
    private Ontology geneOntology = null;
    private InterproMapper interproMapper = null;
    private HbaDealsIsoformSpecificThresholder thresholder = null;
    private Map<String, List<Transcript>> geneSymbolToTranscriptMap = Map.of();
    private List<GoTerm2PValAndCounts> dasGoTerms = List.of();
    private List<GoTerm2PValAndCounts> dgeGoTerms = List.of();
    private AssociationContainer<TermId> transcriptContainer = null;
    private AssociationContainer<TermId> geneContainer = null;
    private Map<AccessionNumber, List<Transcript>> geneIdToTranscriptMap;
    /** Key: transcript id; value: set of Annotating GO Terms. */
    private Map<TermId, Set<TermId>> transcript2GoMap = Map.of();

    public IsopretServiceImpl(Properties pgProperties) {
        this.pgProperties = pgProperties;
        if (this.pgProperties.containsKey("downloaddir")) {
            this.downloadDirProp = new SimpleStringProperty(this.pgProperties.getProperty("downloaddir"));
        } else {
            this.downloadDirProp = new SimpleStringProperty("");
        }
        this.hbaDealsFileProperty = new SimpleStringProperty("");
        this.downloadCompletenessProp = new SimpleDoubleProperty(calculateDownloadCompleteness());
    }

    private double calculateDownloadCompleteness() {
        if (sourcesDownloaded()) return 1.0d;
        else return 0.0;
    }


    @Override
    public boolean sourcesDownloaded() {
        if (! pgProperties.containsKey("downloaddir")) {
            LOGGER.warn("Download directory not initialized");
            return false;
        }
        String downloadPath = pgProperties.getProperty("downloaddir");
        File downloadDir = new File(downloadPath);
        if (! downloadDir.isDirectory()) {
            LOGGER.warn("Download directory not initialized");
            return false;
        }
        Set<String> expectedDownloadedFiles = getExpectedDownloadedFiles();
        File [] downloadedFiles = downloadDir.listFiles();
        if (downloadedFiles == null) {
            LOGGER.warn("No downloaded files");
            return false;
        }
        Set<String> basenames = Arrays.stream(downloadedFiles).map(File::getName).collect(Collectors.toSet());
        int notfound = 0;
        for (String name : expectedDownloadedFiles) {
            if (! basenames.contains(name)) {
                LOGGER.error("Did not find {} in download directory at {}.", name, downloadDir);
                notfound++;
            }
        }
        return notfound == 0; // We are OK if we find all required files.
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
            e.printStackTrace();
        }
    }
    // TODO Add the other files to the download
    @Override
    public Set<String> getExpectedDownloadedFiles() {
        return Set.of("go.json", "goa_human.gaf", "hg38_ensembl.ser", "hgnc_complete_set.txt");
    }

    @Override
    public void downloadSources(File file){
        IsopretDownloader downloader = new IsopretDownloader(file.getAbsolutePath(), true);
        downloader.download();
        pgProperties.setProperty("downloaddir", file.getAbsolutePath());
    }

    @Override
    public void setHbaDealsFile(File file) {
        this.hbaDealsFile = file;
        this.hbaDealsFileProperty.setValue(file.getAbsolutePath());
        LOGGER.info("Set HBA-DEALS file to {}", this.hbaDealsFile);
    }

    @Override
    public StringProperty downloadDirProperty() {
        return this.downloadDirProp;
    }

    @Override
    public StringProperty hbaDealsFileProperty() {
        return hbaDealsFileProperty;
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
    public Optional<File> getHbaDealsFileOpt() {
        if (this.hbaDealsFile == null || ! this.hbaDealsFile.isFile()) {
            return Optional.empty();
        } else  {
            return Optional.of(this.hbaDealsFile);
        }
    }

    @Override
    public void setData(IsopretDataLoadTask task) {
        this.analysisErrors = task.getErrors();
        this.geneOntology = task.getGeneOntology();
        this.interproMapper = task.getInterproMapper();
        this.geneSymbolToTranscriptMap = task.getGeneSymbolToTranscriptMap();
        this.geneContainer = task.getGeneContainer();
        this.transcriptContainer = task.getTranscriptContainer();
        this.dgeGoTerms = task.getDgeResults();
        this.dasGoTerms = task.getDasResults();
        this.thresholder = task.getIsoformSpecificThresholder();
        this.geneIdToTranscriptMap = task.getGeneIdToTranscriptMap();
        this.transcript2GoMap = task.getTranscript2GoMap();
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
            resultsMap.put("Differentially spliced genes", String.valueOf(thresholder.getDasGeneCount()));
            resultsMap.put("FDR threshold", String.valueOf(thresholder.getFdrThreshold()));
            resultsMap.put("Significant DGE GO Terms", String.valueOf(this.dgeGoTerms.size()));
            resultsMap.put("Significant DAS GO Terms", String.valueOf(this.dasGoTerms.size()));

        }
        return resultsMap;
    }

    @Override
    public Visualizable getVisualizableForGene(String symbol) {
        if (! this.thresholder.getRawResults().containsKey(symbol)) {
            LOGGER.error("Could not find HBADEALS results for {}.", symbol);
            return null;
        }
        List<Transcript> transcripts = this.geneSymbolToTranscriptMap.get(symbol);
        HbaDealsResult result = thresholder.getRawResults().get(symbol);
        double splicingThreshold = thresholder.getSplicingThreshold();
        double expressionThreshold = thresholder.getExpressionThreshold();
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
    public List<Visualizable> getGeneVisualizables() {
        int notfound = 0;
        List<Visualizable> visualizables = new ArrayList<>();
        for (var r : thresholder.getRawResults().values()) {
            if (! this.geneSymbolToTranscriptMap.containsKey(r.getSymbol())) {
                notfound++;
                continue;
            }
            List<Transcript> transcripts = this.geneSymbolToTranscriptMap.get(r.getSymbol());
            HbaDealsResult result = thresholder.getRawResults().get(r.getSymbol());
            double splicingThreshold = thresholder.getSplicingThreshold();
            double expressionThreshold = thresholder.getExpressionThreshold();
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
        if (notfound > 0) {
            LOGGER.warn("Could not find transcript map for {} genes", notfound);
        }
       return visualizables;
    }

    public Ontology getGeneOntology() {
        return this.geneOntology;
    }

    @Override
    public String getDasLabel() {
        int n = this.dasGoTerms.size();
        return "GO Overrepresentation analysis of differentially spliced isoforms: " + n + " significantly overrepresented terms.";
    }

    @Override
    public String getDgeLabel() {
        int n = this.dgeGoTerms.size();
        return "GO Overrepresentation analysis of differentially expressed genes: " + n + " significantly overrepresented terms.";
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
        String nterms = String.valueOf(geneOntology.countNonObsoleteTerms());
        return "Gene Ontology (version: " + version +"), " + nterms + " terms.";
    }

    @Override
    public HostServices getHostServices() {
        return hostServices;
    }

    public AssociationContainer<TermId> getTranscriptContainer() {
        return transcriptContainer;
    }

    public AssociationContainer<TermId> getGeneContainer() {
        return geneContainer;
    }


    public GoAnnotationMatrix getGoAnnotationMatrixForGene(HbaDealsResult result) {
        AccessionNumber accession = result.getGeneAccession();
        Set<TermId> expressedTranscriptSet = result.getTranscriptMap().keySet().stream()
                .map(AccessionNumber::toTermId)
                .collect(Collectors.toSet());
        Set<TermId> significantGoSet = dgeGoTerms.stream()
                .map(GoTerm2PValAndCounts::getGoTermId)
                .collect(Collectors.toSet());
        return new GoAnnotationMatrix(this.geneOntology,
                this.geneIdToTranscriptMap,
                this.transcript2GoMap,
                significantGoSet,
                accession,
                expressedTranscriptSet);
    }

    @Override
    public GoComparison getGoComparison() {
        // note that if we can access the button, then we have cnstructed the GO tab
        // and the following three variables are not null
        return new GoComparison(this.dgeGoTerms, this.dasGoTerms, this.geneOntology);
    }

}
