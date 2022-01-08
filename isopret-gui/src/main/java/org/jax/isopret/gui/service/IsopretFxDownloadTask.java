package org.jax.isopret.gui.service;

import javafx.concurrent.Task;
import org.jax.isopret.core.io.IsopretDownloader;

/**
 * A version of {@link IsopretDownloader} intended to be used as a {@link Task} in
 * the Isopret GUI.
 *
 * @author Peter Robinson
 */
public class IsopretFxDownloadTask extends Task<Void> {
    private final IsopretDownloader downloader;
    private final String downloadDir;

    public IsopretFxDownloadTask(String path) {
        boolean overwrite = false;
        downloader = new IsopretDownloader(path, overwrite);
        this.downloadDir = path;
    }

    public IsopretFxDownloadTask(String path, boolean overwrite) {
        downloader = new IsopretDownloader(path, overwrite);
        this.downloadDir = path;
    }

    @Override
    protected Void call() {
        long totalDownloads = 4;
        long i = 0;
        this.updateProgress(i, totalDownloads);
        updateMessage("Starting go.json download...");
        downloader.downloadGoJson();
        this.updateProgress(++i, totalDownloads);
        updateMessage("Starting goa_human.gaf download...");
        downloader.downloadGoAnnotationFile();
        this.updateProgress(++i, totalDownloads);
        updateMessage("Starting Jannovar transcript file download...");
        downloader.downloadJannovar();
        this.updateProgress(++i, totalDownloads);
        updateMessage("Starting HGNC download...");
        downloader.downloadHgnc();
        this.updateProgress(++i, totalDownloads);
        updateMessage("Finished download to " + downloadDir);
        return null;
    }
}