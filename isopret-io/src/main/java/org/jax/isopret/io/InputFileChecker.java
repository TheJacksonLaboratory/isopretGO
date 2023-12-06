package org.jax.isopret.io;


import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jax.isopret.io.impl.DownloadItem;
import org.jax.isopret.io.impl.IsopretDataResolver;

/**
 * If there is an issue with the downloads, this class will check each expected file and will
 * return two maps to show the names and URLs (as Strings) of the successful and failed downloads.
 * A widget will then guide the user in downloading anything that is missing.
 * @author Peter Robinson
 */
public class InputFileChecker {


    private final Map<String, String> successfulDownloads;

    private final Map<String, String> failedDownloads;

    public InputFileChecker(String dataDownload) {
        successfulDownloads = new HashMap<>();
        failedDownloads = new HashMap<>();
        Set<DownloadItem> items = IsopretDataResolver.allDownloadItems();
        for (var ditem: items) {
            String basename = ditem.basename();
            String url = ditem.url().toString();
            Path path = Path.of(dataDownload).resolve(basename);
            if (path.toFile().isFile()) {
                successfulDownloads.put(basename, url);
            } else {
                failedDownloads.put(basename, url);
            }
        }
    }

    public Map<String, String> getSuccessfulDownloads() {
        return successfulDownloads;
    }

    public Map<String, String> getFailedDownloads() {
        return failedDownloads;
    }
}
