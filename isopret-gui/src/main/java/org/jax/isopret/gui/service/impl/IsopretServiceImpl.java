package org.jax.isopret.gui.service.impl;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.jax.isopret.core.go.GoMethod;
import org.jax.isopret.core.go.MtcMethod;
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
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class IsopretServiceImpl implements IsopretService  {
    private static final Logger LOGGER = LoggerFactory.getLogger(IsopretServiceImpl.class);

    /** File for reading/writing settings. */
    @Autowired
    File isopretSettingsFile;

    private final Properties pgProperties;
    private final StringProperty downloadDirProp;
    private final DoubleProperty downloadCompletenessProp;
    private File hbaDealsFile = null;
    private GoMethod goMethod = GoMethod.TFT;
    private MtcMethod mtcMethod = MtcMethod.NONE;

    public IsopretServiceImpl(Properties pgProperties) {
        this.pgProperties = pgProperties;
        if (this.pgProperties.containsKey("downloaddir")) {
            this.downloadDirProp = new SimpleStringProperty(this.pgProperties.getProperty("downloaddir"));
        } else {
            this.downloadDirProp = new SimpleStringProperty("");
        }
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
        LOGGER.info("Set HBA-DEALS file to {}", this.hbaDealsFile);
    }

    @Override
    public StringProperty downloadDirProperty() {
        return this.downloadDirProp;
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
    public void doIsopretAnalysis() {
        LOGGER.info("Starting isopret analysis");
        if (this.hbaDealsFile == null) {
            LOGGER.error("Cannot do isopret analysis because HBA-DEALS file not initialized");
            return;
        }
        LOGGER.info("GO Method: {}", this.goMethod.toString());
        LOGGER.info("MTC Method: {}", this.mtcMethod.toString());
        LOGGER.info("HBA-DEALS file: {}", this.hbaDealsFile);
        // get files from download dir
        LOGGER.info("Getting data files from download dir: {}", this.downloadDirProperty().get());
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
}
