package org.jax.isopret.gui.service;

import javafx.application.HostServices;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import org.jax.isopret.core.visualization.Visualizable;
import org.jax.isopret.gui.configuration.IsopretDataLoadTask;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.stats.GoTerm2PValAndCounts;

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

    void downloadSources(File file);

    StringProperty downloadDirProperty();

    DoubleProperty downloadCompletenessProperty();

    void setHbaDealsFile(File file);

    void setGoMethod(String method);

    void setMtcMethod(String method);

    void doIsopretAnalysis();

    Optional<File> getDownloadDir();

    Optional<File> getHbaDealsFileOpt();

    void setData(IsopretDataLoadTask task);

//    List<HbaDealsGeneRow> getHbaDealsRows();

    Map<String, String> getResultsSummaryMap();

    String getHtmlForGene(String symbol);

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

}
