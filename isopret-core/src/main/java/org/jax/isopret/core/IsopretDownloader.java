package org.jax.isopret.core;


import org.jax.isopret.core.configuration.IsopretDataResolver;
import org.jax.isopret.except.IsopretRuntimeException;
import org.jax.isopret.core.impl.download.FileDownloadException;
import org.jax.isopret.core.impl.download.FileDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

/**
 * Command to download the {@code hp.obo} and {@code phenotype.hpoa} files that
 * we will need to run Isopret. Note that this class is also used by
 * the IsopretFxDownloader (a Task) so several of the methods are made public that
 * do not need to be public for the CLI download.
 * @author Peter N Robinson
 */
public class IsopretDownloader {
    private static final Logger LOGGER = LoggerFactory.getLogger(IsopretDownloader.class);
    /** Directory to which we will download the files. */
    private final String downloadDirectory;
    /** If true, download new version whether or not the file is already present. */
    private final boolean overwrite;


    public IsopretDownloader(String path){
        this(path,false);
    }

    public IsopretDownloader(String path, boolean overwrite){
        this.downloadDirectory=path;
        this.overwrite=overwrite;
    }

    /**
     * Download the files unless they are already present.
     */
    public void download() {
        downloadGoJson();
        downloadJannovar();
        downloadHgnc();
        downloadInterproDomainDesc();
        downloadInterproDomains();
        downloadIsoformFunctionMfList();
        downloadIsoformFunctionBpList();
        downloadIsoformFunctionCcList();
    }

    /**
     * Download the go.json file
     */
    public void downloadGoJson() {
        downloadFileIfNeeded(IsopretDataResolver.go());
    }

    public void downloadJannovar() {
        downloadFileIfNeeded(IsopretDataResolver.jannovarHg38());
    }

    public void downloadHgnc() {
        downloadFileIfNeeded(IsopretDataResolver.hgnc());
    }

    public void downloadInterproDomainDesc() {
        downloadFileIfNeeded(IsopretDataResolver.interproDomainDesc());
    }

    public void downloadInterproDomains() {
        downloadFileIfNeeded(IsopretDataResolver.interproDomains());
    }

    public void downloadIsoformFunctionMfList() {
        downloadFileIfNeeded(IsopretDataResolver.isoformFunctionMf());
    }

    public void downloadIsoformFunctionBpList() {
        downloadFileIfNeeded(IsopretDataResolver.isoformFunctionBp());
    }

    public void downloadIsoformFunctionCcList() {
        downloadFileIfNeeded(IsopretDataResolver.isoformFunctionCc());
    }


    /**
     * Use to download a file that is gzip'd and then needs to be unzipped (Not needed at present).
     * @param gzipDownload a download item for a gzipped file
     */
    private void downloadGzipFileIfNeeded( DownloadItem gzipDownload) {
        String filename = gzipDownload.basename().replace(".gz", "");
        File file = new File(String.format("%s%s%s",downloadDirectory,File.separator,filename));
        File gzfile = new File(String.format("%s%s%s",downloadDirectory,File.separator,gzipDownload.basename()));
        if (! gzfile.exists() || overwrite ) { // download gzip file if needed or if users wants to overwrite
            downloadFileIfNeeded(gzipDownload);
        }
        if (! gzfile.isFile()) {
            // when we get here, the gzFile should exist and be a File
            LOGGER.error("Could not download {}", gzfile.getAbsolutePath());
            return;
        }
        if (! file.exists()) { // if we have not yet g-unzipped the file, do so
            LOGGER.info("Did not find file \"{}\", so we are g-unzipping \"{}\"", file.getAbsolutePath(), gzfile.getAbsolutePath());
            Path source = Paths.get(gzfile.getAbsolutePath());
            Path target = Paths.get(file.getAbsolutePath());
            try (GZIPInputStream gis = new GZIPInputStream(
                    new FileInputStream(source.toFile()));
                 FileOutputStream fos = new FileOutputStream(target.toFile())) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = gis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
            } catch (IOException e) {
                throw new IsopretRuntimeException("Could not un-gzip GAF file: " + e.getMessage());
            }
        }
    }




    private void downloadFileIfNeeded(DownloadItem downloadItem) {
        String filename = downloadItem.basename();
        URL url = downloadItem.url();
        File f = new File(String.format("%s%s%s",downloadDirectory,File.separator,filename));
        if (f.exists() && (! overwrite)) {
            LOGGER.info("Cowardly refusing to download {} since we found it at {}.\n",
                    filename,
                    f.getAbsolutePath());
            return;
        }
        FileDownloader downloader = new FileDownloader();
        try {
            downloader.copyURLToFile(url, new File(f.getAbsolutePath()));
        } catch (FileDownloadException e) {
            LOGGER.error("Error downloading {} from {}: {}\"" ,filename, url ,e.getMessage());
        }
        LOGGER.trace("[INFO] Downloaded " + filename);
    }

}
