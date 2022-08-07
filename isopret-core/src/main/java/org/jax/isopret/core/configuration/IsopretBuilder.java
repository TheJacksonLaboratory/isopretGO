package org.jax.isopret.core.configuration;

import org.jax.isopret.except.IsopretRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Objects;

public class IsopretBuilder {
    private final static Logger LOGGER = LoggerFactory.getLogger(IsopretBuilder.class);

    private final Path dataDirectory;
    private final IsopretDataResolver dataResolver;
    public static IsopretBuilder builder(Path liricalDataDirectory) throws IsopretRuntimeException {
        return new IsopretBuilder(liricalDataDirectory);
    }

    private IsopretBuilder(Path dataDirectory) throws IsopretRuntimeException {
        this.dataDirectory = Objects.requireNonNull(dataDirectory);
        this.dataResolver = IsopretDataResolver.of(dataDirectory);
    }
}
