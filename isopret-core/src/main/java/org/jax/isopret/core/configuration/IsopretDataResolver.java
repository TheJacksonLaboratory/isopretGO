package org.jax.isopret.core.configuration;

//import org.jax.isopret.core.DownloadItem;
import org.jax.isopret.exception.IsopretRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

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

    private final Path dataDirectory;

    public static IsopretDataResolver of(Path dataDirectory) throws IsopretRuntimeException {
        return new IsopretDataResolver(dataDirectory);
    }

    public IsopretDataResolver(Path dataDirectory) throws IsopretRuntimeException {
        this.dataDirectory = Objects.requireNonNull(dataDirectory, "Data directory must not be null!");
        LOGGER.debug("Using isopret directory at `{}`.", dataDirectory.toAbsolutePath());
        checkResources();
    }

    private void checkResources() throws IsopretRuntimeException {
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

}
