package org.jax.isopret.cli.command;

import org.jax.isopret.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.util.List;
import java.util.Map;

public abstract class AbstractIsopretCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractIsopretCommand.class);

    protected Map<GeneSymbolAccession, List<Transcript>>  geneSymbolAccessionListMap = null;

    @CommandLine.Option(names={"-d","--download"},
            scope = CommandLine.ScopeType.INHERIT,
            description = "directory to download HPO data")
    protected String downloadDirectory="data";



//    protected HbaDealsThresholder initializeHbaDealsThresholder(Map<AccessionNumber, GeneModel> hgncMap, String hbadealsPath) {
//        Map<AccessionNumber, GeneResult> hbaDealsResults =
//                RnaSeqResultsParser.fromHbaDeals(hbadealsPath, hgncMap);
//        LOGGER.trace("Analyzing {} genes.", hbaDealsResults.size());
//        return new HbaDealsThresholder(hbaDealsResults);
//    }


    /**
     *
     * @param category either gene-ontology or interpro
     * @param hbaDealsFileName e.g., SRP149366_70.txt
     * @return e.g., gene-ontology-overrep-SRP149366_70.tsv
     */
    protected String getDefaultOutfileName(String category, String hbaDealsFileName) {
        File f = new File(hbaDealsFileName);
        String basename = f.getName();
        String hbaWithoutExtension =  basename.replaceFirst("[.][^.]+$", "");
        return hbaWithoutExtension + "-overrep-" + category + ".tsv";

    }



}
