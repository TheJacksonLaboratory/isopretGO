package org.jax.isopret.core.configuration;

import org.jax.isopret.core.except.IsopretRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class IsopretDataResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(IsopretDataResolver.class);

    private final Path dataDirectory;

    public static IsopretDataResolver of(Path dataDirectory) throws IsopretRuntimeException {
        return new IsopretDataResolver(dataDirectory);
    }

    private IsopretDataResolver(Path dataDirectory) throws IsopretRuntimeException {
        this.dataDirectory = Objects.requireNonNull(dataDirectory, "Data directory must not be null!");
        LOGGER.debug("Using isopret directory at `{}`.", dataDirectory.toAbsolutePath());
        checkResources();
    }

    private void checkResources() throws IsopretRuntimeException {
        boolean error = false;
        List<Path> requiredFiles = List.of(goJson(), isoformFunctionListBp(), isoformFunctionListCc(), isoformFunctionListMf(),
                hg38Ensembl(), hgncCompleteSet(), interproDomainDesc(), interproDomains());
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
        return dataDirectory.resolve("go.json");
    }
    public Path isoformFunctionListBp() {
        return dataDirectory.resolve("isoform_function_list_bp.txt");
    }
    public Path isoformFunctionListCc() {
        return dataDirectory.resolve("isoform_function_list_cc.txt");
    }
    public Path isoformFunctionListMf() {
        return dataDirectory.resolve("isoform_function_list_mf.txt");
    }
    public Path  hg38Ensembl() {
        return dataDirectory.resolve(" hg38_ensembl.ser");
    }
    public Path hgncCompleteSet() {
        return dataDirectory.resolve("hgnc_complete_set.txt");
    }
    public Path interproDomainDesc() {
        return dataDirectory.resolve("interpro_domain_desc.txt");
    }
    public Path interproDomains() {
        return dataDirectory.resolve("interpro_domains.txt");
    }






}
