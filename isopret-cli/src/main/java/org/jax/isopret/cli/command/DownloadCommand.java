package org.jax.isopret.cli.command;

import org.jax.isopret.io.IsopretDownloader;
import picocli.CommandLine;

import java.util.concurrent.Callable;

/**
 * Download a number of files needed for the analysis. We download by default to a subdirectory called
 * {@code data}, which is created if necessary. We download the files {@code hp.obo}, {@code phenotype.hpoa},
 * {@code Homo_sapiencs_gene_info.gz}, and {@code mim2gene_medgen}.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */

@CommandLine.Command(name = "download",
        mixinStandardHelpOptions = true,
        description = "Download files for isopret analysis")
public class DownloadCommand implements Callable<Integer> {
    @CommandLine.Option(names={"-d","--data"}, description ="directory to download data (default: ${DEFAULT-VALUE})" )
    private String datadir="data";
    @CommandLine.Option(names={"-w","--overwrite"}, description = "overwrite previously downloaded files (default: ${DEFAULT-VALUE})")
    private boolean overwrite;

    public DownloadCommand() {
    }


    @Override
    public Integer call() {
        IsopretDownloader downloader = new IsopretDownloader(datadir, overwrite);
        downloader.download();
        return 0;
    }

}
