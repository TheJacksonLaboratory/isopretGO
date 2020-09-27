package org.jax.prositometry.io;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Command to download the {@code hp.obo} and {@code phenotype.hpoa} files that
 * we will need to run the LIRICAL approach.
 * @author Peter N Robinson
 */
public class PrositometryDownloader {
    //private static final Logger logger = LoggerFactory.getLogger(PrositometryDownloader.class);
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



    public PrositometryDownloader(String path){
        this(path,false);
    }

    public PrositometryDownloader(String path, boolean overwrite){
        this.downloadDirectory=path;
        this.overwrite=overwrite;
    }

    /**
     * Download the files unless they are already present.
     */
    public void download() {
        int downloaded = 0;
        downloaded += downloadFileIfNeeded(PROSITE_DAT, PROSITE_DAT_URL);
        downloaded += downloadFileIfNeeded(ENSEMBL_CDNA,ENSEMBL_CDNA_URL);
        System.out.printf("[INFO] Downloaded %d files to \"%s\" (%d files were previously downloaded)\n",
                downloaded,
                downloadDirectory,
                4- downloaded);
    }


    private int downloadFileIfNeeded(String filename, String webAddress) {
        File f = new File(String.format("%s%s%s",downloadDirectory,File.separator,filename));
        if (f.exists() && (! overwrite)) {
            System.out.printf("Cowardly refusing to download %s since we found it at %s.\n",
                    filename,
                    f.getAbsolutePath());
            return 0;
        }
        FileDownloader downloader=new FileDownloader();
        try {
            URL url = new URL(webAddress);
            downloader.copyURLToFile(url, new File(f.getAbsolutePath()));
        } catch (MalformedURLException e) {
            System.err.printf("Malformed URL for %s [%s]: %s",filename, webAddress,e.getMessage());
        } catch (FileDownloadException e) {
            System.err.printf("Error downloading %s from %s: %s\"" ,filename, webAddress,e.getMessage());
        }
        System.out.println("[INFO] Downloaded " + filename);
        return 1;
    }





}
