package org.jax.isopret.gui.service;


import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import org.jax.isopret.core.analysis.IsopretStats;
import org.jax.isopret.core.impl.rnaseqdata.RnaSeqAnalysisMethod;
import org.jax.isopret.data.GoTermIdPlusLabel;
import org.jax.isopret.data.AccessionNumber;
import org.jax.isopret.model.AnnotatedGene;
import org.jax.isopret.visualization.Visualizable;
import org.jax.isopret.gui.service.model.GeneOntologyComparisonMode;
import org.jax.isopret.gui.service.model.GoComparison;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.analysis.stats.GoTerm2PValAndCounts;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface IsopretService {

    /* Settings */
    void saveSettings();

    Set<String> getExpectedDownloadedFiles();


    /**
     * Source files.
     */
    double sourcesDownloaded();

    void setDownloadDir(File file);

    StringProperty downloadDirProperty();

    StringProperty rnaSeqResultsFileProperty();


    DoubleProperty downloadCompletenessProperty();

    /**
     * Set the RNA-Seq results file (with assessment of differential expression and splicing)
     * @param file The RNA-seq file (e.g., output of HBA-DEALS)
     * @param method One of {@link RnaSeqAnalysisMethod}
     */
    void setRnaSeqFile(File file, RnaSeqAnalysisMethod method);

    void setGoMethod(String method);

    void setMtcMethod(String method);

    RnaSeqAnalysisMethod getRnaSeqMethod();

    Optional<File> getDownloadDir();

    Optional<File> getRnaSeqResultsFileOpt();

    void setData(IsopretDataLoadTask task);

    Map<String, String> getResultsSummaryMap();

    List<GoTerm2PValAndCounts> getDasGoTerms();

    List<GoTerm2PValAndCounts> getDgeGoTerms();

    Ontology getGeneOntology();

    String getGoLabel(GeneOntologyComparisonMode mode);

    String getGoMethods();

    String getGoSummary();


    /** Get {@link Visualizable} objects for all genes. */
    List<Visualizable> getGeneVisualizables();
    /** Get {@link Visualizable} objects for a subset of genes. */
    List<Visualizable> getGeneVisualizables(Set<AccessionNumber> includedEnsgAccessionSet);
    /** Get {@link Visualizable} objects for a specific genes. */
    Visualizable getVisualizableForGene(AccessionNumber ensgAccesion);
    GoComparison getGoComparison();

    int totalSignificantGoTermsAnnotatingGene(Set<TermId> goIds);

    IsopretStats getIsopretStats();


    String getGoReport();

    Optional<String>  getGoReportDefaultFilename();

    List<Visualizable> getDgeForGoTerm(TermId goId);
    List<Visualizable> getDasForGoTerm(TermId goId);

    List<AnnotatedGene> getAnnotatedGeneList();
    double getSplicingPepThreshold();
    Map<GoTermIdPlusLabel, Integer> getGoAnnotationsForTranscript(Set<TermId> annotatedItemTermIds);


}
