package org.jax.isopret.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class IsopretDataResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(IsopretDataResolver.class);
    private final static String GO_JSON = "go.json";
    private final static String GO_JSON_URL = "http://purl.obolibrary.org/obo/go.json";
    private static final String JANNOVAR_ZENODO_URL = "https://zenodo.org/record/4311513/files/hg38_ensembl.ser?download=1";
    private static final String JANNOVAR_FILENAME = "hg38_ensembl.ser";
    private static final String HGNC_URL = "ftp://ftp.ebi.ac.uk/pub/databases/genenames/hgnc/tsv/hgnc_complete_set.txt";
    private static final String HGNC_FILENAME = "hgnc_complete_set.txt";
    /** The Base URL of the zenodo repository where we store various input files for Isopret. */
    private static final String ZENODO_BASE_URL = "https://zenodo.org/record/6466530/";
    private static final String INTERPRO_DOMAIN_DESC_URL = ZENODO_BASE_URL + "files/interpro_domain_desc.txt?download=1";
    private static final String INTERPRO_DOMAIN_DESC_FILENAME = "interpro_domain_desc.txt";
    private static final String INTERPRO_DOMAINS_URL = ZENODO_BASE_URL + "files/interpro_domains.txt?download=1";
    private static final String INTERPRO_DOMAINS_FILENAME = "interpro_domains.txt";
    private static final String ISOFORM_FUNCTION_MF_URL = ZENODO_BASE_URL + "files/isoform_function_list_mf.txt?download=1";
    private static final String ISOFORM_FUNCTION_MF_FILENAME = "isoform_function_list_mf.txt";
    private static final String ISOFORM_FUNCTION_BP_URL = ZENODO_BASE_URL + "files/isoform_function_list_bp.txt?download=1";
    private static final String ISOFORM_FUNCTION_BP_FILENAME = "isoform_function_list_bp.txt";
    private static final String ISOFORM_FUNCTION_CC_URL = ZENODO_BASE_URL + "files/isoform_function_list_cc.txt?download=1";
    private static final String ISOFORM_FUNCTION_CC_FILENAME = "isoform_function_list_cc.txt";
    private static final DownloadItem go = makeItem(GO_JSON_URL, GO_JSON);
    private static final DownloadItem jannovarHg38 = makeItem(JANNOVAR_ZENODO_URL, JANNOVAR_FILENAME);
    private static final DownloadItem hgnc = makeItem(HGNC_URL,HGNC_FILENAME);

    private static final DownloadItem interproDomainDesc = makeItem(INTERPRO_DOMAIN_DESC_URL, INTERPRO_DOMAIN_DESC_FILENAME);

    private static final DownloadItem interproDomains = makeItem(INTERPRO_DOMAINS_URL, INTERPRO_DOMAINS_FILENAME);

    private static final DownloadItem isoformFunctionMf = makeItem(ISOFORM_FUNCTION_MF_URL, ISOFORM_FUNCTION_MF_FILENAME);

    private static final DownloadItem isoformFunctionBp = makeItem(ISOFORM_FUNCTION_BP_URL, ISOFORM_FUNCTION_BP_FILENAME);


    private static final DownloadItem isoformFunctionCc = makeItem(ISOFORM_FUNCTION_CC_URL, ISOFORM_FUNCTION_CC_FILENAME);

    static DownloadItem makeItem(String urlString, String base) {
        try {
            URL url = new URL(urlString);
            return new DownloadItem(url, base);
        } catch (MalformedURLException e) {
            // should never happen
            throw new RuntimeException("Could not create URL from " + urlString);
        }
    }
    private static final Set<DownloadItem> allDownloadItems = Set.of(go,
            jannovarHg38, hgnc, interproDomainDesc, interproDomains,
            isoformFunctionMf, isoformFunctionBp, isoformFunctionCc);




    private final Path dataDirectory;

    public static IsopretDataResolver of(Path dataDirectory) throws RuntimeException {
        return new IsopretDataResolver(dataDirectory);
    }

    public IsopretDataResolver(Path dataDirectory) throws RuntimeException {
        this.dataDirectory = Objects.requireNonNull(dataDirectory, "Data directory must not be null!");
        LOGGER.debug("Using isopret directory at `{}`.", dataDirectory.toAbsolutePath());
        checkResources();
    }

    private void checkResources() throws RuntimeException {
        boolean error = false;
        List<Path> requiredFiles = List.of(goJson(), isoformFunctionListBp(), isoformFunctionListCc(), isoformFunctionListMf(),
                hg38Ensembl(), hgncCompleteSet(), interproDomainDescPath(), interproDomainsPath());
        for (Path file : requiredFiles) {
            if (!Files.isRegularFile(file)) {
                LOGGER.error("Missing required file `{}` in `{}`.", file.toFile().getName(), dataDirectory.toAbsolutePath());
                error = true;
            }
        }
        if (error) {
            throw new IsopretRuntimeException("Missing one or more resource files in isopret data directory!");
        }
    }

    public Path goJson() {
        return dataDirectory.resolve(GO_JSON);
    }
    public Path isoformFunctionListBp() {
        return dataDirectory.resolve(ISOFORM_FUNCTION_BP_FILENAME);
    }
    public Path isoformFunctionListCc() {
        return dataDirectory.resolve(ISOFORM_FUNCTION_CC_FILENAME);
    }
    public Path isoformFunctionListMf() {
        return dataDirectory.resolve(ISOFORM_FUNCTION_MF_FILENAME);
    }
    public Path  hg38Ensembl() { return dataDirectory.resolve(JANNOVAR_FILENAME); }
    public Path hgncCompleteSet() {
        return dataDirectory.resolve(HGNC_FILENAME);
    }
    public Path interproDomainDescPath() {
        return dataDirectory.resolve(INTERPRO_DOMAIN_DESC_FILENAME);
    }
    public Path interproDomainsPath() {
        return dataDirectory.resolve(INTERPRO_DOMAINS_FILENAME);
    }


    /** @return Download data for Gene Ontology (go.json)> */
    public static DownloadItem go() { return go; }
    /** @return Download data for Gene Ontology (go.json)> */
    public static DownloadItem jannovarHg38() { return jannovarHg38; }
    public static DownloadItem hgnc() { return hgnc; }
    public static DownloadItem interproDomainDesc() { return interproDomainDesc; }
    public static DownloadItem interproDomains() { return interproDomains; }
    public static DownloadItem isoformFunctionMf() { return isoformFunctionMf; }
    public static DownloadItem isoformFunctionBp() { return isoformFunctionBp; }
    public static DownloadItem isoformFunctionCc() { return isoformFunctionCc; }
    public static Set<DownloadItem> allDownloadItems() { return allDownloadItems; }



}
