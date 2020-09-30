package org.jax.prositometry.command;

import org.jax.prositometry.ensembl.EnsemblCdnaParser;
import org.jax.prositometry.ensembl.EnsemblGene;
import org.jax.prositometry.ensembl.EnsemblTranscript;
import org.jax.prositometry.hbadeals.HbaDealsParser;
import org.jax.prositometry.hbadeals.HbaDealsResult;
import org.jax.prositometry.html.HtmlGene;
import org.jax.prositometry.html.HtmlTemplate;
import org.jax.prositometry.io.PrositometryDownloader;
import org.jax.prositometry.prosite.PrositeComparator;
import org.jax.prositometry.prosite.PrositeParser;
import org.jax.prositometry.prosite.PrositePattern;
import picocli.CommandLine;

import java.util.*;
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
        List<String> unidentifiedSymbols = new ArrayList<>();
        List<HtmlGene> htmlGenes = new ArrayList<>();
        for (var entry : hbaDealsResults.entrySet()) {
            String gene = entry.getKey();
            HbaDealsResult result = entry.getValue();
            if (! result.hasSignificantResult()) {
                continue;
            }
            System.out.printf("[INFO] processing %s: ", gene);
            if (result.hasSignificantExpressionResult()) {
                System.out.printf("; Expression pval = %f", result.getCorrectedPval());
            }
            if (result.hasaSignificantSplicingResult()) {
                System.out.printf("; Splicing pval = %f", result.getMostSignificantSplicingPval());
            }
            System.out.println();
            if (ensemblGeneMap.containsKey(gene)) {
                EnsemblGene egene = ensemblGeneMap.get(gene);
                prositeComparator.annotateEnsemblGene(egene);
                var htmlgene = new HtmlGene(result, egene);
                htmlGenes.add(htmlgene);
            } else {
                unidentifiedSymbols.add(gene);
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("hbaDealsFile", this.hbadealsFile);
        data.put("fastaFile", this.fastaFile);
        data.put("prosite_file", this.prositeDataFile);
        int n_genes = hbaDealsResults.size();
        int n_sig_genes = (int)hbaDealsResults
                .values()
                .stream()
                .filter(HbaDealsResult::hasSignificantResult)
                .count();
        data.put("total_genes", n_genes);
        data.put("total_sig_genes", n_sig_genes);
        data.put("genes", htmlGenes);

        HtmlTemplate template = new HtmlTemplate(data);
        template.outputFile();
        System.out.println("[INFO] Total unidentified genes:"+ unidentifiedSymbols.size());

        return 0;
    }
}
