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
        downloader = new IsopretDownloader(path);
        this.downloadDir = path;
    }

    public IsopretFxDownloadTask(String path, boolean overwrite) {
        downloader = new IsopretDownloader(path, overwrite);
        this.downloadDir = path;
    }

    @Override
    protected Void call() {
        long totalDownloads = 8;
        long i = 0;
        this.updateProgress(i, totalDownloads);
        updateMessage("Starting go.json download...");
        downloader.downloadGoJson();
        this.updateProgress(++i, totalDownloads);
        updateMessage("Starting Jannovar transcript file download...");
        downloader.downloadJannovar();
        this.updateProgress(++i, totalDownloads);
        updateMessage("Starting HGNC download...");
        downloader.downloadHgnc();
        this.updateProgress(++i, totalDownloads);
        updateMessage("Starting interpro domain download");
        downloader.downloadInterproDomains();
        this.updateProgress(++i, totalDownloads);
        updateMessage("Starting interpro domain description download");
        downloader.downloadInterproDomainDesc();
        this.updateProgress(++i, totalDownloads);
        updateMessage("Starting isoform function (MF) download");
        downloader.downloadIsoformFunctionMfList();
        updateProgress(++i, totalDownloads);
        updateMessage("Starting isoform function (BP) download");
        downloader.downloadIsoformFunctionBpList();
        updateProgress(++i, totalDownloads);
        updateMessage("Starting isoform function (CC) download");
        downloader.downloadIsoformFunctionCcList();
        updateProgress(++i, totalDownloads);
        updateMessage("Finished download to " + downloadDir);
        return null;
    }
}
