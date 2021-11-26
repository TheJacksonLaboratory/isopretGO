package org.jax.core.command;

import org.jax.core.analysis.IsopretStats;
import org.jax.core.hgnc.HgncItem;
import org.jax.core.interpro.InterproMapper;
import org.jax.core.transcript.AccessionNumber;
import org.jax.core.transcript.Transcript;
import org.monarchinitiative.phenol.analysis.GoAssociationContainer;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import picocli.CommandLine;

import java.util.List;
import java.util.Map;
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
        Map<AccessionNumber, HgncItem> hgncMap = loadHgncMap();
        Map<String, List<Transcript>> geneSymbolToTranscriptMap = loadJannovarTranscriptMap();
        InterproMapper mapper = loadInterproMapper();
        IsopretStats stats = new IsopretStats(geneOntology, container, hgncMap, geneSymbolToTranscriptMap, mapper);
        stats.display();
        return 0;
    }
}
