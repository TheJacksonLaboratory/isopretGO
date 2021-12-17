package org.jax.isopret.gui.service;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

import java.io.File;
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
}
