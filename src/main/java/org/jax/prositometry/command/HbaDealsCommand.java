package org.jax.prositometry.command;

import org.jax.prositometry.ensembl.EnsemblCdnaParser;
import org.jax.prositometry.io.PrositometryDownloader;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "download", aliases = {"H"},
        mixinStandardHelpOptions = true,
        description = "Download files for prositometry")
public class HbaDealsCommand implements Callable<Integer> {

    @CommandLine.Option(names={"-b","--hbadeals"}, description ="HBA-DEALS output file" )
    private String hbadealsFile = "bla";
    @CommandLine.Option(names={"-f","--fasta"}, description ="FASTA file" )
    private String fastaFile = "data/Homo_sapiens.GRCh38.cdna.all.fa.gz";
    @CommandLine.Option(names={"-p","--prosite"}, description ="prosite.dat file")
    private String prositeDataFile = "data/prosite.dat";

    public HbaDealsCommand() {

    }
    @Override
    public Integer call() {
        EnsemblCdnaParser cDnaParser = new EnsemblCdnaParser(this.fastaFile);
        return 0;
    }
}
