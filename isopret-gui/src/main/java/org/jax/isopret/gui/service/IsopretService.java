package org.jax.isopret.gui.service;

import javafx.application.HostServices;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import org.jax.isopret.core.visualization.Visualizable;
import org.jax.isopret.gui.configuration.IsopretDataLoadTask;
import org.jax.isopret.gui.service.model.GoComparison;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.analysis.stats.GoTerm2PValAndCounts;

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

    String getDasLabel();

    String getDgeLabel();

    String getGoMethods();

    String getGoSummary();

    HostServices getHostServices();

    Visualizable getVisualizableForGene(String symbol);

    List<Visualizable> getGeneVisualizables();

    GoComparison getGoComparison();
}
