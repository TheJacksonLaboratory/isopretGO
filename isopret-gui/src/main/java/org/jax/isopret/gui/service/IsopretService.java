package org.jax.isopret.gui.service;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import org.jax.isopret.core.analysis.IsopretStats;
import org.jax.isopret.core.visualization.Visualizable;
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
    boolean sourcesDownloaded();

    void setDownloadDir(File file);

    StringProperty downloadDirProperty();

    StringProperty hbaDealsFileProperty();

    DoubleProperty downloadCompletenessProperty();

    void setHbaDealsFile(File file);

    void setGoMethod(String method);

    void setMtcMethod(String method);

    Optional<File> getDownloadDir();

    Optional<File> getHbaDealsFileOpt();

    void setData(IsopretDataLoadTask task);

    Map<String, String> getResultsSummaryMap();

    List<GoTerm2PValAndCounts> getDasGoTerms();

    List<GoTerm2PValAndCounts> getDgeGoTerms();

    Ontology getGeneOntology();

    String getGoLabel(GeneOntologyComparisonMode mode);

    String getGoMethods();

    String getGoSummary();

    HostServicesWrapper getHostServices();

    Visualizable getVisualizableForGene(String symbol);

    List<Visualizable> getGeneVisualizables();

    GoComparison getGoComparison();

    int totalSignificantGoTermsAnnotatingGene(Set<TermId> goIds);

    IsopretStats getIsopretStats();


    String getGoReport();

    Optional<String>  getGoReportDefaultFilename();

    List<Visualizable> getDgeForGoTerm(TermId goId);
    List<Visualizable> getDasForGoTerm(TermId goId);
}
