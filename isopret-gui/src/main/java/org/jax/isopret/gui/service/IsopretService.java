package org.jax.isopret.gui.service;

import java.io.File;
import java.util.Set;

public interface IsopretService {

    /* Settings */
    void saveSettings();
    Set<String> getExpectedDownloadedFiles();


    /** Source files. */
    boolean sourcesDownloaded();
    void downloadSources(File file);
}
