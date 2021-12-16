package org.jax.isopret.gui.service.impl;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.jax.isopret.core.io.IsopretDownloader;
import org.jax.isopret.gui.service.IsopretService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class IsopretServiceImpl implements IsopretService  {
    private static final Logger LOGGER = LoggerFactory.getLogger(IsopretServiceImpl.class);

    /** File for reading/writing settings. */
    @Autowired
    File isopretSettingsFile;

    @Autowired
    Properties pgProperties;

    private final StringProperty downloadDirProp = new SimpleStringProperty(null);

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
    public StringProperty downloadDirProperty() {
        return this.downloadDirProp;
    }
}
