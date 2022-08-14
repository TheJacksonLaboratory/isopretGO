package org.jax.isopret.cli.command;

import org.jax.isopret.core.InterproAnalysisResults;
import org.jax.isopret.core.IsopretInterpoAnalysisRunner;
import org.jax.isopret.core.IsopretProvider;
import org.jax.isopret.core.impl.rnaseqdata.HbaDealsIsoformSpecificThresholder;
import org.jax.isopret.core.impl.rnaseqdata.RnaSeqResultsParser;
import org.jax.isopret.core.impl.rnaseqdata.GeneResultImpl;
import org.jax.isopret.model.*;
import org.jax.isopret.core.InterproMapper;
import org.jax.isopret.visualization.InterproOverrepVisualizer;
import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "interpro",
        mixinStandardHelpOptions = true,
        description = "Interpro Overrepresentation")
public class InterproOverrepCommand extends AbstractIsopretCommand implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(InterproOverrepCommand.class);
    @CommandLine.Option(names={"-b","--hbadeals"},
            description ="HBA-DEALS output file" , required = true)
    private String hbadealsFile;
    @CommandLine.Option(names={"--outfile"}, description = "Name of output file to write stats (default: ${DEFAULT-VALUE})")
    private String outfile = "isopret-interpro-overrep.txt";


    private InterproMapper interproMapper = null;

    private IsopretProvider provider = null;

    @Override
    public Integer call() {
        provider = IsopretProvider.provider(Paths.get(this.downloadDirectory));
        Map<AccessionNumber, GeneResult> hbaDealsResults =
                RnaSeqResultsParser.fromHbaDeals(this.hbadealsFile, provider.ensemblGeneModelMap());
        LOGGER.trace("Analyzing {} genes.", hbaDealsResults.size());
        AssociationContainer<TermId> transcriptContainer = provider.transcriptContainer();
        AssociationContainer<TermId> geneContainer = provider.geneContainer();
        HbaDealsIsoformSpecificThresholder isoThresholder = new HbaDealsIsoformSpecificThresholder(hbaDealsResults,
                0.05,
                geneContainer,
                transcriptContainer);
        LOGGER.info("Initialized HBADealsThresholder");
        double splicingPepThreshold = isoThresholder.getSplicingPepThreshold();
        IsopretInterpoAnalysisRunner runner = IsopretInterpoAnalysisRunner.hbadeals(this.hbadealsFile, provider, splicingPepThreshold);
        InterproAnalysisResults results = runner.run();
        LOGGER.info("Got {} InterproOverrepResults.", results.size());
        InterproOverrepVisualizer visualizer = new InterproOverrepVisualizer(results);
        if (outfile == null) {
            outfile = getDefaultOutfileName("interpro", hbadealsFile);
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outfile))) {
            bw.write(visualizer.getTsv());
        } catch (IOException e) {
           e.printStackTrace();
        }
        return null;
    }








}
