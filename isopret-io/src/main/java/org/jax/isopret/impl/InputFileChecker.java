package org.jax.isopret.impl;


import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * If there is an issue with the downloads, this class will check each expected file and will
 * return two maps to show the names and URLs (as Strings) of the successful and failed downloads.
 * A widget will then guide the user in downloading anything that is missing.
 * @author Peter Robinson
 */
public class InputFileChecker {


    private final Map<String, String> successulDownloads;

    private final Map<String, String> failedDownloads;

    public InputFileChecker(String dataDownload) {
        successulDownloads = new HashMap<>();
        failedDownloads = new HashMap<>();
        Set<DownloadItem> items = IsopretDataResolver.allDownloadItems();
        for (var ditem: items) {
            String basename = ditem.basename();
            String url = ditem.url().toString();
            Path path = Path.of(dataDownload).resolve(basename);
            if (path.toFile().isFile()) {
                successulDownloads.put(basename, url);
            } else {
                failedDownloads.put(basename, url);
            }
        }
    }

    public Map<String, String> getSuccessulDownloads() {
        return successulDownloads;
    }

    public Map<String, String> getFailedDownloads() {
        return failedDownloads;
    }
}
