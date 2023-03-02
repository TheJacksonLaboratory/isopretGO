package org.jax.isopret.cli.command;

import picocli.CommandLine;

import java.io.File;

public abstract class AbstractRnaseqAnalysisCommand extends AbstractIsopretCommand {

    @CommandLine.Option(names={"-f","--fdr"},
            scope = CommandLine.ScopeType.INHERIT,
            description = "directory to download HPO data")
    protected Double desiredFdr=0.05;

    protected void checkDownloadDir(String downloadDir) {
        File f = new File(downloadDir);
        if (! f.isDirectory()) {
            System.err.printf("[Could not find download directory at %s. Probably you need to run the download command\n\n",
                    downloadDir);
            System.exit(1);
        }
    }

}
