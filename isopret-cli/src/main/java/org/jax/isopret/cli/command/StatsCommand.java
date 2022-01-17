package org.jax.isopret.cli.command;

import org.jax.isopret.core.analysis.IsopretStats;
import org.jax.isopret.core.hgnc.HgncItem;
import org.jax.isopret.core.interpro.InterproMapper;
import org.jax.isopret.core.transcript.AccessionNumber;
import org.jax.isopret.core.transcript.Transcript;
import org.monarchinitiative.phenol.analysis.GoAssociationContainer;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import picocli.CommandLine;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;


@CommandLine.Command(name = "stats", aliases = {"S"},
        mixinStandardHelpOptions = true,
        description = "Descriptive statistics for input files")
public class StatsCommand extends IsopretCommand implements Callable<Integer> {


    public StatsCommand() {
    }

    @Override
    public Integer call() {
        Ontology geneOntology = loadGeneOntology();
        GoAssociationContainer container = loadGoAssociationContainer();
        Set<TermId> allAnnotated = container.getAllAnnotatedGenes();
        Map<AccessionNumber, HgncItem> hgncMap = loadHgncMap();
        Map<String, List<Transcript>> geneSymbolToTranscriptMap = loadJannovarTranscriptMap();
        int n_transcripts = geneSymbolToTranscriptMap.values()
                .stream()
                .map(List::size)
                .reduce(0, Integer::sum);
        InterproMapper mapper = loadInterproMapper();

        String goVersion = geneOntology.getMetaInfo().getOrDefault("data-version", "n/a/");
        IsopretStats.Builder builder = new IsopretStats.Builder();
        builder.geneOntologyVersion(goVersion)
                .hgncCount(hgncMap.size())
                .goAssociations(container.getRawAssociations().size())
                .gannotatedGeneCount(container.getTotalNumberOfAnnotatedItems())
                .annotatingGoTermCount(container.getAnnotatingTermCount())
                .interproAnnotationCount(mapper.getInterproAnnotationCount())
                .interproDescriptionCount(mapper.getInterproDescriptionCount())
                .geneSymbolCount(geneSymbolToTranscriptMap.size())
                .transcriptsCount(n_transcripts);

        IsopretStats stats = builder.build();
        stats.display();
        return 0;
    }
}
