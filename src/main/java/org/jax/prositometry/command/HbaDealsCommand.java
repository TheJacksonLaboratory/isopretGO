package org.jax.prositometry.command;

import org.jax.prositometry.ensembl.EnsemblCdnaParser;
import org.jax.prositometry.ensembl.EnsemblGene;
import org.jax.prositometry.hbadeals.HbaDealsParser;
import org.jax.prositometry.hbadeals.HbaDealsResult;
import org.jax.prositometry.io.PrositometryDownloader;
import org.jax.prositometry.prosite.PrositeComparator;
import org.jax.prositometry.prosite.PrositeParser;
import org.jax.prositometry.prosite.PrositePattern;
import picocli.CommandLine;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "download", aliases = {"H"},
        mixinStandardHelpOptions = true,
        description = "Download files for prositometry")
public class HbaDealsCommand implements Callable<Integer> {

    @CommandLine.Option(names={"-b","--hbadeals"}, description ="HBA-DEALS output file" , required = true)
    private String hbadealsFile;
    @CommandLine.Option(names={"-f","--fasta"}, description ="FASTA file" )
    private String fastaFile = "data/Homo_sapiens.GRCh38.cdna.all.fa.gz";
    @CommandLine.Option(names={"-p","--prosite"}, description ="prosite.dat file")
    private String prositeDataFile = "data/prosite.dat";

    public HbaDealsCommand() {

    }
    @Override
    public Integer call() {
        EnsemblCdnaParser cDnaParser = new EnsemblCdnaParser(this.fastaFile);
        Map<String, EnsemblGene>  ensemblGeneMap = cDnaParser.getSymbol2GeneMap();
        PrositeComparator prositeComparator = new PrositeComparator(this.prositeDataFile);
        HbaDealsParser hbaParser = new HbaDealsParser(hbadealsFile);
        Map<String, HbaDealsResult> hbaDealsResults = hbaParser.getHbaDealsResultMap();
        System.out.printf("[INFO] Analyzing %d genes.\n", hbaDealsResults.size());
        for (var entry : hbaDealsResults.entrySet()) {
            String gene = entry.getKey();
            HbaDealsResult result = entry.getValue();
            if (result.getExpressionP() < 0.5) {
                continue;
            }
            System.out.println("gene=" + gene);
            if (ensemblGeneMap.containsKey(gene)) {
                EnsemblGene egene = ensemblGeneMap.get(gene);
                prositeComparator.annotateEnsemblGene(egene);
                System.out.println(egene);
            }
        }
        int c = 0;
        for (String s : ensemblGeneMap.keySet()) {
            System.out.println("key="+s);
            if (++c > 20) break;
        }
        return 0;
    }
}
