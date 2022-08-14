package org.jax.isopret.cli.command;

import picocli.CommandLine;

public abstract class AbstractRnaseqAnalysisCommand extends AbstractIsopretCommand {

    @CommandLine.Option(names={"-f","--fdr"},
            scope = CommandLine.ScopeType.INHERIT,
            description = "directory to download HPO data")
    protected Double desiredFdr=0.05;

}
