package org.jax.isopret.io;


import org.jax.isopret.except.IsopretRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

/**
 * Command to download the {@code hp.obo} and {@code phenotype.hpoa} files that
 * we will need to run the LIRICAL approach.
 * @author Peter N Robinson
 */
public class IsopretDownloader {
    private static final Logger LOGGER = LoggerFactory.getLogger(IsopretDownloader.class);
    /** Directory to which we will download the files. */
    private final String downloadDirectory;
    /** If true, download new version whether or not the file is already present. */
    private final boolean overwrite;

    private final static String PROSITE_DAT = "prosite.dat";

    private final static String PROSITE_DAT_URL ="ftp://ftp.expasy.org/databases/prosite/prosite.dat";

    private final static String ENSEMBL_CDNA_URL
            ="ftp://ftp.ensembl.org/pub/release-101/fasta/homo_sapiens/cdna/Homo_sapiens.GRCh38.cdna.all.fa.gz";
    /** Basename of the file with cDNA sequences for ensembl genes. */
    private final static String ENSEMBL_CDNA ="Homo_sapiens.GRCh38.cdna.all.fa.gz";

    private final static String GO_JSON = "go.json";
    private final static String GO_JSON_URL = "http://purl.obolibrary.org/obo/go.json";
    private final static String GO_ANNOT = "goa_human.gaf";
    private final static String GO_ANNOT_GZ = "goa_human.gaf.gz";
    private final static String GO_ANNOT_URL = "http://geneontology.org/gene-associations/goa_human.gaf.gz";

    private static final String JannovarZenodoUrl = "https://zenodo.org/record/4311513/files/hg38_ensembl.ser?download=1";
    private static final String JannovarFilename = "hg38_ensembl.ser";

    private static final String HGNC_URL = "ftp://ftp.ebi.ac.uk/pub/databases/genenames/hgnc/tsv/hgnc_complete_set.txt";
    private static final String HGNC_FILENAME = "hgnc_complete_set.txt";


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
        //downloadFileIfNeeded(PROSITE_DAT, PROSITE_DAT_URL);
       // downloadFileIfNeeded(ENSEMBL_CDNA,ENSEMBL_CDNA_URL);
        downloadGzipFileIfNeeded(GO_ANNOT,GO_ANNOT_GZ, GO_ANNOT_URL);
        downloadFileIfNeeded(GO_JSON, GO_JSON_URL);
        downloadFileIfNeeded(JannovarFilename, JannovarZenodoUrl);
        downloadFileIfNeeded(HGNC_FILENAME, HGNC_URL);
    }


    private void downloadGzipFileIfNeeded(String filename, String gzFilename, String webAddress) {
        File file = new File(String.format("%s%s%s",downloadDirectory,File.separator,filename));
        File gzfile = new File(String.format("%s%s%s",downloadDirectory,File.separator,gzFilename));
        if (! ( file.exists() || gzfile.exists()) && ! overwrite ) {
            downloadFileIfNeeded(gzFilename, webAddress);
        }
        if (! file.exists()) {
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




    private void downloadFileIfNeeded(String filename, String webAddress) {
        File f = new File(String.format("%s%s%s",downloadDirectory,File.separator,filename));
        if (f.exists() && (! overwrite)) {
            LOGGER.info("Cowardly refusing to download {} since we found it at {}.\n",
                    filename,
                    f.getAbsolutePath());
            return;
        }
        FileDownloader downloader=new FileDownloader();
        try {
            URL url = new URL(webAddress);
            downloader.copyURLToFile(url, new File(f.getAbsolutePath()));
        } catch (MalformedURLException e) {
            LOGGER.error("Malformed URL for {} [{}]: {}",filename, webAddress,e.getMessage());
        } catch (FileDownloadException e) {
            LOGGER.error("Error downloading {} from {}: {}\"" ,filename, webAddress,e.getMessage());
        }
        LOGGER.trace("[INFO] Downloaded " + filename);
    }

}
