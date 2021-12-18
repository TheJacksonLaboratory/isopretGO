package org.jax.isopret.gui.service;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;

import java.io.File;
import java.util.Optional;
import java.util.Set;

public interface IsopretService {

    /* Settings */
    void saveSettings();
    Set<String> getExpectedDownloadedFiles();


    /** Source files. */
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
}
